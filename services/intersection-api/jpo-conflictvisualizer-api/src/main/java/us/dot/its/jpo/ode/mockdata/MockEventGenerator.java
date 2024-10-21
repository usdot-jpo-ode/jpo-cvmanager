package us.dot.its.jpo.ode.mockdata;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import us.dot.its.jpo.conflictmonitor.monitor.models.RegulatorIntersectionId;
import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.ConnectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.LaneDirectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.ProcessingTimePeriod;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateConflictEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLinePassageEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLineStopEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.MapBroadcastRateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.SpatBroadcastRateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.MapMinimumDataEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.SpatMinimumDataEvent;
import us.dot.its.jpo.ode.plugin.j2735.J2735MovementPhaseState;

public class MockEventGenerator {

    public static IntersectionReferenceAlignmentEvent getIntersectionReferenceAlignmentEvent() {
        IntersectionReferenceAlignmentEvent event = new IntersectionReferenceAlignmentEvent();
        event.setTimestamp(ZonedDateTime.now().toInstant().toEpochMilli());

        Set<RegulatorIntersectionId> mapIds = new HashSet<>();
        Set<RegulatorIntersectionId> spatIds = new HashSet<>();

        RegulatorIntersectionId mapId = new RegulatorIntersectionId();
        mapId.setIntersectionId(12109);
        mapId.setRoadRegulatorId(0);

        RegulatorIntersectionId spatId = new RegulatorIntersectionId();
        mapId.setIntersectionId(12109);
        mapId.setRoadRegulatorId(-1);

        event.setIntersectionID(12109);
        event.setRoadRegulatorID(-1);

        mapIds.add(mapId);
        spatIds.add(spatId);

        event.setMapRegulatorIntersectionIds(mapIds);
        event.setSpatRegulatorIntersectionIds(spatIds);

        return event;
    }

    public static ConnectionOfTravelEvent getConnectionOfTravelEvent() {
        ConnectionOfTravelEvent event = new ConnectionOfTravelEvent();
        event.setTimestamp(ZonedDateTime.now().toInstant().toEpochMilli());
        event.setRoadRegulatorID(3);
        event.setIntersectionID(2);
        event.setIngressLaneID(2);
        event.setEgressLaneID(7);
        event.setConnectionID(3);
        return event;
    }

