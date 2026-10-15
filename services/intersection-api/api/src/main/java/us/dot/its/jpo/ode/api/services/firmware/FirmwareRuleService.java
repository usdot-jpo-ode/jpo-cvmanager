package us.dot.its.jpo.ode.api.services.firmware;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareImage;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUpgradeRule;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUploadStatus;
import us.dot.its.jpo.ode.api.models.storage.FirmwareRuleModels.*;
import us.dot.its.jpo.ode.api.models.storage.ObjectStorageLocation;
import us.dot.its.jpo.ode.api.repositories.FirmwareImageRepository;
import us.dot.its.jpo.ode.api.repositories.FirmwareUpgradeRuleRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;
import us.dot.its.jpo.ode.api.storage.ObjectStorageServiceRegistry;
import us.dot.its.jpo.ode.api.storage.ObuFirmwareStorageLayout;
import us.dot.its.jpo.ode.api.storage.ObjectStorageUnavailableException;
import us.dot.its.jpo.ode.api.mappers.FirmwareRuleMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirmwareRuleService {
    private final FirmwareImageRepository images;
    private final FirmwareUpgradeRuleRepository rules;
    private final ObjectStorageServiceRegistry storage;
    private final FirmwareRuleMapper mapper;
    private final RsuRepository rsus;

    @Transactional(readOnly = true)
    public List<Rule> list() {
        return rules.findRulesWithImages().stream()
                .filter(rule -> !isObu(rule.getFrom()) && !isObu(rule.getTo()))
                .map(mapper::toRule).toList();
    }

    @Transactional(readOnly = true)
    public Options options(Integer imageId) {
        var destination = image(imageId);
        var sources = images.findModelImages(destination.getModel().getId()).stream()
                .filter(source -> !source.getId().equals(imageId)).map(mapper::toImage).toList();
        // Include existing destinations for the model's sources, including legacy
        // destinations, so the editor can explain exactly what will be replaced.
        var related = list().stream().filter(rule -> rule.destination().firmwareId().equals(imageId)
                || sources.stream().anyMatch(source -> source.firmwareId().equals(rule.source().firmwareId()))
                || rule.source().firmwareId().equals(imageId)).toList();
        // Storage availability only controls adding rules. Existing database rules
        // must remain visible and removable even when verification is unavailable.
        boolean canTarget = false;
        String eligibilityError = null;
        try {
            canTarget = matchesVerifiedObject(destination);
        } catch (ObjectStorageUnavailableException | ResponseStatusException ex) {
            log.warn("Unable to check destination eligibility for firmware {}", imageId, ex);
            eligibilityError = "Unable to check the firmware file in storage. Existing rules can still be viewed or deleted. "
                    + "Close and reopen this dialog to retry before adding rules.";
        }
        return new Options(mapper.toImage(destination), canTarget, eligibilityError, sources, related);
    }

    @Transactional
    public void assign(Integer destinationId, Assignments request) {
        var destination = image(destinationId);
        // Hold image locks through validation so API deletion cannot remove the
        // destination between the metadata check and committing its new paths.
        var modelImages = images.findModelForUpdate(destination.getModel().getId()).stream()
                .collect(Collectors.toMap(FirmwareImage::getId, Function.identity()));
        destination = modelImages.get(destinationId);
        if (destination == null) {
            throw new FirmwareRuleConflictException("The destination was deleted. Refresh the firmware table.");
        }

        // Legacy images can describe installed versions without having a package.
        // A new destination must still match the exact object that was verified.
        if (!matchesVerifiedObject(destination)) {
            throw new FirmwareRuleConflictException(
                    "The destination must be verified and its original file must still be present and unchanged.");
        }
        var seen = new HashSet<Integer>();
        var current = rules.findRulesWithImages().stream()
                .collect(Collectors.toMap(rule -> rule.getFrom().getId(), Function.identity()));

        // Validate the whole selection before changing any path. A stale editor
        // cannot silently replace a destination saved by another administrator.
        for (var assignment : request.sources()) {
            if (!seen.add(assignment.sourceId())) {
                throw new IllegalArgumentException("Each source version may appear only once");
            }
            if (destinationId.equals(assignment.sourceId()) || !modelImages.containsKey(assignment.sourceId())) {
                throw new IllegalArgumentException("Source and destination must be different versions of the same RSU model");
            }
            var existing = current.get(assignment.sourceId());
            Integer previousTarget = existing == null ? null : existing.getTo().getId();
            if (!Objects.equals(previousTarget, assignment.expectedTargetId())) {
                throw new FirmwareRuleConflictException("Upgrade rules changed. Close and reopen the dialog before saving.");
            }
        }
        requireAcknowledgment(request, current, destination);
        for (var assignment : request.sources()) {
            var rule = current.getOrDefault(assignment.sourceId(), new FirmwareUpgradeRule());
            rule.setFrom(modelImages.get(assignment.sourceId()));
            rule.setTo(destination);
            rules.save(rule);
        }
        rules.flush();
    }

    private void requireAcknowledgment(Assignments request, Map<Integer, FirmwareUpgradeRule> current,
            FirmwareImage destination) {
        if (request.acknowledgeUpgradeImpact()) return;
        var impacts = new ArrayList<String>();
        for (var assignment : request.sources()) {
            var existing = current.get(assignment.sourceId());
            if (existing == null || destination.getId().equals(existing.getTo().getId())) continue;
            long count = rsus.countByFirmwareVersionId(assignment.sourceId());
            if (count > 0) {
                impacts.add(count + " RSU(s) use " + existing.getFrom().getVersion()
                        + "; their upgrade destination will change from " + existing.getTo().getVersion()
                        + " to " + destination.getVersion() + ".");
            }
        }
        // Reject the entire batch before writing; acknowledgment bypasses only this warning.
        if (!impacts.isEmpty()) throw new FirmwareRuleImpactException(String.join(" ", impacts));
    }

    @Transactional
    public void delete(Integer ruleId, Integer expectedTargetId) {
        delete(ruleId, expectedTargetId, false);
    }

    @Transactional
    public void delete(Integer ruleId, Integer expectedTargetId, boolean acknowledgeUpgradeImpact) {
        var sourceId = rules.findSourceId(ruleId).orElseThrow(() -> new EntityNotFoundException("Upgrade rule was not found"));
        var source = image(sourceId);
        // Match assignment's lock order before re-reading the path being removed.
        images.findModelForUpdate(source.getModel().getId());
        var current = rules.findRulesWithImages().stream().filter(item -> item.getId().equals(ruleId))
                .findFirst().orElseThrow(() -> new EntityNotFoundException("Upgrade rule was not found"));
        if (isObu(current.getTo())) {
            throw new IllegalArgumentException("OBU firmware is managed separately");
        }
        if (!Objects.equals(current.getTo().getId(), expectedTargetId)) {
            throw new FirmwareRuleConflictException("Upgrade rule changed. Close and reopen the dialog before deleting it.");
        }
        if (!acknowledgeUpgradeImpact) {
            long count = rsus.countByFirmwareVersionId(sourceId);
            if (count > 0) {
                throw new FirmwareRuleImpactException(count + " RSU(s) use " + current.getFrom().getVersion()
                        + "; deleting this rule will leave them without a configured upgrade path.");
            }
        }
        rules.delete(current);
    }

    private FirmwareImage image(Integer id) {
        var image = images.findById(id).orElseThrow(() -> new EntityNotFoundException("Firmware image was not found"));
        if (isObu(image)) {
            throw new IllegalArgumentException("OBU firmware is managed separately");
        }
        return image;
    }

    private boolean matchesVerifiedObject(FirmwareImage image) {
        var upload = image.getVerifiedUpload();
        if (upload == null || upload.getStatus() != FirmwareUploadStatus.VERIFIED
                || upload.getObservedChecksum() == null || upload.getChecksumAlgorithm() == null) {
            return false;
        }
        var service = storage.getActiveService();
        var location = new ObjectStorageLocation(upload.getStorageProvider(), upload.getStorageContainer(), upload.getObjectName());
        if (!location.equals(service.getLocation(upload.getObjectName()))) return false;
        return service.getObjectMetadata(location, upload.getChecksumAlgorithm()).filter(metadata ->
                metadata.providerObjectVersion() != null
                && metadata.providerObjectVersion().equals(upload.getProviderObjectVersion())
                && Objects.equals(metadata.contentLength(), upload.getExpectedSize())
                && metadata.checksum() != null
                && upload.getChecksumAlgorithm().equalsIgnoreCase(metadata.checksum().algorithm())
                && Objects.equals(metadata.checksum().value(), upload.getObservedChecksum())).isPresent();
    }

    private boolean isObu(FirmwareImage image) {
        return ObuFirmwareStorageLayout.isReservedName(image.getModel().getManufacturer().getName());
    }

    public static class FirmwareRuleConflictException extends RuntimeException {
        public FirmwareRuleConflictException(String message) { super(message); }
    }

    public static class FirmwareRuleImpactException extends FirmwareRuleConflictException {
        public FirmwareRuleImpactException(String message) { super(message); }
    }
}
