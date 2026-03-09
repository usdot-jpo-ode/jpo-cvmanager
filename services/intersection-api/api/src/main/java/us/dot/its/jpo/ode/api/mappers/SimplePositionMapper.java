package us.dot.its.jpo.ode.api.mappers;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import us.dot.its.jpo.ode.api.models.SimplePosition;

/**
 * MapStruct mapper for converting between InetAddress and String.
 * This mapper is automatically used by other mappers when they need to convert
 * IP addresses.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SimplePositionMapper {

    public static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * Converts a PostGIS Point geometry to a SimplePosition (lat/lon pair).
     * 
     * @param geography PostGIS Point geometry (nullable)
     * @return SimplePosition with latitude and longitude, or null if input is null
     */
    public static SimplePosition mapPointToSimplePosition(Point geography) {
        if (geography == null) {
            return null;
        }
        return new SimplePosition(geography.getY(), geography.getX());
    }

    /**
     * Converts a SimplePosition (lat/lon pair) to a PostGIS Point geometry.
     * 
     * @param position SimplePosition with latitude and longitude (nullable)
     * @return PostGIS Point geometry with SRID 4326, or null if input is null
     */
    public static Point mapSimplePositionToPoint(SimplePosition position) {
        if (position == null) {
            return null;
        }

        Coordinate coordinate = new Coordinate(position.longitude(), position.latitude());

        return geometryFactory.createPoint(coordinate);
    }
}