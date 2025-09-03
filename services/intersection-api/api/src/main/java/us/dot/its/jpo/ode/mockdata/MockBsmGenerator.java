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

    static String bsmString = "{\"messageId\": 20,\"value\": {\"metadata\": {\"recordType\": \"bsmTx\",\"securityResultCode\": \"success\",\"payloadType\": \"us.dot.its.jpo.ode.model.OdeMessageFramePayload\",\"serialId\": {\"streamId\": \"44a6d71c-8af1-4f45-848c-10bd7f919be8\",\"bundleSize\": 1,\"bundleId\": 0,\"recordId\": 0,\"serialNumber\": 0},\"odeReceivedAt\": \"2025-08-29T16:09:34.416Z\",\"schemaVersion\": 9,\"maxDurationTime\": 0,\"recordGeneratedBy\": \"OBU\",\"sanitized\": false,\"source\": \"EV\",\"isCertPresent\": false},\"payload\": {\"data\": {\"messageId\": 20,\"value\": {\"BasicSafetyMessage\": {\"coreData\": {\"msgCnt\": 37,\"id\": \"31325433\",\"secMark\": 25399,\"lat\": 405659938,\"long\": -1050317754,\"elev\": 14409,\"accuracy\": { \"semiMajor\": 186, \"semiMinor\": 241, \"orientation\": 65535 },\"transmission\": \"unavailable\",\"speed\": 14,\"heading\": 25060,\"angle\": 127,\"accelSet\": { \"long\": 27, \"lat\": 0, \"vert\": 0, \"yaw\": 0 },\"brakes\": {\"wheelBrakes\": \"80\",\"traction\": \"unavailable\",\"abs\": \"unavailable\",\"scs\": \"unavailable\",\"brakeBoost\": \"unavailable\",\"auxBrakes\": \"unavailable\"},\"size\": { \"width\": 190, \"length\": 570 }},\"partII\": [{\"partII-Id\": 0,\"partII-Value\": {\"VehicleSafetyExtensions\": {\"pathHistory\": {\"crumbData\": [{ \"latOffset\": -113, \"lonOffset\": 181, \"elevationOffset\": -6, \"timeOffset\": 190 },{ \"latOffset\": -310, \"lonOffset\": 472, \"elevationOffset\": -23, \"timeOffset\": 610 },{ \"latOffset\": -103, \"lonOffset\": 636, \"elevationOffset\": -14, \"timeOffset\": 1570 },{ \"latOffset\": -52, \"lonOffset\": 615, \"elevationOffset\": -13, \"timeOffset\": 1870 },{ \"latOffset\": 614, \"lonOffset\": 1150, \"elevationOffset\": -17, \"timeOffset\": 2589 },{ \"latOffset\": 1878, \"lonOffset\": 2503, \"elevationOffset\": 7, \"timeOffset\": 3959 },{ \"latOffset\": 2333, \"lonOffset\": 2816, \"elevationOffset\": 31, \"timeOffset\": 4539 },{ \"latOffset\": 2187, \"lonOffset\": 2952, \"elevationOffset\": 39, \"timeOffset\": 4959 },{ \"latOffset\": 1976, \"lonOffset\": 2721, \"elevationOffset\": 46, \"timeOffset\": 5699 },{ \"latOffset\": 1891, \"lonOffset\": 3655, \"elevationOffset\": 84, \"timeOffset\": 6050 },{ \"latOffset\": 2022, \"lonOffset\": 4886, \"elevationOffset\": 137, \"timeOffset\": 6349 },{ \"latOffset\": 1973, \"lonOffset\": 4861, \"elevationOffset\": 144, \"timeOffset\": 6760 },{ \"latOffset\": 1795, \"lonOffset\": 4815, \"elevationOffset\": 144, \"timeOffset\": 7270 },{ \"latOffset\": 1710, \"lonOffset\": 4749, \"elevationOffset\": 135, \"timeOffset\": 7570 },{ \"latOffset\": 1609, \"lonOffset\": 4566, \"elevationOffset\": 121, \"timeOffset\": 7880 }]},\"pathPrediction\": { \"radiusOfCurve\": 32767, \"confidence\": 0 }}}},{\"partII-Id\": 2,\"partII-Value\": {\"SupplementalVehicleExtensions\": {\"classDetails\": { \"keyType\": 0, \"role\": \"basicVehicle\", \"hpmsType\": \"none\", \"fuelType\": 0 },\"vehicleData\": { \"height\": 38 },\"doNotUse2\": { \"airTemp\": 191 }}}}]}}},\"dataType\": \"us.dot.its.jpo.asn.j2735.r2024.BasicSafetyMessage.BasicSafetyMessageMessageFrame\"}}}";
    static String processedBsmString = "{\"type\": \"Feature\",\"geometry\": { \"type\": \"Point\", \"coordinates\": [-105.0317754, 40.5659938] },\"properties\": {\"schemaVersion\": 2,\"messageType\": \"BSM\",\"odeReceivedAt\": \"2025-08-29T16:09:34.416Z\",\"timeStamp\": \"2025-08-29T16:09:25.399Z\",\"validationMessages\": [{\"message\": \"$.metadata.logFileName: is missing but it is required\",\"jsonPath\": \"$.metadata\",\"schemaPath\": \"#/properties/metadata/required\"},{\"message\": \"$.metadata.receivedMessageDetails: is missing but it is required\",\"jsonPath\": \"$.metadata\",\"schemaPath\": \"#/properties/metadata/required\"},{\"message\": \"$.metadata.recordGeneratedAt: is missing but it is required\",\"jsonPath\": \"$.metadata\",\"schemaPath\": \"#/properties/metadata/required\"},{\"message\": \"$.metadata.asn1: is missing but it is required\",\"jsonPath\": \"$.metadata\",\"schemaPath\": \"#/properties/metadata/required\"}],\"accelSet\": { \"accelLat\": 0.0, \"accelLong\": 0.27, \"accelVert\": 0.0, \"accelYaw\": 0.0 },\"accuracy\": { \"semiMajor\": 9.3, \"semiMinor\": 12.05 },\"brakes\": {\"wheelBrakes\": {\"unavailable\": true,\"leftFront\": false,\"leftRear\": false,\"rightFront\": false,\"rightRear\": false},\"traction\": \"UNAVAILABLE\",\"abs\": \"UNAVAILABLE\",\"scs\": \"UNAVAILABLE\",\"brakeBoost\": \"UNAVAILABLE\",\"auxBrakes\": \"UNAVAILABLE\"},\"heading\": 313.25,\"id\": \"31325433\",\"msgCnt\": 37,\"secMark\": 25399,\"size\": { \"width\": 190, \"length\": 570 },\"speed\": 0.28,\"transmission\": \"UNAVAILABLE\"}}";

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
            ProcessedBsm<Point> bsm = objectMapper.readValue(processedBsmString,
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
