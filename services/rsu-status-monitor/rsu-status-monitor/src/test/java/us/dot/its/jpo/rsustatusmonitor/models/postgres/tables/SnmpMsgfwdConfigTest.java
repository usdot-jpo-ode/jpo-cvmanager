package us.dot.its.jpo.rsustatusmonitor.models.postgres.tables;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SnmpMsgfwdConfigTest {

    @Test
    public void testGettersAndSetters() {
        SnmpMsgfwdConfig config = new SnmpMsgfwdConfig();
        LocalDateTime startTime = LocalDateTime.of(2025, 11, 21, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 11, 21, 12, 0);
        
        config.setRsu_id(1);
        config.setMsgfwd_type(2);
        config.setSnmp_index(3);
        config.setMessage_type("BSM");
        config.setDest_ipv4("192.168.1.1");
        config.setDest_port(8080);
        config.setStart_datetime(startTime);
        config.setEnd_datetime(endTime);
        config.setActive(true);
        config.setSecurity(false);
        
        assertEquals(1, config.getRsu_id());
        assertEquals(2, config.getMsgfwd_type());
        assertEquals(3, config.getSnmp_index());
        assertEquals("BSM", config.getMessage_type());
        assertEquals("192.168.1.1", config.getDest_ipv4());
        assertEquals(8080, config.getDest_port());
        assertEquals(startTime, config.getStart_datetime());
        assertEquals(endTime, config.getEnd_datetime());
        assertTrue(config.isActive());
        assertFalse(config.isSecurity());
    }

    @Test
    public void testEqualsAndHashCode() {
        SnmpMsgfwdConfig config1 = new SnmpMsgfwdConfig();
        config1.setRsu_id(1);
        config1.setMsgfwd_type(2);
        config1.setSnmp_index(3);
        
        SnmpMsgfwdConfig config2 = new SnmpMsgfwdConfig();
        config2.setRsu_id(1);
        config2.setMsgfwd_type(2);
        config2.setSnmp_index(3);
        
        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    public void testToString() {
        SnmpMsgfwdConfig config = new SnmpMsgfwdConfig();
        config.setRsu_id(1);
        
        String result = config.toString();
        assertNotNull(result);
    }
}
