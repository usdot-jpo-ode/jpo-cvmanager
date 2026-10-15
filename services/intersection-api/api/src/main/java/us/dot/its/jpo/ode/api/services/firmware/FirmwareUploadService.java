package us.dot.its.jpo.ode.api.services.firmware;

import static us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadSupport.isConstraintViolation;
import static us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadSupport.matchesChecksum;
import static us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadSupport.matchesSize;
import static us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadSupport.normalizeCreatedBy;
import static us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadSupport.validatePathSegment;
import static us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadSupport.validateSignedLocation;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import us.dot.its.jpo.ode.api.mappers.FirmwareUploadMapper;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUpload;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUploadStatus;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuModel;
import us.dot.its.jpo.ode.api.models.storage.FirmwareUploadUrl;
import us.dot.its.jpo.ode.api.models.storage.FirmwareUploadUrlRequest;
import us.dot.its.jpo.ode.api.models.storage.FirmwareUploadVerification;
import us.dot.its.jpo.ode.api.models.storage.ObjectChecksum;
import us.dot.its.jpo.ode.api.models.storage.ObjectStorageLocation;
import us.dot.its.jpo.ode.api.models.storage.ObjectUploadRequest;
import us.dot.its.jpo.ode.api.models.storage.SignedUploadUrl;
import us.dot.its.jpo.ode.api.models.storage.StoredObjectMetadata;
import us.dot.its.jpo.ode.api.repositories.FirmwareUploadRepository;
import us.dot.its.jpo.ode.api.repositories.FirmwareImageRepository;
import us.dot.its.jpo.ode.api.repositories.RsuModelRepository;
import us.dot.its.jpo.ode.api.storage.ObjectStorageProperties;
import us.dot.its.jpo.ode.api.storage.ObjectStorageService;
import us.dot.its.jpo.ode.api.storage.ObjectStorageServiceRegistry;
import us.dot.its.jpo.ode.api.storage.ObuFirmwareStorageLayout;

/**
 * Coordinates the firmware upload lifecycle across PostgreSQL and the selected
 * object-storage provider
 */
@Service
@RequiredArgsConstructor
public class FirmwareUploadService {
    private static final String[] ACTIVE_DESTINATION_CONSTRAINTS = {
            "uq_firmware_uploads_active_destination",
            "uq_firmware_uploads_active_model_version",
            "firmware_images_model_version_unique"};
    private static final String DESTINATION_EXISTS_MESSAGE =
            "Firmware already exists for this manufacturer, model, and version";

    private final RsuModelRepository rsuModelRepository;
    private final FirmwareUploadRepository firmwareUploadRepository;
    private final ObjectStorageServiceRegistry objectStorageServices;
    private final ObjectStorageProperties objectStorageProperties;
    private final FirmwareUploadMapper firmwareUploadMapper;
    private final FirmwareImageRepository firmwareImages;
    private final FirmwareRegistrationService registration;

    public FirmwareUploadUrl createFirmwareSignedUploadUrl(FirmwareUploadUrlRequest request, String createdBy) {
        // Resolve the model from trusted database records instead of accepting an
        // arbitrary vendor/model path from the client
        String vendorName = request.getVendorName().trim();
        String modelName = request.getModelName().trim();
        RsuModel model = rsuModelRepository.findByNameAndManufacturerName(modelName, vendorName)
                .orElseThrow(() -> new EntityNotFoundException(
                        "RSU model '" + modelName + "' was not found for vendor '" + vendorName + "'"));

        if (request.getContentLength() == null || request.getContentLength() <= 0) {
            throw new IllegalArgumentException("content_length must be greater than zero");
        }
        long maxUploadBytes = objectStorageProperties.getMaxUploadSize().toBytes();
        if (request.getContentLength() > maxUploadBytes) {
            throw new IllegalArgumentException(
                    "content_length must not exceed " + maxUploadBytes + " bytes");
        }

        // Build a provider-neutral upload request. The selected provider validates
        // whether it supports the requested checksum algorithm and encoding
        if (firmwareImages.existsByModelIdAndVersion(model.getId(), request.getVersion().trim())) {
            throw new FirmwareVersionAlreadyExistsException("Firmware already exists for this model and version");
        }

        String storedFileName = buildStoredFileName(request, model);
        String checksumAlgorithm = request.getChecksumAlgorithm().trim().toUpperCase(Locale.ROOT);
        ObjectChecksum expectedChecksum = new ObjectChecksum(checksumAlgorithm, request.getChecksum().trim());
        String objectName = buildObjectName(request, storedFileName);
        ObjectStorageService objectStorageService = objectStorageServices.getActiveService();

        // Avoid creating a PENDING intent when the immutable destination is already
        // occupied. The provider's create-only upload condition remains the final
        // protection against another writer winning after this check
        if (objectStorageService.objectExists(objectName)) {
            throw new FirmwareVersionAlreadyExistsException(DESTINATION_EXISTS_MESSAGE);
        }

        SignedUploadUrl signedUrl = objectStorageService.createSignedUploadUrl(new ObjectUploadRequest(
                objectName, request.getContentLength(), request.getContentType().trim(), expectedChecksum));
        ObjectStorageLocation location = signedUrl.location();
        validateSignedLocation(objectStorageService, location, objectName);
        Instant now = Instant.now();

        // Persist the intent only after signing succeeds, so every PENDING row has a
        // usable set of upload instructions associated with it
        FirmwareUpload upload = firmwareUploadMapper.toEntity(request, model, signedUrl,
                expectedChecksum, UUID.randomUUID(), normalizeCreatedBy(createdBy), now, storedFileName);
        try {
            firmwareUploadRepository.save(upload);
        } catch (DataIntegrityViolationException ex) {
            if (isActiveDestinationConflict(ex)) {
                throw new FirmwareVersionAlreadyExistsException(DESTINATION_EXISTS_MESSAGE, ex);
            }
            throw ex;
        }

        return new FirmwareUploadUrl(upload.getId(), signedUrl.uploadUrl(), signedUrl.method(),
                location.objectName(), signedUrl.expiresAt(), signedUrl.requiredHeaders());
    }

