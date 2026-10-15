package us.dot.its.jpo.ode.api.services.firmware;

import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import us.dot.its.jpo.ode.api.mappers.ObuFirmwareMapper;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUploadStatus;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareImage;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareUpload;
import us.dot.its.jpo.ode.api.models.storage.StoredObjectMetadata;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareImageRepository;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareUploadRepository;
import us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadService.FirmwareUploadVerificationException;
import us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadService.FirmwareVersionAlreadyExistsException;

/**
 * Commits OBU upload verification and catalog registration together, without
 * cloud calls in the transaction.
 */
@Service
@RequiredArgsConstructor
public class ObuFirmwareRegistrationService {
    static final String ALREADY_EXISTS_MESSAGE =
            "OBU firmware with this file name already exists for this manufacturer";

    private final ObuFirmwareUploadRepository uploads;
    private final ObuFirmwareImageRepository images;
    private final ObuFirmwareMapper mapper;

    /** A verified upload and the catalog image registered for it. */
    public record Registration(ObuFirmwareUpload upload, Integer obuFirmwareId) {}

    @Transactional
    public void markFailed(UUID uploadId, String reason) {
        FirmwareUploadSupport.markFailed(uploads, uploadId, reason);
    }

    @Transactional
    public Registration register(UUID uploadId, StoredObjectMetadata metadata) {
        // Serialize repeated completion calls for the same upload
        ObuFirmwareUpload upload = uploads.findByIdForUpdate(uploadId)
                .orElseThrow(() -> new EntityNotFoundException("OBU firmware upload '" + uploadId + "' was not found"));

        // OBUs download by file name, so another upload or a local registration
        // cannot be replaced once its name is in the catalog
        var existing = images.findByManufacturerIdAndFileName(upload.getManufacturer().getId(), upload.getFileName());
        if (existing.isPresent() && (existing.get().getVerifiedUpload() == null
                || !uploadId.equals(existing.get().getVerifiedUpload().getId()))) {
            throw new FirmwareVersionAlreadyExistsException(ALREADY_EXISTS_MESSAGE);
        }

        // Catalog images record the object version so later changes to the stored
        // file can be detected, and the database requires it for cloud files
        if (upload.getStatus() != FirmwareUploadStatus.VERIFIED && metadata != null
                && (metadata.providerObjectVersion() == null || metadata.providerObjectVersion().isBlank())) {
            throw new FirmwareUploadVerificationException(
                    "Object storage did not report a version for the uploaded file");
        }

        // Completion is idempotent. An upload not yet verified needs its observed
        // storage metadata checked and its verification state updated
        if (FirmwareUploadSupport.markVerified(upload, metadata)) {
            uploads.save(upload);
        }

        // New images start unpublished, so OBUs only see firmware an admin chose to offer
        ObuFirmwareImage image = existing.orElseGet(() -> images.saveAndFlush(mapper.toImage(upload)));
        return new Registration(upload, image.getId());
    }
}
