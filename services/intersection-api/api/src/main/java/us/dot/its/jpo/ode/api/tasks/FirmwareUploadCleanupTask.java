package us.dot.its.jpo.ode.api.tasks;

import java.time.Instant;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUploadStatus;
import us.dot.its.jpo.ode.api.repositories.UploadLifecycleRepository;
import us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadCleanupProperties;

/**
 * Expires abandoned upload intents and removes old unsuccessful upload history
 * from every firmware upload table.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "firmware-upload.cleanup", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FirmwareUploadCleanupTask {
    public static final String EXPIRATION_REASON = "SIGNED_URL_EXPIRED";
    private static final List<FirmwareUploadStatus> PURGEABLE_STATUSES = List.of(
            FirmwareUploadStatus.FAILED, FirmwareUploadStatus.EXPIRED);

    private final List<UploadLifecycleRepository<?>> uploadRepositories;
    private final FirmwareUploadCleanupProperties properties;

    @Scheduled(fixedDelayString = "${firmware-upload.cleanup.interval:1h}")
    public void cleanUpFirmwareUploads() {
        cleanUpFirmwareUploads(Instant.now());
    }

    void cleanUpFirmwareUploads(Instant now) {
        validateDurations();

        // Keep a grace period after URL expiration for uploads that began shortly
        // before the signed URL expired and still need their completion call
        Instant expirationCutoff = now.minus(properties.getExpirationGrace());
        // Retain unsuccessful rows temporarily for diagnosis, then purge them.
        // VERIFIED upload records are deliberately excluded from this operation
        Instant retentionCutoff = now.minus(properties.getRetention());

        int expired = 0;
        int deleted = 0;
        for (UploadLifecycleRepository<?> repository : uploadRepositories) {
            expired += repository.expirePendingUploads(
                    FirmwareUploadStatus.PENDING,
                    FirmwareUploadStatus.EXPIRED,
                    expirationCutoff,
                    now,
                    EXPIRATION_REASON);
            deleted += repository.deleteFinishedUploadsBefore(PURGEABLE_STATUSES, retentionCutoff);
        }

        if (expired > 0 || deleted > 0) {
            log.info("Firmware upload cleanup expired {} pending rows and deleted {} retained finished rows",
                    expired, deleted);
        }
    }

    private void validateDurations() {
        // Reject unsafe settings rather than allowing a cleanup run to expire or
        // delete rows earlier than intended
        if (properties.getExpirationGrace() == null || properties.getExpirationGrace().isNegative()) {
            throw new IllegalStateException("Firmware upload expiration grace must not be negative");
        }
        if (properties.getRetention() == null || properties.getRetention().isZero()
                || properties.getRetention().isNegative()) {
            throw new IllegalStateException("Firmware upload retention must be greater than zero");
        }
    }
}
