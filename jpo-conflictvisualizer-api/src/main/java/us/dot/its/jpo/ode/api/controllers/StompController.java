package us.dot.its.jpo.ode.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmIntersectionIdKey;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.models.LiveFeedSessionIndex;
import us.dot.its.jpo.ode.model.OdeBsmData;

@Controller
public class StompController {

    @Autowired
    private SimpMessagingTemplate brokerMessagingTemplate;

    // @Scheduled(fixedRate = 10000) // Broadcast a message every second
    public void broadcastMessage(String topic, String message) {
        brokerMessagingTemplate.convertAndSend(topic, message);
    }

    public String buildTopicName(int roadRegulatorID, int intersectionID, String messageType) {
        return String.format("/live/%d/%d/%s", roadRegulatorID, intersectionID, messageType);
    }

    public void broadcastSpat(ProcessedSpat spat) {
        Integer intersectionID = spat.getIntersectionId();
        if (intersectionID == null) {
            intersectionID = -1;
        }

        Integer roadRegulatorID = spat.getRegion();
        if (roadRegulatorID == null) {
            roadRegulatorID = -1;
        }

        if (intersectionID != -1) {
            broadcastMessage(buildTopicName(-1, intersectionID, "spat"), spat.toString());
        }
    }

    public void broadcastMap(ProcessedMap<LineString> map) {
        Integer intersectionID = map.getProperties().getIntersectionId();
        if (intersectionID == null) {
            intersectionID = -1;
        }

        Integer roadRegulatorID = map.getProperties().getRegion();
        if (roadRegulatorID == null) {
            roadRegulatorID = -1;
        }

        if (intersectionID != -1) {
            broadcastMessage(buildTopicName(-1, intersectionID, "map"), map.toString());
        }
    }

    public void broadcastBSM(BsmIntersectionIdKey key, OdeBsmData bsm) {
        if (key.getIntersectionId() != -1) {
            broadcastMessage(buildTopicName(-1, key.getIntersectionId(), "bsm"), bsm.toString());
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
