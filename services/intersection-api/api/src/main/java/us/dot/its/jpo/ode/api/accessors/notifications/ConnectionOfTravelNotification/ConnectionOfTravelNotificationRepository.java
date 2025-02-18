package us.dot.its.jpo.ode.api.accessors.notifications.ConnectionOfTravelNotification;

import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.api.models.PageWithProperties;

public interface ConnectionOfTravelNotificationRepository extends DataLoader<ConnectionOfTravelNotification> {
    long getQueryResultCount(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    long getQueryFullCount(Integer intersectionID, Long startTime, Long endTime, boolean latest);

    PageWithProperties<ConnectionOfTravelNotification> find(Integer intersectionID, Long startTime, Long endTime,
            boolean latest,
            Pageable pageable);
}