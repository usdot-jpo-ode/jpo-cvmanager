package us.dot.its.jpo.ode.api.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import org.mapstruct.NullValuePropertyMappingStrategy;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionCreate;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionDto;
import us.dot.its.jpo.ode.api.models.admin.intersection.IntersectionPatch;
import us.dot.its.jpo.ode.api.models.postgres.tables.Intersection;
import us.dot.its.jpo.ode.api.models.postgres.tables.IntersectionOrganization;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for converting Intersection entities to/from Intersection DTOs.
 *
 * The {@code rsus} field is intentionally excluded (mapped to ignore) because RSU IPs
 * are fetched via a separate query in the service and set manually after mapping.
 * This avoids loading the rsuIntersections lazy collection during entity-to-DTO conversion.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
  uses = {GeometryMapper.class, INetMapper.class})
public interface IntersectionMapper {

  @Mapping(source = "intersectionNumber", target = "intersectionId")
  @Mapping(source = "refPt", target = "refPt")
  @Mapping(source = "bbox", target = "bbox")
  @Mapping(source = "intersectionName", target = "intersectionName")
  @Mapping(source = "originIp", target = "originIp")
  @Mapping(source = "intersectionOrganizations", target = "organizations",
    qualifiedByName = "mapOrgNames")
  @Mapping(target = "rsus", ignore = true)
  IntersectionDto toDto(Intersection intersection);

  @Named("mapOrgNames")
  default List<String> mapOrgNames(List<IntersectionOrganization> intersectionOrganizations) {
    if (intersectionOrganizations == null) {
      return Collections.emptyList();
    }
    return intersectionOrganizations.stream()
      .filter(io -> io.getOrganization() != null && io.getOrganization().getName() != null)
      .map(io -> io.getOrganization().getName())
      .collect(Collectors.toList());
  }

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "intersectionOrganizations", ignore = true)
  @Mapping(target = "rsuIntersections", ignore = true)
  @Mapping(target = "intersectionNumber", source = "intersectionId")
  Intersection toEntity(IntersectionCreate create);

  @Mapping(target = "id", ignore = true) // should never be able to update this
  @Mapping(target = "intersectionOrganizations", ignore = true) // handled directly in the service layer
  @Mapping(target = "rsuIntersections", ignore = true) // handled directly in the service layer
  @Mapping(target = "intersectionNumber", source = "intersectionId")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Intersection partialUpdate(@MappingTarget Intersection intersection, IntersectionPatch patch);
}
