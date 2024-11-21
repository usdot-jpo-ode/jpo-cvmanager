package us.dot.its.jpo.ode.mockdata;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class MockTimGenerator {

    static String timString = "{\"metadata\":{\"securityResultCode\":\"\",\"recordGeneratedBy\":\"RSU\",\"schemaVersion\":\"6\",\"odePacketID\":\"\",\"sanitized\":\"false\",\"recordType\":\"timMsg\",\"recordGeneratedAt\":\"\",\"maxDurationTime\":\"0\",\"odeTimStartDateTime\":\"\",\"receivedMessageDetails\":\"\",\"payloadType\":\"us.dot.its.jpo.ode.model.OdeTimPayload\",\"serialId\":{\"recordId\":\"0\",\"serialNumber\":\"0\",\"streamId\":\"02fd3137-f282-4f06-b735-66c5e2e963be\",\"bundleSize\":\"1\",\"bundleId\":\"0\"},\"logFileName\":\"\",\"odeReceivedAt\":\"2024-05-09T15:33:57.386279283Z\",\"originIp\":\"172.18.0.1\"},\"payload\":{\"data\":{\"MessageFrame\":{\"messageId\":\"31\",\"value\":{\"TravelerInformation\":{\"timeStamp\":\"449089\",\"packetID\":\"0000000000000BBC2B\",\"urlB\":\"null\",\"dataFrames\":{\"TravelerDataFrame\":{\"regions\":{\"GeographicalPath\":{\"closedPath\":{\"false\":\"\"},\"anchor\":{\"lat\":\"411269876\",\"long\":\"-1047269563\"},\"name\":\"westbound_I-80_366.0_365.0_RSU-10.145.1.100_RW_4456\",\"laneWidth\":\"32700\",\"directionality\":{\"both\":\"\"},\"description\":{\"path\":{\"offset\":{\"xy\":{\"nodes\":{\"NodeXY\":[{\"delta\":{\"node-LatLon\":{\"lon\":\"-1047287423\",\"lat\":\"411264686\"}}},{\"delta\":{\"node-LatLon\":{\"lon\":\"-1047305390\",\"lat\":\"411260104\"}}},{\"delta\":{\"node-LatLon\":{\"lon\":\"-1047323629\",\"lat\":\"411256185\"}}},{\"delta\":{\"node-LatLon\":{\"lon\":\"-1047342080\",\"lat\":\"411252886\"}}},{\"delta\":{\"node-LatLon\":{\"lon\":\"-1047360706\",\"lat\":\"411250207\"}}},{\"delta\":{\"node-LatLon\":{\"lon\":\"-1047379480\",\"lat\":\"411248201\"}}},{\"delta\":{\"node-LatLon\":{\"lon\":\"-1047398354\",\"lat\":\"411246839\"}}},{\"delta\":{\"node-LatLon\":{\"lon\":\"-1047417290\",\"lat\":\"411246133\"}}},{\"delta\":{\"node-LatLon\":{\"lon\":\"-1047436246\",\"lat\":\"411245796\"}}},{\"delta\":{\"node-LatLon\":{\"lon\":\"-1047455202\",\"lat\":\"411245470\"}}},{\"delta\":{\"node-LatLon\":{\"lon\":\"-1047474159\",\"lat\":\"411245173\"}}}]}}},\"scale\":\"0\"}},\"id\":{\"id\":\"0\",\"region\":\"0\"},\"direction\":\"0000000000010000\"}},\"duratonTime\":\"1440\",\"sspMsgRights1\":\"1\",\"sspMsgRights2\":\"1\",\"startYear\":\"2018\",\"msgId\":{\"roadSignID\":{\"viewAngle\":\"1111111111111111\",\"mutcdCode\":{\"warning\":\"\"},\"position\":{\"lat\":\"411269876\",\"long\":\"-1047269563\"}}},\"priority\":\"5\",\"content\":{\"advisory\":{\"SEQUENCE\":[{\"item\":{\"itis\":\"777\"}},{\"item\":{\"itis\":\"13579\"}}]}},\"url\":\"null\",\"sspTimRights\":\"1\",\"sspLocationRights\":\"1\",\"frameType\":{\"advisory\":\"\"},\"startTime\":\"448260\"}},\"msgCnt\":\"1\"}}}},\"dataType\":\"TravelerInformation\"}}";

    public static List<ObjectNode> getJsonTims(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        ArrayList<ObjectNode> tims = new ArrayList<>();
        
        try {
            ObjectNode tim = objectMapper.readValue(timString, ObjectNode.class);
            tims.add(tim);
        } catch (JsonMappingException e) {
            System.out.println("A Json Mapping Exception Occurred while trying to get data from mocked tim.");
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            System.out.println("A Json Processing Exception Occurred while trying to get data from a mocked tim.");
            e.printStackTrace();
        }
        return tims;
    }

}



    