package us.dot.its.jpo.ode.api.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;

/**
 * Service to start and stop Kafka Listeners.
 * See <a href="https://www.baeldung.com/kafka-spring-boot-dynamically-manage-listeners">
 *     Dynamically Managing Kafka Listeners in Spring Boot</a>
 */
@Service
@Slf4j
public class KafkaListenerControlService {

    private final KafkaListenerEndpointRegistry registry;

    @Autowired
    public KafkaListenerControlService(KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    public void startListeners() {
        startListener(ListenerIds.MAP);
        startListener(ListenerIds.SPAT);
        startListener(ListenerIds.BSM);
    }

    public void stopListeners() {
        stopListener(ListenerIds.MAP);
        stopListener(ListenerIds.SPAT);
        stopListener(ListenerIds.BSM);
    }

    private void startListener(String listenerId) {
        MessageListenerContainer listenerContainer = registry.getListenerContainer(listenerId);
        if (listenerContainer != null && !listenerContainer.isRunning()) {
            log.info("Starting kafka listener: {}", listenerId);
            listenerContainer.start();
        }
    }

    private void stopListener(String listenerId) {
        MessageListenerContainer listenerContainer = registry.getListenerContainer(listenerId);
        if (listenerContainer != null && listenerContainer.isRunning()) {
            log.info("Stopping kafka listener: {}", listenerId);
            listenerContainer.stop();
        }
    }



}
