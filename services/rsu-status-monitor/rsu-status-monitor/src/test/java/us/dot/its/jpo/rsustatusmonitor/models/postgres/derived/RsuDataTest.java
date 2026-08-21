package us.dot.its.jpo.rsustatusmonitor.models.postgres.derived;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RsuDataTest {

    @Test
    public void testConstructor() {
        RsuData rsuData = new RsuData(1, "192.168.1.1", "12345");
        
        assertEquals(1, rsuData.getRsu_id());
        assertEquals("192.168.1.1", rsuData.getIpv4_address());
        assertEquals("12345", rsuData.getIntersection_id());
    }

    @Test
    public void testGettersAndSetters() {
        RsuData rsuData = new RsuData(1, "192.168.1.1", "12345");
        
        rsuData.setRsu_id(2);
        rsuData.setIpv4_address("10.0.0.1");
        rsuData.setIntersection_id("67890");
        
        assertEquals(2, rsuData.getRsu_id());
        assertEquals("10.0.0.1", rsuData.getIpv4_address());
        assertEquals("67890", rsuData.getIntersection_id());
    }

    @Test
    public void testEqualsAndHashCode() {
        RsuData rsuData1 = new RsuData(1, "192.168.1.1", "12345");
        RsuData rsuData2 = new RsuData(1, "192.168.1.1", "12345");
        RsuData rsuData3 = new RsuData(2, "10.0.0.1", "67890");
        
        assertEquals(rsuData1, rsuData2);
        assertNotEquals(rsuData1, rsuData3);
        assertEquals(rsuData1.hashCode(), rsuData2.hashCode());
    }

    @Test
    public void testToString() {
        RsuData rsuData = new RsuData(1, "192.168.1.1", "12345");
        String result = rsuData.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("192.168.1.1"));
    }
}
