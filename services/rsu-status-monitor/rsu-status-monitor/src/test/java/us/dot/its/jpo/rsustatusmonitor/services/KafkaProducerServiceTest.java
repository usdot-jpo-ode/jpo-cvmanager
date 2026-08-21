package us.dot.its.jpo.rsustatusmonitor.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import us.dot.its.jpo.geojsonconverter.partitioner.RsuIntersectionKey;
import us.dot.its.jpo.rsustatusmonitor.kafka.KafkaTopics;
import us.dot.its.jpo.rsustatusmonitor.models.snmp.RsuState;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private KafkaTopics kafkaTopics;

    private KafkaProducerService service;

    @BeforeEach
    public void setup() {
        service = new KafkaProducerService(kafkaTemplate, objectMapper, kafkaTopics);
    }

    @Test
    public void testSendMessage_Success() {
        String topic = "test-topic";
        String key = "test-key";
        String message = "test-message";

        service.sendMessage(topic, key, message);

        verify(kafkaTemplate).send(topic, key, message);
    }

    @Test
    public void testSendMessage_WithNullValues() {
        String topic = null;
        String key = null;
        String message = null;

        service.sendMessage(topic, key, message);

        verify(kafkaTemplate).send(topic, key, message);
    }

    @Test
    public void testSendMessage_WithEmptyStrings() {
        String topic = "";
        String key = "";
        String message = "";

        service.sendMessage(topic, key, message);

        verify(kafkaTemplate).send(topic, key, message);
    }

    @Test
    public void testSendRsuStatus_Success() throws JsonProcessingException {
        RsuIntersectionKey key = new RsuIntersectionKey();
        key.setRsuId("rsu-123");
        key.setIntersectionId(12345);
        key.setRegion(1);

        RsuState message = new RsuState();

        String expectedTopic = "monitoring-status-topic";
        String serializedKey = "{\"rsuId\":\"rsu-123\",\"intersectionId\":12345,\"region\":1}";
        String serializedMessage = "{\"rsuState\":\"data\"}";

        when(kafkaTopics.getMonitoringStatus()).thenReturn(expectedTopic);
        when(objectMapper.writeValueAsString(key)).thenReturn(serializedKey);
        when(objectMapper.writeValueAsString(message)).thenReturn(serializedMessage);

        service.sendRsuStatus(key, message);

        verify(objectMapper).writeValueAsString(key);
        verify(objectMapper).writeValueAsString(message);
        verify(kafkaTemplate).send(expectedTopic, serializedKey, serializedMessage);
    }

    @Test
    public void testSendRsuStatus_JsonProcessingException_OnMessage() throws JsonProcessingException {
        RsuIntersectionKey key = new RsuIntersectionKey();
        key.setRsuId("rsu-123");
        RsuState message = new RsuState();

        when(objectMapper.writeValueAsString(message)).thenThrow(new JsonProcessingException("Serialization error") {
        });

        service.sendRsuStatus(key, message);

        verify(objectMapper).writeValueAsString(message);
        verify(objectMapper, never()).writeValueAsString(key);
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    public void testSendRsuStatus_JsonProcessingException_OnKey() throws JsonProcessingException {
        RsuIntersectionKey key = new RsuIntersectionKey();
        RsuState message = new RsuState();

        String serializedMessage = "{\"state\":\"data\"}";

        when(objectMapper.writeValueAsString(message)).thenReturn(serializedMessage);
        doThrow(new JsonProcessingException("Serialization error") {
        }).when(objectMapper).writeValueAsString(any(RsuIntersectionKey.class));

        service.sendRsuStatus(key, message);

        verify(objectMapper).writeValueAsString(message);
        verify(objectMapper).writeValueAsString(key);
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    public void testSendRsuStatus_VerifyTopicFromKafkaTopics() throws JsonProcessingException {
        RsuIntersectionKey key = new RsuIntersectionKey();
        RsuState message = new RsuState();

        String expectedTopic = "custom-monitoring-topic";
        String serializedKey = "{}";
        String serializedMessage = "{}";

        when(kafkaTopics.getMonitoringStatus()).thenReturn(expectedTopic);
        when(objectMapper.writeValueAsString(any())).thenReturn(serializedKey, serializedMessage);

        service.sendRsuStatus(key, message);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), eq(serializedKey), eq(serializedMessage));
        assertEquals(expectedTopic, topicCaptor.getValue());
    }

    @Test
    public void testSendRsuStatus_WithComplexRsuState() throws JsonProcessingException {
        RsuIntersectionKey key = new RsuIntersectionKey();
        key.setRsuId("rsu-456");
        key.setIntersectionId(67890);

        RsuState message = new RsuState();

        String expectedTopic = "monitoring-status-topic";
        String serializedKey = "{\"key\":\"data\"}";
        String serializedMessage = "{\"complex\":\"state\",\"nested\":{\"data\":true}}";

        when(kafkaTopics.getMonitoringStatus()).thenReturn(expectedTopic);
        when(objectMapper.writeValueAsString(key)).thenReturn(serializedKey);
        when(objectMapper.writeValueAsString(message)).thenReturn(serializedMessage);

        service.sendRsuStatus(key, message);

        verify(kafkaTemplate).send(expectedTopic, serializedKey, serializedMessage);
    }

    @Test
    public void testSendRsuStatus_WithNullKey() throws JsonProcessingException {
        RsuIntersectionKey key = null;
        RsuState message = new RsuState();

        String expectedTopic = "monitoring-status-topic";
        String serializedMessage = "{\"state\":\"data\"}";
        String serializedKey = "null";

        when(kafkaTopics.getMonitoringStatus()).thenReturn(expectedTopic);
        when(objectMapper.writeValueAsString(message)).thenReturn(serializedMessage);
        when(objectMapper.writeValueAsString(key)).thenReturn(serializedKey);

        service.sendRsuStatus(key, message);

        verify(objectMapper).writeValueAsString(message);
        verify(objectMapper).writeValueAsString(key);
        verify(kafkaTemplate).send(expectedTopic, serializedKey, serializedMessage);
    }

    @Test
    public void testSendRsuStatus_WithNullMessage() throws JsonProcessingException {
        RsuIntersectionKey key = new RsuIntersectionKey();
        RsuState message = null;

        String expectedTopic = "monitoring-status-topic";
        String serializedKey = "{}";
        String serializedMessage = "null";

        when(kafkaTopics.getMonitoringStatus()).thenReturn(expectedTopic);
        when(objectMapper.writeValueAsString(message)).thenReturn(serializedMessage);
        when(objectMapper.writeValueAsString(key)).thenReturn(serializedKey);

        service.sendRsuStatus(key, message);

        verify(objectMapper).writeValueAsString(message);
        verify(objectMapper).writeValueAsString(key);
        verify(kafkaTemplate).send(expectedTopic, serializedKey, serializedMessage);
    }
}
