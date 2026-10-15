package us.dot.its.jpo.ode.api.services.firmware;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.unit.DataSize;

import jakarta.persistence.EntityNotFoundException;
import us.dot.its.jpo.ode.api.mappers.ObuFirmwareMapper;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUploadStatus;
import us.dot.its.jpo.ode.api.models.postgres.tables.Manufacturer;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareDeviceType;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareUpload;
import us.dot.its.jpo.ode.api.models.storage.ObjectChecksum;
import us.dot.its.jpo.ode.api.models.storage.ObjectStorageLocation;
import us.dot.its.jpo.ode.api.models.storage.ObjectUploadRequest;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.ManufacturerOption;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.UploadUrlRequest;
import us.dot.its.jpo.ode.api.models.storage.SignedUploadUrl;
import us.dot.its.jpo.ode.api.models.storage.StoredObjectMetadata;
import us.dot.its.jpo.ode.api.repositories.ManufacturerRepository;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareImageRepository;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareUploadRepository;
import us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadService.FirmwareUploadVerificationException;
import us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadService.FirmwareVersionAlreadyExistsException;
import us.dot.its.jpo.ode.api.services.firmware.ObuFirmwareRegistrationService.Registration;
import us.dot.its.jpo.ode.api.storage.ObjectStorageProperties;
import us.dot.its.jpo.ode.api.storage.ObjectStorageService;
import us.dot.its.jpo.ode.api.storage.ObjectStorageServiceRegistry;

class ObuFirmwareUploadServiceTest {
    private static final Instant EXPIRES_AT = Instant.parse("2026-10-15T12:15:00Z");
    private static final String FILE_NAME = "ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig";
    private static final String OBJECT_NAME = "ota/Commsignia/" + FILE_NAME;

    private final ManufacturerRepository manufacturers = mock(ManufacturerRepository.class);
    private final ObuFirmwareUploadRepository uploads = mock(ObuFirmwareUploadRepository.class);
    private final ObuFirmwareImageRepository images = mock(ObuFirmwareImageRepository.class);
    private final ObjectStorageService objectStorageService = mock(ObjectStorageService.class);
    private final ObjectStorageServiceRegistry objectStorageServices = mock(ObjectStorageServiceRegistry.class);
    private final ObuFirmwareRegistrationService registration = mock(ObuFirmwareRegistrationService.class);
    private final ObjectStorageProperties properties = new ObjectStorageProperties();
    private final ObuFirmwareParserRegistry parsers =
            new ObuFirmwareParserRegistry(List.of(new CommsigniaObuFirmwareFileNameParser()));

    private ObuFirmwareUploadService service;
    private Manufacturer commsignia;

    @BeforeEach
    void setUp() {
        properties.setMaxUploadSize(DataSize.ofMegabytes(500));
        service = new ObuFirmwareUploadService(manufacturers, parsers, uploads, images, objectStorageServices,
                properties, Mappers.getMapper(ObuFirmwareMapper.class), registration);

        commsignia = manufacturer(1, "Commsignia");
        when(manufacturers.findFirstByNameIgnoreCaseOrderByIdAsc(argThat("Commsignia"::equalsIgnoreCase)))
                .thenReturn(Optional.of(commsignia));
        when(objectStorageService.providerName()).thenReturn("gcp");
        when(objectStorageServices.getActiveService()).thenReturn(objectStorageService);
        when(objectStorageServices.getService("gcp")).thenReturn(objectStorageService);
    }

