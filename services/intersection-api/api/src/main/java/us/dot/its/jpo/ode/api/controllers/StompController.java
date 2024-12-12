package us.dot.its.jpo.ode.api.controllers;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;

import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmIntersectionIdKey;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.model.OdeBsmData;

@Controller
public class StompController {

    @Autowired
    private SimpMessagingTemplate brokerMessagingTemplate;

    private ObjectMapper mapper;

    StompController(){
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_INSTANT;
        ZonedDateTimeSerializer zonedDateTimeSerializer = new ZonedDateTimeSerializer(dateTimeFormatter);

        SimpleModule module = new SimpleModule();
        module.addSerializer(ZonedDateTime.class, zonedDateTimeSerializer);
        mapper.registerModule(module);

        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
    }

    

    // @Scheduled(fixedRate = 10000) // Broadcast a message every second
    public void broadcastMessage(String topic, String message) {
        brokerMessagingTemplate.convertAndSend(topic, message);
    }

    public String buildTopicName(int roadRegulatorID, int intersectionID, String messageType) {
        return String.format("/live/%d/%d/%s", roadRegulatorID, intersectionID, messageType);
    }

    public void broadcastSpat(ProcessedSpat spat) {
        if(spat != null){
            Integer intersectionID = spat.getIntersectionId();
            if (intersectionID == null) {
                intersectionID = -1;
            }

            Integer roadRegulatorID = spat.getRegion();
            if (roadRegulatorID == null) {
                roadRegulatorID = -1;
            }

            if (intersectionID != -1) {
                try {
                    broadcastMessage(buildTopicName(-1, intersectionID, "spat"), mapper.writeValueAsString(spat));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                
            }
        }
    }

    public void broadcastMap(ProcessedMap<LineString> map) {
        if(map != null){
            Integer intersectionID = map.getProperties().getIntersectionId();
            if (intersectionID == null) {
                intersectionID = -1;
            }

            Integer roadRegulatorID = map.getProperties().getRegion();
            if (roadRegulatorID == null) {
                roadRegulatorID = -1;
            }

            if (intersectionID != -1) {
                try {
                    broadcastMessage(buildTopicName(-1, intersectionID, "map"),  mapper.writeValueAsString(map));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void broadcastBSM(BsmIntersectionIdKey key, OdeBsmData bsm) {
        if(bsm != null){
            if (key.getIntersectionId() != -1) {
                try {
                    broadcastMessage(buildTopicName(-1, key.getIntersectionId(), "bsm"),  mapper.writeValueAsString(bsm));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Sample Format for receiving a message from a client, and broadcasting a
    // response back. Not needed in current model, but left for future reference

    // @MessageMapping("/server") // Called when Data is received on /broker/server
    // // @SendTo("/live/spat") // Reply with information on /live/spat
    // public String getSpat(String message) {
    // System.out.println("Incoming message: " + message);
    // return "Response From Server: " + message;
    // }
}
