package us.dot.its.jpo.ode.api.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.data.annotation.Id;

import lombok.EqualsAndHashCode;
import java.util.List;
import java.util.Map;

@ToString
@Setter
@EqualsAndHashCode
@Getter
public class ReportDocument {
    
    @Id
    private String reportName;
    private int intersectionID;
    private String roadRegulatorID;
    private long reportGeneratedAt;
    private long reportStartTime;
    private long reportStopTime;
    private byte[] reportContents; 
    private List<IDCount> laneDirectionOfTravelEventCounts;
    private List<IDCount> laneDirectionOfTravelMedianDistanceDistribution;
    private List<IDCount> laneDirectionOfTravelMedianHeadingDistribution;
    private List<LaneDirectionOfTravelReportData> laneDirectionOfTravelReportData;
    private double headingTolerance;
    private double distanceTolerance;
    private List<Map<String, Object>> validConnectionOfTravelData;
    private List<Map<String, Object>> invalidConnectionOfTravelData;
    private List<IDCount> connectionOfTravelEventCounts;
    private List<IDCount> signalStateConflictEventCount;
    private List<IDCount> signalStateEventCounts;
    private List<IDCount> signalStateStopEventCounts;
    private List<IDCount> timeChangeDetailsEventCount;
    private List<IDCount> intersectionReferenceAlignmentEventCounts;
    private List<IDCount> mapBroadcastRateEventCount;
    private List<IDCount> mapMinimumDataEventCount;
    private List<IDCount> spatMinimumDataEventCount;
    private List<IDCount> spatBroadcastRateEventCount;
    private List<String> latestMapMinimumDataEventMissingElements;
    private List<String> latestSpatMinimumDataEventMissingElements;
}