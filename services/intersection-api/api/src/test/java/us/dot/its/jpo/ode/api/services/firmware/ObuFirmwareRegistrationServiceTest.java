package us.dot.its.jpo.ode.api.services.firmware;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import us.dot.its.jpo.ode.api.TestcontainersConfiguration;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUploadStatus;
import us.dot.its.jpo.ode.api.models.postgres.tables.Manufacturer;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareDeviceType;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareFileMetadata;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareImage;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareUpload;
import us.dot.its.jpo.ode.api.models.storage.ObjectChecksum;
import us.dot.its.jpo.ode.api.models.storage.StoredObjectMetadata;
import us.dot.its.jpo.ode.api.repositories.ManufacturerRepository;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareImageRepository;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareUploadRepository;
import us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadService.FirmwareUploadVerificationException;
import us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadService.FirmwareVersionAlreadyExistsException;

@SpringBootTest(properties = "firmware-upload.cleanup.enabled=false")
@ActiveProfiles("integration-test")
@Import(TestcontainersConfiguration.class)
class ObuFirmwareRegistrationServiceTest {
    private static final String FILE_NAME = "ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig";

    @Autowired private ObuFirmwareRegistrationService registration;
    @Autowired private ObuFirmwareUploadRepository uploads;
    @MockitoSpyBean private ObuFirmwareImageRepository images;
    @Autowired private ManufacturerRepository manufacturers;

    private Manufacturer manufacturer;
    private ObuFirmwareUpload upload;
    private final StoredObjectMetadata stored = new StoredObjectMetadata(
            1024L, new ObjectChecksum("CRC32C", "ImIEBA=="), "17");

    @BeforeEach
    void setUp() {
        manufacturer = new Manufacturer();
        manufacturer.setName("ObuRegistration-" + UUID.randomUUID());
        manufacturer = manufacturers.save(manufacturer);
        upload = uploads.saveAndFlush(upload(FILE_NAME));
    }

    @AfterEach
    void cleanUp() {
        reset(images);
        images.findAll().stream()
                .filter(image -> image.getManufacturer().getId().equals(manufacturer.getId()))
                .forEach(images::delete);
        uploads.findAll().stream()
                .filter(record -> record.getManufacturer().getId().equals(manufacturer.getId()))
                .forEach(uploads::delete);
        manufacturers.deleteById(manufacturer.getId());
    }

    @Test
    void registersOneUnpublishedImageOnRepeatedCompletion() {
        var first = registration.register(upload.getId(), stored);
        var second = registration.register(upload.getId(), null);

        assertThat(second.obuFirmwareId()).isEqualTo(first.obuFirmwareId());
        ObuFirmwareImage image = images.findByManufacturerIdAndFileName(manufacturer.getId(), FILE_NAME).orElseThrow();
        assertThat(image.getId()).isEqualTo(first.obuFirmwareId());
        assertThat(image.getPublished()).isFalse();
        assertThat(image.getPublishedAt()).isNull();
        assertThat(image.getRegisteredAt()).isNotNull();
        assertThat(image.getObjectName()).isEqualTo(upload.getObjectName());
        assertThat(image.getStorageProvider()).isEqualTo("gcp");
        assertThat(image.getContentLength()).isEqualTo(1024L);
        assertThat(image.getChecksumAlgorithm()).isEqualTo("CRC32C");
        assertThat(image.getChecksum()).isEqualTo("ImIEBA==");
        assertThat(image.getProviderObjectVersion()).isEqualTo("17");
        assertThat(image.getMetadata()).isEqualTo(upload.getMetadata());

        var verified = uploads.findById(upload.getId()).orElseThrow();
        assertThat(verified.getStatus()).isEqualTo(FirmwareUploadStatus.VERIFIED);
        assertThat(verified.getObservedChecksum()).isEqualTo("ImIEBA==");
        assertThat(verified.getVerifiedAt()).isNotNull();
    }

    @Test
    void mismatchedMetadataDoesNotRegisterAnImage() {
        assertThatThrownBy(() -> registration.register(upload.getId(),
                new StoredObjectMetadata(2048L, stored.checksum(), "17")))
                .isInstanceOf(FirmwareUploadVerificationException.class);

        assertThat(images.findByManufacturerIdAndFileName(manufacturer.getId(), FILE_NAME)).isEmpty();
        assertThat(uploads.findById(upload.getId()).orElseThrow().getStatus()).isEqualTo(FirmwareUploadStatus.PENDING);
    }

