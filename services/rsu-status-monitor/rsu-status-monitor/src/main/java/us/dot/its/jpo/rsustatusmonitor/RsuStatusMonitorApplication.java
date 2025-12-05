package us.dot.its.jpo.rsustatusmonitor;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableKafka
@EnableScheduling
@EnableConfigurationProperties
@Slf4j
public class RsuStatusMonitorApplication {

    public static void main(String[] args) throws MalformedObjectNameException, InterruptedException,
            InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        SpringApplication.run(RsuStatusMonitorApplication.class, args);
    }

    @Bean
    CommandLineRunner init(RsuStatusMonitorProperties rsuStatusMonitorProperties) {
        return args -> {
        };
    }
}
