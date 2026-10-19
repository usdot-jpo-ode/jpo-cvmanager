package us.dot.its.jpo.ode.api.services.firmware;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityNotFoundException;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareImage;
import us.dot.its.jpo.ode.api.models.storage.ObjectChecksum;
import us.dot.its.jpo.ode.api.models.storage.ObjectStorageLocation;
import us.dot.its.jpo.ode.api.models.storage.StoredObjectMetadata;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareImageRepository;
import us.dot.its.jpo.ode.api.storage.ObjectStorageService;
import us.dot.its.jpo.ode.api.storage.ObjectStorageServiceRegistry;

class ObuFirmwarePublicationServiceTest {
    private static final ObjectStorageLocation LOCATION =
            new ObjectStorageLocation("gcp", "bucket", "ota/Commsignia/a.tar.sig");
    private static final ObjectChecksum CHECKSUM = new ObjectChecksum("CRC32C", "ImIEBA==");

    private final ObuFirmwareImageRepository images = mock(ObuFirmwareImageRepository.class);
    private final ObjectStorageServiceRegistry registry = mock(ObjectStorageServiceRegistry.class);
    private final ObjectStorageService storage = mock(ObjectStorageService.class);
    private final ObuFirmwarePublicationService service = new ObuFirmwarePublicationService(images, registry);
    private ObuFirmwareImage image;

    @BeforeEach
    void setUp() {
        image = new ObuFirmwareImage();
        image.setId(42);
        image.setStorageProvider("gcp");
        image.setStorageContainer("bucket");
        image.setObjectName("ota/Commsignia/a.tar.sig");
        image.setContentLength(1024L);
        image.setChecksumAlgorithm("CRC32C");
        image.setChecksum(CHECKSUM.value());
        image.setProviderObjectVersion("17");
        when(images.findByIdForUpdate(42)).thenReturn(Optional.of(image));
        when(registry.getService("gcp")).thenReturn(storage);
    }

    @Test
    void publishesAnUnchangedCloudFile() {
        when(storage.getObjectMetadata(LOCATION, "CRC32C"))
                .thenReturn(Optional.of(new StoredObjectMetadata(1024L, CHECKSUM, "17")));

        var publication = service.setPublished(42, true, " admin@example.com ");

        assertThat(publication.published()).isTrue();
        assertThat(publication.publishedAt()).isNotNull();
        assertThat(publication.publishedBy()).isEqualTo("admin@example.com");
        assertThat(image.getPublished()).isTrue();
        verify(images).save(image);
    }

    @Test
    void refusesToPublishAMissingOrChangedFile() {
        for (var stored : new StoredObjectMetadata[] { null, new StoredObjectMetadata(1024L, CHECKSUM, "18"),
                new StoredObjectMetadata(2048L, CHECKSUM, "17"),
                new StoredObjectMetadata(1024L, new ObjectChecksum("CRC32C", "other=="), "17") }) {
            when(storage.getObjectMetadata(LOCATION, "CRC32C")).thenReturn(Optional.ofNullable(stored));

            assertThatThrownBy(() -> service.setPublished(42, true, "admin"))
                    .isInstanceOf(ObuFirmwareConflictException.class)
                    .hasMessageContaining("missing or has changed");
        }
        assertThat(image.getPublished()).isFalse();
        assertThat(image.getPublishedAt()).isNull();
        verify(images, never()).save(any());
    }

    @Test
    void publishesLocalFilesWithoutReadingStorage() {
        image.setStorageProvider(ObuFirmwareImage.LOCAL_STORAGE_PROVIDER);

        assertThat(service.setPublished(42, true, "admin").published()).isTrue();
        verifyNoInteractions(registry);
    }

    @Test
    void withdrawsFirmwareAndClearsWhoPublishedIt() {
        image.setPublished(true);
        image.setPublishedAt(java.time.Instant.now());
        image.setPublishedBy("admin");

        var publication = service.setPublished(42, false, "other");

        assertThat(publication.published()).isFalse();
        assertThat(publication.publishedAt()).isNull();
        assertThat(publication.publishedBy()).isNull();
        verifyNoInteractions(registry);
        verify(images).save(image);
    }

    @Test
    void repeatingTheCurrentStateChangesNothing() {
        var publication = service.setPublished(42, false, "admin");

        assertThat(publication.published()).isFalse();
        verify(images, never()).save(any());

        image.setPublished(true);
        var publishedAt = java.time.Instant.parse("2026-10-15T12:00:00Z");
        image.setPublishedAt(publishedAt);
        assertThat(service.setPublished(42, true, "someone-else").publishedAt()).isEqualTo(publishedAt);
        verify(images, never()).save(any());
        verifyNoInteractions(registry);
    }

    @Test
    void rejectsUnknownFirmware() {
        when(images.findByIdForUpdate(7)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setPublished(7, true, "admin"))
                .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("7");
    }
}
