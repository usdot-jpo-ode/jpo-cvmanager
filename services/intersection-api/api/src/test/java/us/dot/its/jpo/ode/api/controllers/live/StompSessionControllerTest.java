package us.dot.its.jpo.ode.api.controllers.live;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import us.dot.its.jpo.ode.api.kafka.KafkaListenerControlService;

import java.util.HashMap;
import java.util.Map;

class StompSessionControllerTest {

    @Mock
    KafkaListenerControlService listenerControlService;

    StompSessionController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new StompSessionController(listenerControlService);
    }

    @Test
    void testHandleSessionConnectEvent_StartListeners() {
        // Mock event with sessionId header
        Map<String, Object> headersMap = new HashMap<>();
        headersMap.put("simpSessionId", "session1");
        MessageHeaders headers = new MessageHeaders(headersMap);
        Message<byte[]> message = mock(Message.class);
        when(message.getHeaders()).thenReturn(headers);

        AbstractSubProtocolEvent event = mock(SessionConnectEvent.class);
        when(event.getMessage()).thenReturn(message);

        controller.handleSessionConnectEvent((SessionConnectEvent) event);

        // Should call startListeners since it's the first session
        verify(listenerControlService).startListeners();
    }

    @Test
    void testHandleSessionConnectEvent_NullSessionIdThrows() {
        MessageHeaders headers = new MessageHeaders(new HashMap<>());
        Message<byte[]> message = mock(Message.class);
        when(message.getHeaders()).thenReturn(headers);

        AbstractSubProtocolEvent event = mock(SessionConnectEvent.class);
        when(event.getMessage()).thenReturn(message);

        assertThrows(RuntimeException.class, () -> {
            controller.handleSessionConnectEvent((SessionConnectEvent) event);
        });
    }

    @Test
    void testHandleSessionDisconnectEvent_StopListeners() {
        // Add a session first
        Map<String, Object> headersMap = new HashMap<>();
        headersMap.put("simpSessionId", "session2");
        MessageHeaders headers = new MessageHeaders(headersMap);
        Message<byte[]> message = mock(Message.class);
        when(message.getHeaders()).thenReturn(headers);

        AbstractSubProtocolEvent connectEvent = mock(SessionConnectEvent.class);
        when(connectEvent.getMessage()).thenReturn(message);

        controller.handleSessionConnectEvent((SessionConnectEvent) connectEvent);

        // Now disconnect
        SessionDisconnectEvent disconnectEvent = mock(SessionDisconnectEvent.class);
        when(disconnectEvent.getSessionId()).thenReturn("session2");

        controller.handleSessionDisconnectEvent(disconnectEvent);

        // Should call stopListeners since it's the last session
        verify(listenerControlService).stopListeners();
    }

    @Test
    void testGetSessionIdFromHeader() {
        Map<String, Object> headersMap = new HashMap<>();
        headersMap.put("simpSessionId", "sessionX");
        MessageHeaders headers = new MessageHeaders(headersMap);
        Message<byte[]> message = mock(Message.class);
        when(message.getHeaders()).thenReturn(headers);

        AbstractSubProtocolEvent event = mock(AbstractSubProtocolEvent.class);
        when(event.getMessage()).thenReturn(message);

        String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
        assert sessionId.equals("sessionX");
    }
}