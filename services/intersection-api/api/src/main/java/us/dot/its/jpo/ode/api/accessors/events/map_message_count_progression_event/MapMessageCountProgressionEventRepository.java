package us.dot.its.jpo.ode.api.accessors.events.map_message_count_progression_event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.MapMessageCountProgressionEvent;

public interface MapMessageCountProgressionEventRepository {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<MapMessageCountProgressionEvent> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<MapMessageCountProgressionEvent> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}