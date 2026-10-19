package us.dot.its.jpo.ode.api.services.firmware;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUploadStatus;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareFileMetadata;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareImage;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareUpload;
import us.dot.its.jpo.ode.api.models.storage.ObjectChecksum;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.Item;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.ObjectPage;
import us.dot.its.jpo.ode.api.models.storage.StorageObject;
import us.dot.its.jpo.ode.api.models.storage.StorageObjectPage;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareImageRepository;
import us.dot.its.jpo.ode.api.repositories.ObuFirmwareUploadRepository;
import us.dot.its.jpo.ode.api.storage.ObjectIds;
import us.dot.its.jpo.ode.api.storage.ObjectListings;
import us.dot.its.jpo.ode.api.storage.ObjectStorageServiceRegistry;
import us.dot.its.jpo.ode.api.storage.ObjectStorageUnavailableException;
import us.dot.its.jpo.ode.api.storage.ObuFirmwareStorageLayout;

/**
 * Lists OBU firmware by merging the catalog, the latest upload for each file, and
 * the files beneath {@code ota/} in object storage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ObuFirmwareObjectService {
    /** The stored file is the exact version that was verified. */
    public static final String VERIFIED = "VERIFIED";
    /** The catalog entry's file was replaced or modified in storage. */
    public static final String CHANGED = "CHANGED";
    /** A catalog entry or upload record whose file is not in storage. */
    public static final String MISSING = "MISSING";
    /** A file whose upload has not been verified. */
    public static final String UNVERIFIED = "UNVERIFIED";
    /** A file beneath {@code ota/} with no catalog entry or upload record. */
    public static final String UNTRACKED = "UNTRACKED";
    /** A catalog entry for a file on the OBU OTA server's local firmware folder. */
    public static final String LOCAL = "LOCAL";
    /** A cloud catalog entry whose file could not be checked. */
    public static final String UNKNOWN = "UNKNOWN";

    private final ObjectStorageServiceRegistry storage;
    private final ObuFirmwareUploadRepository uploads;
    private final ObuFirmwareImageRepository images;
    private final ObuFirmwareParserRegistry parsers;

    public ObjectPage list(int pageNumber, int pageSize, String manufacturer, String search, Boolean published,
            String sort) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("page must not be negative");
        }
        if (pageSize < 1 || pageSize > 200) {
            throw new IllegalArgumentException("size must be between 1 and 200");
        }
        Comparator<Item> comparator = itemComparator(sort);
        String manufacturerFilter = manufacturer == null ? "" : manufacturer.trim();
        String searchTerm = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);

        // The catalog is small, so filter, sort, and page the complete result in memory
        Listing listing = readListing();
        var items = listing.items().stream()
                .filter(item -> manufacturerFilter.isEmpty() || manufacturerFilter.equalsIgnoreCase(item.manufacturer()))
                .filter(item -> item.objectName().toLowerCase(Locale.ROOT).contains(searchTerm))
                .filter(item -> published == null || item.published() == published)
                .sorted(comparator)
                .toList();

        int fromIndex = (int) Math.min((long) pageNumber * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return new ObjectPage(listing.provider(), listing.storageAvailable(), items.subList(fromIndex, toIndex),
                items.size());
    }

    /**
     * Returns whether stored file details are the exact version a catalog image
     * recorded when it was verified.
     */
    static boolean matchesStoredFile(ObuFirmwareImage image, String providerObjectVersion, long contentLength,
            ObjectChecksum checksum) {
        return providerObjectVersion != null && providerObjectVersion.equals(image.getProviderObjectVersion())
                && Objects.equals(image.getContentLength(), contentLength)
                && checksum != null && image.getChecksum() != null && image.getChecksumAlgorithm() != null
                && image.getChecksumAlgorithm().equalsIgnoreCase(checksum.algorithm())
                && image.getChecksum().equals(checksum.value());
    }

    private record Listing(String provider, boolean storageAvailable, List<Item> items) {}

    private Listing readListing() {
        var catalog = images.findAllWithManufacturer();
        StorageObjectPage page;
        try {
            page = ObjectListings.listAll(storage.getActiveService(), ObuFirmwareStorageLayout.PREFIX);
        } catch (ObjectStorageUnavailableException ex) {
            // Deployments that serve only local firmware still need the catalog
            log.warn("Listing OBU firmware catalog entries only because storage is unavailable: {}", ex.getMessage());
            return new Listing(null, false, catalog.stream()
                    .map(image -> item(image.getStorageProvider(), image.getObjectName(), null, image, null,
                            image.isLocal() ? LOCAL : UNKNOWN))
                    .toList());
        }

        var items = new ArrayList<Item>();
        var cloudImages = new HashMap<String, ObuFirmwareImage>();
        for (var image : catalog) {
            if (image.isLocal()) {
                items.add(item(image.getStorageProvider(), image.getObjectName(), null, image, null, LOCAL));
            } else if (page.provider().equals(image.getStorageProvider())
                    && page.container().equals(image.getStorageContainer())) {
                cloudImages.put(image.getObjectName(), image);
            } else {
                // Stored with a provider or bucket that is no longer active
                items.add(item(image.getStorageProvider(), image.getObjectName(), null, image, null, UNKNOWN));
            }
        }
        var latestUploads = uploads.findListingUploads(page.provider(), page.container()).stream()
                .collect(Collectors.toMap(ObuFirmwareUpload::getObjectName, Function.identity()));

        var listed = new HashSet<String>();
        for (var object : page.objects()) {
            String name = object.objectName();
            if (name.endsWith("/") || !ObuFirmwareStorageLayout.isObuObjectName(name)) {
                continue;
            }
            listed.add(name);
            var image = cloudImages.get(name);
            var upload = latestUploads.get(name);
            items.add(item(page.provider(), name, object, image, upload, storedFileState(object, image, upload)));
        }

        // Only a complete, successful listing establishes that a file is absent.
        // Uploads still able to write their file are in progress, not missing.
        Instant now = Instant.now();
        for (var image : cloudImages.values()) {
            if (!listed.contains(image.getObjectName())) {
                items.add(item(page.provider(), image.getObjectName(), null, image,
                        latestUploads.get(image.getObjectName()), MISSING));
            }
        }
        for (var upload : latestUploads.values()) {
            String name = upload.getObjectName();
            if (!listed.contains(name) && !cloudImages.containsKey(name)
                    && (upload.getStatus() == FirmwareUploadStatus.VERIFIED || !upload.getExpiresAt().isAfter(now))) {
                items.add(item(page.provider(), name, null, null, upload, MISSING));
            }
        }
        return new Listing(page.provider(), true, items);
    }

    private String storedFileState(StorageObject object, ObuFirmwareImage image, ObuFirmwareUpload upload) {
        if (image != null) {
            return matchesStoredFile(image, object.providerObjectVersion(), object.contentLength(), object.checksum())
                    ? VERIFIED : CHANGED;
        }
        return upload == null ? UNTRACKED : UNVERIFIED;
    }

    private Item item(String provider, String objectName, StorageObject object, ObuFirmwareImage image,
            ObuFirmwareUpload upload, String verificationStatus) {
        // Object names are ota/<manufacturer>/<file name>. Upload manufacturers are
        // read from the path so the lazy association is never loaded here.
        String[] path = objectName.split("/");
        String fileName = image != null ? image.getFileName() : path[path.length - 1];
        String manufacturer = image != null ? image.getManufacturer().getName()
                : path.length >= 3 ? path[1] : null;
        ObuFirmwareFileMetadata metadata = image != null ? image.getMetadata()
                : upload != null ? upload.getMetadata()
                : parsers.find(manufacturer).flatMap(parser -> parser.parse(fileName)).orElse(null);
        Long contentLength = object != null ? Long.valueOf(object.contentLength())
                : image != null ? image.getContentLength()
                : upload != null ? upload.getExpectedSize() : null;
        boolean published = image != null && Boolean.TRUE.equals(image.getPublished());

        return new Item(ObjectIds.encode(provider, objectName), objectName, provider,
                object == null ? null : object.providerObjectVersion(), manufacturer, fileName,
                metadata == null ? null : metadata.getDeviceType(),
                metadata == null ? null : metadata.getVariant(),
                metadata == null ? null : metadata.getReleaseVersion(),
                metadata == null ? null : metadata.getWritableSystemPartition(),
                metadata == null ? null : metadata.getSecureBoot(),
                contentLength, object == null ? null : object.updatedAt(),
                image == null ? null : image.getRegisteredAt(),
                upload == null ? null : upload.getId(), upload == null ? null : upload.getStatus().name(),
                image == null ? null : image.getId(), published,
                image == null ? null : image.getPublishedAt(), image == null ? null : image.getPublishedBy(),
                verificationStatus);
    }

    private Comparator<Item> itemComparator(String sort) {
        String[] parts = sort == null ? new String[0] : sort.trim().split(",", 2);
        String field = parts.length == 0 || parts[0].isBlank() ? "manufacturer" : parts[0].trim();
        boolean descending = parts.length == 2 && "desc".equalsIgnoreCase(parts[1].trim());
        if (parts.length == 2 && !descending && !"asc".equalsIgnoreCase(parts[1].trim())) {
            throw new IllegalArgumentException("sort direction must be asc or desc");
        }

        Comparator<String> text = Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER);
        Comparator<Item> comparator = switch (field) {
            case "manufacturer" -> Comparator.comparing(Item::manufacturer, text);
            case "file_name" -> Comparator.comparing(Item::fileName, text);
            case "release_version" -> Comparator.comparing(Item::releaseVersion, text);
            case "device_type" -> Comparator.comparing((Item item) -> item.deviceType() == null ? null
                    : item.deviceType().name(), text);
            case "content_length" -> Comparator.comparing(Item::contentLength,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "updated_at" -> Comparator.comparing(Item::updatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
            case "registered_at" -> Comparator.comparing(Item::registeredAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "published" -> Comparator.comparing(Item::published);
            case "verification_status" -> Comparator.comparing(Item::verificationStatus, text);
            default -> throw new IllegalArgumentException("sort field is not supported");
        };
        if (descending) {
            comparator = comparator.reversed();
        }
        return comparator.thenComparing(Item::objectName, text);
    }
}
