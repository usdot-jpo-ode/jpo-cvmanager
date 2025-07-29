package us.dot.its.jpo.ode.api.converters;

import us.dot.its.jpo.ode.api.models.haas.HaasLocation;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import us.dot.its.jpo.ode.api.models.geojson.GeoJsonFeature;
import us.dot.its.jpo.ode.api.models.geojson.GeoJsonFeatureCollection;

import java.util.List;
import java.util.stream.Collectors;

public class HaasLocationConverter {

    /**
     * Converts a list of HaasLocation objects to a GeoJsonFeatureCollection.
     * Each HaasLocation is represented as a GeoJsonFeature with a GeoJsonPoint
     * geometry and the HaasLocation object as properties.
     *
     * @param locations The list of HaasLocation objects to convert.
     * @return A GeoJsonFeatureCollection containing the converted features.
     */
    public static GeoJsonFeatureCollection toGeoJson(List<HaasLocation> locations) {
        List<GeoJsonFeature> features = locations.stream()
                .map(location -> {
                    GeoJsonPoint geometry = new GeoJsonPoint(location.getLon(), location.getLat());

                    GeoJsonFeature feature = new GeoJsonFeature();
                    feature.setGeometry(geometry);
                    feature.setProperties(location);
                    return feature;
                })
                .collect(Collectors.toList());

        GeoJsonFeatureCollection collection = new GeoJsonFeatureCollection();
        collection.setFeatures(features);
        return collection;
    }
}