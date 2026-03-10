package us.dot.its.jpo.ode.api.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import us.dot.its.jpo.ode.api.models.credentials.RsuCredentialDTO;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuCredential;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface RsuCredentialMapper {
    @Mapping(target = "ownerOrganization.id", source = "ownerOrganizationId")
    RsuCredential toEntity(RsuCredentialDTO rsuCredentialDTO);

    @Mapping(target = "ownerOrganizationId", source = "ownerOrganization.id")
    RsuCredentialDTO toDto(RsuCredential rsuCredential);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "ownerOrganization.id", source = "ownerOrganizationId")
    RsuCredential partialUpdate(RsuCredentialDTO rsuCredentialDTO, @MappingTarget RsuCredential rsuCredential);
}