package us.dot.its.jpo.rsustatusmonitor.models.postgres.tables;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import static org.junit.jupiter.api.Assertions.*;

public class RsusTest {

    @Test
    public void testGettersAndSetters() {
        Rsus rsu = new Rsus();
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(1.0, 2.0));
        
        rsu.setRsu_id(1);
        rsu.setGeography(point);
        rsu.setMilepost(12.5f);
        rsu.setIpv4_address("192.168.1.1");
        rsu.setSerial_number("SN12345");
        rsu.setIss_scms_id("SCMS001");
        rsu.setPrimary_route("Route 66");
        rsu.setModel(1);
        rsu.setCredential_id(10);
        rsu.setSnmp_credential_id(20);
        rsu.setSnmp_protocol_id(30);
        rsu.setFirmware_version(100);
        rsu.setTarget_firmware_version(101);
        
        assertEquals(1, rsu.getRsu_id());
        assertEquals(point, rsu.getGeography());
        assertEquals(12.5f, rsu.getMilepost());
        assertEquals("192.168.1.1", rsu.getIpv4_address());
        assertEquals("SN12345", rsu.getSerial_number());
        assertEquals("SCMS001", rsu.getIss_scms_id());
        assertEquals("Route 66", rsu.getPrimary_route());
        assertEquals(1, rsu.getModel());
        assertEquals(10, rsu.getCredential_id());
        assertEquals(20, rsu.getSnmp_credential_id());
        assertEquals(30, rsu.getSnmp_protocol_id());
        assertEquals(100, rsu.getFirmware_version());
        assertEquals(101, rsu.getTarget_firmware_version());
    }

    @Test
    public void testEqualsAndHashCode() {
        Rsus rsu1 = new Rsus();
        rsu1.setRsu_id(1);
        rsu1.setIpv4_address("192.168.1.1");
        
        Rsus rsu2 = new Rsus();
        rsu2.setRsu_id(1);
        rsu2.setIpv4_address("192.168.1.1");
        
        assertEquals(rsu1, rsu2);
        assertEquals(rsu1.hashCode(), rsu2.hashCode());
    }

    @Test
    public void testToString() {
        Rsus rsu = new Rsus();
        rsu.setRsu_id(1);
        
        String result = rsu.toString();
        assertNotNull(result);
    }
}
