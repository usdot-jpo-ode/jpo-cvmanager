package us.dot.its.jpo.ode.api.services.firmware;

import java.util.UUID;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import us.dot.its.jpo.ode.api.mappers.FirmwareUploadMapper;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUpload;
import us.dot.its.jpo.ode.api.models.storage.StoredObjectMetadata;
import us.dot.its.jpo.ode.api.repositories.FirmwareImageRepository;
import us.dot.its.jpo.ode.api.repositories.FirmwareUploadRepository;
import us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadService.FirmwareVersionAlreadyExistsException;

/**
 * Commits verification and image registration together, without cloud calls in
 * the transaction.
 */
@Service
@RequiredArgsConstructor
public class FirmwareRegistrationService {
    private final FirmwareUploadRepository uploads;
    private final FirmwareImageRepository images;
    private final FirmwareUploadMapper mapper;

    @Transactional
    public void markFailed(UUID uploadId, String reason) {
        FirmwareUploadSupport.markFailed(uploads, uploadId, reason);
    }

    @Transactional
    public FirmwareUpload register(UUID uploadId, StoredObjectMetadata metadata) {
        // Serialize repeated completion calls for the same upload
        FirmwareUpload upload = uploads.findByIdForUpdate(uploadId)
                .orElseThrow(() -> new EntityNotFoundException("Firmware upload '" + uploadId + "' was not found"));

        // A model/version identifies one installable image, so another upload cannot
        // replace an image that has already been registered
        var existing = images.findByModelIdAndVersion(upload.getModel().getId(), upload.getVersion());
        if (existing.isPresent() && (existing.get().getVerifiedUpload() == null
                || !uploadId.equals(existing.get().getVerifiedUpload().getId()))) {
            throw new FirmwareVersionAlreadyExistsException("Firmware already exists for this model and version");
        }

        // Completion is idempotent. An upload not yet verified needs its observed storage
        // metadata checked and its verification state updated
        if (FirmwareUploadSupport.markVerified(upload, metadata)) {
            uploads.save(upload);
        }

        // Registering the image in the same transaction prevents a verified upload
        // from existing without its corresponding firmware image
        if (existing.isEmpty()) {
            images.saveAndFlush(mapper.toFirmwareImage(upload));
        }
        return upload;
    }
}
