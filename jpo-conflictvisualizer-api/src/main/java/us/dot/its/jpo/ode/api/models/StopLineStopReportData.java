package us.dot.its.jpo.ode.api.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StopLineStopReportData {
    private int signalGroup;
    private int numberOfEvents;
    private double timeStoppedOnRed;
    private double timeStoppedOnYellow;
    private double timeStoppedOnGreen;
    private double timeStoppedOnDark;
}