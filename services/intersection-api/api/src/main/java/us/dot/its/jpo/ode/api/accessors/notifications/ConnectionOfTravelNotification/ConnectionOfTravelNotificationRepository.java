package us.dot.its.jpo.ode.api.accessors.notifications.ConnectionOfTravelNotification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface ConnectionOfTravelNotificationRepository extends DataLoader<ConnectionOfTravelNotification> {
    long getQueryResultCount(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<ConnectionOfTravelNotification> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<ConnectionOfTravelNotification> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}