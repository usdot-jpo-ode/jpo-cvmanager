package us.dot.its.jpo.rsustatusmonitor.snmp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Kafka topics.
 */
@Configuration
@ConfigurationProperties(prefix = "rsu-status-monitor.snmp")
@Data
public class SnmpProperties {
    private int retries;
    private int timeout;
    private int port;
}
