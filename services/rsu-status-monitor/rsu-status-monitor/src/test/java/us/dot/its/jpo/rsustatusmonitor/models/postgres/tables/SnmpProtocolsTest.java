package us.dot.its.jpo.rsustatusmonitor.models.postgres.tables;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SnmpProtocolsTest {

    @Test
    public void testGettersAndSetters() {
        SnmpProtocols protocol = new SnmpProtocols();
        
        protocol.setSnmp_protocol_id(1);
        protocol.setProtocol_code("SHA");
        protocol.setNickname("Secure Hash");
        
        assertEquals(1, protocol.getSnmp_protocol_id());
        assertEquals("SHA", protocol.getProtocol_code());
        assertEquals("Secure Hash", protocol.getNickname());
    }

    @Test
    public void testEqualsAndHashCode() {
        SnmpProtocols protocol1 = new SnmpProtocols();
        protocol1.setSnmp_protocol_id(1);
        protocol1.setProtocol_code("SHA");
        
        SnmpProtocols protocol2 = new SnmpProtocols();
        protocol2.setSnmp_protocol_id(1);
        protocol2.setProtocol_code("SHA");
        
        assertEquals(protocol1, protocol2);
        assertEquals(protocol1.hashCode(), protocol2.hashCode());
    }

    @Test
    public void testToString() {
        SnmpProtocols protocol = new SnmpProtocols();
        protocol.setSnmp_protocol_id(1);
        
        String result = protocol.toString();
        assertNotNull(result);
    }
}
