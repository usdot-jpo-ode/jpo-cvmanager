package us.dot.its.jpo.ode.api.controllers.live;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.Point;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.bsm.ProcessedBsm;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.MapSharedProperties;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;

public class StompControllerTest {

    @Mock
    SimpMessagingTemplate brokerMessagingTemplate;

    StompController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new StompController(brokerMessagingTemplate);
    }

    @Test
    void testBroadcastMessage() {
        controller.broadcastMessage("/topic/test", "hello");
        verify(brokerMessagingTemplate).convertAndSend("/topic/test", "hello");
    }

    @Test
    void testBuildTopicName() {
        String topic = controller.buildTopicName(123, "processed-spat");
        assert topic.equals("/live/123/processed-spat");
    }

    @Test
    void testBroadcastSpat() throws JsonProcessingException {
        ProcessedSpat spat = mock(ProcessedSpat.class);
        when(spat.getIntersectionId()).thenReturn(42);

        controller.broadcastProcessedSpat(spat);

        verify(brokerMessagingTemplate).convertAndSend(startsWith("/live/42/processed-spat"), anyString());
    }

    @Test
    void testBroadcastSpatNullIntersectionId() {
        ProcessedSpat spat = mock(ProcessedSpat.class);
        when(spat.getIntersectionId()).thenReturn(null);

        controller.broadcastProcessedSpat(spat);

        // Should not send if intersectionID is null
        verify(brokerMessagingTemplate, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    void testBroadcastSpatMinusOneIntersectionId() {
        ProcessedSpat spat = mock(ProcessedSpat.class);
        when(spat.getIntersectionId()).thenReturn(-1);

        controller.broadcastProcessedSpat(spat);

        // Should not send if intersectionID == -1
        verify(brokerMessagingTemplate, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    void testBroadcastMap() throws JsonProcessingException {
        ProcessedMap<LineString> map = mock(ProcessedMap.class);
        MapSharedProperties props = mock(MapSharedProperties.class);
        when(map.getProperties()).thenReturn(props);
        when(props.getIntersectionId()).thenReturn(99);

        controller.broadcastProcessedMap(map);

        verify(brokerMessagingTemplate).convertAndSend(startsWith("/live/99/processed-map"), anyString());
    }

    @Test
    void testBroadcastMapNullIntersectionId() {
        ProcessedMap<LineString> map = mock(ProcessedMap.class);
        MapSharedProperties props = mock(MapSharedProperties.class);
        when(map.getProperties()).thenReturn(props);
        when(props.getIntersectionId()).thenReturn(null);

        controller.broadcastProcessedMap(map);

        verify(brokerMessagingTemplate, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    void testBroadcastBSM() throws JsonProcessingException {
        ProcessedBsm<Point> bsm = mock(ProcessedBsm.class);

        controller.broadcastBSM(77, bsm);

        verify(brokerMessagingTemplate).convertAndSend(startsWith("/live/77/ode-bsm-json"), anyString());
    }

    @Test
    void testBroadcastBSMIntersectionIdMinusOne() {
        ProcessedBsm<Point> bsm = mock(ProcessedBsm.class);

        controller.broadcastBSM(-1, bsm);

        verify(brokerMessagingTemplate, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    void testBroadcastBSMNullBsm() {
        controller.broadcastBSM(77, null);

        verify(brokerMessagingTemplate, never()).convertAndSend(anyString(), anyString());
    }
}