package us.dot.its.jpo.rsustatusmonitor.models.snmp;

import org.junit.jupiter.api.Test;
import us.dot.its.jpo.rsustatusmonitor.models.postgres.derived.RsuSnmpCredentials;

import static org.junit.jupiter.api.Assertions.*;

public class RsuMsgfwdValuesTest {

    @Test
    public void testNoArgsConstructor() {
        RsuMsgfwdValues values = new RsuMsgfwdValues();
        assertNotNull(values);
    }

    @Test
    public void testAllArgsConstructor() {
        RsuSnmpCredentials credentials = new RsuSnmpCredentials(
            1, "192.168.1.1", "user", "pass", "encrypt", "SHA", "12345"
        );
        
        RsuMsgfwdValues values = new RsuMsgfwdValues(
            credentials, "psid123", "10.0.0.1", 8080, 1, -50, 1000,
            "07e9010100000000", "07e9123123595959", 1, 1, 500
        );
        
        assertEquals(credentials, values.getRsuSnmpCredentials());
        assertEquals("psid123", values.getRsuReceivedMsgPsid());
        assertEquals("10.0.0.1", values.getRsuReceivedMsgDestIpAddr());
        assertEquals(8080, values.getRsuReceivedMsgDestPort());
        assertEquals(1, values.getRsuReceivedMsgProtocol());
        assertEquals(-50, values.getRsuReceivedMsgRssi());
        assertEquals(1000, values.getRsuReceivedMsgInterval());
        assertEquals("07e9010100000000", values.getRsuReceivedMsgDeliveryStart());
        assertEquals("07e9123123595959", values.getRsuReceivedMsgDeliveryStop());
        assertEquals(1, values.getRsuReceivedMsgStatus());
        assertEquals(1, values.getRsuReceivedMsgSecure());
        assertEquals(500, values.getRsuReceivedMsgAuthMsgInterval());
    }

    @Test
    public void testGettersAndSetters() {
        RsuMsgfwdValues values = new RsuMsgfwdValues();
        RsuSnmpCredentials credentials = new RsuSnmpCredentials(
            1, "192.168.1.1", "user", "pass", "encrypt", "SHA", "12345"
        );
        
        values.setRsuSnmpCredentials(credentials);
        values.setRsuReceivedMsgPsid("psid456");
        values.setRsuReceivedMsgDestIpAddr("192.168.2.2");
        values.setRsuReceivedMsgDestPort(9090);
        values.setRsuReceivedMsgProtocol(2);
        values.setRsuReceivedMsgRssi(-60);
        values.setRsuReceivedMsgInterval(2000);
        values.setRsuReceivedMsgDeliveryStart("07e9020200000000");
        values.setRsuReceivedMsgDeliveryStop("07e9121231235959");
        values.setRsuReceivedMsgStatus(2);
        values.setRsuReceivedMsgSecure(0);
        values.setRsuReceivedMsgAuthMsgInterval(1000);
        
        assertEquals(credentials, values.getRsuSnmpCredentials());
        assertEquals("psid456", values.getRsuReceivedMsgPsid());
        assertEquals("192.168.2.2", values.getRsuReceivedMsgDestIpAddr());
        assertEquals(9090, values.getRsuReceivedMsgDestPort());
        assertEquals(2, values.getRsuReceivedMsgProtocol());
        assertEquals(-60, values.getRsuReceivedMsgRssi());
        assertEquals(2000, values.getRsuReceivedMsgInterval());
        assertEquals("07e9020200000000", values.getRsuReceivedMsgDeliveryStart());
        assertEquals("07e9121231235959", values.getRsuReceivedMsgDeliveryStop());
        assertEquals(2, values.getRsuReceivedMsgStatus());
        assertEquals(0, values.getRsuReceivedMsgSecure());
        assertEquals(1000, values.getRsuReceivedMsgAuthMsgInterval());
    }

    @Test
    public void testEqualsAndHashCode() {
        RsuSnmpCredentials credentials = new RsuSnmpCredentials(
            1, "192.168.1.1", "user", "pass", "encrypt", "SHA", "12345"
        );
        
        RsuMsgfwdValues values1 = new RsuMsgfwdValues(
            credentials, "psid123", "10.0.0.1", 8080, 1, -50, 1000,
            "07e9010100000000", "07e9123123595959", 1, 1, 500
        );
        
        RsuMsgfwdValues values2 = new RsuMsgfwdValues(
            credentials, "psid123", "10.0.0.1", 8080, 1, -50, 1000,
            "07e9010100000000", "07e9123123595959", 1, 1, 500
        );
        
        assertEquals(values1, values2);
        assertEquals(values1.hashCode(), values2.hashCode());
    }

    @Test
    public void testToString() {
        RsuMsgfwdValues values = new RsuMsgfwdValues();
        values.setRsuReceivedMsgPsid("psid123");
        
        String result = values.toString();
        assertNotNull(result);
        assertTrue(result.contains("psid123"));
    }
}
