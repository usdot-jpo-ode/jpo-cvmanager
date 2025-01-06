package us.dot.its.jpo.ode.api.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ConnectionData {
    private String connectionID;
    private int ingressLaneID;
    private int egressLaneID;
    private int eventCount;

    public ConnectionData(String connectionID, int ingressLaneID, int egressLaneID, int eventCount) {
        this.connectionID = connectionID;
        this.ingressLaneID = ingressLaneID;
        this.egressLaneID = egressLaneID;
        this.eventCount = eventCount;
    }
}