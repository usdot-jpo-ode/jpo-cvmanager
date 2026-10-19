package us.dot.its.jpo.ode.api.services.firmware;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUploadStatus;
import us.dot.its.jpo.ode.api.models.postgres.tables.Manufacturer;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareDeviceType;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareFileMetadata;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareImage;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareUpload;
import us.dot.its.jpo.ode.api.models.storage.ObjectChecksum;
import us.dot.its.jpo.ode.api.models.storage.ObjectListRequest;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.Item;
import us.dot.its.jpo.ode.api.models.storage.StorageObject;
import us.dot.its.jpo.ode.api.models.storage.StorageObjectPage;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareImageRepository;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareUploadRepository;
import us.dot.its.jpo.ode.api.storage.ObjectIds;
import us.dot.its.jpo.ode.api.storage.ObjectStorageService;
import us.dot.its.jpo.ode.api.storage.ObjectStorageServiceRegistry;
import us.dot.its.jpo.ode.api.storage.ObjectStorageUnavailableException;

class ObuFirmwareObjectServiceTest {
    private static final ObjectChecksum CHECKSUM = new ObjectChecksum("CRC32C", "ImIEBA==");
    private static final Instant UPDATED = Instant.parse("2026-10-15T12:00:00Z");

    private final ObjectStorageServiceRegistry registry = mock(ObjectStorageServiceRegistry.class);
    private final ObjectStorageService storage = mock(ObjectStorageService.class);
    private final ObuFirmwareUploadRepository uploads = mock(ObuFirmwareUploadRepository.class);
    private final ObuFirmwareImageRepository images = mock(ObuFirmwareImageRepository.class);
    private final List<StorageObject> stored = new ArrayList<>();
    private final Manufacturer commsignia = new Manufacturer();

    private ObuFirmwareObjectService service;

    @BeforeEach
    void setUp() {
        commsignia.setId(1);
        commsignia.setName("Commsignia");
        when(registry.getActiveService()).thenReturn(storage);
        when(storage.listObjects(new ObjectListRequest("ota/", 200, null)))
                .thenAnswer(invocation -> new StorageObjectPage("gcp", "bucket", List.copyOf(stored), null));
        service = new ObuFirmwareObjectService(registry, uploads, images,
                new ObuFirmwareParserRegistry(List.of(new CommsigniaObuFirmwareFileNameParser())));
    }

