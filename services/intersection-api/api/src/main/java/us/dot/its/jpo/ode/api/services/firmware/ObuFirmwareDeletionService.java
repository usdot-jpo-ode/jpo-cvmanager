package us.dot.its.jpo.ode.api.services.firmware;

import java.time.Instant;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareImage;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareImageRepository;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareUploadRepository;
import us.dot.its.jpo.ode.api.storage.ObjectIds;
import us.dot.its.jpo.ode.api.storage.ObjectStorageServiceRegistry;
import us.dot.its.jpo.ode.api.storage.ObuFirmwareStorageLayout;

/**
 * Deletes OBU firmware files and their upload and catalog records. Files with no
 * records, such as uploads that were never completed, can be deleted too, which
 * frees their file name for a new upload. OBU OTA request history is kept.
 */
@Service
@RequiredArgsConstructor
public class ObuFirmwareDeletionService {
    static final String UNPUBLISH_FIRST_MESSAGE = "Unpublish this firmware before deleting it.";

    private final ObjectStorageServiceRegistry storage;
    private final ObuFirmwareUploadRepository uploads;
    private final ObuFirmwareImageRepository images;

    @Transactional
    public void delete(String objectId, String providerObjectVersion) {
        if (providerObjectVersion == null || providerObjectVersion.isBlank()) {
            throw new IllegalArgumentException("The listed object version is required for deletion");
        }
        deleteRecordsAndObject(objectId, providerObjectVersion);
    }

    /** Removes records for a file that is missing from storage, or for a local file. */
    @Transactional
    public void cleanupMissingObject(String objectId) {
        deleteRecordsAndObject(objectId, null);
    }

    private void deleteRecordsAndObject(String objectId, String providerObjectVersion) {
        var identity = ObjectIds.decode(objectId);
        String objectName = identity.objectName();
        if (!ObuFirmwareStorageLayout.isObuObjectName(objectName)) {
            throw new IllegalArgumentException("Only OBU firmware beneath ota/ can be deleted through this API");
        }
        if (ObuFirmwareImage.LOCAL_STORAGE_PROVIDER.equals(identity.provider())) {
            deleteLocalRecords(objectName, providerObjectVersion);
            return;
        }

        var service = storage.getActiveService();
        if (!service.providerName().equals(identity.provider())) {
            throw new ObuFirmwareConflictException("The storage provider changed. Refresh the OBU firmware list.");
        }
        var location = service.getLocation(objectName);

        // Lock uploads before images, matching completion's lock order. An issued
        // upload URL can still write the file after verification or failure.
        var records = uploads.findDestinationForUpdate(location.provider(), location.container(), objectName);
        var liveUploadUrl = FirmwareUploadSupport.liveUploadUrlMessage(records, Instant.now());
        if (liveUploadUrl.isPresent()) {
            throw new ObuFirmwareConflictException(liveUploadUrl.get());
        }
        var registered = images.findDestinationForUpdate(location.provider(), location.container(), objectName);
        requireUnpublished(registered);
        // Recovery never deletes a cloud object. A file that reappeared must be
        // listed and explicitly confirmed with its current version first.
        if (providerObjectVersion == null && service.objectExists(objectName)) {
            throw new ObuFirmwareConflictException(
                    "The firmware file is present in storage. Refresh the OBU firmware list before deleting it.");
        }

        // Flush database deletes first so conflicts are detected before touching
        // storage. A storage failure rolls them back; an absent object lets a retry
        // finish cleanup if a previous database commit failed.
        try {
            images.deleteAll(registered);
            images.flush();
            uploads.deleteAll(records);
            uploads.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ObuFirmwareConflictException(
                    "This firmware is still referenced by other records and cannot be deleted.");
        }
        if (providerObjectVersion != null) {
            service.deleteObject(location, providerObjectVersion);
        }
    }

    private void deleteLocalRecords(String objectName, String providerObjectVersion) {
        // The OBU OTA server's local firmware folder is not reachable from the API
        if (providerObjectVersion != null) {
            throw new IllegalArgumentException("Local OBU firmware files are removed from the OBU OTA server's "
                    + "firmware folder. Use records cleanup to remove the catalog entry.");
        }
        var registered = images.findDestinationForUpdate(ObuFirmwareImage.LOCAL_STORAGE_PROVIDER,
                ObuFirmwareImage.LOCAL_STORAGE_CONTAINER, objectName);
        requireUnpublished(registered);
        images.deleteAll(registered);
        images.flush();
    }

    private void requireUnpublished(List<ObuFirmwareImage> registered) {
        // OBUs may still be downloading published firmware, so withdrawing it is a
        // separate, deliberate step
        if (registered.stream().anyMatch(image -> Boolean.TRUE.equals(image.getPublished()))) {
            throw new ObuFirmwareConflictException(UNPUBLISH_FIRST_MESSAGE);
        }
    }
}
