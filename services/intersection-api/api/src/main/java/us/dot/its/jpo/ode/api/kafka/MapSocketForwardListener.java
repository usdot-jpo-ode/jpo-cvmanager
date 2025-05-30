package us.dot.its.jpo.ode.api.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.controllers.StompController;

@Component
@Slf4j
@ConditionalOnProperty(
    name = "enable.api",
    havingValue = "true",
    matchIfMissing = false
)
public class MapSocketForwardListener extends BaseSeekToEndListener {

    public MapSocketForwardListener(StompController stompController) {
        super(stompController);
    }

    @KafkaListener(id = ListenerIds.MAP,
            idIsGroup = false,
            topics = "topic.ProcessedMap",
            concurrency = "1",
            containerFactory = "mapListenerContainerFactory",
            autoStartup = "${conflict.monitor.api.kafka-consumers-always-on}")
    public void listen(ConsumerRecord<String, ProcessedMap<LineString>> record) {
        stompController.broadcastMap(record.value());
        log.trace("Received map with offset {}", record.offset());
    }
}
