package us.dot.its.jpo.ode.api.models.storage;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUploadStatus;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareDeviceType;

/** Request and response bodies for the OBU firmware admin API. */
public final class ObuFirmwareModels {
    private ObuFirmwareModels() {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record UploadOptions(List<ManufacturerOption> manufacturers) {}

    /**
     * A manufacturer that accepts OBU firmware uploads. The file name pattern is a
     * JavaScript-compatible regular expression for early feedback in the webapp;
     * the API remains the authority on whether a name is accepted.
     */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ManufacturerOption(Integer manufacturerId, String name, String fileExtension,
            String fileNamePattern, int maxFileNameLength, String exampleFileName) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "Identifies an OBU firmware file to upload to object storage")
    public record UploadUrlRequest(
            @NotBlank @Size(max = 128)
            @Pattern(regexp = "^[^/\\\\\\p{Cntrl}]+$",
                    message = "must not contain path separators or control characters")
            String manufacturerName,

            @NotBlank @Size(max = 128)
            @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]*$",
                    message = "must start with an alphanumeric character and contain only letters, numbers, dots, underscores, or hyphens")
            @Schema(description = "Original vendor file name; must follow the manufacturer's OTA naming convention",
                    example = "ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig")
            String fileName,

            @NotNull @Positive
            @Schema(description = "Exact upload size in bytes", example = "135055616")
            Long contentLength,

            @NotBlank @Size(max = 32)
            @Pattern(regexp = "^[A-Za-z0-9_-]+$",
                    message = "must contain only letters, numbers, underscores, or hyphens")
            @Schema(example = "CRC32C")
            String checksumAlgorithm,

            @NotBlank @Size(max = 128)
            @Pattern(regexp = "^[A-Za-z0-9+/=_-]+$", message = "contains invalid checksum characters")
            @Schema(example = "ImIEBA==")
            String checksum,

            @Size(max = 255)
            @Pattern(regexp = "^[A-Za-z0-9!#$&^_.+\\-]+/[A-Za-z0-9!#$&^_.+\\-]+$",
                    message = "must be a valid media type")
            @Schema(description = "Defaults to application/octet-stream")
            String contentType) {
        public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

        public UploadUrlRequest {
            if (contentType == null || contentType.isBlank()) {
                contentType = DEFAULT_CONTENT_TYPE;
            }
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "Verified state of an OBU firmware upload and its catalog entry")
    public record UploadVerification(UUID uploadId, FirmwareUploadStatus status, String objectName,
            Long contentLength, String checksumAlgorithm, String checksum, String providerObjectVersion,
            @JsonFormat(shape = JsonFormat.Shape.STRING) Instant verifiedAt, Integer obuFirmwareId) {}

    /**
     * One page of OBU firmware files and catalog entries. {@code storageAvailable} is
     * false when object storage could not be listed, in which case only catalog
     * entries are returned and cloud files report {@code UNKNOWN}.
     */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ObjectPage(String provider, boolean storageAvailable, List<Item> objects, long totalElements) {}

    /**
     * An OBU firmware file in storage, its catalog entry, or both. Parsed metadata
     * comes from the catalog, the upload, or for untracked files the file name.
     */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Item(String objectId, String objectName, String storageProvider, String providerObjectVersion,
            String manufacturer, String fileName, ObuFirmwareDeviceType deviceType, String variant,
            String releaseVersion, Boolean writableSystemPartition, Boolean secureBoot, Long contentLength,
            @JsonFormat(shape = JsonFormat.Shape.STRING) Instant updatedAt,
            @JsonFormat(shape = JsonFormat.Shape.STRING) Instant registeredAt,
            UUID uploadId, String uploadStatus, Integer obuFirmwareId, boolean published,
            @JsonFormat(shape = JsonFormat.Shape.STRING) Instant publishedAt, String publishedBy,
            String verificationStatus) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "Whether the OBU OTA server should offer this firmware to OBUs")
    public record PublicationRequest(@NotNull Boolean published) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "Publish state of an OBU firmware catalog entry")
    public record Publication(Integer obuFirmwareId, boolean published,
            @JsonFormat(shape = JsonFormat.Shape.STRING) Instant publishedAt, String publishedBy) {}
}
