package us.dot.its.jpo.ode.api.mappers;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Point;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import us.dot.its.jpo.ode.api.models.SimplePosition;
import us.dot.its.jpo.ode.api.models.postgres.dtos.RsuInfoDto;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuModel;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuOrganization;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RsuInfoMapper {

    /**
     * Convert Rsu entity to RsuInfoDto
     * MapStruct will automatically map fields with the same name
     */
    @Mapping(source = "ipv4Address", target = "ipv4Address", qualifiedByName = "mapInetAddressToString")
    @Mapping(source = "geography", target = "geoPosition", qualifiedByName = "mapGeoPosition")
    @Mapping(source = "model", target = "model", qualifiedByName = "mapModelNames")
    @Mapping(source = "credential.nickname", target = "sshCredentialGroup")
    @Mapping(source = "snmpCredential.nickname", target = "snmpCredentialGroup")
    @Mapping(source = "snmpProtocol.nickname", target = "snmpVersionGroup")
    @Mapping(source = "rsuOrganizations", target = "organizations", qualifiedByName = "mapOrganizationNames")
    RsuInfoDto toDto(Rsu rsu);

    /**
     * Convert InetAddress to String representation (IP address)
     */
    @Named("mapInetAddressToString")
    default String mapInetAddressToString(InetAddress inetAddress) {
        if (inetAddress == null) {
            return null;
        }
        return inetAddress.getHostAddress();
    }

    /**
     * Convert JTS Point geometry to SimplePosition (latitude/longitude)
     */
    @Named("mapGeoPosition")
    default SimplePosition mapGeoPosition(Point geography) {
        if (geography == null) {
            return null;
        }
        return new SimplePosition(geography.getY(), geography.getX());
    }

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
     * Extract organization names from RsuOrganization list
     * Returns a list of organization name strings
     */
    @Named("mapOrganizationNames")
    default List<String> mapOrganizationNames(List<RsuOrganization> rsuOrganizations) {
        if (rsuOrganizations == null) {
            return null;
        }
        return rsuOrganizations.stream()
                .filter(ro -> ro != null && ro.getOrganization() != null && ro.getOrganization().getName() != null)
                .map(ro -> ro.getOrganization().getName())
                .collect(Collectors.toList());
    }
}