    @Test
    void reportsTheStateOfEveryFileAndCatalogEntry() {
        var verified = image(1, "ob4-generic-ro-y20.1.tar.sig", "gcp", "17");
        verified.setPublished(true);
        verified.setPublishedAt(UPDATED);
        verified.setPublishedBy("admin");
        var changed = image(2, "ob4-generic-ro-y20.2.tar.sig", "gcp", "17");
        var missing = image(3, "ob4-generic-ro-y20.3.tar.sig", "gcp", "17");
        var local = image(4, "ob4-generic-rw-y20.4.tar.sig", ObuFirmwareImage.LOCAL_STORAGE_PROVIDER, null);
        var otherBucket = image(5, "ob4-generic-ro-y20.5.tar.sig", "gcp", "17");
        otherBucket.setStorageContainer("old-bucket");
        when(images.findAllWithManufacturer()).thenReturn(List.of(verified, changed, missing, local, otherBucket));

        var unverified = upload("ob4-generic-ro-y20.6.tar.sig", FirmwareUploadStatus.PENDING, UPDATED);
        var abandoned = upload("ob4-generic-ro-y20.7.tar.sig", FirmwareUploadStatus.EXPIRED, UPDATED);
        var inProgress = upload("ob4-generic-ro-y20.8.tar.sig", FirmwareUploadStatus.PENDING,
                Instant.now().plusSeconds(300));
        when(uploads.findListingUploads("gcp", "bucket")).thenReturn(List.of(unverified, abandoned, inProgress));

        stored.add(object("ob4-generic-ro-y20.1.tar.sig", "17"));
        stored.add(object("ob4-generic-ro-y20.2.tar.sig", "18"));
        stored.add(object("ob4-generic-ro-y20.6.tar.sig", "19"));
        stored.add(object("ob4-generic-ro-secureboot-y20.9.tar.sig", "20"));
        stored.add(new StorageObject("ota/Commsignia/", 0, UPDATED, "1", null));

        var page = service.list(0, 200, null, null, null, "file_name,asc");

        assertThat(page.storageAvailable()).isTrue();
        assertThat(page.provider()).isEqualTo("gcp");
        Map<String, Item> byFile = page.objects().stream()
                .collect(Collectors.toMap(Item::fileName, Function.identity()));
        assertThat(byFile).hasSize(8);
        assertThat(byFile.get("ob4-generic-ro-y20.1.tar.sig"))
                .returns("VERIFIED", Item::verificationStatus)
                .returns(1, Item::obuFirmwareId)
                .returns(true, Item::published)
                .returns("admin", Item::publishedBy)
                .returns("17", Item::providerObjectVersion)
                .returns(ObjectIds.encode("gcp", "ota/Commsignia/ob4-generic-ro-y20.1.tar.sig"), Item::objectId);
        assertThat(byFile.get("ob4-generic-ro-y20.2.tar.sig").verificationStatus()).isEqualTo("CHANGED");
        assertThat(byFile.get("ob4-generic-ro-y20.3.tar.sig"))
                .returns("MISSING", Item::verificationStatus)
                .returns(null, Item::providerObjectVersion)
                .returns(1024L, Item::contentLength);
        assertThat(byFile.get("ob4-generic-rw-y20.4.tar.sig"))
                .returns("LOCAL", Item::verificationStatus)
                .returns(ObjectIds.encode("local", "ota/Commsignia/ob4-generic-rw-y20.4.tar.sig"), Item::objectId);
        assertThat(byFile.get("ob4-generic-ro-y20.5.tar.sig").verificationStatus()).isEqualTo("UNKNOWN");
        assertThat(byFile.get("ob4-generic-ro-y20.6.tar.sig"))
                .returns("UNVERIFIED", Item::verificationStatus)
                .returns(unverified.getId(), Item::uploadId)
                .returns("PENDING", Item::uploadStatus)
                .returns("y20.6", Item::releaseVersion);
        assertThat(byFile.get("ob4-generic-ro-y20.7.tar.sig"))
                .returns("MISSING", Item::verificationStatus)
                .returns("EXPIRED", Item::uploadStatus);
        // Untracked files are described by parsing their name
        assertThat(byFile.get("ob4-generic-ro-secureboot-y20.9.tar.sig"))
                .returns("UNTRACKED", Item::verificationStatus)
                .returns("Commsignia", Item::manufacturer)
                .returns(ObuFirmwareDeviceType.OBU, Item::deviceType)
                .returns(true, Item::secureBoot)
                .returns("y20.9", Item::releaseVersion)
                .returns(null, Item::obuFirmwareId);
        // An upload that can still write its file is in progress, not missing
        assertThat(byFile).doesNotContainKey("ob4-generic-ro-y20.8.tar.sig");
    }

    @Test
    void listsOnlyCatalogEntriesWhenStorageIsUnavailable() {
        when(registry.getActiveService()).thenThrow(new ObjectStorageUnavailableException("not configured"));
        when(images.findAllWithManufacturer()).thenReturn(List.of(
                image(1, "ob4-generic-ro-y20.1.tar.sig", "gcp", "17"),
                image(2, "ob4-generic-rw-y20.2.tar.sig", ObuFirmwareImage.LOCAL_STORAGE_PROVIDER, null)));

        var page = service.list(0, 100, null, null, null, null);

        assertThat(page.storageAvailable()).isFalse();
        assertThat(page.provider()).isNull();
        assertThat(page.objects()).extracting(Item::verificationStatus).containsExactly("UNKNOWN", "LOCAL");
        verify(uploads, never()).findListingUploads(any(), any());
    }

