package us.dot.its.jpo.ode.api.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.AbstractConsumerSeekAware;
import us.dot.its.jpo.ode.api.controllers.StompController;

import java.util.Map;

/**
 * Base class for Kafka listeners that seek to the last offset before consuming when starting up.
 * See <a href="https://docs.spring.io/spring-kafka/reference/kafka/seek.html">
 *     Spring Kafka: Seeking to a specific offset</a>
 */
@Slf4j
public class BaseSeekToEndListener extends AbstractConsumerSeekAware {

    protected final StompController stompController;

    public BaseSeekToEndListener(StompController stompController) {
        this.stompController = stompController;
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        log.info("Seek to end for TopicPartitions {}", assignments.keySet());
        callback.seekToEnd(assignments.keySet());
        super.onPartitionsAssigned(assignments, callback);
    }

}
