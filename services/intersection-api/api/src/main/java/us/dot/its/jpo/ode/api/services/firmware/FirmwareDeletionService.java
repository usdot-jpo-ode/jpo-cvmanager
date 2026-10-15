package us.dot.its.jpo.ode.api.services.firmware;

import java.time.Instant;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import us.dot.its.jpo.ode.api.repositories.FirmwareImageRepository;
import us.dot.its.jpo.ode.api.repositories.FirmwareUpgradeRuleRepository;
import us.dot.its.jpo.ode.api.repositories.FirmwareUploadRepository;
import us.dot.its.jpo.ode.api.repositories.MaxRetryLimitReachedInstanceRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareImage;
import us.dot.its.jpo.ode.api.storage.ObjectIds;
import us.dot.its.jpo.ode.api.storage.ObjectStorageServiceRegistry;
import us.dot.its.jpo.ode.api.storage.ObuFirmwareStorageLayout;

@Service
@RequiredArgsConstructor
public class FirmwareDeletionService {
    private final ObjectStorageServiceRegistry storage;
    private final FirmwareUploadRepository uploads;
    private final FirmwareImageRepository images;
    private final FirmwareUpgradeRuleRepository rules;
    private final RsuRepository rsus;
    private final MaxRetryLimitReachedInstanceRepository failureHistory;

    @Transactional
    public void delete(String objectId, String providerObjectVersion) {
        if (providerObjectVersion == null || providerObjectVersion.isBlank()) {
            throw new IllegalArgumentException("The listed object version is required for deletion");
        }
        deleteRecordsAndObject(objectId, providerObjectVersion);
    }

    @Transactional
    public void cleanupMissingObject(String objectId) {
        deleteRecordsAndObject(objectId, null);
    }

    private void deleteRecordsAndObject(String objectId, String providerObjectVersion) {
        var identity = ObjectIds.decode(objectId);
        String objectName = identity.objectName();
        if (ObuFirmwareStorageLayout.isObuObjectName(objectName)) {
            throw new IllegalArgumentException("OBU firmware cannot be deleted through the RSU firmware API");
        }

        var service = storage.getActiveService();
        if (!service.providerName().equals(identity.provider())) {
            throw new FirmwareDeletionConflictException("The storage provider changed. Refresh the firmware table.");
        }
        var location = service.getLocation(objectName);

        // Lock uploads before images, matching completion's lock order. An issued
        // upload URL remains usable even after verification or failure.
        var records = uploads.findDestinationForUpdate(location.provider(), location.container(), objectName);
        var liveUploadUrl = FirmwareUploadSupport.liveUploadUrlMessage(records, Instant.now());
        if (liveUploadUrl.isPresent()) {
            throw new FirmwareDeletionConflictException(liveUploadUrl.get());
        }
        var registeredImages = images.findDestinationForUpdate(location.provider(), location.container(), objectName);
        var imageIds = registeredImages.stream().map(FirmwareImage::getId).toList();
        if (!imageIds.isEmpty() && rsus.referencesFirmwareImages(imageIds)) {
            throw new FirmwareDeletionConflictException(
                    "This firmware is an RSU's current or target version and cannot be deleted.");
        }
        // Recovery never deletes a cloud object. A file that reappeared must be
        // listed and explicitly confirmed with its current version first.
        if (providerObjectVersion == null && service.objectExists(objectName)) {
            throw new FirmwareDeletionConflictException(
                    "The firmware file is present in storage. Refresh the table before deleting it.");
        }

        // Flush database deletes first so foreign-key conflicts are detected before
        // touching storage. A storage failure rolls them back; an absent object
        // allows a retry to finish cleanup if a previous database commit failed.
        try {
            if (!imageIds.isEmpty()) {
                // Retry-limit entries describe the image being removed and cannot
                // remain once that image is no longer registered.
                failureHistory.deleteByTargetFirmwareVersionIdIn(imageIds);
                rules.deleteForImages(imageIds);
                images.deleteAll(registeredImages);
                images.flush();
            }
            uploads.deleteAll(records);
            uploads.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new FirmwareDeletionConflictException(
                    "This firmware is still referenced by other records and cannot be deleted.");
        }
        if (providerObjectVersion != null) {
            service.deleteObject(location, providerObjectVersion);
        }
    }

    public static class FirmwareDeletionConflictException extends RuntimeException {
        public FirmwareDeletionConflictException(String message) {
            super(message);
        }
    }
}
