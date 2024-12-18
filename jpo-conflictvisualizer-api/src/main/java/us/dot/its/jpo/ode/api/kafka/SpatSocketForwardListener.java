package us.dot.its.jpo.ode.api.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AbstractConsumerSeekAware;
import org.springframework.stereotype.Component;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.controllers.StompController;

/**
 * Kafka listener that can seek to the latest offset.
 * See <a href="https://docs.spring.io/spring-kafka/reference/kafka/seek.html">
 *     Spring Kafka: Seeking to a specific offset</a>
 */
@Component
public class SpatSocketForwardListener extends AbstractConsumerSeekAware {

    final StompController stompController;

    @Autowired
    public SpatSocketForwardListener(StompController stompController) {
        this.stompController = stompController;
    }

    @KafkaListener(id = ListenerIds.SPAT,
            groupId = ListenerIds.SPAT,
            topics = "topic.ProcessedSpat",
            concurrency = "1",
            containerFactory = "spatListenerContainerFactory",
            autoStartup = "false")
    public void listen(ConsumerRecord<String, ProcessedSpat> record) {
        stompController.broadcastSpat(record.value());
    }

}
