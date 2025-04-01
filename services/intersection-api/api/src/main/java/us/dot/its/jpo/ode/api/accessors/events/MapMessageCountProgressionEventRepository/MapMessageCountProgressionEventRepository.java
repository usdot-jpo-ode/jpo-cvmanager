package us.dot.its.jpo.ode.api.accessors.events.MapMessageCountProgressionEventRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.MapMessageCountProgressionEvent;

public interface MapMessageCountProgressionEventRepository extends DataLoader<MapMessageCountProgressionEvent> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<MapMessageCountProgressionEvent> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<MapMessageCountProgressionEvent> find(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);
}