    @Test
    void rollsBackVerificationIfTheImageCannotBeSaved() {
        doThrow(new DataIntegrityViolationException("image insert failed")).when(images).saveAndFlush(any());

        assertThatThrownBy(() -> registration.register(upload.getId(), stored))
                .isInstanceOf(DataIntegrityViolationException.class);
        var pending = uploads.findById(upload.getId()).orElseThrow();
        assertThat(pending.getStatus()).isEqualTo(FirmwareUploadStatus.PENDING);
        assertThat(pending.getVerifiedAt()).isNull();
    }

    @Test
    void doesNotReplaceAnImageAlreadyRegisteredUnderTheSameName() {
        var local = new ObuFirmwareImage();
        local.setManufacturer(manufacturer);
        local.setFileName(FILE_NAME);
        local.setMetadata(metadata());
        local.setStorageProvider(ObuFirmwareImage.LOCAL_STORAGE_PROVIDER);
        local.setStorageContainer("local");
        local.setObjectName(FILE_NAME);
        images.saveAndFlush(local);

        assertThatThrownBy(() -> registration.register(upload.getId(), stored))
                .isInstanceOf(FirmwareVersionAlreadyExistsException.class);
        assertThat(uploads.findById(upload.getId()).orElseThrow().getStatus()).isEqualTo(FirmwareUploadStatus.PENDING);
    }

    @Test
    void lateFailureDoesNotDowngradeAVerifiedUpload() {
        registration.register(upload.getId(), stored);
        registration.markFailed(upload.getId(), "CHECKSUM_MISMATCH");

        var verified = uploads.findById(upload.getId()).orElseThrow();
        assertThat(verified.getStatus()).isEqualTo(FirmwareUploadStatus.VERIFIED);
        assertThat(verified.getFailureReason()).isNull();
    }

    @Test
    void marksAPendingUploadFailed() {
        registration.markFailed(upload.getId(), "SIZE_MISMATCH");

        var failed = uploads.findById(upload.getId()).orElseThrow();
        assertThat(failed.getStatus()).isEqualTo(FirmwareUploadStatus.FAILED);
        assertThat(failed.getFailureReason()).isEqualTo("SIZE_MISMATCH");
        assertThat(failed.getFinishedAt()).isNotNull();
    }

    @Test
    void concurrentCompletionRegistersOnlyOneImage() throws Exception {
        try (var executor = Executors.newFixedThreadPool(2)) {
            var start = new CountDownLatch(1);
            Callable<Integer> complete = () -> {
                start.await();
                return registration.register(upload.getId(), stored).obuFirmwareId();
            };
            var first = executor.submit(complete);
            var second = executor.submit(complete);
            start.countDown();

            assertThat(first.get(20, TimeUnit.SECONDS)).isEqualTo(second.get(20, TimeUnit.SECONDS));
        }
        assertThat(images.findAll().stream()
                .filter(image -> image.getManufacturer().getId().equals(manufacturer.getId()))).hasSize(1);
    }

    private ObuFirmwareUpload upload(String fileName) {
        var record = new ObuFirmwareUpload();
        record.setId(UUID.randomUUID());
        record.setManufacturer(manufacturer);
        record.setFileName(fileName);
        record.setMetadata(metadata());
        record.setContentType("application/octet-stream");
        record.setStorageProvider("gcp");
        record.setStorageContainer("bucket");
        record.setObjectName("ota/" + manufacturer.getName() + "/" + fileName);
        record.setExpectedSize(1024L);
        record.setChecksumAlgorithm("CRC32C");
        record.setExpectedChecksum("ImIEBA==");
        record.setStatus(FirmwareUploadStatus.PENDING);
        record.setCreatedBy("test");
        record.setCreatedAt(Instant.now());
        record.setExpiresAt(Instant.now().plusSeconds(900));
        return record;
    }

    private static ObuFirmwareFileMetadata metadata() {
        return new ObuFirmwareFileMetadata(ObuFirmwareDeviceType.OBU, "y20.48.2-b228647", "ob4-generic", false, true);
    }
}
