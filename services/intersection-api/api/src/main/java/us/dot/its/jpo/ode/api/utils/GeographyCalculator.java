package us.dot.its.jpo.ode.api.utils;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class GeographyCalculator {
    /**
     * Calculate the latitude range for a given center point and distance
     * 
     * @param centerLng the center longitude
     * @param centerLat the center latitude
     * @param distance  the distance in meters
     * @return double[] containing the min and max latitudes
     */
    @Deprecated
    public static double[] calculateLatitudes(double centerLng, double centerLat, double distance) {
        GeodeticCalculator calculator = new GeodeticCalculator();
        calculator.setStartingGeographicPoint(centerLng, centerLat);

        calculator.setDirection(0, distance);
        double maxLat = calculator.getDestinationGeographicPoint().getY();

        calculator.setDirection(180, distance);
        double minLat = calculator.getDestinationGeographicPoint().getY();

        return new double[] { minLat, maxLat };
    }

    /**
     * Calculate the longitude range for a given center point and distance
     * 
     * @param centerLng the center longitude
     * @param centerLat the center latitude
     * @param distance  the distance in meters
     * @return double[] containing the min and max longitudes
     */
    @Deprecated
    public static double[] calculateLongitudes(double centerLng, double centerLat, double distance) {
        GeodeticCalculator calculator = new GeodeticCalculator();
        calculator.setStartingGeographicPoint(centerLng, centerLat);

        calculator.setDirection(90, distance);
        double maxLng = calculator.getDestinationGeographicPoint().getX();

        calculator.setDirection(270, distance);
        double minLng = calculator.getDestinationGeographicPoint().getX();

        return new double[] { minLng, maxLng };
    }

    /**
     * Calculate a bounding box for a given center point and distance.
     *
     * @param centerLng the center longitude
     * @param centerLat the center latitude
     * @param distance  the distance in meters
     * @return double[] containing [minLat, maxLat, minLng, maxLng]
     */
    public static ReferencedEnvelope calculateBoundingBox(double centerLng, double centerLat, double distance) {
        GeodeticCalculator calculator = new GeodeticCalculator();
        calculator.setStartingGeographicPoint(centerLng, centerLat);

        // North (0°)
        calculator.setDirection(0, distance);
        double maxLat = calculator.getDestinationGeographicPoint().getY();

        // South (180°)
        calculator.setDirection(180, distance);
        double minLat = calculator.getDestinationGeographicPoint().getY();

        // East (90°)
        calculator.setDirection(90, distance);
        double maxLng = calculator.getDestinationGeographicPoint().getX();

        // West (270°)
        calculator.setDirection(270, distance);
        double minLng = calculator.getDestinationGeographicPoint().getX();

        // return new double[] { minLat, maxLat, minLng, maxLng };
        CoordinateReferenceSystem crs;
        try {
            crs = CRS.decode("EPSG:4326", true);
            return new ReferencedEnvelope(minLng, maxLng, minLat, maxLat, crs);
        } catch (NoSuchAuthorityCodeException e) {
            e.printStackTrace();
        } catch (FactoryException e) {
            e.printStackTrace();
        }
        return null;
    }
}