    @Test
    void filtersByManufacturerSearchAndPublishState() {
        var published = image(1, "ob4-generic-ro-y20.1.tar.sig", "gcp", "17");
        published.setPublished(true);
        when(images.findAllWithManufacturer()).thenReturn(List.of(published,
                image(2, "ob4-generic-ro-y20.2.tar.sig", "gcp", "17")));
        stored.add(object("ob4-generic-ro-y20.1.tar.sig", "17"));
        stored.add(object("ob4-generic-ro-y20.2.tar.sig", "17"));
        stored.add(new StorageObject("ota/Other/firmware.bin", 1, UPDATED, "1", CHECKSUM));

        assertThat(service.list(0, 100, null, null, true, null).objects())
                .extracting(Item::obuFirmwareId).containsExactly(1);
        assertThat(service.list(0, 100, null, null, false, null).objects()).extracting(Item::fileName)
                .containsExactly("ob4-generic-ro-y20.2.tar.sig", "firmware.bin");
        assertThat(service.list(0, 100, " commsignia ", "Y20.2", null, null).objects())
                .extracting(Item::obuFirmwareId).containsExactly(2);
        assertThat(service.list(0, 100, "Other", null, null, null).objects())
                .extracting(Item::verificationStatus, Item::deviceType)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("UNTRACKED", null));
    }

    @Test
    void sortsAndPagesTheCompleteResult() {
        when(images.findAllWithManufacturer()).thenReturn(List.of());
        for (int i = 1; i <= 5; i++) {
            stored.add(object("ob4-generic-ro-y20." + i + ".tar.sig", "1"));
        }

        var page = service.list(1, 2, null, null, null, "release_version,desc");

        assertThat(page.totalElements()).isEqualTo(5);
        assertThat(page.objects()).extracting(Item::releaseVersion).containsExactly("y20.3", "y20.2");
        assertThat(service.list(9, 2, null, null, null, null).objects()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "-1|10|manufacturer,asc|page must not be negative",
            "0|0|manufacturer,asc|size must be between 1 and 200",
            "0|201|manufacturer,asc|size must be between 1 and 200",
            "0|10|object_name,asc|sort field is not supported",
            "0|10|file_name,up|sort direction must be asc or desc" })
    void rejectsInvalidPagingAndSortingBeforeListing(int page, int size, String sort, String message) {
        assertThatThrownBy(() -> service.list(page, size, null, null, null, sort))
                .isInstanceOf(IllegalArgumentException.class).hasMessage(message);
        verify(images, never()).findAllWithManufacturer();
    }

    private ObuFirmwareImage image(int id, String fileName, String provider, String version) {
        var image = new ObuFirmwareImage();
        image.setId(id);
        image.setManufacturer(commsignia);
        image.setFileName(fileName);
        image.setMetadata(new CommsigniaObuFirmwareFileNameParser().parse(fileName).orElseThrow());
        image.setStorageProvider(provider);
        image.setStorageContainer(ObuFirmwareImage.LOCAL_STORAGE_PROVIDER.equals(provider) ? "local" : "bucket");
        image.setObjectName("ota/Commsignia/" + fileName);
        if (version != null) {
            image.setContentLength(1024L);
            image.setChecksumAlgorithm("CRC32C");
            image.setChecksum(CHECKSUM.value());
            image.setProviderObjectVersion(version);
        }
        return image;
    }

    private ObuFirmwareUpload upload(String fileName, FirmwareUploadStatus status, Instant expiresAt) {
        var upload = new ObuFirmwareUpload();
        upload.setId(UUID.randomUUID());
        upload.setFileName(fileName);
        upload.setObjectName("ota/Commsignia/" + fileName);
        upload.setMetadata(new ObuFirmwareFileMetadata(ObuFirmwareDeviceType.OBU,
                fileName.replace("ob4-generic-ro-", "").replace(".tar.sig", ""), "ob4-generic", false, false));
        upload.setExpectedSize(1024L);
        upload.setStatus(status);
        upload.setExpiresAt(expiresAt);
        return upload;
    }

    private static StorageObject object(String fileName, String version) {
        return new StorageObject("ota/Commsignia/" + fileName, 1024, UPDATED, version, CHECKSUM);
    }
}
