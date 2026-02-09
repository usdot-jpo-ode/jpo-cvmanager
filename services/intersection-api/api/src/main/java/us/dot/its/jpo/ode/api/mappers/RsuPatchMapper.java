package us.dot.its.jpo.ode.api.mappers;

import java.net.InetAddress;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import us.dot.its.jpo.ode.api.models.SimplePosition;
import us.dot.its.jpo.ode.api.models.devices.management.RsuPatch;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RsuPatchMapper {

    /**
     * Update existing Rsu entity with non-null values from RsuPatch
     * Null values in the patch are ignored (existing values preserved)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "ipv4Address", target = "ipv4Address", qualifiedByName = "mapStringToInetAddress")
    @Mapping(source = "geoPosition", target = "geography", qualifiedByName = "mapGeoPosition")
    @Mapping(target = "model", ignore = true) // Set in service layer
    @Mapping(target = "credential", ignore = true) // Set in service layer
    @Mapping(target = "snmpCredential", ignore = true) // Set in service layer
    @Mapping(target = "snmpProtocol", ignore = true) // Set in service layer
    @Mapping(target = "rsuOrganizations", ignore = true) // Set in service layer
    @Mapping(target = "id", ignore = true) // Never update ID
    @Mapping(target = "firmwareVersion", ignore = true)
    @Mapping(target = "targetFirmwareVersion", ignore = true)
    void updateRsuFromPatch(RsuPatch patch, @MappingTarget Rsu rsu);

    /**
     * Convert RsuPatch to new Rsu entity (for create operations)
     */
    @Mapping(source = "ipv4Address", target = "ipv4Address", qualifiedByName = "mapStringToInetAddress")
    @Mapping(source = "geoPosition", target = "geography", qualifiedByName = "mapGeoPosition")
    @Mapping(target = "model", ignore = true) // Set in service layer
    @Mapping(target = "credential", ignore = true) // Set in service layer
    @Mapping(target = "snmpCredential", ignore = true) // Set in service layer
    @Mapping(target = "snmpProtocol", ignore = true) // Set in service layer
    @Mapping(target = "rsuOrganizations", ignore = true) // Set in service layer
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firmwareVersion", ignore = true)
    @Mapping(target = "targetFirmwareVersion", ignore = true)
    Rsu toRsu(RsuPatch rsuPatch);

    /**
     * Map String IP address to InetAddress
     */
    @Named("mapStringToInetAddress")
    default InetAddress mapStringToInetAddress(String ipv4Address) {
        if (ipv4Address == null) {
            return null;
        }
        try {
            return InetAddress.getByName(ipv4Address);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid IP address: " + ipv4Address, e);
        }
    }

    /**
     * Convert SimplePosition (lat/long) to JTS Point
     * Note: PostGIS uses SRID 4326 (WGS 84) for geographic coordinates
     * Coordinate order is (longitude, latitude) for POINT geometry
     */
    @Named("mapGeoPosition")
    default Point mapGeoPosition(SimplePosition position) {
        if (position == null || position.latitude() == null || position.longitude() == null) {
            return null;
        }

        // Create GeometryFactory with SRID 4326 (WGS 84 - standard for GPS coordinates)
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        // Create coordinate (longitude, latitude) - ORDER MATTERS!
        // PostGIS uses (X, Y) = (longitude, latitude)
        Coordinate coordinate = new Coordinate(position.longitude(), position.latitude());

        // Create and return Point
        return geometryFactory.createPoint(coordinate);
    }

    /**
     * Convert JTS Point back to SimplePosition (for reverse mapping if needed)
     */
    @Named("mapPointToSimplePosition")
    default SimplePosition mapPointToSimplePosition(Point point) {
        if (point == null) {
            return null;
        }

        return new SimplePosition(point.getY(), point.getX()); // latitude, longitude
    }
}