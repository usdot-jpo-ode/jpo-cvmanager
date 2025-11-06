package us.dot.its.jpo.ode.api.accessors.notifications.connection_of_travel_notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;

public interface ConnectionOfTravelNotificationRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<ConnectionOfTravelNotification> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<ConnectionOfTravelNotification> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}