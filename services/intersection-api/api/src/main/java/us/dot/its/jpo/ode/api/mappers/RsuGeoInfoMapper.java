package us.dot.its.jpo.ode.api.mappers;

import org.locationtech.jts.geom.Point;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import us.dot.its.jpo.ode.api.models.geojson.GeoJsonPointDto;
import us.dot.its.jpo.ode.api.models.postgres.dtos.RsuGeoInfoDto;
import us.dot.its.jpo.ode.api.models.postgres.dtos.RsuGeoInfoPropertiesDto;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {
        INetMapper.class }, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RsuGeoInfoMapper {

    /**
     * Convert Rsu entity to RsuGeoInfoDto
     * MapStruct will automatically map fields with the same name
     */
    @Mapping(source = "geography", target = "geometry", qualifiedByName = "mapPointToGeoJson")
    @Mapping(source = ".", target = "properties")
    RsuGeoInfoDto toDto(Rsu rsu);

    /**
     * Extract properties from Rsu entity
     * Returns a RsuGeoInfoPropertiesDto
     */
    @Mapping(source = "id", target = "rsuId")
    @Mapping(source = "rsuOption.timDeposit", target = "timDeposit", defaultValue = "false")
    @Mapping(source = "rsuOption.snmpMonitoring", target = "snmpMonitoring", defaultValue = "false")
    @Mapping(source = "model.name", target = "modelName")
    @Mapping(source = "model.manufacturer.name", target = "manufacturerName")
    RsuGeoInfoPropertiesDto toPropertiesDto(Rsu rsu);

    @Named("mapPointToGeoJson")
    default GeoJsonPointDto mapPointToGeoJson(Point point) {
        if (point == null) {
            return null;
        }
        // JTS Point: getX() = longitude, getY() = latitude
        return new GeoJsonPointDto(new double[] { point.getX(), point.getY() });
    }
}
