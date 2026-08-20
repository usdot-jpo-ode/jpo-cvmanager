package us.dot.its.jpo.rsustatusmonitor.models.postgres.keys;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SnmpMsgfwdConfigIdTest {

    @Test
    public void testNoArgsConstructor() {
        SnmpMsgfwdConfigId id = new SnmpMsgfwdConfigId();
        assertNotNull(id);
    }

    @Test
    public void testAllArgsConstructor() {
        SnmpMsgfwdConfigId id = new SnmpMsgfwdConfigId(1, 2, 3);
        assertNotNull(id);
    }

    @Test
    public void testEqualsAndHashCode() {
        SnmpMsgfwdConfigId id1 = new SnmpMsgfwdConfigId(1, 2, 3);
        SnmpMsgfwdConfigId id2 = new SnmpMsgfwdConfigId(1, 2, 3);
        SnmpMsgfwdConfigId id3 = new SnmpMsgfwdConfigId(4, 5, 6);
        
        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertEquals(id1.hashCode(), id2.hashCode());
    }
}
