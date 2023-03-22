package us.dot.its.jpo.ode.mockdata;

import java.time.ZonedDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.ConnectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.LaneDirectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.ProcessingTimePeriod;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateConflictEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateStopEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.MapBroadcastRateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.SpatBroadcastRateEvent;
import us.dot.its.jpo.ode.plugin.j2735.J2735MovementPhaseState;

public class MockEventGenerator {

    public static IntersectionReferenceAlignmentEvent getIntersectionReferenceAlignmentEvent(){
        IntersectionReferenceAlignmentEvent event = new IntersectionReferenceAlignmentEvent();
        event.setTimestamp(ZonedDateTime.now().toInstant().toEpochMilli());
        event.setMapIntersectionIds(Stream.of(1,2).collect(Collectors.toSet()));
        event.setSpatIntersectionIds(Stream.of(1,3).collect(Collectors.toSet()));
        event.setSpatRoadRegulatorIds(Stream.of(2,3).collect(Collectors.toSet()));
        event.setMapRoadRegulatorIds(Stream.of(2,4).collect(Collectors.toSet()));
        return event;
    }

    public static ConnectionOfTravelEvent getConnectionOfTravelEvent(){
        ConnectionOfTravelEvent event = new ConnectionOfTravelEvent();
        event.setTimestamp(ZonedDateTime.now().toInstant().toEpochMilli());
        event.setRoadRegulatorID(3);
        event.setIntersectionID(2);
        event.setIngressLaneID(2);
        event.setEgressLaneID(7);
        event.setConnectionID(3);
        return event;
    }

    public static LaneDirectionOfTravelEvent getLaneDirectionOfTravelEvent(){
        LaneDirectionOfTravelEvent event = new LaneDirectionOfTravelEvent();
        event.setTimestamp(ZonedDateTime.now().toInstant().toEpochMilli());
        event.setExpectedHeading(55);
        event.setRoadRegulatorID(2);
        event.setIntersectionID(3);
        event.setLaneID(5);
        event.setLaneSegmentNumber(1);
        event.setLaneSegmentFinalLatitude(55.000001);
        event.setLaneSegmentInitialLatitude(55);
        event.setLaneSegmentFinalLongitude(-104.000001);
        event.setLaneSegmentInitialLongitude(-104);
        event.setMedianDistanceFromCenterline(22);
        event.setAggregateBSMCount(4);
        return event;
    }

    public static SignalGroupAlignmentEvent getSignalGroupAlignmentEvent(){
        SignalGroupAlignmentEvent event = new SignalGroupAlignmentEvent();
        event.setSourceID("Made in China");
        event.setTimestamp(ZonedDateTime.now().toInstant().toEpochMilli());
        event.setSpatSignalGroupIds(Stream.of(1,6).collect(Collectors.toSet()));
        event.setMapSignalGroupIds(Stream.of(2,7).collect(Collectors.toSet()));
        return event;
    }

    public static SignalStateConflictEvent getSignalStateConflictEvent(){
        SignalStateConflictEvent event = new SignalStateConflictEvent();
        event.setTimestamp(ZonedDateTime.now().toInstant().toEpochMilli());
        event.setRoadRegulatorID(5);
        event.setIntersectionID(2);
        event.setConflictType(J2735MovementPhaseState.DARK);
        event.setFirstConflictingSignalGroup(2);
        event.setFirstConflictingSignalState(J2735MovementPhaseState.PROTECTED_CLEARANCE);
        event.setSecondConflictingSignalGroup(2);
        event.setSecondConflictingSignalState(J2735MovementPhaseState.PRE_MOVEMENT);
        return event;
    }

    public static SignalStateEvent getSignalStateEvent(){
        SignalStateEvent event = new SignalStateEvent();
        event.setTimestamp(ZonedDateTime.now().toInstant().toEpochMilli());
        event.setRoadRegulatorID(5);
        event.setIngressLane(2);
        event.setEgressLane(4);
        event.setConnectionID(2);
        event.setEventState(J2735MovementPhaseState.STOP_THEN_PROCEED);
        event.setVehicleID("C0FFEE");
        event.setLatitude(-104.124742);
        event.setLongitude(55.12745);
        event.setHeading(53);
        event.setSpeed(54);
        event.setSignalGroup(3);
        return event;
    }

    public static SignalStateStopEvent getSignalStateStopEvent(){
        SignalStateStopEvent event = new SignalStateStopEvent();
        event.setTimestamp(ZonedDateTime.now().toInstant().toEpochMilli());
        event.setRoadRegulatorID(0);
        event.setIngressLane(1);
        event.setEgressLane(5);
        event.setConnectionID(3);
        event.setLatitude(-104.124742);
        event.setLongitude(55.12745);
        event.setEventState(J2735MovementPhaseState.CAUTION_CONFLICTING_TRAFFIC);
        event.setVehicleID("C0FFEE");
        event.setHeading(53);
        event.setSpeed(54);

        return event;
    }

    public static TimeChangeDetailsEvent getTimeChangeDetailsEvent(){
        TimeChangeDetailsEvent event = new TimeChangeDetailsEvent();
        return event;
    }

    public static SpatBroadcastRateEvent getSpatBroadcastRateEvent(){
        SpatBroadcastRateEvent event = new SpatBroadcastRateEvent();
        event.setIntersectionId(12109);
        event.setNumberOfMessages(20);
        event.setTopicName("ProcessedSpat");
        event.setTimePeriod(new ProcessingTimePeriod());
        return event;
    }

    public static MapBroadcastRateEvent getMapBroadcastRateEvent(){
        MapBroadcastRateEvent event = new MapBroadcastRateEvent();
        event.setIntersectionId(12109);
        event.setNumberOfMessages(18);
        event.setTopicName("Processed Map");
        event.setTimePeriod(new ProcessingTimePeriod());
        return event;
    }
}
