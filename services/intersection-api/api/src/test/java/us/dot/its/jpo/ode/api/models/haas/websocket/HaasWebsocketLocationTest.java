package us.dot.its.jpo.ode.api.models.haas.websocket;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.ode.api.models.haas.HaasLocation;

import java.io.InputStream;

class HaasWebsocketLocationTest {

    @Test
    void testSerializationDeserialization() throws Exception {
        InputStream jsonStream = getClass().getResourceAsStream("/json/haas/HaasAlert.Websocket.Location.json");
        assertNotNull(jsonStream, "Test JSON file not found");

        ObjectMapper mapper = new ObjectMapper();

        HaasLocation location = mapper.readValue(jsonStream, HaasLocation.class);

        assertNotNull(location.getFeatures());
        assertEquals(1, location.getFeatures().size());
        assertEquals("point", location.getFeatures().getFirst().getType());
        assertEquals(1, location.getFeatures().getFirst().getGeometry().size());
        assertEquals(-100, location.getFeatures().getFirst().getGeometry().getFirst().getLon());
        assertEquals(10, location.getFeatures().getFirst().getGeometry().getFirst().getLat());

        assertEquals("000000000000000000000000-2025-02-25T20:35:26.155Z", location.getExternalId());
        assertEquals("emergency", location.getLocationType());
        assertEquals("10, -100.00", location.getStreetName());
        assertEquals("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", location.getId());

        String serialized = mapper.writeValueAsString(location);
        HaasLocation deserialized = mapper.readValue(serialized, HaasLocation.class);

        assertEquals(location.getFeatures().getFirst().getGeometry().getFirst().getLon(),
                deserialized.getFeatures().getFirst().getGeometry().getFirst().getLon());
        assertEquals(location.getExternalId(), deserialized.getExternalId());
    }
}