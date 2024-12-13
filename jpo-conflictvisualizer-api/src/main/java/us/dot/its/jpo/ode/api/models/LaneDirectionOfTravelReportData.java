package us.dot.its.jpo.ode.api.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LaneDirectionOfTravelReportData {
    private long timestamp;
    private int laneID;
    private int segmentID;
    private double headingDelta;
    private double medianCenterlineDistance;
}