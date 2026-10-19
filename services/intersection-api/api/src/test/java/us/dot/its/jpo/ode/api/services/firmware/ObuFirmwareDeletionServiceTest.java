package us.dot.its.jpo.ode.api.services.firmware;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareImage;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareUpload;
import us.dot.its.jpo.ode.api.models.storage.ObjectStorageLocation;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareImageRepository;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareUploadRepository;
import us.dot.its.jpo.ode.api.storage.ObjectIds;
import us.dot.its.jpo.ode.api.storage.ObjectStorageService;
import us.dot.its.jpo.ode.api.storage.ObjectStorageServiceRegistry;
import us.dot.its.jpo.ode.api.storage.ObjectStorageUnavailableException;

class ObuFirmwareDeletionServiceTest {
    private static final String NAME = "ota/Commsignia/ob4-generic-ro-y20.48.2.tar.sig";
    private static final String OBJECT_ID = ObjectIds.encode("gcp", NAME);
    private static final ObjectStorageLocation LOCATION = new ObjectStorageLocation("gcp", "bucket", NAME);

    private final ObjectStorageServiceRegistry registry = mock(ObjectStorageServiceRegistry.class);
    private final ObjectStorageService storage = mock(ObjectStorageService.class);
    private final ObuFirmwareUploadRepository uploads = mock(ObuFirmwareUploadRepository.class);
    private final ObuFirmwareImageRepository images = mock(ObuFirmwareImageRepository.class);
    private final ObuFirmwareDeletionService service = new ObuFirmwareDeletionService(registry, uploads, images);

    @BeforeEach
    void setUp() {
        when(registry.getActiveService()).thenReturn(storage);
        when(storage.providerName()).thenReturn("gcp");
        when(storage.getLocation(NAME)).thenReturn(LOCATION);
    }

    @Test
    void deletesRecordsBeforeTheStoredFile() {
        var upload = upload(Instant.now().minusSeconds(60));
        var image = image(false);
        when(uploads.findDestinationForUpdate("gcp", "bucket", NAME)).thenReturn(List.of(upload));
        when(images.findDestinationForUpdate("gcp", "bucket", NAME)).thenReturn(List.of(image));

        service.delete(OBJECT_ID, "17");

        var order = inOrder(uploads, images, storage);
        order.verify(uploads).findDestinationForUpdate("gcp", "bucket", NAME);
        order.verify(images).findDestinationForUpdate("gcp", "bucket", NAME);
        order.verify(images).deleteAll(List.of(image));
        order.verify(images).flush();
        order.verify(uploads).deleteAll(List.of(upload));
        order.verify(uploads).flush();
        order.verify(storage).deleteObject(LOCATION, "17");
    }

    @Test
    void deletesFilesThatHaveNoRecords() {
        // An upload that was never completed leaves a file with no catalog entry,
        // which must be deletable so its file name can be uploaded again
        when(uploads.findDestinationForUpdate(any(), any(), any())).thenReturn(List.of());
        when(images.findDestinationForUpdate(any(), any(), any())).thenReturn(List.of());

        service.delete(OBJECT_ID, "17");

        verify(storage).deleteObject(LOCATION, "17");
    }

    @Test
    void refusesToDeletePublishedFirmware() {
        when(images.findDestinationForUpdate("gcp", "bucket", NAME)).thenReturn(List.of(image(true)));

        assertThatThrownBy(() -> service.delete(OBJECT_ID, "17"))
                .isInstanceOf(ObuFirmwareConflictException.class)
                .hasMessage(ObuFirmwareDeletionService.UNPUBLISH_FIRST_MESSAGE);
        assertThatThrownBy(() -> service.cleanupMissingObject(OBJECT_ID))
                .isInstanceOf(ObuFirmwareConflictException.class);
        verify(images, never()).deleteAll(any());
        verify(storage, never()).deleteObject(any(), any());
    }

