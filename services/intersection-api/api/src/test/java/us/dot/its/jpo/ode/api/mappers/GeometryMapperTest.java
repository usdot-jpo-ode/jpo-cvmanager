package us.dot.its.jpo.ode.api.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

import us.dot.its.jpo.ode.api.models.admin.intersection.Bbox;
import us.dot.its.jpo.ode.api.models.admin.intersection.RefPt;

import static org.junit.jupiter.api.Assertions.*;

class GeometryMapperTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
    private static final double DELTA = 0.0000001;

    private GeometryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GeometryMapperImpl();
    }

    @Nested
    @DisplayName("toRefPt(Point) — JTS Point to RefPt DTO")
    class ToRefPtTests {

        @Test
        @DisplayName("null input returns null")
        void toRefPt_null_returnsNull() {
            assertNull(mapper.toRefPt(null));
        }

        @Test
        @DisplayName("valid Point maps x→longitude, y→latitude")
        void toRefPt_validPoint_mapsCoordinates() {
            double latitude = 39.7392;
            double longitude = -104.9903;
            Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));

            RefPt result = mapper.toRefPt(point);

            assertNotNull(result);
            assertEquals(latitude, result.getLatitude(), DELTA);
            assertEquals(longitude, result.getLongitude(), DELTA);
        }

        @Test
        @DisplayName("southern-hemisphere point preserves negative latitude")
        void toRefPt_negativeLatitude_preserved() {
            double latitude = -33.8688;
            double longitude = 151.2093;
            Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));

            RefPt result = mapper.toRefPt(point);

            assertNotNull(result);
            assertEquals(latitude, result.getLatitude(), DELTA);
            assertEquals(longitude, result.getLongitude(), DELTA);
        }

        @Test
        @DisplayName("western-hemisphere point preserves negative longitude")
        void toRefPt_negativeLongitude_preserved() {
            double latitude = -34.6037;
            double longitude = -58.3816;
            Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));

            RefPt result = mapper.toRefPt(point);

            assertNotNull(result);
            assertEquals(latitude, result.getLatitude(), DELTA);
            assertEquals(longitude, result.getLongitude(), DELTA);
        }
    }

    @Nested
    @DisplayName("toPoint(RefPt) — RefPt DTO to JTS Point")
    class ToPointTests {

        @Test
        @DisplayName("null input returns null")
        void toPoint_null_returnsNull() {
            assertNull(mapper.toPoint(null));
        }

        @Test
        @DisplayName("valid RefPt maps latitude→y, longitude→x with SRID 4326")
        void toPoint_validRefPt_mapsCoordinatesAndSrid() {
            double latitude = 39.7392;
            double longitude = -104.9903;
            RefPt refPt = new RefPt(latitude, longitude);

            Point result = mapper.toPoint(refPt);

            assertNotNull(result);
            assertEquals(longitude, result.getX(), DELTA);
            assertEquals(latitude, result.getY(), DELTA);
            assertEquals(4326, result.getSRID());
        }

        @Test
        @DisplayName("southern/western RefPt preserves negative coordinates")
        void toPoint_negativeCoords_preserved() {
            RefPt refPt = new RefPt(-33.8688, -58.3816);

            Point result = mapper.toPoint(refPt);

            assertNotNull(result);
            assertEquals(-58.3816, result.getX(), DELTA);
            assertEquals(-33.8688, result.getY(), DELTA);
        }
    }

    @Nested
    @DisplayName("toBbox(Polygon) — JTS Polygon to Bbox DTO")
    class ToBboxTests {

        @Test
        @DisplayName("null input returns null")
        void toBbox_null_returnsNull() {
            assertNull(mapper.toBbox(null));
        }

        @Test
        @DisplayName("rectangular polygon maps envelope to Bbox corners")
        void toBbox_rectangularPolygon_mapsEnvelope() {
            double minLat = 39.73;
            double minLon = -105.00;
            double maxLat = 39.74;
            double maxLon = -104.99;
            Polygon polygon = (Polygon) GEOMETRY_FACTORY.toGeometry(
                    new Envelope(minLon, maxLon, minLat, maxLat));

            Bbox result = mapper.toBbox(polygon);

            assertNotNull(result);
            assertEquals(minLat, result.getLatitude1(), DELTA);
            assertEquals(minLon, result.getLongitude1(), DELTA);
            assertEquals(maxLat, result.getLatitude2(), DELTA);
            assertEquals(maxLon, result.getLongitude2(), DELTA);
        }

        @Test
        @DisplayName("polygon spanning the equator maps negative and positive latitudes")
        void toBbox_crossesEquator_preservesSigns() {
            double minLat = -1.0;
            double maxLat = 1.0;
            double minLon = -1.0;
            double maxLon = 1.0;
            Polygon polygon = (Polygon) GEOMETRY_FACTORY.toGeometry(
                    new Envelope(minLon, maxLon, minLat, maxLat));

            Bbox result = mapper.toBbox(polygon);

            assertNotNull(result);
            assertEquals(minLat, result.getLatitude1(), DELTA);
            assertEquals(minLon, result.getLongitude1(), DELTA);
            assertEquals(maxLat, result.getLatitude2(), DELTA);
            assertEquals(maxLon, result.getLongitude2(), DELTA);
        }
    }

    @Nested
    @DisplayName("toPolygon(Bbox) — Bbox DTO to JTS Polygon")
    class ToPolygonTests {

        @Test
        @DisplayName("null input returns null")
        void toPolygon_null_returnsNull() {
            assertNull(mapper.toPolygon(null));
        }

        @Test
        @DisplayName("valid Bbox produces polygon with correct envelope and SRID 4326")
        void toPolygon_validBbox_correctEnvelopeAndSrid() {
            double lat1 = 39.73;
            double lon1 = -105.00;
            double lat2 = 39.74;
            double lon2 = -104.99;
            Bbox bbox = new Bbox(lat1, lon1, lat2, lon2);

            Polygon result = mapper.toPolygon(bbox);

            assertNotNull(result);
            Envelope env = result.getEnvelopeInternal();
            assertEquals(lon1, env.getMinX(), DELTA);
            assertEquals(lon2, env.getMaxX(), DELTA);
            assertEquals(lat1, env.getMinY(), DELTA);
            assertEquals(lat2, env.getMaxY(), DELTA);
            assertEquals(4326, result.getSRID());
        }

        @Test
        @DisplayName("Bbox spanning the antimeridian preserves negative longitude")
        void toPolygon_negativeLongitude_preserved() {
            Bbox bbox = new Bbox(-1.0, -180.0, 1.0, -179.0);

            Polygon result = mapper.toPolygon(bbox);

            assertNotNull(result);
            Envelope env = result.getEnvelopeInternal();
            assertEquals(-180.0, env.getMinX(), DELTA);
            assertEquals(-179.0, env.getMaxX(), DELTA);
        }
    }

    @Nested
    @DisplayName("Round-trip conversion tests")
    class RoundTripTests {

        @Test
        @DisplayName("Point → RefPt → Point preserves coordinates and SRID")
        void roundTrip_pointToRefPtToPoint() {
            double latitude = 39.7392;
            double longitude = -104.9903;
            Point original = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));

            RefPt refPt = mapper.toRefPt(original);
            Point result = mapper.toPoint(refPt);

            assertNotNull(result);
            assertEquals(original.getX(), result.getX(), DELTA);
            assertEquals(original.getY(), result.getY(), DELTA);
            assertEquals(4326, result.getSRID());
        }

        @Test
        @DisplayName("RefPt → Point → RefPt preserves latitude and longitude")
        void roundTrip_refPtToPointToRefPt() {
            RefPt original = new RefPt(39.7392, -104.9903);

            Point point = mapper.toPoint(original);
            RefPt result = mapper.toRefPt(point);

            assertNotNull(result);
            assertEquals(original.getLatitude(), result.getLatitude(), DELTA);
            assertEquals(original.getLongitude(), result.getLongitude(), DELTA);
        }

        @Test
        @DisplayName("Polygon → Bbox → Polygon preserves envelope bounds")
        void roundTrip_polygonToBboxToPolygon() {
            Envelope original = new Envelope(-105.00, -104.99, 39.73, 39.74);
            Polygon originalPolygon = (Polygon) GEOMETRY_FACTORY.toGeometry(original);

            Bbox bbox = mapper.toBbox(originalPolygon);
            Polygon result = mapper.toPolygon(bbox);

            assertNotNull(result);
            Envelope resultEnv = result.getEnvelopeInternal();
            assertEquals(original.getMinX(), resultEnv.getMinX(), DELTA);
            assertEquals(original.getMaxX(), resultEnv.getMaxX(), DELTA);
            assertEquals(original.getMinY(), resultEnv.getMinY(), DELTA);
            assertEquals(original.getMaxY(), resultEnv.getMaxY(), DELTA);
        }

        @Test
        @DisplayName("Bbox → Polygon → Bbox preserves all four corners")
        void roundTrip_bboxToPolygonToBbox() {
            Bbox original = new Bbox(39.73, -105.00, 39.74, -104.99);

            Polygon polygon = mapper.toPolygon(original);
            Bbox result = mapper.toBbox(polygon);

            assertNotNull(result);
            assertEquals(original.getLatitude1(), result.getLatitude1(), DELTA);
            assertEquals(original.getLongitude1(), result.getLongitude1(), DELTA);
            assertEquals(original.getLatitude2(), result.getLatitude2(), DELTA);
            assertEquals(original.getLongitude2(), result.getLongitude2(), DELTA);
        }

        @Test
        @DisplayName("round-trip holds across multiple geographic locations")
        void roundTrip_multipleLocations() {
            double[][] locations = {
                    { 39.7392, -104.9903 },  // Denver, CO
                    { 35.6762, 139.6503 },   // Tokyo, Japan
                    { -33.8688, 151.2093 },  // Sydney, Australia
                    { -34.6037, -58.3816 },  // Buenos Aires, Argentina
                    { 51.5074, -0.1278 }     // London, UK
            };

            for (double[] coords : locations) {
                double latitude = coords[0];
                double longitude = coords[1];
                RefPt original = new RefPt(latitude, longitude);

                Point point = mapper.toPoint(original);
                RefPt result = mapper.toRefPt(point);

                assertEquals(latitude, result.getLatitude(), DELTA,
                        "Latitude mismatch for (" + latitude + ", " + longitude + ")");
                assertEquals(longitude, result.getLongitude(), DELTA,
                        "Longitude mismatch for (" + latitude + ", " + longitude + ")");
            }
        }
    }
}
