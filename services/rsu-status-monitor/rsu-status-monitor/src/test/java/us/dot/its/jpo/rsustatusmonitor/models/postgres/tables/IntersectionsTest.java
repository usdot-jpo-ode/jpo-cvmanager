package us.dot.its.jpo.rsustatusmonitor.models.postgres.tables;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import static org.junit.jupiter.api.Assertions.*;

public class IntersectionsTest {

    @Test
    public void testGettersAndSetters() {
        Intersections intersection = new Intersections();
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(1.0, 2.0));
        
        intersection.setIntersection_id(123);
        intersection.setIntersection_number("INT-001");
        intersection.setRef_pt(point);
        intersection.setBbox(point);
        intersection.setIntersection_name("Main St & 1st Ave");
        intersection.setOrigin_ip("192.168.1.1");
        
        assertEquals(123, intersection.getIntersection_id());
        assertEquals("INT-001", intersection.getIntersection_number());
        assertEquals(point, intersection.getRef_pt());
        assertEquals(point, intersection.getBbox());
        assertEquals("Main St & 1st Ave", intersection.getIntersection_name());
        assertEquals("192.168.1.1", intersection.getOrigin_ip());
    }

    @Test
    public void testEqualsAndHashCode() {
        Intersections intersection1 = new Intersections();
        intersection1.setIntersection_id(123);
        intersection1.setIntersection_name("Test");
        
        Intersections intersection2 = new Intersections();
        intersection2.setIntersection_id(123);
        intersection2.setIntersection_name("Test");
        
        assertEquals(intersection1, intersection2);
        assertEquals(intersection1.hashCode(), intersection2.hashCode());
    }

    @Test
    public void testToString() {
        Intersections intersection = new Intersections();
        intersection.setIntersection_id(123);
        
        String result = intersection.toString();
        assertNotNull(result);
    }
}
