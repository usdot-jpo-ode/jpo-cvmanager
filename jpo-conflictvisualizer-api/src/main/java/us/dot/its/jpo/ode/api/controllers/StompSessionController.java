package us.dot.its.jpo.ode.api.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import us.dot.its.jpo.ode.api.kafka.KafkaListenerControlService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Component that keeps track of connected STOMP WebSocket clients.  Starts Kafka Streams
 * topologies when any clients connect, and stops them when there are 0 clients.
 */
@Component
@Slf4j
public class StompSessionController {

    //final List<RestartableTopology> topologies;

    private final Set<String> sessions = Collections.synchronizedSet(new HashSet<>(10));

    private final KafkaListenerControlService listenerControlService;

    @Autowired
    public StompSessionController(KafkaListenerControlService listenerControlService) {
        this.listenerControlService = listenerControlService;
    }

    @EventListener(SessionConnectEvent.class)
    public void handleSessionConnectEvent(SessionConnectEvent event) {
        String sessionId = getSessionIdFromHeader(event);
        log.info("Session Connect Event, session ID: {}, event: {}", sessionId, event);

        if (sessionId == null) {
            throw new RuntimeException("Null session ID from connect event.  This should not happen.");
        }

        // Update sessions set and start kafka streams in an atomic operation for thread safety
        synchronized (sessions) {
            final int beforeNumSessions = sessions.size();
            sessions.add(sessionId);
            if (beforeNumSessions == 0) {
                listenerControlService.startListeners();
            }
        }
    }


    @EventListener(SessionDisconnectEvent.class)
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        log.info("Session Disconnect Event, session ID: {}, event: {}", event.getSessionId(), event);

        if (event.getSessionId() == null) {
            throw new RuntimeException("Null session ID from disconnect event.  This should not happen.");
        }

        // Update sessions set and stop kafka streams in an atomic operation for thread safety
        synchronized (sessions) {
            final int beforeNumSessions = sessions.size();
            sessions.remove(event.getSessionId());
            final int afterNumSessions = sessions.size();
            if (beforeNumSessions > 0 && afterNumSessions == 0) {
                listenerControlService.stopListeners();
            }
        }
    }


    private static final String SIMP_SESSION_ID = "simpSessionId";

    private String getSessionIdFromHeader(AbstractSubProtocolEvent event) {
        var message = event.getMessage();
        MessageHeaders headers = message.getHeaders();
        if (headers.containsKey(SIMP_SESSION_ID)) {
            return headers.get(SIMP_SESSION_ID, String.class);
        }
        return null;
    }


}
