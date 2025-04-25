package us.dot.its.jpo.ode.api.accessors.haas.websocket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import us.dot.its.jpo.ode.api.models.DataLoader;
import us.dot.its.jpo.ode.api.models.haas.websocket.HaasWebsocketLocation;

public interface HaasWebsocketLocationDataRepository extends DataLoader<HaasWebsocketLocation> {
    long count(boolean activeOnly, Long startTime, Long endTime, Pageable pageable);

    Page<HaasWebsocketLocation> findLatest(boolean activeOnly, Long startTime, Long endTime, Pageable pageable);

    // Page<HaasWebsocketLocation> find(Integer intersectionID, Long startTime, Long
    // endTime, Pageable pageable);
}