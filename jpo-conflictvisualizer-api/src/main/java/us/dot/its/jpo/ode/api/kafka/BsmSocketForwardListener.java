package us.dot.its.jpo.ode.api.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmIntersectionIdKey;
import us.dot.its.jpo.ode.api.controllers.StompController;
import us.dot.its.jpo.ode.model.OdeBsmData;

@Component
@Slf4j
public class BsmSocketForwardListener extends BaseSeekToEndListener {

    @Autowired
    public BsmSocketForwardListener(StompController stompController) {
        super(stompController);
    }

    @KafkaListener(
            id = ListenerIds.BSM,
            groupId = ListenerIds.BSM,
            topics = "topic.CmBsmIntersection",
            concurrency = "1",
            containerFactory = "bsmListenerContainerFactory",
            autoStartup = "false")
    public void listen(ConsumerRecord<BsmIntersectionIdKey, OdeBsmData> record) {
        stompController.broadcastBSM(record.key(), record.value());
        log.trace("Received bsm with offset {}", record.offset());
    }


}
