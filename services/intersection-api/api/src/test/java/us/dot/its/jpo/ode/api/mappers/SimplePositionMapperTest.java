package us.dot.its.jpo.ode.api.mappers;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import us.dot.its.jpo.ode.api.models.SimplePosition;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

class SimplePositionMapperTest {

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private static final double DELTA = 0.0000001; // Precision for double comparisons

    @Nested
    @DisplayName("Tests for mapPointToSimplePosition mapper method")
    class MapPointToSimplePositionTests {
        @Test
        void testMapPointToSimplePosition_Success() {
            double latitude = 39.7392;
            double longitude = -104.9903;
            Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

            SimplePosition result = SimplePositionMapper.mapPointToSimplePosition(point);

            assertNotNull(result);
            assertEquals(latitude, result.latitude(), DELTA);
            assertEquals(longitude, result.longitude(), DELTA);
        }

        @Test
        void testMapPointToSimplePosition_Null() {
            SimplePosition result = SimplePositionMapper.mapPointToSimplePosition(null);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Tests for mapSimplePositionToPoint mapper method")
    class MapSimplePositionToPointTests {
        @Test
        void testMapSimplePositionToPoint_Success() {
            double latitude = 39.7392;
            double longitude = -104.9903;
            SimplePosition position = new SimplePosition(latitude, longitude);

            Point result = SimplePositionMapper.mapSimplePositionToPoint(position);

            assertNotNull(result);
            assertEquals(latitude, result.getY(), DELTA);
            assertEquals(longitude, result.getX(), DELTA);
            assertEquals(4326, result.getSRID());
        }

        @Test
        void testMapSimplePositionToPoint_Null() {
            Point result = SimplePositionMapper.mapSimplePositionToPoint(null);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Round trip conversion tests for Point <-> SimplePosition")
    class RoundTripConversionTests {
        @Test
        void testRoundTripConversion_PointToSimplePositionToPoint() {
            double latitude = 39.7392;
            double longitude = -104.9903;
            Point originalPoint = geometryFactory.createPoint(new Coordinate(longitude, latitude));

            SimplePosition simplePosition = SimplePositionMapper.mapPointToSimplePosition(originalPoint);
            Point resultPoint = SimplePositionMapper.mapSimplePositionToPoint(simplePosition);

            assertNotNull(resultPoint);
            assertEquals(originalPoint.getY(), resultPoint.getY(), DELTA);
            assertEquals(originalPoint.getX(), resultPoint.getX(), DELTA);
            assertEquals(originalPoint.getSRID(), resultPoint.getSRID());
        }

        @Test
        void testRoundTripConversion_SimplePositionToPointToSimplePosition() {
            double latitude = 39.7392;
            double longitude = -104.9903;
            SimplePosition originalPosition = new SimplePosition(latitude, longitude);

            Point point = SimplePositionMapper.mapSimplePositionToPoint(originalPosition);
            SimplePosition resultPosition = SimplePositionMapper.mapPointToSimplePosition(point);

            assertNotNull(resultPosition);
            assertEquals(originalPosition.latitude(), resultPosition.latitude(), DELTA);
            assertEquals(originalPosition.longitude(), resultPosition.longitude(), DELTA);
        }

        @Test
        void testRoundTripConversion_MultipleLocations() {
            double[][] locations = {
                    { 39.7392, -104.9903 }, // Denver, CO
                    { 35.6762, 139.6503 }, // Tokyo, Japan
                    { -33.8688, 151.2093 }, // Sydney, Australia
                    { -34.6037, -58.3816 }, // Buenos Aires, Argentina
                    { 51.5074, -0.1278 } // London, UK
            };

            for (double[] coords : locations) {
                double latitude = coords[0];
                double longitude = coords[1];

                SimplePosition originalPosition = new SimplePosition(latitude, longitude);
                Point point = SimplePositionMapper.mapSimplePositionToPoint(originalPosition);
                SimplePosition resultPosition = SimplePositionMapper.mapPointToSimplePosition(point);

                assertEquals(latitude, resultPosition.latitude(), DELTA,
                        "Latitude mismatch for coordinates: " + latitude + ", " + longitude);
                assertEquals(longitude, resultPosition.longitude(), DELTA,
                        "Longitude mismatch for coordinates: " + latitude + ", " + longitude);
            }
        }
    }
}