    @Test
    void waitsForIssuedUploadUrlsToExpire() {
        when(uploads.findDestinationForUpdate("gcp", "bucket", NAME))
                .thenReturn(List.of(upload(Instant.now().plusSeconds(90))));

        assertThatThrownBy(() -> service.delete(OBJECT_ID, "17"))
                .isInstanceOf(ObuFirmwareConflictException.class)
                .hasMessageContaining("still valid").hasMessageContaining("2 minutes");
        verify(images, never()).findDestinationForUpdate(any(), any(), any());
        verify(storage, never()).deleteObject(any(), any());
    }

    @Test
    void cleansUpRecordsOnlyWhenTheFileIsGone() {
        var image = image(false);
        when(images.findDestinationForUpdate("gcp", "bucket", NAME)).thenReturn(List.of(image));
        when(storage.objectExists(NAME)).thenReturn(true);

        assertThatThrownBy(() -> service.cleanupMissingObject(OBJECT_ID))
                .isInstanceOf(ObuFirmwareConflictException.class).hasMessageContaining("present in storage");
        verify(images, never()).deleteAll(any());

        when(storage.objectExists(NAME)).thenReturn(false);
        service.cleanupMissingObject(OBJECT_ID);
        verify(images).deleteAll(List.of(image));
        verify(storage, never()).deleteObject(any(), any());
    }

    @Test
    void removesLocalCatalogEntriesWithoutStorage() {
        String localId = ObjectIds.encode("local", NAME);
        var local = image(false);
        when(images.findDestinationForUpdate("local", "local", NAME)).thenReturn(List.of(local));

        service.cleanupMissingObject(localId);

        verify(images).deleteAll(List.of(local));
        verifyNoInteractions(registry, uploads);
        assertThatThrownBy(() -> service.delete(localId, "17"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("records cleanup");

        local.setPublished(true);
        assertThatThrownBy(() -> service.cleanupMissingObject(localId))
                .isInstanceOf(ObuFirmwareConflictException.class);
    }

    @Test
    void rejectsObjectsOutsideTheObuFirmwareRootAndChangedProviders() {
        assertThatThrownBy(() -> service.delete(ObjectIds.encode("gcp", "Commsignia/ITS-RS4-M/v1/a.tar.sig"), "1"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("ota/");
        assertThatThrownBy(() -> service.delete("not base64!", "1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.delete(OBJECT_ID, " ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.delete(ObjectIds.encode("aws", NAME), "1"))
                .isInstanceOf(ObuFirmwareConflictException.class).hasMessageContaining("provider changed");
        verifyNoInteractions(uploads, images);
    }

    @Test
    void reportsRecordsThatAreStillReferenced() {
        when(images.findDestinationForUpdate("gcp", "bucket", NAME)).thenReturn(List.of(image(false)));
        doThrow(new DataIntegrityViolationException("fk")).when(images).flush();

        assertThatThrownBy(() -> service.delete(OBJECT_ID, "17"))
                .isInstanceOf(ObuFirmwareConflictException.class).hasMessageContaining("still referenced");
        verify(storage, never()).deleteObject(any(), any());
    }

    @Test
    void propagatesStorageFailuresSoTheRecordDeletesRollBack() {
        when(images.findDestinationForUpdate("gcp", "bucket", NAME)).thenReturn(List.of(image(false)));
        doThrow(new ObjectStorageUnavailableException("retry")).when(storage).deleteObject(LOCATION, "17");

        assertThatThrownBy(() -> service.delete(OBJECT_ID, "17"))
                .isInstanceOf(ObjectStorageUnavailableException.class);
    }

    private static ObuFirmwareImage image(boolean published) {
        var image = new ObuFirmwareImage();
        image.setObjectName(NAME);
        image.setPublished(published);
        return image;
    }

    private static ObuFirmwareUpload upload(Instant expiresAt) {
        var upload = new ObuFirmwareUpload();
        upload.setObjectName(NAME);
        upload.setExpiresAt(expiresAt);
        return upload;
    }
}
