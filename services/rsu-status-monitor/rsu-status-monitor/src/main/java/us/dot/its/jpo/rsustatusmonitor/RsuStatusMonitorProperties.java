package us.dot.its.jpo.rsustatusmonitor;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@Slf4j
public class RsuStatusMonitorProperties {

    final BuildProperties buildProperties;

    @Autowired
    public RsuStatusMonitorProperties(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @PostConstruct
    void initialize() {
        log.info("groupId: {}", buildProperties.getGroup());
        log.info("artifactId: {}", buildProperties.getArtifact());
        log.info("version: {}", buildProperties.getVersion());
    }

    public String getVersion() {
        return buildProperties.getVersion();
    }
}
