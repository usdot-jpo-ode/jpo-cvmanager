package us.dot.its.jpo.ode.api.services.firmware;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUploadStatus;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUpload;
import us.dot.its.jpo.ode.api.models.storage.ObjectChecksum;
import us.dot.its.jpo.ode.api.models.storage.ObjectStorageLocation;
import us.dot.its.jpo.ode.api.models.storage.StorageObject;
import us.dot.its.jpo.ode.api.models.storage.StoredObjectMetadata;
import us.dot.its.jpo.ode.api.repositories.FirmwareUploadRepository;
import us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadService.FirmwareUploadVerificationException;
import us.dot.its.jpo.ode.api.storage.ObjectStorageService;
import us.dot.its.jpo.ode.api.storage.ObjectStorageServiceRegistry;

class FirmwareUploadSupportTest {
    private static final Instant NOW = Instant.parse("2026-09-03T20:00:00Z");

    private FirmwareUpload upload;

    @BeforeEach
    void setUp() {
        upload = new FirmwareUpload();
        upload.setExpectedSize(1024L);
        upload.setChecksumAlgorithm("CRC32C");
        upload.setExpectedChecksum("ImIEBA==");
        upload.setObservedChecksum("ImIEBA==");
        upload.setProviderObjectVersion("17");
        upload.setStatus(FirmwareUploadStatus.VERIFIED);
        upload.setExpiresAt(NOW.minusSeconds(1));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", ".", "..", "dir/file", "dir\\file", "file\u0001name" })
    void rejectsUnsafePathSegments(String segment) {
        assertThatThrownBy(() -> FirmwareUploadSupport.validatePathSegment(segment, "file_name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("file_name must be a valid object path segment");
    }

    @Test
    void trimsValidPathSegments() {
        assertThat(FirmwareUploadSupport.validatePathSegment(" y20.48.2 ", "version")).isEqualTo("y20.48.2");
    }

    @Test
    void normalizesCreator() {
        assertThat(FirmwareUploadSupport.normalizeCreatedBy(null)).isEqualTo("unknown");
        assertThat(FirmwareUploadSupport.normalizeCreatedBy("  ")).isEqualTo("unknown");
        assertThat(FirmwareUploadSupport.normalizeCreatedBy(" admin ")).isEqualTo("admin");
        assertThat(FirmwareUploadSupport.normalizeCreatedBy("a".repeat(300))).hasSize(255);
    }

    @Test
    void validatesSignedLocationAgainstTheRequestedDestination() {
        ObjectStorageService service = mock(ObjectStorageService.class);
        when(service.providerName()).thenReturn("gcp");

        FirmwareUploadSupport.validateSignedLocation(service,
                new ObjectStorageLocation("GCP", "bucket", "ota/file"), "ota/file");
        assertThatThrownBy(() -> FirmwareUploadSupport.validateSignedLocation(service,
                new ObjectStorageLocation("gcp", "bucket", "other"), "ota/file"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void findsConstraintNamesAnywhereInTheCauseChain() {
        var nested = new RuntimeException("wrapper", new RuntimeException(
                "duplicate key value violates unique constraint \"UQ_OBU_FIRMWARE_UPLOADS_ACTIVE_FILE_NAME\""));

        assertThat(FirmwareUploadSupport.isConstraintViolation(nested,
                "uq_obu_firmware_uploads_active_destination", "uq_obu_firmware_uploads_active_file_name")).isTrue();
        assertThat(FirmwareUploadSupport.isConstraintViolation(nested, "other_constraint")).isFalse();
        assertThat(FirmwareUploadSupport.isConstraintViolation(new RuntimeException(), "other_constraint"))
                .isFalse();
    }

    @Test
    void matchesStoredMetadataBySizeChecksumAndAlgorithm() {
        var checksum = new ObjectChecksum("crc32c", "ImIEBA==");

        assertThat(FirmwareUploadSupport.matches(upload, new StoredObjectMetadata(1024, checksum, "17"))).isTrue();
        assertThat(FirmwareUploadSupport.matches(upload, null)).isFalse();
        assertThat(FirmwareUploadSupport.matchesSize(upload, new StoredObjectMetadata(1023, checksum, "17")))
                .isFalse();
        assertThat(FirmwareUploadSupport.matchesChecksum(upload, new StoredObjectMetadata(1024, null, "17")))
                .isFalse();
        assertThat(FirmwareUploadSupport.matchesChecksum(upload,
                new StoredObjectMetadata(1024, new ObjectChecksum("MD5", "ImIEBA=="), "17"))).isFalse();
        assertThat(FirmwareUploadSupport.matchesChecksum(upload,
                new StoredObjectMetadata(1024, new ObjectChecksum("CRC32C", "other"), "17"))).isFalse();
    }

    @Test
    void matchesOnlyTheListedObjectVersionThatWasVerified() {
        var checksum = new ObjectChecksum("CRC32C", "ImIEBA==");

        assertThat(FirmwareUploadSupport.matchesListedObject(upload,
                new StorageObject("ota/file", 1024, NOW, "17", checksum))).isTrue();
        assertThat(FirmwareUploadSupport.matchesListedObject(upload,
                new StorageObject("ota/file", 1024, NOW, "18", checksum))).isFalse();
        assertThat(FirmwareUploadSupport.matchesListedObject(upload,
                new StorageObject("ota/file", 1024, NOW, "17", null))).isFalse();

        upload.setStatus(FirmwareUploadStatus.PENDING);
        assertThat(FirmwareUploadSupport.matchesListedObject(upload,
                new StorageObject("ota/file", 1024, NOW, "17", checksum))).isFalse();
    }

    @Test
    void reportsTheLatestLiveUploadUrl() {
        var expired = new FirmwareUpload();
        expired.setExpiresAt(NOW.minusSeconds(60));
        var soon = new FirmwareUpload();
        soon.setExpiresAt(NOW.plusSeconds(30));
        var later = new FirmwareUpload();
        later.setExpiresAt(NOW.plusSeconds(150));

        assertThat(FirmwareUploadSupport.liveUploadUrlMessage(List.of(expired), NOW)).isEmpty();
        assertThat(FirmwareUploadSupport.liveUploadUrlMessage(List.of(expired, soon), NOW))
                .contains("An upload URL for this firmware is still valid. Try again in 1 minute.");
        assertThat(FirmwareUploadSupport.liveUploadUrlMessage(List.of(soon, later), NOW))
                .contains("An upload URL for this firmware is still valid. Try again in 3 minutes.");
    }

    @Test
    void reportsHowLongAPendingUploadReservesItsFile() {
        var expired = new FirmwareUpload();
        expired.setExpiresAt(NOW.minus(Duration.ofMinutes(61)));
        var inGrace = new FirmwareUpload();
        inGrace.setExpiresAt(NOW.minus(Duration.ofMinutes(30)));
        var live = new FirmwareUpload();
        live.setExpiresAt(NOW.plus(Duration.ofMinutes(4)));
        Duration grace = Duration.ofHours(1);

        assertThat(FirmwareUploadSupport.uploadInProgressMessage(List.of(expired), NOW, grace)).isEmpty();
        assertThat(FirmwareUploadSupport.uploadInProgressMessage(List.of(expired, inGrace), NOW, grace))
                .contains("Another upload of this file is in progress. Finish it, or try again in 30 minutes.");
        assertThat(FirmwareUploadSupport.uploadInProgressMessage(List.of(inGrace, live), NOW, grace))
                .contains("Another upload of this file is in progress. Finish it, or try again in 64 minutes.");
        assertThat(FirmwareUploadSupport.uploadInProgressMessage(List.of(live), NOW, Duration.ZERO))
                .contains("Another upload of this file is in progress. Finish it, or try again in 4 minutes.");
    }

    @Test
    void readsTheUploadedObjectFromTheRecordedProvider() {
        var storage = mock(ObjectStorageServiceRegistry.class);
        var service = mock(ObjectStorageService.class);
        upload.setStorageProvider("gcp");
        upload.setStorageContainer("bucket");
        upload.setObjectName("ota/Commsignia/a.tar.sig");
        when(storage.getService("gcp")).thenReturn(service);
        var location = new ObjectStorageLocation("gcp", "bucket", "ota/Commsignia/a.tar.sig");
        var stored = new StoredObjectMetadata(1024L, new ObjectChecksum("CRC32C", "ImIEBA=="), "17");
        var failures = new ArrayList<String>();

        when(service.getObjectMetadata(location, "CRC32C")).thenReturn(Optional.of(stored));
        assertThat(FirmwareUploadSupport.readMatchingObject(storage, upload, failures::add)).isSameAs(stored);

        when(service.getObjectMetadata(location, "CRC32C")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> FirmwareUploadSupport.readMatchingObject(storage, upload, failures::add))
                .isInstanceOf(FirmwareUploadVerificationException.class).hasMessageContaining("not found in storage");
        assertThat(failures).isEmpty();

        when(service.getObjectMetadata(location, "CRC32C")).thenReturn(Optional.of(
                new StoredObjectMetadata(2048L, stored.checksum(), "17")));
        assertThatThrownBy(() -> FirmwareUploadSupport.readMatchingObject(storage, upload, failures::add))
                .isInstanceOf(FirmwareUploadVerificationException.class).hasMessageContaining("size");

        when(service.getObjectMetadata(location, "CRC32C")).thenReturn(Optional.of(
                new StoredObjectMetadata(1024L, new ObjectChecksum("CRC32C", "other=="), "17")));
        assertThatThrownBy(() -> FirmwareUploadSupport.readMatchingObject(storage, upload, failures::add))
                .isInstanceOf(FirmwareUploadVerificationException.class).hasMessageContaining("checksum");
        assertThat(failures).containsExactly("SIZE_MISMATCH", "CHECKSUM_MISMATCH");
    }

    @Test
    void marksOnlyUnverifiedUploadsFailed() {
        var uploads = mock(FirmwareUploadRepository.class);
        UUID id = UUID.randomUUID();
        upload.setStatus(FirmwareUploadStatus.PENDING);
        when(uploads.findByIdForUpdate(id)).thenReturn(Optional.of(upload));

        FirmwareUploadSupport.markFailed(uploads, id, "SIZE_MISMATCH");

        assertThat(upload.getStatus()).isEqualTo(FirmwareUploadStatus.FAILED);
        assertThat(upload.getFailureReason()).isEqualTo("SIZE_MISMATCH");
        assertThat(upload.getFinishedAt()).isNotNull();
        verify(uploads).save(upload);

        var verified = mock(FirmwareUploadRepository.class);
        var verifiedUpload = new FirmwareUpload();
        verifiedUpload.setStatus(FirmwareUploadStatus.VERIFIED);
        when(verified.findByIdForUpdate(id)).thenReturn(Optional.of(verifiedUpload));
        FirmwareUploadSupport.markFailed(verified, id, "CHECKSUM_MISMATCH");
        FirmwareUploadSupport.markFailed(verified, UUID.randomUUID(), "CHECKSUM_MISMATCH");
        assertThat(verifiedUpload.getStatus()).isEqualTo(FirmwareUploadStatus.VERIFIED);
        verify(verified, never()).save(any());
    }

    @Test
    void marksMatchingUploadsVerifiedOnce() {
        var stored = new StoredObjectMetadata(1024L, new ObjectChecksum("CRC32C", "ImIEBA=="), "18");
        assertThat(FirmwareUploadSupport.markVerified(upload, stored)).isFalse();
        assertThat(upload.getProviderObjectVersion()).isEqualTo("17");

        upload.setStatus(FirmwareUploadStatus.EXPIRED);
        upload.setFailureReason("SIGNED_URL_EXPIRED");
        assertThat(FirmwareUploadSupport.markVerified(upload, stored)).isTrue();
        assertThat(upload.getStatus()).isEqualTo(FirmwareUploadStatus.VERIFIED);
        assertThat(upload.getProviderObjectVersion()).isEqualTo("18");
        assertThat(upload.getObservedChecksum()).isEqualTo("ImIEBA==");
        assertThat(upload.getVerifiedAt()).isNotNull().isEqualTo(upload.getFinishedAt());
        assertThat(upload.getFailureReason()).isNull();

        upload.setStatus(FirmwareUploadStatus.PENDING);
        assertThatThrownBy(() -> FirmwareUploadSupport.markVerified(upload, null))
                .isInstanceOf(FirmwareUploadVerificationException.class);
        assertThatThrownBy(() -> FirmwareUploadSupport.markVerified(upload,
                new StoredObjectMetadata(1L, stored.checksum(), "18")))
                .isInstanceOf(FirmwareUploadVerificationException.class);
        assertThat(upload.getStatus()).isEqualTo(FirmwareUploadStatus.PENDING);
    }
}
