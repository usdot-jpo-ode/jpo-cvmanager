package us.dot.its.jpo.ode.api.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import us.dot.its.jpo.ode.api.models.postgres.tables.User;
import us.dot.its.jpo.ode.api.models.users.UserPatch;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserPatchMapper {

    /**
     * Update existing User entity with non-null values from UserPatch
     * Null values in the patch are ignored (existing values preserved)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "userOrganizations", ignore = true) // Set in service layer
    @Mapping(target = "createdTimestamp", ignore = true) // Never update creation timestamp
    @Mapping(target = "id", ignore = true) // Never update ID
    @Mapping(target = "keycloakId", ignore = true) // Never update Keycloak ID
    void updateUserFromPatch(UserPatch patch, @MappingTarget User user);

    /**
     * Convert UserPatch to new User entity (for create operations)
     */
    @Mapping(target = "userOrganizations", ignore = true) // Set in service layer
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "createdTimestamp", ignore = true)
    User toUser(UserPatch userPatch);
}