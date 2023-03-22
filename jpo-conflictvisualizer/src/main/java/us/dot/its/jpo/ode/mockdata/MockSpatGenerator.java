package us.dot.its.jpo.ode.mockdata;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
public class MockSpatGenerator {

    static String spatString = "{\"messageType\":\"SPAT\",\"odeReceivedAt\":\"2022-06-17T19:15:13.671068Z\",\"originIp\":\"10.11.81.12\",\"intersectionId\":12109,\"cti4501Conformant\":false,\"validationMessages\":[{\"message\":\"$.metadata.@class: is missing but it is required\",\"jsonPath\":\"$.metadata\",\"schemaPath\":\"#/$defs/OdeSpatMetadata/required\"},{\"message\":\"$.payload.@class: is missing but it is required\",\"jsonPath\":\"$.payload\",\"schemaPath\":\"#/$defs/OdeSpatPayload/required\"},{\"message\":\"$.payload.data.timeStamp: null found, integer expected\",\"jsonPath\":\"$.payload.data.timeStamp\",\"schemaPath\":\"#/$defs/J2735MinuteOfTheYear/type\"},{\"message\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].id.region: null found, integer expected\",\"jsonPath\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].id.region\",\"schemaPath\":\"#/$defs/J2735RoadRegulatorID/type\"},{\"message\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[0].state_time_speed.movementEventList[0].timing.startTime: null found, integer expected\",\"jsonPath\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[0].state_time_speed.movementEventList[0].timing.startTime\",\"schemaPath\":\"#/$defs/J2735TimeMark/type\"},{\"message\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[0].state_time_speed.movementEventList[0].timing.nextTime: null found, integer expected\",\"jsonPath\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[0].state_time_speed.movementEventList[0].timing.nextTime\",\"schemaPath\":\"#/$defs/J2735TimeMark/type\"},{\"message\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[1].state_time_speed.movementEventList[0].timing.startTime: null found, integer expected\",\"jsonPath\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[1].state_time_speed.movementEventList[0].timing.startTime\",\"schemaPath\":\"#/$defs/J2735TimeMark/type\"},{\"message\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[1].state_time_speed.movementEventList[0].timing.nextTime: null found, integer expected\",\"jsonPath\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[1].state_time_speed.movementEventList[0].timing.nextTime\",\"schemaPath\":\"#/$defs/J2735TimeMark/type\"},{\"message\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[2].state_time_speed.movementEventList[0].timing.startTime: null found, integer expected\",\"jsonPath\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[2].state_time_speed.movementEventList[0].timing.startTime\",\"schemaPath\":\"#/$defs/J2735TimeMark/type\"},{\"message\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[2].state_time_speed.movementEventList[0].timing.nextTime: null found, integer expected\",\"jsonPath\":\"$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[2].state_time_speed.movementEventList[0].timing.nextTime\",\"schemaPath\":\"#/$defs/J2735TimeMark/type\"}],\"revision\":0,\"status\":{\"manualControlIsEnabled\":true,\"stopTimeIsActivated\":false,\"failureFlash\":true,\"preemptIsActive\":false,\"signalPriorityIsActive\":false,\"fixedTimeOperation\":false,\"trafficDependentOperation\":false,\"standbyOperation\":false,\"failureMode\":false,\"off\":false,\"recentMAPmessageUpdate\":false,\"recentChangeInMAPassignedLanesIDsUsed\":false,\"noValidMAPisAvailableAtThisTime\":false,\"noValidSPATisAvailableAtThisTime\":false},\"utcTimeStamp\":\"2022-06-17T19:15:13.745Z\",\"enabledLanes\":[],\"states\":[{\"signalGroup\":2,\"stateTimeSpeed\":[{\"eventState\":\"STOP_AND_REMAIN\",\"timing\":{\"startTime\":null,\"minEndTime\":\"2022-06-17T19:15:12.5Z\",\"maxEndTime\":\"2022-06-17T19:15:12.5Z\",\"likelyTime\":null,\"confidence\":null,\"nextTime\":null}}]},{\"signalGroup\":4,\"stateTimeSpeed\":[{\"eventState\":\"STOP_AND_REMAIN\",\"timing\":{\"startTime\":null,\"minEndTime\":\"2022-06-17T19:15:12.2Z\",\"maxEndTime\":\"2022-06-17T19:15:12.2Z\",\"likelyTime\":null,\"confidence\":null,\"nextTime\":null}}]},{\"signalGroup\":6,\"stateTimeSpeed\":[{\"eventState\":\"STOP_AND_REMAIN\",\"timing\":{\"startTime\":null,\"minEndTime\":\"2022-06-17T19:15:12.5Z\",\"maxEndTime\":\"2022-06-17T19:15:12.5Z\",\"likelyTime\":null,\"confidence\":null,\"nextTime\":null}}]}]}";

    public static List<ProcessedSpat> getProcessedSpats(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        ArrayList<ProcessedSpat> spats = new ArrayList<>();
        
        try {
            ProcessedSpat spat = objectMapper.readValue(spatString, ProcessedSpat.class);
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



    