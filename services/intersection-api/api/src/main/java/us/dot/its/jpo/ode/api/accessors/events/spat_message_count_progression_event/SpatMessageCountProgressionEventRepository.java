package us.dot.its.jpo.ode.api.accessors.events.spat_message_count_progression_event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SpatMessageCountProgressionEvent;

public interface SpatMessageCountProgressionEventRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<SpatMessageCountProgressionEvent> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<SpatMessageCountProgressionEvent> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}