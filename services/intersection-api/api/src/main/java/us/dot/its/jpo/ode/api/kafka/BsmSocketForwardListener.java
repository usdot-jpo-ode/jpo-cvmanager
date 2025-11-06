package us.dot.its.jpo.ode.api.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmIntersectionIdKey;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.Point;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.bsm.ProcessedBsm;
import us.dot.its.jpo.ode.api.controllers.live.StompController;

@Component
@Slf4j
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
public class BsmSocketForwardListener extends BaseSeekToEndListener {

    public BsmSocketForwardListener(StompController stompController) {
        super(stompController);
    }

    @KafkaListener(id = ListenerIds.BSM, idIsGroup = false, topics = "topic.CmBsmIntersection", concurrency = "1", containerFactory = "bsmListenerContainerFactory", autoStartup = "${conflict.monitor.api.kafka-consumers-always-on}")
    public void listen(ConsumerRecord<BsmIntersectionIdKey, ProcessedBsm<Point>> record) {
        stompController.broadcastBSM(record.key().getIntersectionId(), record.value());
        log.trace("Received bsm with offset {}", record.offset());
    }

}
