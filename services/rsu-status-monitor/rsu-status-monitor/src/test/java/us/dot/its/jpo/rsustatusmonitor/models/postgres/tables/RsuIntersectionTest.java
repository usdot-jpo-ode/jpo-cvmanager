package us.dot.its.jpo.rsustatusmonitor.models.postgres.tables;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RsuIntersectionTest {

    @Test
    public void testGettersAndSetters() {
        RsuIntersection rsuIntersection = new RsuIntersection();
        
        rsuIntersection.setRsu_intersection_id(1);
        rsuIntersection.setRsu_id(100);
        rsuIntersection.setIntersection_id(200);
        
        assertEquals(1, rsuIntersection.getRsu_intersection_id());
        assertEquals(100, rsuIntersection.getRsu_id());
        assertEquals(200, rsuIntersection.getIntersection_id());
    }

    @Test
    public void testEqualsAndHashCode() {
        RsuIntersection rsuIntersection1 = new RsuIntersection();
        rsuIntersection1.setRsu_intersection_id(1);
        rsuIntersection1.setRsu_id(100);
        
        RsuIntersection rsuIntersection2 = new RsuIntersection();
        rsuIntersection2.setRsu_intersection_id(1);
        rsuIntersection2.setRsu_id(100);
        
        assertEquals(rsuIntersection1, rsuIntersection2);
        assertEquals(rsuIntersection1.hashCode(), rsuIntersection2.hashCode());
    }

    @Test
    public void testToString() {
        RsuIntersection rsuIntersection = new RsuIntersection();
        rsuIntersection.setRsu_intersection_id(1);
        
        String result = rsuIntersection.toString();
        assertNotNull(result);
    }
}