    @Test
    void createsAPendingUploadInTheManufacturerFolderWithTheOriginalFileName() {
        stubSignedUrl(OBJECT_NAME);

        var result = service.createSignedUploadUrl(new UploadUrlRequest(" Commsignia ", " " + FILE_NAME + " ",
                135_055_616L, " crc32c ", " ImIEBA== ", null), " admin@example.com ");

        var requestCaptor = ArgumentCaptor.forClass(ObjectUploadRequest.class);
        verify(objectStorageService).objectExists(OBJECT_NAME);
        verify(objectStorageService).createSignedUploadUrl(requestCaptor.capture());
        assertThat(requestCaptor.getValue().objectName()).isEqualTo(OBJECT_NAME);
        assertThat(requestCaptor.getValue().contentType()).isEqualTo("application/octet-stream");
        assertThat(requestCaptor.getValue().checksum()).isEqualTo(new ObjectChecksum("CRC32C", "ImIEBA=="));

        var uploadCaptor = ArgumentCaptor.forClass(ObuFirmwareUpload.class);
        verify(uploads).save(uploadCaptor.capture());
        ObuFirmwareUpload upload = uploadCaptor.getValue();
        assertThat(upload.getId()).isEqualTo(result.uploadId());
        assertThat(upload.getManufacturer()).isSameAs(commsignia);
        assertThat(upload.getFileName()).isEqualTo(FILE_NAME);
        assertThat(upload.getMetadata().getDeviceType()).isEqualTo(ObuFirmwareDeviceType.OBU);
        assertThat(upload.getMetadata().getReleaseVersion()).isEqualTo("y20.48.2-b228647");
        assertThat(upload.getMetadata().getVariant()).isEqualTo("ob4-generic");
        assertThat(upload.getMetadata().getWritableSystemPartition()).isFalse();
        assertThat(upload.getMetadata().getSecureBoot()).isTrue();
        assertThat(upload.getObjectName()).isEqualTo(OBJECT_NAME);
        assertThat(upload.getStorageProvider()).isEqualTo("gcp");
        assertThat(upload.getStorageContainer()).isEqualTo("bucket");
        assertThat(upload.getExpectedSize()).isEqualTo(135_055_616L);
        assertThat(upload.getChecksumAlgorithm()).isEqualTo("CRC32C");
        assertThat(upload.getExpectedChecksum()).isEqualTo("ImIEBA==");
        assertThat(upload.getStatus()).isEqualTo(FirmwareUploadStatus.PENDING);
        assertThat(upload.getCreatedBy()).isEqualTo("admin@example.com");
        assertThat(upload.getExpiresAt()).isEqualTo(EXPIRES_AT);
        assertThat(upload.getVerifiedAt()).isNull();
        assertThat(result.objectName()).isEqualTo(OBJECT_NAME);
        assertThat(result.method()).isEqualTo("PUT");
    }

    @ParameterizedTest
    @ValueSource(strings = { "commsignia", "COMMSIGNIA" })
    void matchesManufacturersIgnoringCaseAndKeepsTheStoredSpelling(String name) {
        stubSignedUrl(OBJECT_NAME);

        var result = service.createSignedUploadUrl(request(name, FILE_NAME), "admin");

        assertThat(result.objectName()).isEqualTo("ota/Commsignia/" + FILE_NAME);
        verify(objectStorageService).objectExists("ota/Commsignia/" + FILE_NAME);
    }

    @Test
    void rejectsUnknownManufacturers() {
        when(manufacturers.findFirstByNameIgnoreCaseOrderByIdAsc("Acme")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createSignedUploadUrl(request("Acme", FILE_NAME), "admin"))
                .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("Acme");
        verifyNoInteractions(objectStorageService);
    }

