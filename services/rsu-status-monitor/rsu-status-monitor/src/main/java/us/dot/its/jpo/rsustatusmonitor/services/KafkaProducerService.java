package us.dot.its.jpo.rsustatusmonitor.services;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.geojsonconverter.partitioner.RsuIntersectionKey;
import us.dot.its.jpo.rsustatusmonitor.kafka.KafkaTopics;
import us.dot.its.jpo.rsustatusmonitor.models.snmp.RsuState;

@Service
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper;

    private KafkaTopics kafkaTopics;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper,
            KafkaTopics kafkaTopics) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTopics = kafkaTopics;
    }

    public void sendMessage(String topic, String key, String message) {
        kafkaTemplate.send(topic, key, message);
    }

    public void sendRsuStatus(RsuIntersectionKey key, RsuState message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            String jsonKey = objectMapper.writeValueAsString(key);
            kafkaTemplate.send(kafkaTopics.getMonitoringStatus(), jsonKey, jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message" + message);
        }

    }
}
