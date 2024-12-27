package us.dot.its.jpo.ode.api.models;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Arrays;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.connectinglanes.ConnectingLanesFeatureCollection;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;

public class ConnectionOfTravelData {
    private List<ConnectionData> validConnections;
    private List<ConnectionData> invalidConnections;

    public ConnectionOfTravelData(List<ConnectionData> validConnections, List<ConnectionData> invalidConnections) {
        this.validConnections = validConnections;
        this.invalidConnections = invalidConnections;
    }

    public List<ConnectionData> getValidConnections() {
        return validConnections;
    }

    public List<ConnectionData> getInvalidConnections() {
        return invalidConnections;
    }

    public static ConnectionOfTravelData processConnectionOfTravelData(List<LaneConnectionCount> connectionCounts, ProcessedMap<LineString> mostRecentProcessedMap) {
        // Get the valid connections from the most recent processed map
        ConnectingLanesFeatureCollection<LineString> connectingLanesFeatureCollection = mostRecentProcessedMap.getConnectingLanesFeatureCollection();
        Set<String> validConnectionIDs = Arrays.stream(connectingLanesFeatureCollection.getFeatures())
            .map(feature -> feature.getId())
            .collect(Collectors.toSet());

        List<ConnectionData> validConnections = new ArrayList<>();
        List<ConnectionData> invalidConnections = new ArrayList<>();

        for (LaneConnectionCount count : connectionCounts) {
            String key = count.getIngressLaneID() + "-" + count.getEgressLaneID();
            ConnectionData connectionData = new ConnectionData(key, count.getIngressLaneID(), count.getEgressLaneID(), count.getCount());

            if (validConnectionIDs.contains(key)) {
                validConnections.add(connectionData);
            } else {
                invalidConnections.add(connectionData);
            }
        }

        return new ConnectionOfTravelData(validConnections, invalidConnections);
    }
}