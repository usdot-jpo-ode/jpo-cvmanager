package us.dot.its.jpo.ode.api.models.haas.websocket;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

class HaasWebsocketLocationTest {

    @Test
    void testSerializationDeserialization() throws Exception {
        // Load test JSON file from resources
        InputStream jsonStream = getClass().getResourceAsStream("/json/haas/HaasAlert.Websocket.Location.json");
        assertNotNull(jsonStream, "Test JSON file not found");

        ObjectMapper mapper = new ObjectMapper();

        // Test deserialization
        HaasWebsocketLocation location = mapper.readValue(jsonStream, HaasWebsocketLocation.class);

        // Verify the features were properly mapped
        assertNotNull(location.getFeatures());
        assertEquals(1, location.getFeatures().size());
        assertEquals("point", location.getFeatures().get(0).getType());
        assertEquals(1, location.getFeatures().get(0).getGeometry().size());
        assertEquals(-100, location.getFeatures().get(0).getGeometry().get(0).getLon());
        assertEquals(10, location.getFeatures().get(0).getGeometry().get(0).getLat());

        // Verify other fields from the test file
        assertEquals("000000000000000000000000-2025-02-25T20:35:26.155Z", location.getExternalId());
        assertEquals("emergency", location.getLocationType());
        assertEquals("10, -100", location.getStreetName());
        assertEquals("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", location.getId());

        // Test serialization
        String serialized = mapper.writeValueAsString(location);
        HaasWebsocketLocation deserialized = mapper.readValue(serialized, HaasWebsocketLocation.class);

        // Verify round-trip
        assertEquals(location.getFeatures().get(0).getGeometry().get(0).getLon(),
                deserialized.getFeatures().get(0).getGeometry().get(0).getLon());
        assertEquals(location.getExternalId(), deserialized.getExternalId());
    }
}