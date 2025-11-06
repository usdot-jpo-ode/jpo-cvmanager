package us.dot.its.jpo.ode.mockdata;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.ode.api.models.haas.HaasLocation;
import us.dot.its.jpo.ode.api.models.haas.HaasLocation.ThingReference;
import us.dot.its.jpo.ode.api.models.haas.HaasLocation.Feature;
import us.dot.its.jpo.ode.api.models.haas.HaasLocation.Geometry;

@Slf4j
public class MockHaasGenerator {

    public static List<HaasLocation> getHaasLocations() {
        List<HaasLocation> locations = new ArrayList<>();

        HaasLocation location = new HaasLocation();
        location.setId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        location.setType("location");
        location.setDetailedType("emergency");
        location.setExternalId("000000000000000000000000-2025-02-25T20:35:26.155Z");
        location.setStartTime("2025-02-25T20:35:26.36Z");
        location.setEndTime("2025-02-25T20:48:58.01Z");
        location.setLat(10.00);
        location.setLon(-100.00);
        location.setAlt(1483.67);
        location.setStreetName("10, -100.00");
        location.setLocationType("emergency");
        location.setActive(false);

        // Create ThingReference for things_active
        ThingReference activeThing = new ThingReference();
        activeThing.setStartTime("2025-02-25T20:35:26.36Z");
        activeThing.setEndTime("2025-02-25T20:48:57.97Z");
        activeThing.setExternalId("000000000000000000000000");
        activeThing.setId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        // Create ThingReference for things_inactive
        ThingReference inactiveThing = new ThingReference();
        inactiveThing.setStartTime("2025-02-25T20:35:26.36Z");
        inactiveThing.setEndTime("2025-02-25T20:48:57.97Z");
        inactiveThing.setExternalId("000000000000000000000000");
        inactiveThing.setId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        List<ThingReference> thingsActive = new ArrayList<>();
        thingsActive.add(activeThing);
        location.setThingsActive(thingsActive);

        List<ThingReference> thingsInactive = new ArrayList<>();
        thingsInactive.add(inactiveThing);
        location.setThingsInactive(thingsInactive);

        // Create Feature
        Feature feature = new Feature();
        feature.setType("point");
        List<Geometry> geometries = new ArrayList<>();
        Geometry geometry = new Geometry();
        geometry.setLon(-100.00);
        geometry.setLat(10.00);
        geometries.add(geometry);
        feature.setGeometry(geometries);

        List<Feature> features = new ArrayList<>();
        features.add(feature);
        location.setFeatures(features);

        locations.add(location);
        return locations;
    }
}