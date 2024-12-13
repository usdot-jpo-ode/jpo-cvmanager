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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Component that keeps track of connected STOMP WebSocket clients.  Starts Kafka Streams
 * topologies when any clients connect, and stops them when there are 0 clients.
 */
@Component
@Slf4j
public class StompSessionController {

    final List<RestartableTopology> topologies;

    private final Set<String> sessions = Collections.synchronizedSet(new HashSet<>(10));


    @Autowired
    public StompSessionController(APIServiceController apiServiceController) {
        this.topologies = apiServiceController.getTopologies();
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
                startKafkaStreams();
            }
        }
    }


    @EventListener(SessionDisconnectEvent.class)
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        log.info("Session Disconnect Event, session ID: {}, event: {}", event.getSessionId(), event);
        if (event.getSessionId() == null) {
            throw new RuntimeException("Null session ID from disconnect event.  This should not happen.");
        }
        // Update sessions set and start kafka streams in an atomic operation for thread safety
        synchronized (sessions) {
            final int beforeNumSessions = sessions.size();
            sessions.remove(event.getSessionId());
            final int afterNumSessions = sessions.size();
            if (beforeNumSessions > 0 && afterNumSessions == 0) {
                stopKafkaStreams();
            }
        }
    }

    private void startKafkaStreams() {
        log.info("Starting Kafka Streams");
        for (final RestartableTopology topology : topologies) {
            try {
                log.debug("Starting topology for {}", topology.getTopicName());
                topology.start();
                log.debug("Started topology for {}", topology.getTopicName());
            } catch (Exception ex) {
                log.error("Exception starting topology", ex);
            }
        }
        log.info("Started all Kafka Streams");
    }

    private void stopKafkaStreams() {
        log.info("Stopping Kafka Streams");
        for (final RestartableTopology topology : topologies) {
            try {
                log.debug("Stopping topology for {}", topology.getTopicName());
                topology.stop();
                log.debug("Stopped topology for {}", topology.getTopicName());
            } catch (Exception ex) {
                log.error("Exception stopping topology", ex);
            }
        }
        log.info("Stopped all Kafka Streams");
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