    public static LaneDirectionOfTravelEvent getLaneDirectionOfTravelEvent() {
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

    public static SignalGroupAlignmentEvent getSignalGroupAlignmentEvent() {
        SignalGroupAlignmentEvent event = new SignalGroupAlignmentEvent();
        // event.setSource("Made in China");
        event.setTimestamp(ZonedDateTime.now().toInstant().toEpochMilli());
        event.setSpatSignalGroupIds(Stream.of(1, 6).collect(Collectors.toSet()));
        event.setMapSignalGroupIds(Stream.of(2, 7).collect(Collectors.toSet()));
        return event;
    }

    public static SignalStateConflictEvent getSignalStateConflictEvent() {
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

    public static StopLinePassageEvent getStopLinePassageEvent() {
        StopLinePassageEvent event = new StopLinePassageEvent();
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

    public static StopLineStopEvent getStopLineStopEvent() {
        StopLineStopEvent event = new StopLineStopEvent();
        event.setInitialTimestamp(ZonedDateTime.now().toInstant().toEpochMilli());
        event.setFinalTimestamp(ZonedDateTime.now().toInstant().toEpochMilli() + 100);
        event.setRoadRegulatorID(0);
        event.setIngressLane(1);
        event.setEgressLane(5);
        event.setConnectionID(3);
        event.setLatitude(-104.124742);
        event.setLongitude(55.12745);
        event.setInitialEventState(J2735MovementPhaseState.CAUTION_CONFLICTING_TRAFFIC);
        event.setFinalEventState(J2735MovementPhaseState.PROTECTED_CLEARANCE);
        event.setVehicleID("C0FFEE");
        event.setHeading(53);
        event.setTimeStoppedDuringRed(0.1);
        event.setTimeStoppedDuringGreen(1);
        event.setTimeStoppedDuringYellow(0.0);
        event.setTimeStoppedDuringDark(0);
        event.setSignalGroup(3);

        return event;
    }

    public static TimeChangeDetailsEvent getTimeChangeDetailsEvent() {
        
        TimeChangeDetailsEvent event = new TimeChangeDetailsEvent();
        event.setRoadRegulatorID(104);
        event.setIntersectionID(12109);
        event.setSignalGroup(6);
        event.setFirstSpatTimestamp(ZonedDateTime.now().toInstant().toEpochMilli());
        event.setSecondSpatTimestamp(ZonedDateTime.now().toInstant().toEpochMilli());
        event.setFirstConflictingTimemark((ZonedDateTime.now().toInstant().toEpochMilli()+100)  % (60 * 60 * 1000) / 100);
        event.setSecondConflictingTimemark(ZonedDateTime.now().toInstant().toEpochMilli()  % (60 * 60 * 1000) / 100);
        event.setFirstState(J2735MovementPhaseState.PROTECTED_CLEARANCE);
        event.setSecondState(J2735MovementPhaseState.PROTECTED_CLEARANCE);
        event.setFirstTimeMarkType("minEndTime");
        event.setSecondTimeMarkType("maxEndTime");
        event.setFirstConflictingUtcTimestamp(ZonedDateTime.now().toInstant().toEpochMilli()+100);
        event.setSecondConflictingUtcTimestamp(ZonedDateTime.now().toInstant().toEpochMilli());
        event.setSource("{\"intersectionID\": 12109, \"roadRegulatorID\": 104, \"originIp\": \"192.168.1.1\"}");
        return event;
    }

    public static SpatBroadcastRateEvent getSpatBroadcastRateEvent() {
        SpatBroadcastRateEvent event = new SpatBroadcastRateEvent();
        event.setIntersectionID(12109);
        event.setNumberOfMessages(20);
        event.setTopicName("ProcessedSpat");
        event.setTimePeriod(new ProcessingTimePeriod());
        return event;
    }

    public static SpatMinimumDataEvent getSpatMinimumDataEvent(){
        SpatMinimumDataEvent event = new SpatMinimumDataEvent();
        event.setIntersectionID(12109);
        event.setTimePeriod(new ProcessingTimePeriod());
        event.setMissingDataElements(new ArrayList<String>());
        return event;
    }

    public static MapMinimumDataEvent getMapMinimumDataEvent(){
        MapMinimumDataEvent event = new MapMinimumDataEvent();
        event.setIntersectionID(12109);
        event.setTimePeriod(new ProcessingTimePeriod());
        event.setMissingDataElements(new ArrayList<String>());
        return event;
    }

    public static MapBroadcastRateEvent getMapBroadcastRateEvent() {
        MapBroadcastRateEvent event = new MapBroadcastRateEvent();
        event.setIntersectionID(12109);
        event.setNumberOfMessages(18);
        event.setTopicName("Processed Map");
        event.setTimePeriod(new ProcessingTimePeriod());
        return event;
    }

    public static BsmEvent getBsmEvent() {
        BsmEvent event = new BsmEvent();
        event.setIntersectionID(12109);
        event.setStartingBsm(MockBsmGenerator.getJsonBsms().getFirst());
        event.setEndingBsm(MockBsmGenerator.getJsonBsms().getLast());
        event.setStartingBsmTimestamp(Instant.parse(event.getStartingBsm().getMetadata().getOdeReceivedAt()).toEpochMilli());
        event.setEndingBsmTimestamp(Instant.parse(event.getEndingBsm().getMetadata().getOdeReceivedAt()).toEpochMilli());
        event.setWktMapBoundingBox("LINESTRING (-105.09071084163995 39.587773371787485, -105.09071620693672 39.58779610924971, -105.09072266805292 39.58781264558122, -105.09072836868071 39.587833057609934)");
        event.setInMapBoundingBox(true);
        event.setWallClockTimestamp(Instant.now().toEpochMilli());
        event.setWktPath("LINESTRING (-105.09071084163995 39.587773371787485, -105.09071620693672 39.58779610924971, -105.09072266805292 39.58781264558122, -105.09072836868071 39.587833057609934)");
        return event;
    }
}