    public FirmwareUploadVerification completeFirmwareUpload(UUID uploadId) {
        FirmwareUpload upload = firmwareUploadRepository.findById(uploadId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Firmware upload '" + uploadId + "' was not found"));

        if (upload.getStatus() == FirmwareUploadStatus.VERIFIED) {
            return toVerification(register(uploadId, null));
        }

        // Route completion through the cloud provider recorded when the URL was
        // issued. This remains valid even if the application's active provider later
        // changes
        ObjectStorageService objectStorageService = objectStorageServices.getService(upload.getStorageProvider());
        ObjectStorageLocation location = new ObjectStorageLocation(
                upload.getStorageProvider(), upload.getStorageContainer(), upload.getObjectName());
        StoredObjectMetadata metadata = objectStorageService
                .getObjectMetadata(location, upload.getChecksumAlgorithm())
                .orElseThrow(() -> new FirmwareUploadVerificationException(
                        "Cannot verify this upload because the expected firmware file was not found in storage. "
                                + "Ensure the file upload using the signed URL completed successfully before "
                                + "requesting verification."));

        // Verification requires both the expected byte size and the exact checksum
        if (!matchesSize(upload, metadata)) {
            markFailed(upload, "SIZE_MISMATCH");
            throw new FirmwareUploadVerificationException(
                    "Uploaded object size does not match content_length");
        }
        if (!matchesChecksum(upload, metadata)) {
            markFailed(upload, "CHECKSUM_MISMATCH");
            throw new FirmwareUploadVerificationException(
                    "Uploaded object checksum does not match the expected checksum");
        }

        // Retain the provider's observed values so the verified record identifies the
        // exact stored object version that was checked
        return toVerification(register(uploadId, metadata));
    }

    private FirmwareUpload register(UUID uploadId, StoredObjectMetadata metadata) {
        try {
            return registration.register(uploadId, metadata);
        } catch (DataIntegrityViolationException ex) {
            if (isActiveDestinationConflict(ex)) {
                throw new FirmwareVersionAlreadyExistsException("Firmware already exists for this model and version", ex);
            }
            throw ex;
        }
    }

    private FirmwareUploadVerification toVerification(FirmwareUpload upload) {
        return new FirmwareUploadVerification(upload.getId(), upload.getStatus(), upload.getObjectName(),
                upload.getExpectedSize(), upload.getChecksumAlgorithm(), upload.getObservedChecksum(),
                upload.getProviderObjectVersion(), upload.getVerifiedAt());
    }

    private void markFailed(FirmwareUpload upload, String reason) {
        registration.markFailed(upload.getId(), reason);
    }

    private boolean isActiveDestinationConflict(Throwable exception) {
        return isConstraintViolation(exception, ACTIVE_DESTINATION_CONSTRAINTS);
    }

    private String buildStoredFileName(FirmwareUploadUrlRequest request, RsuModel model) {
        String manufacturerName = model.getManufacturer().getName();
        String extension = model.getManufacturer().getFirmwareFileExtension();
        if (extension == null || !extension.matches("^\\.[A-Za-z0-9]++(?:\\.[A-Za-z0-9]++)*+$")) {
            throw new FirmwareUploadConfigurationException(
                    "Firmware uploads are not configured for manufacturer '" + manufacturerName + "'");
        }

        String sourceFileName = validatePathSegment(request.getFileName(), "file_name");
        if (!sourceFileName.toLowerCase(Locale.ROOT).endsWith(extension.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException(
                    "file_name must use the " + extension + " extension for manufacturer '"
                            + manufacturerName + "'");
        }

        String storedFileName = validatePathSegment(request.getVersion(), "version") + extension;
        if (storedFileName.length() > 128) {
            throw new IllegalArgumentException(
                    "version is too long when combined with the manufacturer firmware file extension");
        }
        return storedFileName;
    }

    private String buildObjectName(FirmwareUploadUrlRequest request, String storedFileName) {
        String vendor = validatePathSegment(request.getVendorName(), "vendor_name");
        if (ObuFirmwareStorageLayout.isReservedName(vendor)) {
            throw new IllegalArgumentException("vendor_name is reserved for OBU firmware");
        }
        return String.join("/",
                vendor,
                validatePathSegment(request.getModelName(), "model_name"),
                validatePathSegment(request.getVersion(), "version"),
                storedFileName);
    }

    public static class FirmwareUploadConfigurationException extends RuntimeException {
        public FirmwareUploadConfigurationException(String message) {
            super(message);
        }
    }

    public static class FirmwareVersionAlreadyExistsException extends RuntimeException {
        public FirmwareVersionAlreadyExistsException(String message) {
            super(message);
        }

        public FirmwareVersionAlreadyExistsException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class FirmwareUploadVerificationException extends RuntimeException {
        public FirmwareUploadVerificationException(String message) {
            super(message);
        }
    }
}
