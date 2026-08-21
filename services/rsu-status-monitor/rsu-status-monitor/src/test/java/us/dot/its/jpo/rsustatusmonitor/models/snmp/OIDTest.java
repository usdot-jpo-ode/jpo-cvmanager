package us.dot.its.jpo.rsustatusmonitor.models.snmp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OIDTest {

    @Test
    public void testConstructor() {
        OID oid = new OID("rsuModeStatus", OID_TYPE.SCALAR, "1.3.6.1.4.1.1.1206.4.2.18.16.3.0");
        
        assertEquals("rsuModeStatus", oid.getName());
        assertEquals(OID_TYPE.SCALAR, oid.getType());
        assertEquals("1.3.6.1.4.1.1.1206.4.2.18.16.3.0", oid.getOid());
    }

    @Test
    public void testGettersAndSetters() {
        OID oid = new OID("test", OID_TYPE.NODE, "1.2.3.4");
        
        oid.setName("rsuStatus");
        oid.setType(OID_TYPE.TABLE);
        oid.setOid("1.3.6.1.4.1");
        
        assertEquals("rsuStatus", oid.getName());
        assertEquals(OID_TYPE.TABLE, oid.getType());
        assertEquals("1.3.6.1.4.1", oid.getOid());
    }

    @Test
    public void testEqualsAndHashCode() {
        OID oid1 = new OID("test", OID_TYPE.SCALAR, "1.2.3.4");
        OID oid2 = new OID("test", OID_TYPE.SCALAR, "1.2.3.4");
        OID oid3 = new OID("different", OID_TYPE.NODE, "5.6.7.8");
        
        assertEquals(oid1, oid2);
        assertNotEquals(oid1, oid3);
        assertEquals(oid1.hashCode(), oid2.hashCode());
    }

    @Test
    public void testToString() {
        OID oid = new OID("rsuModeStatus", OID_TYPE.SCALAR, "1.3.6.1.4.1.1.1206.4.2.18.16.3.0");
        String result = oid.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("rsuModeStatus"));
    }

    @Test
    public void testOIDTypes() {
        assertEquals(OID_TYPE.SCALAR, OID_TYPE.valueOf("SCALAR"));
        assertEquals(OID_TYPE.NODE, OID_TYPE.valueOf("NODE"));
        assertEquals(OID_TYPE.TABLE, OID_TYPE.valueOf("TABLE"));
        assertEquals(OID_TYPE.ROW, OID_TYPE.valueOf("ROW"));
    }
}
