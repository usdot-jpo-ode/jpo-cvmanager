package us.dot.its.jpo.ode.api.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StopLinePassageReportData {
    private int signalGroup;
    private int totalEvents;
    private int redEvents;
    private int yellowEvents;
    private int greenEvents;
    private int darkEvents;
}