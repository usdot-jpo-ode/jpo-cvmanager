package us.dot.its.jpo.rsustatusmonitor;

import java.util.Properties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    public BuildProperties buildProperties() {
        Properties properties = new Properties();
        properties.setProperty("group", "usdot.jpo.ode");
        properties.setProperty("artifact", "rsu-status-monitor");
        properties.setProperty("version", "0.1.0-TEST");
        properties.setProperty("name", "rsu-status-monitor");
        properties.setProperty("time", "2025-11-21T00:00:00Z");
        return new BuildProperties(properties);
    }

    @Bean
    public RsuStatusMonitorProperties rsuStatusMonitorProperties(BuildProperties buildProperties) {
        return new RsuStatusMonitorProperties(buildProperties);
    }
}
