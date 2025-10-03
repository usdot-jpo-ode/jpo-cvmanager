package us.dot.its.jpo.ode.api.accessors.notifications.lane_direction_of_travel_notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.LaneDirectionOfTravelNotification;

public interface LaneDirectionOfTravelNotificationRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<LaneDirectionOfTravelNotification> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<LaneDirectionOfTravelNotification> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}