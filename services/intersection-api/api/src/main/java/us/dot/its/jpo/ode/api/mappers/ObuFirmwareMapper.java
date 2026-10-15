package us.dot.its.jpo.ode.api.mappers;

import java.time.Instant;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import us.dot.its.jpo.ode.api.models.postgres.tables.Manufacturer;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareFileMetadata;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareImage;
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareUpload;
import us.dot.its.jpo.ode.api.models.storage.ObjectChecksum;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.UploadUrlRequest;
import us.dot.its.jpo.ode.api.models.storage.SignedUploadUrl;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ObuFirmwareMapper {
    @Mapping(target = "id", source = "uploadId")
    @Mapping(target = "manufacturer", source = "manufacturer")
    @Mapping(target = "metadata", source = "metadata")
    @Mapping(target = "fileName", source = "request.fileName", qualifiedByName = "trim")
    @Mapping(target = "contentType", source = "request.contentType", qualifiedByName = "trim")
    @Mapping(target = "storageProvider", source = "signedUrl.location.provider")
    @Mapping(target = "storageContainer", source = "signedUrl.location.container")
    @Mapping(target = "objectName", source = "signedUrl.location.objectName")
    @Mapping(target = "expectedSize", source = "request.contentLength")
    @Mapping(target = "checksumAlgorithm", source = "checksum.algorithm")
    @Mapping(target = "expectedChecksum", source = "checksum.value")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "expiresAt", source = "signedUrl.expiresAt")
    @Mapping(target = "verifiedAt", ignore = true)
    @Mapping(target = "finishedAt", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    @Mapping(target = "providerObjectVersion", ignore = true)
    @Mapping(target = "observedChecksum", ignore = true)
    ObuFirmwareUpload toEntity(UploadUrlRequest request, Manufacturer manufacturer,
            ObuFirmwareFileMetadata metadata, SignedUploadUrl signedUrl, ObjectChecksum checksum,
            UUID uploadId, String createdBy, Instant createdAt);

    /** Builds an unpublished catalog image from a verified upload. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contentLength", source = "expectedSize")
    @Mapping(target = "checksum", source = "observedChecksum")
    @Mapping(target = "verifiedUpload", source = "upload")
    @Mapping(target = "registeredAt", ignore = true)
    @Mapping(target = "published", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "publishedBy", ignore = true)
    ObuFirmwareImage toImage(ObuFirmwareUpload upload);

    /** Copies embedded metadata so an upload and its image never share one instance. */
    ObuFirmwareFileMetadata copy(ObuFirmwareFileMetadata metadata);

    @Named("trim")
    default String trim(String value) {
        return value == null ? null : value.trim();
    }
}
