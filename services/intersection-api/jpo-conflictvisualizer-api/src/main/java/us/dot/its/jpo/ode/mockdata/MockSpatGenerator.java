package us.dot.its.jpo.ode.mockdata;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.model.OdeSpatData;
public class MockSpatGenerator {

    static String processedSpatString = "{\"messageType\":\"SPAT\",\"odeReceivedAt\":\"2022-06-17T19:15:13.671068Z\",\"originIp\":\"10.11.81.12\",\"intersectionId\":12109,\"cti4501Conformant\":false,\"validationMessages\":[{\"message\":\"$.metadata.@class: is missing but it is required\",\"jsonPath\":\"$.metadata\",\"schemaPath\":\"#/$defs/OdeSpatMetadata/required\"},{\"message\":\"$.payload.@class: is missing but it is required\",\"jsonPath\":\"$.payload\",\"schemaPath\":\"#/$defs/OdeSpatPayload/required\"},{\"message\":\"$.payload.data.timeStamp: null found, integer expected\",\"jsonPath\":\"$.payload.data.timeStamp\",\"schemaPath\":\"#/$defs/J2735MinuteOfTheYear/type\"},{\"message\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].id.region: null found, integer expected\",\"jsonPath\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].id.region\",\"schemaPath\":\"#/$defs/J2735RoadRegulatorID/type\"},{\"message\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[0].state_time_speed.movementEventList[0].timing.startTime: null found, integer expected\",\"jsonPath\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[0].state_time_speed.movementEventList[0].timing.startTime\",\"schemaPath\":\"#/$defs/J2735TimeMark/type\"},{\"message\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[0].state_time_speed.movementEventList[0].timing.nextTime: null found, integer expected\",\"jsonPath\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[0].state_time_speed.movementEventList[0].timing.nextTime\",\"schemaPath\":\"#/$defs/J2735TimeMark/type\"},{\"message\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[1].state_time_speed.movementEventList[0].timing.startTime: null found, integer expected\",\"jsonPath\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[1].state_time_speed.movementEventList[0].timing.startTime\",\"schemaPath\":\"#/$defs/J2735TimeMark/type\"},{\"message\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[1].state_time_speed.movementEventList[0].timing.nextTime: null found, integer expected\",\"jsonPath\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[1].state_time_speed.movementEventList[0].timing.nextTime\",\"schemaPath\":\"#/$defs/J2735TimeMark/type\"},{\"message\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[2].state_time_speed.movementEventList[0].timing.startTime: null found, integer expected\",\"jsonPath\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[2].state_time_speed.movementEventList[0].timing.startTime\",\"schemaPath\":\"#/$defs/J2735TimeMark/type\"},{\"message\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[2].state_time_speed.movementEventList[0].timing.nextTime: null found, integer expected\",\"jsonPath\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[2].state_time_speed.movementEventList[0].timing.nextTime\",\"schemaPath\":\"#/$defs/J2735TimeMark/type\"}],\"revision\":0,\"status\":{\"manualControlIsEnabled\":true,\"stopTimeIsActivated\":false,\"failureFlash\":true,\"preemptIsActive\":false,\"signalPriorityIsActive\":false,\"fixedTimeOperation\":false,\"trafficDependentOperation\":false,\"standbyOperation\":false,\"failureMode\":false,\"off\":false,\"recentMAPmessageUpdate\":false,\"recentChangeInMAPassignedLanesIDsUsed\":false,\"noValidMAPisAvailableAtThisTime\":false,\"noValidSPATisAvailableAtThisTime\":false},\"utcTimeStamp\":\"2022-06-17T19:15:13.745Z\",\"enabledLanes\":[],\"states\":[{\"signalGroup\":2,\"stateTimeSpeed\":[{\"eventState\":\"STOP_AND_REMAIN\",\"timing\":{\"startTime\":null,\"minEndTime\":\"2022-06-17T19:15:12.5Z\",\"maxEndTime\":\"2022-06-17T19:15:12.5Z\",\"likelyTime\":null,\"confidence\":null,\"nextTime\":null}}]},{\"signalGroup\":4,\"stateTimeSpeed\":[{\"eventState\":\"STOP_AND_REMAIN\",\"timing\":{\"startTime\":null,\"minEndTime\":\"2022-06-17T19:15:12.2Z\",\"maxEndTime\":\"2022-06-17T19:15:12.2Z\",\"likelyTime\":null,\"confidence\":null,\"nextTime\":null}}]},{\"signalGroup\":6,\"stateTimeSpeed\":[{\"eventState\":\"STOP_AND_REMAIN\",\"timing\":{\"startTime\":null,\"minEndTime\":\"2022-06-17T19:15:12.5Z\",\"maxEndTime\":\"2022-06-17T19:15:12.5Z\",\"likelyTime\":null,\"confidence\":null,\"nextTime\":null}}]}]}";
    static String spatString = "{\"metadata\":{\"logFileName\":\"\",\"recordType\":\"spatTx\",\"securityResultCode\":\"success\",\"receivedMessageDetails\":{\"rxSource\":\"NA\"},\"payloadType\":\"us.dot.its.jpo.ode.model.OdeSpatPayload\",\"serialId\":{\"streamId\":\"1c0a9b7e-b4c2-41db-bbb0-315101331a4f\",\"bundleSize\":1,\"bundleId\":0,\"recordId\":0,\"serialNumber\":0},\"odeReceivedAt\":\"2024-05-09T17:16:05.703050114Z\",\"schemaVersion\":6,\"maxDurationTime\":0,\"recordGeneratedAt\":\"\",\"sanitized\":false,\"odePacketID\":\"\",\"odeTimStartDateTime\":\"\",\"spatSource\":\"V2X\",\"originIp\":\"172.18.0.1\",\"isCertPresent\":false},\"payload\":{\"data\":{\"intersectionStateList\":{\"intersectionStatelist\":[{\"id\":{\"id\":12111},\"revision\":0,\"status\":{\"failureFlash\":false,\"noValidSPATisAvailableAtThisTime\":false,\"fixedTimeOperation\":false,\"standbyOperation\":false,\"trafficDependentOperation\":false,\"manualControlIsEnabled\":false,\"off\":false,\"stopTimeIsActivated\":false,\"recentChangeInMAPassignedLanesIDsUsed\":false,\"recentMAPmessageUpdate\":false,\"failureMode\":false,\"noValidMAPisAvailableAtThisTime\":false,\"signalPriorityIsActive\":false,\"preemptIsActive\":false},\"timeStamp\":35176,\"states\":{\"movementList\":[{\"signalGroup\":2,\"state_time_speed\":{\"movementEventList\":[{\"eventState\":\"PROTECTED_MOVEMENT_ALLOWED\",\"timing\":{\"minEndTime\":22120,\"maxEndTime\":22121}}]}},{\"signalGroup\":4,\"state_time_speed\":{\"movementEventList\":[{\"eventState\":\"STOP_AND_REMAIN\",\"timing\":{\"minEndTime\":22181,\"maxEndTime\":22181}}]}},{\"signalGroup\":6,\"state_time_speed\":{\"movementEventList\":[{\"eventState\":\"PROTECTED_MOVEMENT_ALLOWED\",\"timing\":{\"minEndTime\":22120,\"maxEndTime\":22121}}]}},{\"signalGroup\":8,\"state_time_speed\":{\"movementEventList\":[{\"eventState\":\"STOP_AND_REMAIN\",\"timing\":{\"minEndTime\":21852,\"maxEndTime\":21852}}]}},{\"signalGroup\":1,\"state_time_speed\":{\"movementEventList\":[{\"eventState\":\"STOP_AND_REMAIN\",\"timing\":{\"minEndTime\":21852,\"maxEndTime\":21852}}]}},{\"signalGroup\":5,\"state_time_speed\":{\"movementEventList\":[{\"eventState\":\"STOP_AND_REMAIN\",\"timing\":{\"minEndTime\":21852,\"maxEndTime\":21852}}]}}]}}]}},\"dataType\":\"us.dot.its.jpo.ode.plugin.j2735.J2735SPAT\"}}";

    public static List<ProcessedSpat> getProcessedSpats(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        ArrayList<ProcessedSpat> spats = new ArrayList<>();
        
        try {
            ProcessedSpat spat = objectMapper.readValue(processedSpatString, ProcessedSpat.class);
            spats.add(spat);
        } catch (JsonMappingException e) {
            System.out.println("A Json Mapping Exception Occurred while trying to get data from mocked spat.");
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            System.out.println("A Json Processing Exception Occurred while trying to get data from a mocked spat.");
            e.printStackTrace();
        }
        return spats;
    }

    public static List<OdeSpatData> getJsonSpats(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        ArrayList<OdeSpatData> spats = new ArrayList<>();
        
        try {
            OdeSpatData spat = objectMapper.readValue(spatString, OdeSpatData.class);
            spats.add(spat);
        } catch (JsonMappingException e) {
            System.out.println("A Json Mapping Exception Occurred while trying to get data from mocked spat.");
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            System.out.println("A Json Processing Exception Occurred while trying to get data from a mocked spat.");
            e.printStackTrace();
        }
        return spats;
    }

}



    