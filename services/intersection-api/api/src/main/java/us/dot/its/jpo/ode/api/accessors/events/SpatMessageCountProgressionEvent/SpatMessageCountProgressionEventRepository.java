package us.dot.its.jpo.ode.api.accessors.events.SpatMessageCountProgressionEvent;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SpatMessageCountProgressionEvent;

public interface SpatMessageCountProgressionEventRepository extends DataLoader<SpatMessageCountProgressionEvent> {
    long count(Integer intersectionID, Long startTime, Long endTime, Pageable pageable);

    Page<SpatMessageCountProgressionEvent> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<SpatMessageCountProgressionEvent> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}