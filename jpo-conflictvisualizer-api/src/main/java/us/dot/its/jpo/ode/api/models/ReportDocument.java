package us.dot.its.jpo.ode.api.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.data.annotation.Id;

import lombok.EqualsAndHashCode;
import java.util.List;

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
    private List<IDCount> mapBroadcastRateEventCount;
    private List<IDCount> mapMinimumDataEventCount;
    private List<IDCount> timeChangeDetailsEventCount;
    private List<IDCount> spatMinimumDataEventCount;
    private List<IDCount> spatBroadcastRateEventCount;
    private List<IDCount> signalStateConflictEventCount;
}