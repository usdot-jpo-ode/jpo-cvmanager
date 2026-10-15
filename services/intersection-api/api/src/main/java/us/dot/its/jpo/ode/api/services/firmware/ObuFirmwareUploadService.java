package us.dot.its.jpo.ode.api.services.firmware;

import static us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadSupport.isConstraintViolation;
import static us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadSupport.normalizeCreatedBy;
import static us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadSupport.readMatchingObject;
import static us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadSupport.validatePathSegment;
import static us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadSupport.validateSignedLocation;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import us.dot.its.jpo.ode.api.mappers.ObuFirmwareMapper;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUploadStatus;
import us.dot.its.jpo.ode.api.models.postgres.tables.Manufacturer;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareFileMetadata;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareUpload;
import us.dot.its.jpo.ode.api.models.storage.FirmwareUploadUrl;
import us.dot.its.jpo.ode.api.models.storage.ObjectChecksum;
import us.dot.its.jpo.ode.api.models.storage.ObjectStorageLocation;
import us.dot.its.jpo.ode.api.models.storage.ObjectUploadRequest;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.UploadUrlRequest;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.UploadVerification;
import us.dot.its.jpo.ode.api.models.storage.SignedUploadUrl;
import us.dot.its.jpo.ode.api.models.storage.StoredObjectMetadata;
import us.dot.its.jpo.ode.api.repositories.ManufacturerRepository;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareImageRepository;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareUploadRepository;
import us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadService.FirmwareUploadConfigurationException;
import us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadService.FirmwareVersionAlreadyExistsException;
import us.dot.its.jpo.ode.api.services.firmware.ObuFirmwareRegistrationService.Registration;
import us.dot.its.jpo.ode.api.storage.ObjectStorageProperties;
import us.dot.its.jpo.ode.api.storage.ObjectStorageService;
import us.dot.its.jpo.ode.api.storage.ObjectStorageServiceRegistry;
import us.dot.its.jpo.ode.api.storage.ObuFirmwareStorageLayout;

/**
 * Coordinates the OBU firmware upload lifecycle across PostgreSQL and the
 * selected object-storage provider. Uploads keep the vendor's original file
 * name, which the OBU OTA server serves to OBUs.
 */
@Service
@RequiredArgsConstructor
public class ObuFirmwareUploadService {
    private static final String[] DESTINATION_CONSTRAINTS = {
            "uq_obu_firmware_uploads_active_destination",
            "uq_obu_firmware_uploads_active_file_name",
            "obu_firmware_images_manufacturer_file_name_unique",
            "obu_firmware_images_destination_unique"};

    private final ManufacturerRepository manufacturers;
    private final ObuFirmwareParserRegistry parsers;
    private final ObuFirmwareUploadRepository uploads;
    private final ObuFirmwareImageRepository images;
    private final ObjectStorageServiceRegistry objectStorageServices;
    private final ObjectStorageProperties objectStorageProperties;
    private final ObuFirmwareMapper mapper;
    private final ObuFirmwareRegistrationService registration;

