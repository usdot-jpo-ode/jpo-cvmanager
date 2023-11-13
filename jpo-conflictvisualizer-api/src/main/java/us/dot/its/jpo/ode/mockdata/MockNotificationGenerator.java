package us.dot.its.jpo.ode.mockdata;

import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.ConnectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLinePassageAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLineStopAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLineStopAssessmentGroup;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateConflictEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.MapBroadcastRateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.broadcast_rate.SpatBroadcastRateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.IntersectionReferenceAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.LaneDirectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalGroupAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalStateConflictNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.StopLinePassageNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.StopLineStopNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.TimeChangeDetailsNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.MapBroadcastRateNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.SpatBroadcastRateNotification;

public class MockNotificationGenerator {
    public static ConnectionOfTravelNotification getConnectionOfTravelNotification(){
        ConnectionOfTravelNotification notification = new ConnectionOfTravelNotification();
        ConnectionOfTravelAssessment assessment = MockAssessmentGenerator.getConnectionOfTravelAssessment();
        notification.setIntersectionID(assessment.getIntersectionID());
        notification.setRoadRegulatorID(assessment.getRoadRegulatorID());
        notification.setAssessment(assessment);
        notification.setNotificationHeading("Connection of Travel Notification");
        notification.setNotificationText("Mocked Connection of Travel Notification, created from Mocked Connection of Travel Assessment.");
        return notification;
    }

    public static IntersectionReferenceAlignmentNotification getIntersectionReferenceAlignmentNotification(){
        IntersectionReferenceAlignmentNotification notification = new IntersectionReferenceAlignmentNotification();
        IntersectionReferenceAlignmentEvent event = MockEventGenerator.getIntersectionReferenceAlignmentEvent();
        notification.setIntersectionID(event.getIntersectionID());
        notification.setRoadRegulatorID(event.getRoadRegulatorID());
        notification.setEvent(event);
        notification.setNotificationHeading("Intersection Reference Alignment Notification");
        notification.setNotificationText("Mocked Intersection Reference Alignment Notification, created from Mocked Intersection Reference Alignment Event.");
        return notification;
    }

    public static LaneDirectionOfTravelNotification getLaneDirectionOfTravelNotification(){
        LaneDirectionOfTravelNotification notification = new LaneDirectionOfTravelNotification();
        LaneDirectionOfTravelAssessment assessment = MockAssessmentGenerator.getLaneDirectionOfTravelAssessment();
        notification.setIntersectionID(assessment.getIntersectionID());
        notification.setRoadRegulatorID(assessment.getRoadRegulatorID());
        notification.setAssessment(assessment);
        notification.setNotificationHeading("Lane Direction of Travel Notification");
        notification.setNotificationText("Mocked Lane Direction of Travel Notification, created from Mocked Lane Direction of Travel Assessment.");
        return notification;
    }

    public static SignalGroupAlignmentNotification getSignalGroupAlignmentNotification(){
        SignalGroupAlignmentNotification notification = new SignalGroupAlignmentNotification();
        SignalGroupAlignmentEvent event = MockEventGenerator.getSignalGroupAlignmentEvent();
        notification.setIntersectionID(event.getIntersectionID());
        notification.setRoadRegulatorID(event.getRoadRegulatorID());
        notification.setEvent(event);
        notification.setNotificationHeading("Signal Group Alignment Notification");
        notification.setNotificationText("Mocked Signal Group Alignment notification, created from Mocked Signal Group Alignment Event.");
        return notification;
    }

    public static SignalStateConflictNotification getSignalStateConflictNotification(){
        SignalStateConflictNotification notification = new SignalStateConflictNotification();
        SignalStateConflictEvent event = MockEventGenerator.getSignalStateConflictEvent();
        notification.setIntersectionID(event.getIntersectionID());
        notification.setRoadRegulatorID(event.getRoadRegulatorID());
        notification.setEvent(event);
        notification.setNotificationHeading("Signal State Conflict Notification");
        notification.setNotificationText("Mocked Signal State Conflict Notification, created from Mocked Signal State Conflict Event.");
        return notification;
    }

    public static MapBroadcastRateNotification getMapBroadcastRateNotification(){
        MapBroadcastRateNotification notification = new MapBroadcastRateNotification();
        MapBroadcastRateEvent event = MockEventGenerator.getMapBroadcastRateEvent();
        event.setIntersectionID(event.getIntersectionID());
        event.setRoadRegulatorID(event.getRoadRegulatorID());
        notification.setEvent(event);
        notification.setNotificationHeading("Map Broadcast Rate Notification");
        notification.setNotificationText("Mocked Map Broadcast Rate Notification, created from Mocked Map Broadcast Rate Event");
        return notification;
    }

    public static SpatBroadcastRateNotification getSpatBroadcastRateNotification(){
        SpatBroadcastRateNotification notification = new SpatBroadcastRateNotification();
        SpatBroadcastRateEvent event = MockEventGenerator.getSpatBroadcastRateEvent();
        notification.setIntersectionID(event.getIntersectionID());
        notification.setRoadRegulatorID(event.getRoadRegulatorID());
        notification.setEvent(event);
        notification.setNotificationHeading("Spat Broadcast Rate Notification");
        notification.setNotificationText("Spat Broadcast Rate Notification, created from Mocked Spat Broadcast Rate Event");
        return notification;
    }

    public static TimeChangeDetailsNotification getTimeChangeDetailsNotification(){
        TimeChangeDetailsNotification notification = new TimeChangeDetailsNotification();
        TimeChangeDetailsEvent event = MockEventGenerator.getTimeChangeDetailsEvent();
        notification.setIntersectionID(event.getIntersectionID());
        notification.setRoadRegulatorID(event.getRoadRegulatorID());
        notification.setEvent(event);
        notification.setNotificationText(
                                    "Time Change Details Notification, generated because corresponding time change details event was generated.");
        notification.setNotificationHeading("Time Change Details");
        return notification;
    }

    public static StopLineStopNotification getStopLineStopNotification(){
        StopLineStopNotification notification = new StopLineStopNotification();
        StopLineStopAssessment assessment = MockAssessmentGenerator.getStopLineStopAssessment();
        StopLineStopAssessmentGroup group = assessment.getStopLineStopAssessmentGroup().get(0);
        notification.setSignalGroup(group.getSignalGroup());
        notification.setIntersectionID(assessment.getIntersectionID());
        notification.setRoadRegulatorID(assessment.getRoadRegulatorID());
        notification.setAssessment(assessment);
        notification.setNotificationText("Stop Line Stop Notification, Percent Time stopped on green: " + group.getTimeStoppedOnGreen() + " For Signal group: " + group.getSignalGroup() + " Exceeds Maximum Allowable Percent");
                            notification.setNotificationHeading("Stop Line Stop Notification");
        return notification;
    }

    public static StopLinePassageNotification getStopLinePassageNotification(){
        StopLinePassageNotification notification = new StopLinePassageNotification();
        StopLinePassageAssessment assessment = MockAssessmentGenerator.getStopLinePassageAssessment();
        notification.setIntersectionID(assessment.getIntersectionID());
        notification.setRoadRegulatorID(assessment.getRoadRegulatorID());
        notification.setAssessment(assessment);
        
        return notification;
    }
}
