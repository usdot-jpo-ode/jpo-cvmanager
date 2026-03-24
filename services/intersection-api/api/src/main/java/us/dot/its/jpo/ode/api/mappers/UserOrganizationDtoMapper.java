package us.dot.its.jpo.ode.api.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import us.dot.its.jpo.ode.api.models.postgres.tables.UserOrganization;
import us.dot.its.jpo.ode.api.models.users.UserOrganizationDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserOrganizationDtoMapper {

    /**
     * Convert User entity to UserDto
     * MapStruct will automatically map fields with the same name
     */
    @Mapping(source = "organization.name", target = "organization")
    @Mapping(source = "role.name", target = "role")
    UserOrganizationDto toDto(UserOrganization user);
}