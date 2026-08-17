package us.dot.its.jpo.ode.api.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import us.dot.its.jpo.ode.api.models.postgres.projections.ScmsHealthRsuProjection;
import us.dot.its.jpo.ode.api.models.postgres.projections.ScmsHealthRsuProjectionImpl;
import us.dot.its.jpo.ode.api.models.scms.ScmsHealthDto;
import us.dot.its.jpo.ode.api.models.scms.ScmsHealthResponse;

class ScmsHealthMapperTest {

    private final ScmsHealthMapper mapper = new ScmsHealthMapperImpl();

    @Test
    @DisplayName("Maps projections to ScmsHealthResponse successfully")
    void testToResponse_Success() throws UnknownHostException {
        String ip = "10.0.0.1";
        InetAddress inetAddress = InetAddress.getByName(ip);
        Instant expiration = Instant.parse("2024-03-27T15:00:00Z");

        ScmsHealthRsuProjection projection = new ScmsHealthRsuProjectionImpl(inetAddress, true, expiration);
        List<ScmsHealthRsuProjection> projections = new ArrayList<>();
        projections.add(projection);

        ScmsHealthResponse response = mapper.toResponse(projections);

        assertNotNull(response);
        Map<String, ScmsHealthDto> result = response.getScmsHealthByIp();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(ip));
        ScmsHealthDto dto = result.get(ip);
        assertNotNull(dto);
        assertTrue(dto.getHealth());
        assertEquals(expiration, dto.getExpiration());
    }

    @Test
    @DisplayName("Maps projections with inactive health")
    void testToResponse_InactiveHealth_ReturnsDtoWithFalse() throws UnknownHostException {
        String ip = "10.0.0.1";
        InetAddress inetAddress = InetAddress.getByName(ip);

        ScmsHealthRsuProjection projection = new ScmsHealthRsuProjectionImpl(inetAddress, false, null);
        List<ScmsHealthRsuProjection> projections = new ArrayList<>();
        projections.add(projection);

        ScmsHealthResponse response = mapper.toResponse(projections);

        assertNotNull(response);
        Map<String, ScmsHealthDto> result = response.getScmsHealthByIp();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(ip));
        assertNotNull(result.get(ip));
        assertFalse(result.get(ip).getHealth());
    }

    @Test
    @DisplayName("Maps projections with no health")
    void testToResponse_NoHealth_ReturnsNullValue() throws UnknownHostException {
        String ip = "10.0.0.1";
        InetAddress inetAddress = InetAddress.getByName(ip);

        ScmsHealthRsuProjection projection = new ScmsHealthRsuProjectionImpl(inetAddress, null, null);
        List<ScmsHealthRsuProjection> projections = new ArrayList<>();
        projections.add(projection);

        ScmsHealthResponse response = mapper.toResponse(projections);

        assertNotNull(response);
        Map<String, ScmsHealthDto> result = response.getScmsHealthByIp();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(ip));
        assertNull(result.get(ip));
    }

    @Test
    @DisplayName("Null input returns null")
    void testToDto_NullInput() {
        assertNull(mapper.toDto(null));
    }

    @Test
    @DisplayName("Maps projections with null health")
    void testToDto_NullHealth() throws UnknownHostException {
        String ip = "10.0.0.1";
        InetAddress inetAddress = InetAddress.getByName(ip);

        ScmsHealthRsuProjection projection = new ScmsHealthRsuProjectionImpl(inetAddress, null, null);

        ScmsHealthDto dto = mapper.toDto(projection);

        assertNotNull(dto);
        assertNull(dto.getHealth());
        assertNull(dto.getExpiration());
    }

    @Test
    @DisplayName("Empty input returns response with empty map")
    void testToResponse_EmptyInput() {
        ScmsHealthResponse response = mapper.toResponse(new ArrayList<>());
        assertNotNull(response);
        assertNotNull(response.getScmsHealthByIp());
        assertTrue(response.getScmsHealthByIp().isEmpty());
    }

    @Test
    @DisplayName("Maps projection to DTO successfully")
    void testToDto_Success() throws UnknownHostException {
        String ip = "10.0.0.1";
        InetAddress inetAddress = InetAddress.getByName(ip);
        Instant expiration = Instant.parse("2024-03-27T15:00:00Z");

        ScmsHealthRsuProjection projection = new ScmsHealthRsuProjectionImpl(inetAddress, true, expiration);

        ScmsHealthDto dto = mapper.toDto(projection);

        assertNotNull(dto);
        assertTrue(dto.getHealth());
        assertEquals(expiration, dto.getExpiration());
    }
}
