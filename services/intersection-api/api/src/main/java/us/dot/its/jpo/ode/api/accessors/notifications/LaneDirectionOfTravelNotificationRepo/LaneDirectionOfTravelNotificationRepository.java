package us.dot.its.jpo.ode.api.accessors.notifications.LaneDirectionOfTravelNotificationRepo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.LaneDirectionOfTravelNotification;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface LaneDirectionOfTravelNotificationRepository extends DataLoader<LaneDirectionOfTravelNotification> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<LaneDirectionOfTravelNotification> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<LaneDirectionOfTravelNotification> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}