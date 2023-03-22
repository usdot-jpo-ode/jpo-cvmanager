package us.dot.its.jpo.ode.mockdata;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.IntersectionReferenceAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.LaneDirectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalGroupAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalStateConflictNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.MapBroadcastRateNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.broadcast_rate.SpatBroadcastRateNotification;

public class MockNotificationGenerator {
    public static ConnectionOfTravelNotification getConnectionOfTravelNotification(){
        ConnectionOfTravelNotification notification = new ConnectionOfTravelNotification();
        notification.setAssessment(MockAssessmentGenerator.getConnectionOfTravelAssessment());
        notification.setNotificationHeading("Connection of Travel Notification");
        notification.setNotificationText("Mocked Connection of Travel Notification, created from Mocked Connection of Travel Assessment.");
        return notification;
    }

    public static IntersectionReferenceAlignmentNotification getIntersectionReferenceAlignmentNotification(){
        IntersectionReferenceAlignmentNotification notification = new IntersectionReferenceAlignmentNotification();
        notification.setEvent(MockEventGenerator.getIntersectionReferenceAlignmentEvent());
        notification.setNotificationHeading("Intersection Reference Alignment Notification");
        notification.setNotificationText("Mocked Intersection Reference Alignment Notification, created from Mocked Intersection Reference Alignment Event.");
        return notification;
    }

    public static LaneDirectionOfTravelNotification getLaneDirectionOfTravelNotification(){
        LaneDirectionOfTravelNotification notification = new LaneDirectionOfTravelNotification();
        notification.setAssessment(MockAssessmentGenerator.getLaneDirectionOfTravelAssessment());
        notification.setNotificationHeading("Lane Direction of Travel Notification");
        notification.setNotificationText("Mocked Lane Direction of Travel Notification, created from Mocked Lane Direction of Travel Assessment.");
        return notification;
    }

    public static SignalGroupAlignmentNotification getSignalGroupAlignmentNotification(){
        SignalGroupAlignmentNotification notification = new SignalGroupAlignmentNotification();
        notification.setEvent(MockEventGenerator.getSignalGroupAlignmentEvent());
        notification.setNotificationHeading("Signal Group Alignment Notification");
        notification.setNotificationText("Mocked Signal Group Alignment notification, created from Mocked Signal Group Alignment Event.");
        return notification;
    }

    public static SignalStateConflictNotification getSignalStateConflictNotification(){
        SignalStateConflictNotification notification = new SignalStateConflictNotification();
        notification.setEvent(MockEventGenerator.getSignalStateConflictEvent());
        notification.setNotificationHeading("Signal State Conflict Notification");
        notification.setNotificationText("Mocked Signal State Conflict Notification, created from Mocked Signal State Conflict Event.");
        return notification;
    }

    public static MapBroadcastRateNotification getMapBroadcastRateNotification(){
        MapBroadcastRateNotification notification = new MapBroadcastRateNotification();
        notification.setEvent(MockEventGenerator.getMapBroadcastRateEvent());
        notification.setNotificationHeading("Map Broadcast Rate Notification");
        notification.setNotificationText("Mocked Map Broadcast Rate Notification, created from Mocked Map Broadcast Rate Event");
        return notification;
    }

    public static SpatBroadcastRateNotification getSpatBroadcastRateNotification(){
        SpatBroadcastRateNotification notification = new SpatBroadcastRateNotification();
        notification.setEvent(MockEventGenerator.getSpatBroadcastRateEvent());
        notification.setNotificationHeading("Spat Broadcast Rate Notification");
        notification.setNotificationText("Spat Broadcast Rate Notification, created from Mocked Spat Broadcast Rate Event");
        return notification;
    }
}
