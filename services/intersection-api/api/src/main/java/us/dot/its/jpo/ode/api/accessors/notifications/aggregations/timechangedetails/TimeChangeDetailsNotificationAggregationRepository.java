package us.dot.its.jpo.ode.api.accessors.notifications.aggregations.timechangedetails;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.TimeChangeDetailsNotificationAggregation;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface TimeChangeDetailsNotificationAggregationRepository
                extends DataLoader<TimeChangeDetailsNotificationAggregation> {
        long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

        Page<TimeChangeDetailsNotificationAggregation> findLatest(Integer intersectionID, Long startTime, Long endTime);

        Page<TimeChangeDetailsNotificationAggregation> find(Integer intersectionID, Long startTime, Long endTime,
                        Pageable pageable);
}