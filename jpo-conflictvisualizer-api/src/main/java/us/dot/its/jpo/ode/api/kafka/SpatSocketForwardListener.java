package us.dot.its.jpo.ode.api.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.controllers.StompController;


@Component
@Slf4j
public class SpatSocketForwardListener extends BaseSeekToEndListener {

    @Autowired
    public SpatSocketForwardListener(StompController stompController) {
        super(stompController);
    }

    @KafkaListener(id = ListenerIds.SPAT,
            groupId = ListenerIds.SPAT,
            topics = "topic.ProcessedSpat",
            concurrency = "1",
            containerFactory = "spatListenerContainerFactory",
            autoStartup = "false")
    public void listen(ConsumerRecord<String, ProcessedSpat> record) {
        stompController.broadcastSpat(record.value());
        log.trace("Received spat with offset {}", record.offset());
    }

}
