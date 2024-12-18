package us.dot.its.jpo.ode.api.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.controllers.StompController;

@Component
@Slf4j
public class MapSocketForwardListener extends BaseSeekToEndListener {

    @Autowired
    public MapSocketForwardListener(StompController stompController) {
        super(stompController);
    }

    @KafkaListener(id = ListenerIds.MAP,
            groupId = ListenerIds.MAP,
            topics = "topic.ProcessedMap",
            concurrency = "1",
            containerFactory = "mapListenerContainerFactory",
            autoStartup = "false")
    public void listen(ConsumerRecord<String, ProcessedMap<LineString>> record) {
        stompController.broadcastMap(record.value());
        log.trace("Received map with offset {}", record.offset());
    }
}
