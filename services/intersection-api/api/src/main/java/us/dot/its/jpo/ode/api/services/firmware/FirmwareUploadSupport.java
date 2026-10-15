package us.dot.its.jpo.ode.api.services.firmware;

import java.time.Instant;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUploadRecord;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUploadStatus;
import us.dot.its.jpo.ode.api.models.postgres.tables.VerifiableUpload;
import us.dot.its.jpo.ode.api.models.storage.ObjectStorageLocation;
import us.dot.its.jpo.ode.api.models.storage.StorageObject;
import us.dot.its.jpo.ode.api.models.storage.StoredObjectMetadata;
import us.dot.its.jpo.ode.api.repositories.UploadLifecycleRepository;
import us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadService.FirmwareUploadVerificationException;
import us.dot.its.jpo.ode.api.storage.ObjectStorageService;
import us.dot.its.jpo.ode.api.storage.ObjectStorageServiceRegistry;

/** Validation and verification rules shared by RSU and OBU firmware uploads. */
public final class FirmwareUploadSupport {
    private FirmwareUploadSupport() {
    }

    public static String validatePathSegment(String value, String fieldName) {
        String segment = value == null ? "" : value.trim();
        if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)
                || segment.indexOf('/') >= 0 || segment.indexOf('\\') >= 0
                || segment.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(fieldName + " must be a valid object path segment");
        }
        return segment;
    }

    public static String normalizeCreatedBy(String createdBy) {
        if (createdBy == null || createdBy.isBlank()) {
            return "unknown";
        }
        String normalized = createdBy.trim();
        return normalized.length() <= 255 ? normalized : normalized.substring(0, 255);
    }

    /** Rejects a signed upload whose location differs from the requested destination. */
    public static void validateSignedLocation(
            ObjectStorageService service, ObjectStorageLocation location, String requestedObjectName) {
        if (location == null
                || !service.providerName().equalsIgnoreCase(location.provider())
                || location.container() == null || location.container().isBlank()
                || !requestedObjectName.equals(location.objectName())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Object storage returned an invalid upload location");
        }
    }

    /** Returns whether any exception in the cause chain names one of the given constraints. */
    public static boolean isConstraintViolation(Throwable exception, String... constraintNames) {
        Throwable cause = exception;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase(Locale.ROOT);
                for (String name : constraintNames) {
                    if (normalized.contains(name.toLowerCase(Locale.ROOT))) {
                        return true;
                    }
                }
            }
            cause = cause.getCause();
        }
        return false;
    }

    public static boolean matchesSize(VerifiableUpload upload, StoredObjectMetadata metadata) {
        return metadata != null && Objects.equals(upload.getExpectedSize(), metadata.contentLength());
    }

    /**
     * Compares the algorithm as well as the value so equal-looking values from
     * different checksum formats are not treated as equivalent.
     */
    public static boolean matchesChecksum(VerifiableUpload upload, StoredObjectMetadata metadata) {
        return metadata != null && metadata.checksum() != null
                && upload.getChecksumAlgorithm().equalsIgnoreCase(metadata.checksum().algorithm())
                && upload.getExpectedChecksum().equals(metadata.checksum().value());
    }

    /** Returns whether stored metadata matches the size and checksum the upload promised. */
    public static boolean matches(VerifiableUpload upload, StoredObjectMetadata metadata) {
        return matchesSize(upload, metadata) && matchesChecksum(upload, metadata);
    }

    /**
     * Reads the uploaded object from the provider recorded on the upload and checks
     * it against the size and checksum the upload promised. A mismatch is reported
     * through {@code markFailed} before the verification error is thrown.
     *
     * <p>Callers read the upload without a lock before this call and re-read it under
     * a lock when registering, so no row lock is held during the storage request.
     */
    public static StoredObjectMetadata readMatchingObject(ObjectStorageServiceRegistry storage,
            VerifiableUpload upload, Consumer<String> markFailed) {
        // The recorded provider stays valid even if the active provider later changes
        ObjectStorageService service = storage.getService(upload.getStorageProvider());
        ObjectStorageLocation location = new ObjectStorageLocation(
                upload.getStorageProvider(), upload.getStorageContainer(), upload.getObjectName());
        StoredObjectMetadata metadata = service.getObjectMetadata(location, upload.getChecksumAlgorithm())
                .orElseThrow(() -> new FirmwareUploadVerificationException(
                        "Cannot verify this upload because the expected firmware file was not found in storage. "
                                + "Ensure the file upload using the signed URL completed successfully before "
                                + "requesting verification."));
        if (!matchesSize(upload, metadata)) {
            markFailed.accept("SIZE_MISMATCH");
            throw new FirmwareUploadVerificationException("Uploaded object size does not match content_length");
        }
        if (!matchesChecksum(upload, metadata)) {
            markFailed.accept("CHECKSUM_MISMATCH");
            throw new FirmwareUploadVerificationException(
                    "Uploaded object checksum does not match the expected checksum");
        }
        return metadata;
    }

    /**
     * Marks an upload FAILED. A late verification response must not recreate a
     * deleted upload or downgrade an upload already verified by another request.
     * Callers run this inside a transaction so the row lock is held until commit.
     */
    public static <T extends FirmwareUploadRecord> void markFailed(UploadLifecycleRepository<T> uploads,
            UUID uploadId, String reason) {
        uploads.findByIdForUpdate(uploadId).ifPresent(upload -> {
            if (upload.getStatus() != FirmwareUploadStatus.VERIFIED) {
                upload.setStatus(FirmwareUploadStatus.FAILED);
                upload.setFailureReason(reason);
                upload.setFinishedAt(Instant.now());
                uploads.save(upload);
            }
        });
    }

    /**
     * Records verification evidence on an upload that is not yet VERIFIED, keeping the
     * provider's observed values so the record identifies the exact object version
     * checked. Returns whether the upload changed; a VERIFIED upload is left as is.
     */
    public static boolean markVerified(FirmwareUploadRecord upload, StoredObjectMetadata metadata) {
        if (upload.getStatus() == FirmwareUploadStatus.VERIFIED) {
            return false;
        }
        if (!matches(upload, metadata)) {
            throw new FirmwareUploadVerificationException("Firmware metadata does not match the upload intent");
        }
        upload.setStatus(FirmwareUploadStatus.VERIFIED);
        upload.setObservedChecksum(metadata.checksum().value());
        upload.setProviderObjectVersion(metadata.providerObjectVersion());
        upload.setVerifiedAt(Instant.now());
        upload.setFinishedAt(upload.getVerifiedAt());
        upload.setFailureReason(null);
        return true;
    }

    /**
     * Returns whether a listed object is the exact object version a verified
     * upload checked. Verification belongs to the object version, not just its path.
     */
    public static boolean matchesListedObject(VerifiableUpload upload, StorageObject object) {
        return upload.getStatus() == FirmwareUploadStatus.VERIFIED
                && object.providerObjectVersion() != null
                && object.providerObjectVersion().equals(upload.getProviderObjectVersion())
                && Objects.equals(upload.getExpectedSize(), object.contentLength())
                && object.checksum() != null && upload.getObservedChecksum() != null
                && upload.getChecksumAlgorithm().equalsIgnoreCase(object.checksum().algorithm())
                && Objects.equals(upload.getObservedChecksum(), object.checksum().value());
    }

    /**
     * Describes how long to wait when an issued upload URL for a destination can
     * still write to it. Empty when every URL has expired.
     */
    public static Optional<String> liveUploadUrlMessage(Collection<? extends VerifiableUpload> uploads,
            Instant now) {
        return uploads.stream()
                .map(VerifiableUpload::getExpiresAt)
                .filter(expiration -> expiration.isAfter(now))
                .max(Instant::compareTo)
                .map(expiration -> {
                    long remainingMillis = expiration.toEpochMilli() - now.toEpochMilli();
                    long remainingMinutes = Math.max(1, Math.ceilDiv(remainingMillis, 60_000L));
                    String minuteLabel = remainingMinutes == 1 ? "minute" : "minutes";
                    return "An upload URL for this firmware is still valid. Try again in "
                            + remainingMinutes + " " + minuteLabel + ".";
                });
    }
}
