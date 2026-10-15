package us.dot.its.jpo.ode.api.services.firmware;

import java.time.Instant;
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
        // A late verification response must not recreate a deleted upload or
        // downgrade an upload already verified by another request.
        uploads.findByIdForUpdate(uploadId).ifPresent(upload -> {
            if (upload.getStatus() != FirmwareUploadStatus.VERIFIED) {
                upload.setStatus(FirmwareUploadStatus.FAILED);
                upload.setFailureReason(reason);
                upload.setFinishedAt(Instant.now());
                uploads.save(upload);
            }
        });
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

        // Completion is idempotent. An upload not yet verified needs its observed
        // storage metadata checked and its verification state updated
        if (upload.getStatus() != FirmwareUploadStatus.VERIFIED) {
            if (!FirmwareUploadSupport.matches(upload, metadata)) {
                throw new FirmwareUploadVerificationException("Firmware metadata does not match the upload intent");
            }
            upload.setStatus(FirmwareUploadStatus.VERIFIED);
            upload.setObservedChecksum(metadata.checksum().value());
            upload.setProviderObjectVersion(metadata.providerObjectVersion());
            upload.setVerifiedAt(Instant.now());
            upload.setFinishedAt(upload.getVerifiedAt());
            upload.setFailureReason(null);
            uploads.save(upload);
        }

        // New images start unpublished, so OBUs only see firmware an admin chose to offer
        ObuFirmwareImage image = existing.orElseGet(() -> images.saveAndFlush(mapper.toImage(upload)));
        return new Registration(upload, image.getId());
    }
}
