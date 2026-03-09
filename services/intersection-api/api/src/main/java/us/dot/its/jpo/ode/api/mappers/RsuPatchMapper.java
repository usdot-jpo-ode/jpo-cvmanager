package us.dot.its.jpo.ode.api.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import us.dot.its.jpo.ode.api.models.devices.management.RsuPatch;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = { INetMapper.class,
        SimplePositionMapper.class })
public interface RsuPatchMapper {

    /**
     * Update existing Rsu entity with non-null values from RsuPatch
     * Null values in the patch are ignored (existing values preserved)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "ipv4Address", target = "ipv4Address")
    @Mapping(source = "geoPosition", target = "geography")
    @Mapping(target = "id", ignore = true) // Never update ID

    // Joined fields are ignored here and should be handled in the service layer
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "credential", ignore = true)
    @Mapping(target = "snmpCredential", ignore = true)
    @Mapping(target = "snmpProtocol", ignore = true)
    @Mapping(target = "rsuOrganizations", ignore = true)
    @Mapping(target = "firmwareVersion", ignore = true)
    @Mapping(target = "targetFirmwareVersion", ignore = true)
    @Mapping(target = "consecutiveFirmwareUpgradeFailure", ignore = true)
    @Mapping(target = "maxRetryLimitReachedInstances", ignore = true)
    @Mapping(target = "pings", ignore = true)
    @Mapping(target = "rsuIntersections", ignore = true)
    @Mapping(target = "rsuOption", ignore = true)
    @Mapping(target = "scmsHealths", ignore = true)
    @Mapping(target = "snmpMsgfwdConfigs", ignore = true)
    void updateRsuFromPatch(RsuPatch patch, @MappingTarget Rsu rsu);

    /**
     * Convert RsuPatch to new Rsu entity (for create operations)
     */
    @Mapping(source = "ipv4Address", target = "ipv4Address")
    @Mapping(source = "geoPosition", target = "geography")
    @Mapping(target = "id", ignore = true) // ID will be auto-generated

    // Joined fields are ignored here and should be handled in the service layer
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "credential", ignore = true)
    @Mapping(target = "snmpCredential", ignore = true)
    @Mapping(target = "snmpProtocol", ignore = true)
    @Mapping(target = "rsuOrganizations", ignore = true)
    @Mapping(target = "firmwareVersion", ignore = true)
    @Mapping(target = "targetFirmwareVersion", ignore = true)
    @Mapping(target = "consecutiveFirmwareUpgradeFailure", ignore = true)
    @Mapping(target = "maxRetryLimitReachedInstances", ignore = true)
    @Mapping(target = "pings", ignore = true)
    @Mapping(target = "rsuIntersections", ignore = true)
    @Mapping(target = "rsuOption", ignore = true)
    @Mapping(target = "scmsHealths", ignore = true)
    @Mapping(target = "snmpMsgfwdConfigs", ignore = true)
    Rsu toRsu(RsuPatch rsuPatch);
}