package us.dot.its.jpo.ode.api.services.firmware;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUpload;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareImage;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUploadStatus;
import us.dot.its.jpo.ode.api.models.storage.FirmwareObjectPage;
import us.dot.its.jpo.ode.api.models.storage.StorageObject;
import us.dot.its.jpo.ode.api.models.storage.StorageObjectPage;
import us.dot.its.jpo.ode.api.repositories.FirmwareImageRepository;
import us.dot.its.jpo.ode.api.repositories.FirmwareUploadRepository;
import us.dot.its.jpo.ode.api.storage.ObjectIds;
import us.dot.its.jpo.ode.api.storage.ObjectListings;
import us.dot.its.jpo.ode.api.storage.ObjectStorageServiceRegistry;
import us.dot.its.jpo.ode.api.storage.ObuFirmwareStorageLayout;

@Service
@RequiredArgsConstructor
public class FirmwareObjectService {
    private final ObjectStorageServiceRegistry storage;
    private final FirmwareUploadRepository uploads;
    private final FirmwareImageRepository images;

    public FirmwareObjectPage list(int pageNumber, int pageSize, String manufacturer, String search, String sort) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("page must not be negative");
        }
        if (pageSize < 1 || pageSize > 200) {
            throw new IllegalArgumentException("size must be between 1 and 200");
        }

        // Limit the provider query when a manufacturer is selected, then remove
        // folder markers and the OBU hierarchy from the RSU firmware results
        String prefix = manufacturer == null || manufacturer.isBlank()
                ? null
                : validateManufacturer(manufacturer) + "/";
        String searchTerm = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        Listing listing = readListing(prefix, searchTerm);
        StorageObjectPage page = listing.page();
        var objects = page.objects();
        if (objects.isEmpty()) {
            return new FirmwareObjectPage(page.provider(), List.of(), 0);
        }

        // Attach upload and image evidence before sorting the complete result.
        var records = uploads.findListingUploads(page.provider(), page.container(),
                objects.stream().map(StorageObject::objectName).toList());
        var byName = records.stream().collect(Collectors.toMap(FirmwareUpload::getObjectName, Function.identity()));
        Map<UUID, Integer> imageIds = records.isEmpty() ? Map.of()
                : images.findByVerifiedUploadIdIn(records.stream().map(FirmwareUpload::getId).toList()).stream()
                        .collect(Collectors.toMap(image -> image.getVerifiedUpload().getId(), FirmwareImage::getId));
        var items = objects.stream()
                .map(object -> toItem(page.provider(), object, byName.get(object.objectName()), imageIds, listing))
                .sorted(itemComparator(sort)).toList();

        int fromIndex = (int) Math.min((long) pageNumber * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return new FirmwareObjectPage(page.provider(), items.subList(fromIndex, toIndex), items.size());
    }

    private record Listing(StorageObjectPage page, Set<String> missingNames, Map<String, Integer> legacyImageIds) {}

    private Listing readListing(String prefix, String searchTerm) {
        var legacyImageIds = new HashMap<String, Integer>();
        var objects = new ArrayList<StorageObject>();
        var missingNames = new HashSet<String>();

        // Search every provider page so sorting and pagination apply to the complete
        // filtered result, including untracked and missing files.
        StorageObjectPage page = ObjectListings.listAll(storage.getActiveService(), prefix);
        var listedObjects = page.objects();

        // Only a complete, successful storage listing establishes absence. Database
        // records remain discoverable after a lost deletion response or failed commit.
        var missing = missingCandidates(page, legacyImageIds);

        for (var object : listedObjects) {
            String name = object.objectName();
            missing.remove(name);
            if (isMatchingObject(name, searchTerm)) objects.add(object);
        }

        // Include missing records in the same searched and manufacturer-filtered result.
        for (var object : missing.values()) {
            String name = object.objectName();
            if (isMatchingObject(name, searchTerm) && (prefix == null || name.startsWith(prefix))) {
                objects.add(object);
                missingNames.add(name);
            }
        }
        return new Listing(new StorageObjectPage(page.provider(), page.container(), objects, null),
                missingNames, legacyImageIds);
    }

    private Map<String, StorageObject> missingCandidates(StorageObjectPage page, Map<String, Integer> legacyImageIds) {
        var missing = new TreeMap<String, StorageObject>();
        Instant now = Instant.now();
        for (var upload : uploads.findListingCandidates(page.provider(), page.container())) {
            if (!upload.getExpiresAt().isAfter(now) || upload.getStatus() == FirmwareUploadStatus.VERIFIED) {
                missing.put(upload.getObjectName(), new StorageObject(upload.getObjectName(),
                        upload.getExpectedSize(), null, null, null));
            }
        }
        for (var image : images.findLegacyImagesWithModelAndManufacturer()) {
            String name = String.join("/", image.getModel().getManufacturer().getName(),
                    image.getModel().getName(), image.getVersion(), image.getInstallPackage());
            legacyImageIds.put(name, image.getId());
            missing.putIfAbsent(name, new StorageObject(name, 0L, null, null, null));
        }
        return missing;
    }

    private boolean isMatchingObject(String name, String searchTerm) {
        return !name.endsWith("/") && !isReservedObject(name)
                && name.toLowerCase(Locale.ROOT).contains(searchTerm);
    }

    private FirmwareObjectPage.Item toItem(String provider, StorageObject object, FirmwareUpload upload,
            Map<UUID, Integer> imageIds, Listing listing) {
        boolean absent = listing.missingNames().contains(object.objectName());
        // Parse conventional paths into columns, retaining the full path for actions.
        String id = ObjectIds.encode(provider, object.objectName());
        String[] path = object.objectName().split("/", 4);
        String manufacturerName = path.length == 4 ? path[0] : null;
        String modelName = path.length == 4 ? path[1] : null;
        String version = path.length == 4 ? path[2] : null;
        String fileName = path.length == 4 ? path[3] : object.objectName();
        return new FirmwareObjectPage.Item(id, object.objectName(), manufacturerName, modelName,
                version, fileName, absent && upload == null ? null : Long.valueOf(object.contentLength()),
                object.updatedAt(), object.providerObjectVersion(), upload == null ? null : upload.getId(),
                upload == null ? listing.legacyImageIds().get(object.objectName()) : imageIds.get(upload.getId()),
                upload == null ? null : upload.getStatus().name(), absent ? "MISSING" : verificationState(object, upload));
    }

    private String verificationState(StorageObject object, FirmwareUpload upload) {
        if (upload == null) return "UNTRACKED";
        if (upload.getStatus() != FirmwareUploadStatus.VERIFIED) return "UNVERIFIED";
        return FirmwareUploadSupport.matchesListedObject(upload, object) ? "VERIFIED" : "CHANGED";
    }

    private Comparator<FirmwareObjectPage.Item> itemComparator(String sort) {
        String[] parts = sort == null ? new String[0] : sort.trim().split(",", 2);
        String field = parts.length == 0 || parts[0].isBlank() ? "manufacturer" : parts[0].trim();
        boolean descending = parts.length == 2 && "desc".equalsIgnoreCase(parts[1].trim());
        if (parts.length == 2 && !descending && !"asc".equalsIgnoreCase(parts[1].trim())) {
            throw new IllegalArgumentException("sort direction must be asc or desc");
        }

        Comparator<String> text = Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER);
        Comparator<FirmwareObjectPage.Item> comparator = switch (field) {
            case "manufacturer" -> Comparator.comparing(FirmwareObjectPage.Item::manufacturer, text);
            case "model" -> Comparator.comparing(FirmwareObjectPage.Item::model, text);
            case "version" -> Comparator.comparing(FirmwareObjectPage.Item::version, text);
            case "content_length" -> Comparator.comparing(FirmwareObjectPage.Item::contentLength,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "updated_at" -> Comparator.comparing(FirmwareObjectPage.Item::updatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "verification_status" -> Comparator.comparing(FirmwareObjectPage.Item::verificationStatus, text);
            default -> throw new IllegalArgumentException("sort field is not supported");
        };
        if (descending) {
            comparator = comparator.reversed();
        }
        return comparator.thenComparing(FirmwareObjectPage.Item::objectName, text);
    }

    private String validateManufacturer(String manufacturer) {
        String normalized = manufacturer.trim();
        if (normalized.isEmpty() || normalized.length() > 128
                || normalized.chars().anyMatch(character -> character == '/' || character == '\\'
                        || Character.isISOControl(character))) {
            throw new IllegalArgumentException("manufacturer must be a valid object path segment");
        }
        if (ObuFirmwareStorageLayout.isReservedName(normalized)) {
            throw new IllegalArgumentException("manufacturer is reserved for a separate firmware workflow");
        }
        return normalized;
    }

    private boolean isReservedObject(String objectName) {
        return ObuFirmwareStorageLayout.isObuObjectName(objectName);
    }
}
