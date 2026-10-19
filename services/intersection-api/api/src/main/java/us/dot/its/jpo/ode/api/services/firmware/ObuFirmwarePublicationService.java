package us.dot.its.jpo.ode.api.services.firmware;

import java.time.Instant;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareImage;
import us.dot.its.jpo.ode.api.models.storage.ObjectStorageLocation;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.Publication;
import us.dot.its.jpo.ode.api.storage.ObjectStorageServiceRegistry;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareImageRepository;

/** Publishes OBU firmware to, or withdraws it from, what the OBU OTA server offers. */
@Service
@RequiredArgsConstructor
public class ObuFirmwarePublicationService {
    private final ObuFirmwareImageRepository images;
    private final ObjectStorageServiceRegistry storage;

    /**
     * Sets the publish state. Both directions are idempotent. Publishing a cloud
     * file first checks that storage still holds the exact version that was
     * verified, so OBUs are never offered a replaced or missing file.
     */
    @Transactional
    public Publication setPublished(Integer obuFirmwareId, boolean published, String publishedBy) {
        // Lock the entry so publication and deletion of the same firmware serialize
        ObuFirmwareImage image = images.findByIdForUpdate(obuFirmwareId)
                .orElseThrow(() -> new EntityNotFoundException("OBU firmware '" + obuFirmwareId + "' was not found"));

        if (Boolean.TRUE.equals(image.getPublished()) != published) {
            if (published) {
                requireVerifiedFile(image);
                image.setPublished(true);
                image.setPublishedAt(Instant.now());
                image.setPublishedBy(FirmwareUploadSupport.normalizeCreatedBy(publishedBy));
            } else {
                image.setPublished(false);
                image.setPublishedAt(null);
                image.setPublishedBy(null);
            }
            images.save(image);
        }
        return new Publication(image.getId(), image.getPublished(), image.getPublishedAt(), image.getPublishedBy());
    }

    private void requireVerifiedFile(ObuFirmwareImage image) {
        // The API cannot inspect the OBU OTA server's local firmware folder
        if (image.isLocal()) {
            return;
        }
        var location = new ObjectStorageLocation(
                image.getStorageProvider(), image.getStorageContainer(), image.getObjectName());
        boolean verified = storage.getService(image.getStorageProvider())
                .getObjectMetadata(location, image.getChecksumAlgorithm())
                .filter(metadata -> ObuFirmwareObjectService.matchesStoredFile(image,
                        metadata.providerObjectVersion(), metadata.contentLength(), metadata.checksum()))
                .isPresent();
        if (!verified) {
            throw new ObuFirmwareConflictException("The firmware file is missing or has changed in storage. "
                    + "Refresh the OBU firmware list before publishing it.");
        }
    }
}
