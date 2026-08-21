package us.dot.its.jpo.rsustatusmonitor.models.snmp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RsuStateTest {

    @Test
    public void testGettersAndSetters() {
        RsuState state = new RsuState();
        
        state.setTimestamp(1700000000000L);
        state.setIntersectionID("12345");
        state.setRsuIP("192.168.1.1");
        state.setTemperature(35.5);
        state.setUptime(3600);
        state.setMode(4);
        
        assertEquals(1700000000000L, state.getTimestamp());
        assertEquals("12345", state.getIntersectionID());
        assertEquals("192.168.1.1", state.getRsuIP());
        assertEquals(35.5, state.getTemperature());
        assertEquals(3600, state.getUptime());
        assertEquals(4, state.getMode());
    }

    @Test
    public void testDefaultValues() {
        RsuState state = new RsuState();
        
        assertEquals(0L, state.getTimestamp());
        assertNull(state.getIntersectionID());
        assertNull(state.getRsuIP());
        assertEquals(0.0, state.getTemperature());
        assertEquals(0, state.getUptime());
        assertEquals(0, state.getMode());
    }

    @Test
    public void testModeValues() {
        RsuState state = new RsuState();
        
        // Test common RSU modes
        state.setMode(2); // Standby
        assertEquals(2, state.getMode());
        
        state.setMode(4); // Operational
        assertEquals(4, state.getMode());
        
        state.setMode(16); // Off
        assertEquals(16, state.getMode());
    }

    @Test
    public void testTemperatureRange() {
        RsuState state = new RsuState();
        
        state.setTemperature(-40.0);
        assertEquals(-40.0, state.getTemperature());
        
        state.setTemperature(85.0);
        assertEquals(85.0, state.getTemperature());
    }

    @Test
    public void testUptimeValues() {
        RsuState state = new RsuState();
        
        // Test various uptime values
        state.setUptime(0); // Just booted
        assertEquals(0, state.getUptime());
        
        state.setUptime(86400); // 1 day
        assertEquals(86400, state.getUptime());
        
        state.setUptime(2592000); // 30 days
        assertEquals(2592000, state.getUptime());
    }
}
