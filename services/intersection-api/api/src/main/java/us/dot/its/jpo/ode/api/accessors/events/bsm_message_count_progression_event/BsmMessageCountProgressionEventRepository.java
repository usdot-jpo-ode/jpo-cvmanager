package us.dot.its.jpo.ode.api.accessors.events.bsm_message_count_progression_event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.BsmMessageCountProgressionEvent;

public interface BsmMessageCountProgressionEventRepository extends DataLoader<BsmMessageCountProgressionEvent> {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<BsmMessageCountProgressionEvent> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<BsmMessageCountProgressionEvent> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}