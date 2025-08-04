package us.dot.its.jpo.ode.api.converters;

import org.junit.jupiter.api.Test;
import us.dot.its.jpo.ode.api.models.geojson.GeoJsonFeatureCollection;
import us.dot.its.jpo.ode.api.models.haas.HaasLocation;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HaasLocationConverterTest {

    @Test
    void testToGeoJson_EmptyList() {
        List<HaasLocation> emptyLocations = new ArrayList<>();
        GeoJsonFeatureCollection result = HaasLocationConverter.toGeoJson(emptyLocations);

        assertNotNull(result);
        assertNotNull(result.getFeatures());
        assertTrue(result.getFeatures().isEmpty());
    }

    @Test
    void testToGeoJson_SingleLocation() {
        List<HaasLocation> locations = new ArrayList<>();
        HaasLocation location = new HaasLocation();
        location.setLat(10.00);
        location.setLon(-100.00);
        locations.add(location);

        GeoJsonFeatureCollection result = HaasLocationConverter.toGeoJson(locations);

        assertNotNull(result);
        assertNotNull(result.getFeatures());
        assertEquals(1, result.getFeatures().size());

        var feature = result.getFeatures().get(0);
        assertNotNull(feature.getGeometry());
        assertEquals("Point", feature.getGeometry().getType());
        List<Double> coords = feature.getGeometry().getCoordinates();
        assertArrayEquals(
                new double[] { -100.00, 10.00 },
                new double[] { coords.get(0), coords.get(1) },
                0.0001);
        assertEquals(location, feature.getProperties());
    }

    @Test
    void testToGeoJson_MultipleLocations() {
        List<HaasLocation> locations = new ArrayList<>();

        HaasLocation location1 = new HaasLocation();
        location1.setLat(10.00);
        location1.setLon(-100.00);
        locations.add(location1);

        HaasLocation location2 = new HaasLocation();
        location2.setLat(40.7128);
        location2.setLon(-74.0060);
        locations.add(location2);

        GeoJsonFeatureCollection result = HaasLocationConverter.toGeoJson(locations);

        assertNotNull(result);
        assertNotNull(result.getFeatures());
        assertEquals(2, result.getFeatures().size());

        var feature1 = result.getFeatures().get(0);
        assertNotNull(feature1.getGeometry());
        assertEquals("Point", feature1.getGeometry().getType());
        List<Double> coords1 = feature1.getGeometry().getCoordinates();
        assertArrayEquals(
                new double[] { -100.00, 10.00 },
                new double[] { coords1.get(0), coords1.get(1) },
                0.0001);
        assertEquals(location1, feature1.getProperties());

        var feature2 = result.getFeatures().get(1);
        assertNotNull(feature2.getGeometry());
        assertEquals("Point", feature2.getGeometry().getType());
        List<Double> coords2 = feature2.getGeometry().getCoordinates();
        assertArrayEquals(
                new double[] { -74.0060, 40.7128 },
                new double[] { coords2.get(0), coords2.get(1) },
                0.0001);
        assertEquals(location2, feature2.getProperties());
    }

    @Test
    void testToGeoJson_NullLocation() {
        List<HaasLocation> locations = new ArrayList<>();
        locations.add(null);

        assertThrows(NullPointerException.class, () -> {
            HaasLocationConverter.toGeoJson(locations);
        });
    }
}