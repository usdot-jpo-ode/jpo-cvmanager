package us.dot.its.jpo.ode.api.controllers.live;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.Point;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.bsm.ProcessedBsm;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;

@Controller
@Slf4j
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
public class StompController {

    private final SimpMessagingTemplate brokerMessagingTemplate;

    private final ObjectMapper mapper;

    @Autowired
    public StompController(SimpMessagingTemplate brokerMessagingTemplate) {
        this.brokerMessagingTemplate = brokerMessagingTemplate;

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_INSTANT;
        ZonedDateTimeSerializer zonedDateTimeSerializer = new ZonedDateTimeSerializer(dateTimeFormatter);

        SimpleModule module = new SimpleModule();
        module.addSerializer(ZonedDateTime.class, zonedDateTimeSerializer);
        mapper.registerModule(module);

        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
    }

    public void broadcastMessage(String topic, String message) {
        brokerMessagingTemplate.convertAndSend(topic, message);
    }

    public String buildTopicName(int intersectionID, String messageType) {
        return String.format("/live/%d/%s", intersectionID, messageType);
    }

    public void broadcastProcessedSpat(ProcessedSpat spat) {
        if (spat != null) {
            Integer intersectionID = spat.getIntersectionId();
            if (intersectionID == null) {
                intersectionID = -1;
            }

            if (intersectionID != -1) {
                try {
                    broadcastMessage(buildTopicName(intersectionID, "processed-spat"), mapper.writeValueAsString(spat));
                } catch (JsonProcessingException e) {
                    log.error("Exception encoding SPaT data to STOMP topic", e);
                }

            }
        }
    }

    public void broadcastProcessedMap(ProcessedMap<LineString> map) {
        if (map != null) {
            Integer intersectionID = map.getProperties().getIntersectionId();
            if (intersectionID == null) {
                intersectionID = -1;
            }

            if (intersectionID != -1) {
                try {
                    broadcastMessage(buildTopicName(intersectionID, "processed-map"), mapper.writeValueAsString(map));
                } catch (JsonProcessingException e) {
                    log.error("Exception encoding MAP data to STOMP topic", e);
                }
            }
        }
    }

    public void broadcastBSM(int intersectionId, ProcessedBsm<Point> bsm) {
        if (bsm != null) {
            if (intersectionId != -1) {
                try {
                    broadcastMessage(buildTopicName(intersectionId, "processed-bsm"),
                            mapper.writeValueAsString(bsm));
                } catch (JsonProcessingException e) {
                    log.error("Exception encoding BSM data to STOMP topic", e);
                }
            }
        }
    }

}
