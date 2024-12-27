package us.dot.its.jpo.ode.api.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLineStopEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

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

    public static List<StopLineStopReportData> aggregateStopLineStopEvents(List<StopLineStopEvent> events) {
        Map<Integer, StopLineStopReportData> reportDataMap = new HashMap<>();

        for (StopLineStopEvent event : events) {
            int signalGroup = event.getSignalGroup();
            StopLineStopReportData data = reportDataMap.getOrDefault(signalGroup, new StopLineStopReportData());
            data.setSignalGroup(signalGroup);
            data.setNumberOfEvents(data.getNumberOfEvents() + 1);
            data.setTimeStoppedOnRed(data.getTimeStoppedOnRed() + event.getTimeStoppedDuringRed());
            data.setTimeStoppedOnYellow(data.getTimeStoppedOnYellow() + event.getTimeStoppedDuringYellow());
            data.setTimeStoppedOnGreen(data.getTimeStoppedOnGreen() + event.getTimeStoppedDuringGreen());
            data.setTimeStoppedOnDark(data.getTimeStoppedOnDark() + event.getTimeStoppedDuringDark());
            reportDataMap.put(signalGroup, data);
        }

        return new ArrayList<>(reportDataMap.values());
    }
}