package us.dot.its.jpo.ode.api.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import us.dot.its.jpo.ode.api.APIServiceController;
import us.dot.its.jpo.ode.api.topologies.RestartableTopology;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Component that keeps track of connected STOMP WebSocket clients.  Starts Kafka Streams
 * topologies when any clients connect, and stops them when there are none.
 */
@Component
@Slf4j
public class StompSessionController {

    final List<RestartableTopology> topologies;

    // Use the keys of a ConcurrentHashMap as a thread-safe set of session ID's because Java
    // lacks a "ConcurrentHashSet".  The Boolean value of the map is not meaningful.
    final Map<String, Boolean> sessions = new ConcurrentHashMap<>();


    @Autowired
    public StompSessionController(APIServiceController apiServiceController) {
        this.topologies = apiServiceController.getTopologies();
    }

    @EventListener(SessionConnectEvent.class)
    public void handleSessionConnectEvent(SessionConnectEvent event) {
        log.info("Session Connect Event, session ID: {}, event: {}", getSessionIdFromHeader(event), event);
    }

    @EventListener(SessionConnectedEvent.class)
    public void handleSessionConnectedEvent(SessionConnectedEvent event) {
        String sessionId = getSessionIdFromHeader(event);
        log.info("Session Connected Event, session ID: {}, event: {}", sessionId, event);
        sessions.put(sessionId, true);
    }

    @EventListener(SessionDisconnectEvent.class)
    public void handleSessionDisconnedtEvent(SessionDisconnectEvent event) {
        log.info("Session Disconnect Event, session ID: {}, event: {}", event.getSessionId(), event);
    }

    private String getSessionIdFromHeader(AbstractSubProtocolEvent event) {
        var message = event.getMessage();
        MessageHeaders headers = message.getHeaders();
        if (headers.containsKey("simpSessionId")) {
            return headers.get("simpSessionId", String.class);
        }
        return null;
    }


}
