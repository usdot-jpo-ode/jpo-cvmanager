package us.dot.its.jpo.ode.api.converters;

import us.dot.its.jpo.ode.api.models.haas.websocket.HaasWebsocketLocation;
import us.dot.its.jpo.ode.api.models.geojson.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class HaasLocationConverter {
    public static GeoJsonFeatureCollection toGeoJson(List<HaasWebsocketLocation> locations) {
        List<GeoJsonFeature> features = locations.stream()
                .flatMap(location -> {
                    List<GeoJsonFeature> allFeatures = new ArrayList<>();

                    // Add main location point
                    GeoJsonFeature mainFeature = new GeoJsonFeature();
                    GeoJsonGeometry geometry = new GeoJsonGeometry();
                    geometry.setType("Point");
                    geometry.setCoordinates(new double[] { location.getLon(), location.getLat() });
                    mainFeature.setGeometry(geometry);

                    mainFeature.setProperties(location);
                    allFeatures.add(mainFeature);

                    return allFeatures.stream();
                })
                .collect(Collectors.toList());

        GeoJsonFeatureCollection collection = new GeoJsonFeatureCollection();
        collection.setFeatures(features);
        return collection;
    }
}