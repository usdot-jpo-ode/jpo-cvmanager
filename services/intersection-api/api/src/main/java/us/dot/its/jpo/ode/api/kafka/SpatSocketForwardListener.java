package us.dot.its.jpo.ode.api.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.controllers.live.StompController;

@Component
@Slf4j
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
public class SpatSocketForwardListener extends BaseSeekToEndListener {

    public SpatSocketForwardListener(StompController stompController) {
        super(stompController);
    }

    @KafkaListener(id = ListenerIds.SPAT, idIsGroup = false, topics = "topic.DeduplicatedProcessedSpat", concurrency = "1", containerFactory = "spatListenerContainerFactory", autoStartup = "${conflict.monitor.api.kafka-consumers-always-on}")
    public void listen(ConsumerRecord<String, ProcessedSpat> record) {
        stompController.broadcastProcessedSpat(record.value());
        log.trace("Received spat with offset {}", record.offset());
    }

}