    @Test
    void rejectsManufacturersWithoutANamingConventionAsBadRequests() {
        when(manufacturers.findFirstByNameIgnoreCaseOrderByIdAsc("Kapsch"))
                .thenReturn(Optional.of(manufacturer(2, "Kapsch")));

        assertThatThrownBy(() -> service.createSignedUploadUrl(request("Kapsch", FILE_NAME), "admin"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("not supported")
                .hasMessageContaining("Kapsch");
        verifyNoInteractions(objectStorageService);
    }

    @ParameterizedTest
    @ValueSource(strings = { "ota", "OTA" })
    void rejectsTheReservedManufacturerName(String name) {
        assertThatThrownBy(() -> service.createSignedUploadUrl(request(name, FILE_NAME), "admin"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("reserved");
        verifyNoInteractions(manufacturers, objectStorageService);
    }

    @ParameterizedTest
    @ValueSource(strings = { "firmware.tar.sig", "ob4-generic-ro-y20.48.2.tar", "../" + FILE_NAME, "dir/" + FILE_NAME })
    void rejectsFileNamesOutsideTheNamingConvention(String fileName) {
        assertThatThrownBy(() -> service.createSignedUploadUrl(request("Commsignia", fileName), "admin"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("file_name");
        verifyNoInteractions(objectStorageService);
        verify(uploads, never()).save(any());
    }

    @Test
    void namingConventionErrorsIncludeAnExample() {
        assertThatThrownBy(() -> service.createSignedUploadUrl(request("Commsignia", "firmware.tar.sig"), "admin"))
                .hasMessageContaining("Commsignia").hasMessageContaining(FILE_NAME);
    }

    @Test
    void rejectsUploadsLargerThanTheConfiguredMaximum() {
        properties.setMaxUploadSize(DataSize.ofBytes(10));

        assertThatThrownBy(() -> service.createSignedUploadUrl(request("Commsignia", FILE_NAME), "admin"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("10 bytes");
        verifyNoInteractions(objectStorageService);
    }

    @Test
    void rejectsAFileNameAlreadyInTheCatalog() {
        when(images.existsByManufacturerIdAndFileName(1, FILE_NAME)).thenReturn(true);

        assertThatThrownBy(() -> service.createSignedUploadUrl(request("Commsignia", FILE_NAME), "admin"))
                .isInstanceOf(FirmwareVersionAlreadyExistsException.class).hasMessageContaining("already exists");
        verifyNoInteractions(objectStorageService);
    }

    @Test
    void rejectsAnExistingObjectBeforeSigning() {
        when(objectStorageService.objectExists(OBJECT_NAME)).thenReturn(true);

        assertThatThrownBy(() -> service.createSignedUploadUrl(request("Commsignia", FILE_NAME), "admin"))
                .isInstanceOf(FirmwareVersionAlreadyExistsException.class);
        verify(objectStorageService, never()).createSignedUploadUrl(any());
        verify(uploads, never()).save(any());
    }

    @Test
    void rejectsASignedLocationOtherThanTheRequestedObject() {
        stubSignedUrl("ota/Commsignia/elsewhere.tar.sig");

        assertThatThrownBy(() -> service.createSignedUploadUrl(request("Commsignia", FILE_NAME), "admin"))
                .hasMessageContaining("invalid upload location");
        verify(uploads, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "uq_obu_firmware_uploads_active_file_name", "uq_obu_firmware_uploads_active_destination" })
    void reportsConcurrentClaimsOfTheSameNameAsConflicts(String constraint) {
        stubSignedUrl(OBJECT_NAME);
        when(uploads.save(any())).thenThrow(new DataIntegrityViolationException("violates " + constraint));

        assertThatThrownBy(() -> service.createSignedUploadUrl(request("Commsignia", FILE_NAME), "admin"))
                .isInstanceOf(FirmwareVersionAlreadyExistsException.class);
    }

    @Test
    void preservesUnrelatedDatabaseFailures() {
        stubSignedUrl(OBJECT_NAME);
        var failure = new DataIntegrityViolationException("some other constraint");
        when(uploads.save(any())).thenThrow(failure);

        assertThatThrownBy(() -> service.createSignedUploadUrl(request("Commsignia", FILE_NAME), "admin"))
                .isSameAs(failure);
    }

    @Test
    void verifiesAndRegistersACompletedUpload() {
        ObuFirmwareUpload upload = pendingUpload();
        var stored = new StoredObjectMetadata(1024L, new ObjectChecksum("CRC32C", "ImIEBA=="), "17");
        when(uploads.findById(upload.getId())).thenReturn(Optional.of(upload));
        when(objectStorageService.getObjectMetadata(new ObjectStorageLocation("gcp", "bucket", OBJECT_NAME), "CRC32C"))
                .thenReturn(Optional.of(stored));
        when(registration.register(upload.getId(), stored)).thenAnswer(invocation -> {
            upload.setStatus(FirmwareUploadStatus.VERIFIED);
            upload.setObservedChecksum("ImIEBA==");
            upload.setProviderObjectVersion("17");
            upload.setVerifiedAt(EXPIRES_AT);
            return new Registration(upload, 42);
        });

        var result = service.completeUpload(upload.getId());

        assertThat(result.status()).isEqualTo(FirmwareUploadStatus.VERIFIED);
        assertThat(result.obuFirmwareId()).isEqualTo(42);
        assertThat(result.objectName()).isEqualTo(OBJECT_NAME);
        assertThat(result.providerObjectVersion()).isEqualTo("17");
        assertThat(result.checksum()).isEqualTo("ImIEBA==");
    }

    @Test
    void repeatedCompletionOfAVerifiedUploadDoesNotReadStorage() {
        ObuFirmwareUpload upload = pendingUpload();
        upload.setStatus(FirmwareUploadStatus.VERIFIED);
        when(uploads.findById(upload.getId())).thenReturn(Optional.of(upload));
        when(registration.register(upload.getId(), null)).thenReturn(new Registration(upload, 42));

        assertThat(service.completeUpload(upload.getId()).obuFirmwareId()).isEqualTo(42);
        verifyNoInteractions(objectStorageService);
    }

    @Test
    void rejectsCompletionWhenTheObjectIsMissing() {
        ObuFirmwareUpload upload = pendingUpload();
        when(uploads.findById(upload.getId())).thenReturn(Optional.of(upload));
        when(objectStorageService.getObjectMetadata(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.completeUpload(upload.getId()))
                .isInstanceOf(FirmwareUploadVerificationException.class).hasMessageContaining("not found");
        verify(registration, never()).markFailed(any(), any());
    }

    @Test
    void marksSizeAndChecksumMismatchesAsFailed() {
        ObuFirmwareUpload upload = pendingUpload();
        when(uploads.findById(upload.getId())).thenReturn(Optional.of(upload));
        when(objectStorageService.getObjectMetadata(any(), any())).thenReturn(Optional.of(
                new StoredObjectMetadata(2048L, new ObjectChecksum("CRC32C", "ImIEBA=="), "17")));

        assertThatThrownBy(() -> service.completeUpload(upload.getId()))
                .isInstanceOf(FirmwareUploadVerificationException.class).hasMessageContaining("size");
        verify(registration).markFailed(upload.getId(), "SIZE_MISMATCH");

        when(objectStorageService.getObjectMetadata(any(), any())).thenReturn(Optional.of(
                new StoredObjectMetadata(1024L, new ObjectChecksum("CRC32C", "other=="), "17")));
        assertThatThrownBy(() -> service.completeUpload(upload.getId()))
                .isInstanceOf(FirmwareUploadVerificationException.class).hasMessageContaining("checksum");
        verify(registration).markFailed(upload.getId(), "CHECKSUM_MISMATCH");
        verify(registration, never()).register(any(), any());
    }

    @Test
    void reportsRegistrationNameConflicts() {
        ObuFirmwareUpload upload = pendingUpload();
        upload.setStatus(FirmwareUploadStatus.VERIFIED);
        when(uploads.findById(upload.getId())).thenReturn(Optional.of(upload));
        when(registration.register(eq(upload.getId()), any())).thenThrow(new DataIntegrityViolationException(
                "violates obu_firmware_images_manufacturer_file_name_unique"));

        assertThatThrownBy(() -> service.completeUpload(upload.getId()))
                .isInstanceOf(FirmwareVersionAlreadyExistsException.class);
    }

    @Test
    void rejectsCompletionOfAnUnknownUpload() {
        UUID id = UUID.randomUUID();
        when(uploads.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.completeUpload(id)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void uploadOptionsListManufacturersWithANamingConvention() {
        var options = new ObuFirmwareUploadOptionsService(manufacturers, parsers).getOptions();

        assertThat(options.manufacturers()).containsExactly(new ManufacturerOption(1, "Commsignia", ".tar.sig",
                CommsigniaObuFirmwareFileNameParser.PATTERN, 128, FILE_NAME));
        verify(manufacturers, never()).findAll();
    }

    @Test
    void uploadOptionsOmitParsersWhoseManufacturerIsMissing() {
        when(manufacturers.findFirstByNameIgnoreCaseOrderByIdAsc(any())).thenReturn(Optional.empty());

        assertThat(new ObuFirmwareUploadOptionsService(manufacturers, parsers).getOptions().manufacturers()).isEmpty();
    }

    private UploadUrlRequest request(String manufacturer, String fileName) {
        return new UploadUrlRequest(manufacturer, fileName, 1024L, "CRC32C", "ImIEBA==", "application/octet-stream");
    }

    private void stubSignedUrl(String objectName) {
        when(objectStorageService.createSignedUploadUrl(any())).thenReturn(new SignedUploadUrl(
                "https://storage.googleapis.com/signed", "PUT", new ObjectStorageLocation("gcp", "bucket", objectName),
                EXPIRES_AT, Map.of("x-goog-hash", "crc32c=ImIEBA==")));
    }

    private ObuFirmwareUpload pendingUpload() {
        var upload = new ObuFirmwareUpload();
        upload.setId(UUID.randomUUID());
        upload.setManufacturer(commsignia);
        upload.setFileName(FILE_NAME);
        upload.setStorageProvider("gcp");
        upload.setStorageContainer("bucket");
        upload.setObjectName(OBJECT_NAME);
        upload.setExpectedSize(1024L);
        upload.setChecksumAlgorithm("CRC32C");
        upload.setExpectedChecksum("ImIEBA==");
        upload.setStatus(FirmwareUploadStatus.PENDING);
        upload.setExpiresAt(EXPIRES_AT);
        return upload;
    }

    private static Manufacturer manufacturer(int id, String name) {
        var manufacturer = new Manufacturer();
        manufacturer.setId(id);
        manufacturer.setName(name);
        return manufacturer;
    }
}
