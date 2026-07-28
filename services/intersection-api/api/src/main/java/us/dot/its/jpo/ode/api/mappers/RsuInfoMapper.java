package us.dot.its.jpo.ode.api.mappers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import us.dot.its.jpo.ode.api.models.devices.RsuInfoDto;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuModel;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuOrganization;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = { INetMapper.class,
        SimplePositionMapper.class })
public interface RsuInfoMapper {

    /**
     * Convert Rsu entity to RsuInfoDto
     * MapStruct will automatically map fields with the same name
     */
    @Mapping(source = "ipv4Address", target = "ipv4Address")
    @Mapping(source = "geography", target = "geoPosition")
    @Mapping(source = "model", target = "model", qualifiedByName = "mapModelNames")
    @Mapping(source = "credential.nickname", target = "sshCredentialGroup")
    @Mapping(source = "snmpCredential.nickname", target = "snmpCredentialGroup")
    @Mapping(source = "snmpProtocol.nickname", target = "snmpVersionGroup")
    @Mapping(source = "rsuOrganizations", target = "organizations", qualifiedByName = "mapOrganizationNames")
    @Mapping(source = "rsuOption", target = "timDeposit", qualifiedByName = "mapTimDeposit")
    @Mapping(source = "rsuOption", target = "snmpMonitoring", qualifiedByName = "mapSnmpMonitoring")
    RsuInfoDto toDto(Rsu rsu);

    /**
     * Convert RsuInfoDto to Rsu entity
     * Note: Relationships (model, credentials, organizations) should be set in
     * service layer
     */
    @Mapping(source = "ipv4Address", target = "ipv4Address")
    @Mapping(source = "geoPosition", target = "geography")
    @Mapping(target = "id", ignore = true) // Auto-generated

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
    Rsu toEntity(RsuInfoDto dto);

    /**
     * Combine manufacturer name and model name into a single string
     * Returns format: "Manufacturer Model" (e.g., "Commsignia ITS-RS4-M")
     */
    @Named("mapModelNames")
    default String mapModelNames(RsuModel rsuModel) {
        if (rsuModel == null || rsuModel.getManufacturer() == null) {
            return rsuModel != null ? rsuModel.getName() : null;
        }
        return String.format("%s %s", rsuModel.getManufacturer().getName(), rsuModel.getName());
    }

    /**
     * Extract organization names from RsuOrganization list.
     * Returns a list of organization name strings.
     */
    @Named("mapOrganizationNames")
    default List<String> mapOrganizationNames(Set<RsuOrganization> rsuOrganizations) {
        if (rsuOrganizations == null) {
            return null;
        }
        return rsuOrganizations.stream()
                .filter(ro -> ro != null && ro.getOrganization() != null && ro.getOrganization().getName() != null)
                .map(ro -> ro.getOrganization().getName())
                .collect(Collectors.toList());
    }

    /**
     * Extract timDeposit flag from RsuOption
     * Returns null if rsuOption is not loaded
     */
    @Named("mapTimDeposit")
    default Boolean mapTimDeposit(us.dot.its.jpo.ode.api.models.postgres.tables.RsuOption rsuOption) {
        if (rsuOption == null) {
            return null;
        }
        return rsuOption.getTimDeposit();
    }

    /**
     * Extract snmpMonitoring flag from RsuOption
     * Returns null if rsuOption is not loaded
     */
    @Named("mapSnmpMonitoring")
    default Boolean mapSnmpMonitoring(us.dot.its.jpo.ode.api.models.postgres.tables.RsuOption rsuOption) {
        if (rsuOption == null) {
            return null;
        }
        return rsuOption.getSnmpMonitoring();
    }
}