package us.dot.its.jpo.ode.mockdata;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.asn.j2735.r2024.BasicSafetyMessage.BasicSafetyMessageMessageFrame;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.Point;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.bsm.ProcessedBsm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Slf4j
public class MockBsmGenerator {

    static String bsmString = "{\"metadata\": {\"bsmSource\": \"RV\", \"logFileName\": \"\", \"recordType\": \"bsmTx\", \"securityResultCode\": \"success\", \"receivedMessageDetails\": {\"locationData\": {\"latitude\": \"\", \"longitude\": \"\", \"elevation\": \"\", \"speed\": \"\", \"heading\": \"\"}, \"rxSource\": \"RV\"}, \"payloadType\": \"us.dot.its.jpo.ode.model.OdeBsmPayload\", \"serialId\": {\"streamId\": \"849c3ac6-4747-426f-ae5e-28ac8531b32c\", \"bundleSize\": 1, \"bundleId\": 0, \"recordId\": 0, \"serialNumber\": 0}, \"odeReceivedAt\": \"2022-06-17T19:14:21.124599Z\", \"schemaVersion\": 8, \"maxDurationTime\": 0, \"recordGeneratedAt\": \"\", \"sanitized\": false, \"odePacketID\": \"\", \"odeTimStartDateTime\": \"\", \"originIp\": \"1.1.1.1\"}, \"payload\": {\"data\": {\"coreData\": {\"msgCnt\": 45, \"id\": \"E6A99808\", \"secMark\": 20962, \"position\": {\"latitude\": 39.5881502, \"longitude\": -105.091045, \"elevation\": 1691.9}, \"accelSet\": {\"accelLong\": -0.05, \"accelYaw\": -0.65}, \"accuracy\": {\"semiMajor\": 2.0, \"semiMinor\": 2.0, \"orientation\": 0.0}, \"transmission\": \"UNAVAILABLE\", \"speed\": 22.62, \"heading\": 169.2, \"brakes\": {\"wheelBrakes\": {\"leftFront\": false, \"rightFront\": false, \"unavailable\": true, \"leftRear\": false, \"rightRear\": false}, \"traction\": \"unavailable\", \"abs\": \"off\", \"scs\": \"unavailable\", \"brakeBoost\": \"unavailable\", \"auxBrakes\": \"unavailable\"}, \"size\": {\"width\": 180, \"length\": 480}}}, \"dataType\": \"us.dot.its.jpo.ode.plugin.j2735.J2735Bsm\"}}";
    static String processedBsmString = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[-105.0915261,39.5976029]},\"properties\":{\"schemaVersion\":6,\"messageType\":\"BSM\",\"odeReceivedAt\":\"2025-01-07T21:23:42.793Z\",\"timeStamp\":\"2025-01-07T21:23:42.793Z\",\"originIp\":\"10.11.81.12\",\"validationMessages\":[],\"accelSet\":{\"accelLong\":0,\"accelYaw\":0},\"accuracy\":{\"semiMajor\":0,\"semiMinor\":0,\"orientation\":0},\"brakes\":{\"wheelBrakes\":{\"leftFront\":false,\"rightFront\":false,\"unavailable\":true,\"leftRear\":false,\"rightRear\":false},\"traction\":\"unavailable\",\"abs\":\"off\",\"scs\":\"unavailable\",\"brakeBoost\":\"unavailable\",\"auxBrakes\":\"unavailable\"},\"heading\":180.2,\"id\":\"AABBCC\",\"msgCnt\":78,\"secMark\":\"100\",\"size\":{\"width\":0,\"length\":0},\"transmission\":\"UNAVAILABLE\",\"speed\":22,\"logName\":\"\"}}";

    static TypeReference<ProcessedBsm<Point>> processedBsmTypeReference = new TypeReference<>() {
    };

    public static List<BasicSafetyMessageMessageFrame> getJsonBsms() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ArrayList<BasicSafetyMessageMessageFrame> bsms = new ArrayList<>();

        try {
            BasicSafetyMessageMessageFrame bsm = objectMapper.readValue(bsmString,
                    BasicSafetyMessageMessageFrame.class);
            bsms.add(bsm);
        } catch (JsonMappingException e) {
            log.error("JsonMappingException", e);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException", e);
        }
        return bsms;
    }

    public static List<ProcessedBsm<Point>> getProcessedBsms() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ArrayList<ProcessedBsm<Point>> bsms = new ArrayList<>();

        try {
            ProcessedBsm<Point> bsm = objectMapper.readValue(bsmString,
                    processedBsmTypeReference);
            bsms.add(bsm);
        } catch (JsonMappingException e) {
            log.error("JsonMappingException", e);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException", e);
        }
        return bsms;
    }
}
