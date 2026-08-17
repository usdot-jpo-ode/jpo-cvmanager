package us.dot.its.jpo.rsustatusmonitor.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Kafka topics.
 */
@Configuration
@ConfigurationProperties(prefix = "rsu-status-monitor.kafka.topics")
@Data
public class KafkaTopics {
    private String monitoringStatus;
}
