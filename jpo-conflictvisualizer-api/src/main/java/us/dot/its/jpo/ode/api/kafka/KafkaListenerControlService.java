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
    private final SpatSocketForwardListener spatListener;

    @Autowired
    public KafkaListenerControlService(KafkaListenerEndpointRegistry registry,
                                       SpatSocketForwardListener spatListener) {
        this.registry = registry;
        this.spatListener = spatListener;
    }

    public void startSpatListener() {
        startListener(ListenerIds.SPAT);
        spatListener.seekToEnd();
    }

    public void stopSpatListener() {
        stopListener(ListenerIds.SPAT);
    }

    private void startListener(String listenerId) {
        MessageListenerContainer listenerContainer = registry.getListenerContainer(listenerId);
        if (listenerContainer != null && !listenerContainer.isRunning()) {
            listenerContainer.start();
        }
    }

    private void stopListener(String listenerId) {
        MessageListenerContainer listenerContainer = registry.getListenerContainer(listenerId);
        if (listenerContainer != null && listenerContainer.isRunning()) {
            listenerContainer.stop();
        }
    }



}
