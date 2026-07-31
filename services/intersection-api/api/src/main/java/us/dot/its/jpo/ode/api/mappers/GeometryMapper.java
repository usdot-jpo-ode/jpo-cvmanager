package us.dot.its.jpo.ode.api.mappers;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import us.dot.its.jpo.ode.api.models.admin.intersection.Bbox;
import us.dot.its.jpo.ode.api.models.admin.intersection.RefPt;

/**
 * MapStruct mapper for converting between JTS geometry types and DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GeometryMapper {

    GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * Converts a JTS Point (x=longitude, y=latitude) to a RefPt DTO.
     */
    @Mapping(source = "x", target = "longitude")
    @Mapping(source = "y", target = "latitude")
    RefPt toRefPt(Point point);

    /**
     * Converts a RefPt DTO to a JTS Point with SRID 4326.
     */
    default Point toPoint(RefPt refPt) {
        if (refPt == null) {
            return null;
        }
        Coordinate coordinate = new Coordinate(refPt.getLongitude(), refPt.getLatitude());
        return GEOMETRY_FACTORY.createPoint(coordinate);
    }

    /**
     * Converts a JTS Polygon to a Bbox DTO using the polygon's envelope.
     */
    @Mapping(source = "envelopeInternal.minX", target = "longitude1")
    @Mapping(source = "envelopeInternal.minY", target = "latitude1")
    @Mapping(source = "envelopeInternal.maxX", target = "longitude2")
    @Mapping(source = "envelopeInternal.maxY", target = "latitude2")
    Bbox toBbox(Polygon polygon);

    /**
     * Converts a Bbox DTO to a JTS Polygon (rectangular envelope) with SRID 4326.
     */
    default Polygon toPolygon(Bbox bbox) {
        if (bbox == null) {
            return null;
        }
        Envelope env = new Envelope(
                bbox.getLongitude1(), bbox.getLongitude2(),
                bbox.getLatitude1(), bbox.getLatitude2());
        return (Polygon) GEOMETRY_FACTORY.toGeometry(env);
    }
}
