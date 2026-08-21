package us.dot.its.jpo.rsustatusmonitor.models.snmp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OIDMapTest {

    @Test
    public void testOIDMapContainsKnownOIDs() {
        assertNotNull(OIDMap.oids);
        assertFalse(OIDMap.oids.isEmpty());
        
        // Test some known OIDs
        assertTrue(OIDMap.oids.containsKey("rsuModeStatus"));
        assertTrue(OIDMap.oids.containsKey("rsuMode"));
        assertTrue(OIDMap.oids.containsKey("rsuGnssStatus"));
        assertTrue(OIDMap.oids.containsKey("rsuStatus"));
    }

    @Test
    public void testOIDMapValues() {
        OID rsuModeStatus = OIDMap.oids.get("rsuModeStatus");
        assertNotNull(rsuModeStatus);
        assertEquals("rsuModeStatus", rsuModeStatus.getName());
        assertEquals(OID_TYPE.SCALAR, rsuModeStatus.getType());
        assertEquals("1.3.6.1.4.1.1.1206.4.2.18.16.3.0", rsuModeStatus.getOid());
    }

    @Test
    public void testOIDMapContainsTableEntries() {
        assertTrue(OIDMap.oids.containsKey("rsuReceivedMsgTable"));
        assertTrue(OIDMap.oids.containsKey("rsuReceivedMsgEntry"));
        
        OID table = OIDMap.oids.get("rsuReceivedMsgTable");
        assertEquals(OID_TYPE.TABLE, table.getType());
    }

    @Test
    public void testOIDMapIsImmutable() {
        int originalSize = OIDMap.oids.size();
        
        assertThrows(UnsupportedOperationException.class, () -> {
            OIDMap.oids.put("newOID", new OID("test", OID_TYPE.SCALAR, "1.2.3"));
        });
        
        assertEquals(originalSize, OIDMap.oids.size());
    }
}
