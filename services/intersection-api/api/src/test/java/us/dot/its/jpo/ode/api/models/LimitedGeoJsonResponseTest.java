package us.dot.its.jpo.ode.api.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import us.dot.its.jpo.ode.api.models.haas.HaasLocation;

class LimitedGeoJsonResponseTest {

    @Test
    void testFromList_WithTruncation() {
        // Arrange
        List<HaasLocation> locations = new ArrayList<>();
        HaasLocation location1 = new HaasLocation();
        location1.setId("1");
        location1.setLat(40.7128);
        location1.setLon(-74.0060);
        locations.add(location1);

        HaasLocation location2 = new HaasLocation();
        location2.setId("2");
        location2.setLat(34.0522);
        location2.setLon(-118.2437);
        locations.add(location2);

        int limit = 1;
        boolean hasMoreResults = true;

        // Act
        LimitedGeoJsonResponse response = LimitedGeoJsonResponse.fromList(locations, limit, hasMoreResults);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals("FeatureCollection", response.getData().getType());
        assertEquals(2, response.getData().getFeatures().size());

        assertNotNull(response.getMetadata());
        assertEquals(limit, response.getMetadata().getLimit());
        assertEquals(2, response.getMetadata().getReturnedCount());
        assertTrue(response.getMetadata().isTruncated());
        assertTrue(response.getMetadata().getMessage().contains("truncated"));
    }

    @Test
    void testFromList_WithoutTruncation() {
        // Arrange
        List<HaasLocation> locations = new ArrayList<>();
        HaasLocation location = new HaasLocation();
        location.setId("1");
        location.setLat(40.7128);
        location.setLon(-74.0060);
        locations.add(location);

        int limit = 10;
        boolean hasMoreResults = false;

        // Act
        LimitedGeoJsonResponse response = LimitedGeoJsonResponse.fromList(locations, limit, hasMoreResults);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals("FeatureCollection", response.getData().getType());
        assertEquals(1, response.getData().getFeatures().size());

        assertNotNull(response.getMetadata());
        assertEquals(limit, response.getMetadata().getLimit());
        assertEquals(1, response.getMetadata().getReturnedCount());
        assertFalse(response.getMetadata().isTruncated());
        assertTrue(response.getMetadata().getMessage().contains("All available results"));
    }

    @Test
    void testFromList_EmptyList() {
        // Arrange
        List<HaasLocation> locations = new ArrayList<>();
        int limit = 100;
        boolean hasMoreResults = false;

        // Act
        LimitedGeoJsonResponse response = LimitedGeoJsonResponse.fromList(locations, limit, hasMoreResults);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals("FeatureCollection", response.getData().getType());
        assertEquals(0, response.getData().getFeatures().size());

        assertNotNull(response.getMetadata());
        assertEquals(limit, response.getMetadata().getLimit());
        assertEquals(0, response.getMetadata().getReturnedCount());
        assertFalse(response.getMetadata().isTruncated());
        assertTrue(response.getMetadata().getMessage().contains("All available results"));
    }

    @Test
    void testFromList_ExactLimit() {
        // Arrange
        List<HaasLocation> locations = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            HaasLocation location = new HaasLocation();
            location.setId(String.valueOf(i));
            location.setLat(40.0 + i);
            location.setLon(-74.0 + i);
            locations.add(location);
        }

        int limit = 5;
        boolean hasMoreResults = false;

        // Act
        LimitedGeoJsonResponse response = LimitedGeoJsonResponse.fromList(locations, limit, hasMoreResults);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals("FeatureCollection", response.getData().getType());
        assertEquals(5, response.getData().getFeatures().size());

        assertNotNull(response.getMetadata());
        assertEquals(limit, response.getMetadata().getLimit());
        assertEquals(5, response.getMetadata().getReturnedCount());
        assertFalse(response.getMetadata().isTruncated());
    }
}