package us.dot.its.jpo.ode.mockdata;

import java.time.Instant;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.BsmMessageCountProgressionEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.EventStateProgressionEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.MapMessageCountProgressionEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.ProcessingTimePeriod;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateConflictEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SpatMessageCountProgressionEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.MapMinimumDataEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.SpatMinimumDataEventAggregation;
import us.dot.its.jpo.ode.plugin.j2735.J2735MovementPhaseState;

public class MockAggregatedEventGenerator {

    public static ProcessingTimePeriod getProcessingTimePeriod() {
        ProcessingTimePeriod period = new ProcessingTimePeriod();
        long now = Instant.now().toEpochMilli() - 600000;
        period.setBeginTimestamp(now - 600000);
        period.setEndTimestamp(now);
        return period;
    }

    public static SpatMinimumDataEventAggregation getSpatMinimumDataEventAggregation() {
        SpatMinimumDataEventAggregation event = new SpatMinimumDataEventAggregation();
        event.setRoadRegulatorID(-1);
        event.setIntersectionID(2);
        event.setNumberOfEvents(53);
        event.setTimePeriod(getProcessingTimePeriod());
        event.setSource("{\"intersectionID\": 2, \"roadRegulatorID\": -1, \"originIp\": \"1.1.1.1\"}");
        return event;
    }

    public static MapMinimumDataEventAggregation getMapMinimumDataEventAggregation() {
        MapMinimumDataEventAggregation event = new MapMinimumDataEventAggregation();
        event.setRoadRegulatorID(-1);
        event.setIntersectionID(2);
        event.setNumberOfEvents(53);
        event.setTimePeriod(getProcessingTimePeriod());
        event.setSource("{\"intersectionID\": 2, \"roadRegulatorID\": -1, \"originIp\": \"1.1.1.1\"}");
        return event;
    }

    public static IntersectionReferenceAlignmentEventAggregation getIntersectionReferenceAlignmentEventAggregation() {
        IntersectionReferenceAlignmentEventAggregation event = new IntersectionReferenceAlignmentEventAggregation();
        event.setRoadRegulatorID(-1);
        event.setIntersectionID(2);
        event.setNumberOfEvents(53);
        event.setTimePeriod(getProcessingTimePeriod());
        event.setSource("{\"intersectionID\": 2, \"roadRegulatorID\": -1, \"originIp\": \"1.1.1.1\"}");
        return event;
    }

    public static SignalGroupAlignmentEventAggregation getSignalGroupAlignmentEventAggregation() {
        SignalGroupAlignmentEventAggregation event = new SignalGroupAlignmentEventAggregation();
        event.setRoadRegulatorID(-1);
        event.setIntersectionID(2);
        event.setNumberOfEvents(53);
        event.setTimePeriod(getProcessingTimePeriod());
        event.setSource("{\"intersectionID\": 2, \"roadRegulatorID\": -1, \"originIp\": \"1.1.1.1\"}");
        return event;
    }

    public static SignalStateConflictEventAggregation getSignalStateConflictEventAggregation() {
        SignalStateConflictEventAggregation event = new SignalStateConflictEventAggregation();
        event.setRoadRegulatorID(-1);
        event.setIntersectionID(2);
        event.setNumberOfEvents(53);
        event.setTimePeriod(getProcessingTimePeriod());
        event.setSource("{\"intersectionID\": 2, \"roadRegulatorID\": -1, \"originIp\": \"1.1.1.1\"}");
        return event;
    }

    public static TimeChangeDetailsEventAggregation getTimeChangeDetailsEventAggregation() {
        TimeChangeDetailsEventAggregation event = new TimeChangeDetailsEventAggregation();
        event.setRoadRegulatorID(-1);
        event.setIntersectionID(2);
        event.setNumberOfEvents(53);
        event.setTimePeriod(getProcessingTimePeriod());
        event.setSource("{\"intersectionID\": 2, \"roadRegulatorID\": -1, \"originIp\": \"1.1.1.1\"}");
        event.setEventStateA(J2735MovementPhaseState.PROTECTED_MOVEMENT_ALLOWED);
        event.setEventStateB(J2735MovementPhaseState.STOP_AND_REMAIN);
        event.setTimeMarkTypeA(null);
        return event;
    }

    public static EventStateProgressionEventAggregation getEventStateProgressionEventAggregation() {
        EventStateProgressionEventAggregation event = new EventStateProgressionEventAggregation();
        event.setRoadRegulatorID(-1);
        event.setIntersectionID(2);
        event.setNumberOfEvents(53);
        event.setTimePeriod(getProcessingTimePeriod());
        event.setSource("{\"intersectionID\": 2, \"roadRegulatorID\": -1, \"originIp\": \"1.1.1.1\"}");
        event.setEventStateA(J2735MovementPhaseState.PROTECTED_MOVEMENT_ALLOWED);
        event.setEventStateB(J2735MovementPhaseState.STOP_AND_REMAIN);
        event.setSignalGroupID(5);
        return event;
    }

    public static BsmMessageCountProgressionEventAggregation getBsmMessageCountProgressionEventAggregation() {
        BsmMessageCountProgressionEventAggregation event = new BsmMessageCountProgressionEventAggregation();
        event.setRoadRegulatorID(-1);
        event.setIntersectionID(2);
        event.setNumberOfEvents(53);
        event.setTimePeriod(getProcessingTimePeriod());
        event.setSource("{\"intersectionID\": 2, \"roadRegulatorID\": -1, \"originIp\": \"1.1.1.1\"}");
        event.setChange(null);
        event.setDataFrame(null);
        return event;
    }

    public static MapMessageCountProgressionEventAggregation getMapMessageCountProgressionEventAggregation() {
        MapMessageCountProgressionEventAggregation event = new MapMessageCountProgressionEventAggregation();
        event.setRoadRegulatorID(-1);
        event.setIntersectionID(2);
        event.setNumberOfEvents(53);
        event.setTimePeriod(getProcessingTimePeriod());
        event.setSource("{\"intersectionID\": 2, \"roadRegulatorID\": -1, \"originIp\": \"1.1.1.1\"}");
        event.setChange(null);
        event.setDataFrame(null);
        return event;
    }

    public static SpatMessageCountProgressionEventAggregation getSpatMessageCountProgressionEventAggregation() {
        SpatMessageCountProgressionEventAggregation event = new SpatMessageCountProgressionEventAggregation();
        event.setRoadRegulatorID(-1);
        event.setIntersectionID(2);
        event.setNumberOfEvents(53);
        event.setTimePeriod(getProcessingTimePeriod());
        event.setSource("{\"intersectionID\": 2, \"roadRegulatorID\": -1, \"originIp\": \"1.1.1.1\"}");
        event.setChange(null);
        event.setDataFrame(null);
        return event;
    }

}