    public FirmwareUploadUrl createSignedUploadUrl(UploadUrlRequest request, String createdBy) {
        // Resolve the manufacturer and its naming convention from trusted records
        String manufacturerName = validatePathSegment(request.manufacturerName(), "manufacturer_name");
        if (ObuFirmwareStorageLayout.isReservedName(manufacturerName)) {
            throw new IllegalArgumentException("manufacturer_name is reserved");
        }
        Manufacturer manufacturer = manufacturers.findByName(manufacturerName)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Manufacturer '" + manufacturerName + "' was not found"));
        ObuFirmwareFileNameParser parser = parsers.find(manufacturer.getName())
                .orElseThrow(() -> new FirmwareUploadConfigurationException(
                        "OBU firmware uploads are not supported for manufacturer '" + manufacturer.getName() + "'"));

        String fileName = validatePathSegment(request.fileName(), "file_name");
        ObuFirmwareFileMetadata metadata = parser.parse(fileName)
                .orElseThrow(() -> new IllegalArgumentException("file_name must follow the "
                        + manufacturer.getName() + " OBU firmware naming convention, for example "
                        + parser.exampleFileName()));

        if (request.contentLength() == null || request.contentLength() <= 0) {
            throw new IllegalArgumentException("content_length must be greater than zero");
        }
        long maxUploadBytes = objectStorageProperties.getMaxUploadSize().toBytes();
        if (request.contentLength() > maxUploadBytes) {
            throw new IllegalArgumentException("content_length must not exceed " + maxUploadBytes + " bytes");
        }

        if (images.existsByManufacturerIdAndFileName(manufacturer.getId(), fileName)) {
            throw new FirmwareVersionAlreadyExistsException(ObuFirmwareRegistrationService.ALREADY_EXISTS_MESSAGE);
        }

        String objectName = ObuFirmwareStorageLayout.objectName(
                validatePathSegment(manufacturer.getName(), "manufacturer"),
                validatePathSegment(metadata.getReleaseVersion(), "release_version"),
                fileName);
        ObjectChecksum expectedChecksum = new ObjectChecksum(
                request.checksumAlgorithm().trim().toUpperCase(Locale.ROOT), request.checksum().trim());
        ObjectStorageService objectStorageService = objectStorageServices.getActiveService();

        // Avoid creating a PENDING intent when the immutable destination is already
        // occupied. The provider's create-only upload condition remains the final
        // protection against another writer winning after this check
        if (objectStorageService.objectExists(objectName)) {
            throw new FirmwareVersionAlreadyExistsException(ObuFirmwareRegistrationService.ALREADY_EXISTS_MESSAGE);
        }

        SignedUploadUrl signedUrl = objectStorageService.createSignedUploadUrl(new ObjectUploadRequest(
                objectName, request.contentLength(), request.contentType().trim(), expectedChecksum));
        ObjectStorageLocation location = signedUrl.location();
        validateSignedLocation(objectStorageService, location, objectName);

        // Persist the intent only after signing succeeds, so every PENDING row has a
        // usable set of upload instructions associated with it
        ObuFirmwareUpload upload = mapper.toEntity(request, manufacturer, metadata, signedUrl, expectedChecksum,
                UUID.randomUUID(), normalizeCreatedBy(createdBy), Instant.now());
        try {
            uploads.save(upload);
        } catch (DataIntegrityViolationException ex) {
            throw conflictOr(ex);
        }

        return new FirmwareUploadUrl(upload.getId(), signedUrl.uploadUrl(), signedUrl.method(),
                location.objectName(), signedUrl.expiresAt(), signedUrl.requiredHeaders());
    }

    public UploadVerification completeUpload(UUID uploadId) {
        ObuFirmwareUpload upload = uploads.findById(uploadId)
                .orElseThrow(() -> new EntityNotFoundException("OBU firmware upload '" + uploadId + "' was not found"));

        if (upload.getStatus() == FirmwareUploadStatus.VERIFIED) {
            return toVerification(register(uploadId, null));
        }

        StoredObjectMetadata metadata = readMatchingObject(objectStorageServices, upload,
                reason -> registration.markFailed(uploadId, reason));
        return toVerification(register(uploadId, metadata));
    }

    private Registration register(UUID uploadId, StoredObjectMetadata metadata) {
        try {
            return registration.register(uploadId, metadata);
        } catch (DataIntegrityViolationException ex) {
            throw conflictOr(ex);
        }
    }

    private RuntimeException conflictOr(DataIntegrityViolationException ex) {
        if (isConstraintViolation(ex, DESTINATION_CONSTRAINTS)) {
            return new FirmwareVersionAlreadyExistsException(ObuFirmwareRegistrationService.ALREADY_EXISTS_MESSAGE, ex);
        }
        return ex;
    }

    private UploadVerification toVerification(Registration registered) {
        ObuFirmwareUpload upload = registered.upload();
        return new UploadVerification(upload.getId(), upload.getStatus(), upload.getObjectName(),
                upload.getExpectedSize(), upload.getChecksumAlgorithm(), upload.getObservedChecksum(),
                upload.getProviderObjectVersion(), upload.getVerifiedAt(), registered.obuFirmwareId());
    }
}
