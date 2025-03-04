package us.dot.its.jpo.ode.mockdata;

import us.dot.its.jpo.conflictmonitor.monitor.models.events.EventStateProgressionEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateConflictEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEventAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.EventStateProgressionNotificationAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.IntersectionReferenceAlignmentNotificationAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalGroupAlignmentNotificationAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalStateConflictNotificationAggregation;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.TimeChangeDetailsNotificationAggregation;

public class MockAggregatedNotificationGenerator {
    public static IntersectionReferenceAlignmentNotificationAggregation getIntersectionReferenceAlignmentNotificationAggregation() {

        IntersectionReferenceAlignmentEventAggregation event = MockAggregatedEventGenerator
                .getIntersectionReferenceAlignmentEventAggregation();
        IntersectionReferenceAlignmentNotificationAggregation notification = new IntersectionReferenceAlignmentNotificationAggregation();

        notification.setEventAggregation(event);
        notification.setNotificationText(
                "Intersection Reference Alignment Notification, generated because one or more" +
                        " corresponding intersection reference alignment events were generated.");
        notification.setNotificationHeading("Intersection Reference Alignment");
        notification.setIntersectionID(2);
        notification.setRoadRegulatorID(-1);
        notification.setKey(notification.getUniqueId());

        return notification;
    }

    public static SignalGroupAlignmentNotificationAggregation getSignalGroupAlignmentNotificationAggregation() {

        SignalGroupAlignmentEventAggregation event = MockAggregatedEventGenerator
                .getSignalGroupAlignmentEventAggregation();
        SignalGroupAlignmentNotificationAggregation notification = new SignalGroupAlignmentNotificationAggregation();

        notification.setEventAggregation(event);
        notification.setNotificationText(
                "Signal Group Alignment Notification, generated because corresponding signal group alignment event was generated.");
        notification.setNotificationHeading("Signal Group Alignment");
        notification.setIntersectionID(2);
        notification.setRoadRegulatorID(-1);
        notification.setKey(notification.getUniqueId());

        return notification;
    }

    public static SignalStateConflictNotificationAggregation getSignalStateConflictNotificationAggregation() {

        SignalStateConflictEventAggregation event = MockAggregatedEventGenerator
                .getSignalStateConflictEventAggregation();
        SignalStateConflictNotificationAggregation notification = new SignalStateConflictNotificationAggregation();

        notification.setEventAggregation(event);
        notification.setNotificationText(
                "Signal State Conflict Notification, generated because corresponding signal state conflict" +
                        " event was generated.");
        notification.setNotificationHeading("Signal State Conflict");
        notification.setIntersectionID(2);
        notification.setRoadRegulatorID(-1);
        notification.setKey(notification.getUniqueId());

        return notification;
    }

    public static TimeChangeDetailsNotificationAggregation getTimeChangeDetailsNotificationAggregation() {

        TimeChangeDetailsEventAggregation event = MockAggregatedEventGenerator
                .getTimeChangeDetailsEventAggregation();
        TimeChangeDetailsNotificationAggregation notification = new TimeChangeDetailsNotificationAggregation();

        notification.setEventAggregation(event);
        notification.setNotificationText(
                "Time Change Details Notification, " +
                        "generated because one or more corresponding time change details events were generated.");
        notification.setNotificationHeading("Time Change Details");
        notification.setIntersectionID(2);
        notification.setRoadRegulatorID(-1);
        notification.setKey(notification.getUniqueId());

        return notification;
    }

    public static EventStateProgressionNotificationAggregation getEventStateProgressionNotificationAggregation() {

        EventStateProgressionEventAggregation event = MockAggregatedEventGenerator
                .getEventStateProgressionEventAggregation();
        EventStateProgressionNotificationAggregation notification = new EventStateProgressionNotificationAggregation();

        notification.setEventAggregation(event);
        notification.setNotificationText(
                "Event State Progression Event Notification, " +
                        "generated because one or more corresponding event state progression events were generated.");
        notification.setNotificationHeading("Event State Progression");
        notification.setIntersectionID(2);
        notification.setRoadRegulatorID(-1);
        notification.setKey(notification.getUniqueId());

        return notification;
    }
}
