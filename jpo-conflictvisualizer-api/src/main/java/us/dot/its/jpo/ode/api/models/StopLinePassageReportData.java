package us.dot.its.jpo.ode.api.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLinePassageEvent;

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

    public static List<StopLinePassageReportData> aggregateSignalStateEvents(List<StopLinePassageEvent> events) {
        Map<Integer, StopLinePassageReportData> reportDataMap = new HashMap<>();
    
        for (StopLinePassageEvent event : events) {
            int signalGroup = event.getSignalGroup();
            StopLinePassageReportData data = reportDataMap.getOrDefault(signalGroup, new StopLinePassageReportData());
            data.setSignalGroup(signalGroup);
            data.setTotalEvents(data.getTotalEvents() + 1);
    
            switch(event.getEventState()) {
                case UNAVAILABLE:
                case DARK:
                    data.setDarkEvents(data.getDarkEvents() + 1);
                    break;
                case STOP_THEN_PROCEED:
                case STOP_AND_REMAIN:
                    data.setRedEvents(data.getRedEvents() + 1);
                    break;
                case PRE_MOVEMENT:
                case PERMISSIVE_MOVEMENT_ALLOWED:
                case PROTECTED_MOVEMENT_ALLOWED:
                    data.setGreenEvents(data.getGreenEvents() + 1);
                    break;
                case PERMISSIVE_CLEARANCE:
                case PROTECTED_CLEARANCE:
                case CAUTION_CONFLICTING_TRAFFIC:
                    data.setYellowEvents(data.getYellowEvents() + 1);
                    break;
                default:
                    break;
            }
    
            reportDataMap.put(signalGroup, data);
        }
    
        return new ArrayList<>(reportDataMap.values());
    }
}