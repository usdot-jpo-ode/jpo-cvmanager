package us.dot.its.jpo.ode.api.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AbstractConsumerSeekAware;
import org.springframework.stereotype.Component;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.controllers.StompController;

@Component
public class MapSocketForwardListener extends AbstractConsumerSeekAware {

    final StompController stompController;

    @Autowired
    public MapSocketForwardListener(StompController stompController) {
        this.stompController = stompController;
    }

    @KafkaListener(id = ListenerIds.MAP,
            groupId = ListenerIds.MAP,
            topics = "topic.ProcessedMap",
            concurrency = "1",
            containerFactory = "mapListenerContainerFactory",
            autoStartup = "false")
    public void listen(ConsumerRecord<String, ProcessedMap<LineString>> record) {
        stompController.broadcastMap(record.value());
    }
}
