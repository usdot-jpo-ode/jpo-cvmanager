package us.dot.its.jpo.rsustatusmonitor.models.postgres.tables;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SnmpMsgfwdTypeTest {

    @Test
    public void testGettersAndSetters() {
        SnmpMsgfwdType type = new SnmpMsgfwdType();
        
        type.setSnmp_msgfwd_type_id(1);
        type.setName("BSM");
        
        assertEquals(1, type.getSnmp_msgfwd_type_id());
        assertEquals("BSM", type.getName());
    }

    @Test
    public void testEqualsAndHashCode() {
        SnmpMsgfwdType type1 = new SnmpMsgfwdType();
        type1.setSnmp_msgfwd_type_id(1);
        type1.setName("BSM");
        
        SnmpMsgfwdType type2 = new SnmpMsgfwdType();
        type2.setSnmp_msgfwd_type_id(1);
        type2.setName("BSM");
        
        assertEquals(type1, type2);
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    @Test
    public void testToString() {
        SnmpMsgfwdType type = new SnmpMsgfwdType();
        type.setSnmp_msgfwd_type_id(1);
        
        String result = type.toString();
        assertNotNull(result);
    }
}
