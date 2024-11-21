package us.dot.its.jpo.ode.mockdata;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import us.dot.its.jpo.ode.model.OdeSrmData;
public class MockSrmGenerator {

    static String srmString = "{\"metadata\":{\"logFileName\":\"\",\"recordType\":\"srmTx\",\"receivedMessageDetails\":{\"rxSource\":\"NA\"},\"payloadType\":\"us.dot.its.jpo.ode.model.OdeSrmPayload\",\"serialId\":{\"streamId\":\"79a1093a-7e2f-442c-b9ae-324b5a09c030\",\"bundleSize\":1,\"bundleId\":0,\"recordId\":0,\"serialNumber\":0},\"odeReceivedAt\":\"2024-05-09T15:17:48.020668755Z\",\"schemaVersion\":6,\"maxDurationTime\":0,\"recordGeneratedAt\":\"\",\"sanitized\":false,\"odePacketID\":\"\",\"odeTimStartDateTime\":\"\",\"originIp\":\"172.18.0.1\",\"srmSource\":\"RSU\"},\"payload\":{\"data\":{\"second\":0,\"sequenceNumber\":1,\"requests\":{\"signalRequestPackage\":[{\"request\":{\"id\":{\"id\":12109},\"requestID\":4,\"requestType\":\"priorityRequest\",\"inBoundLane\":{\"lane\":13},\"outBoundLane\":{\"lane\":4}},\"duration\":10979}]},\"requestor\":{\"id\":{\"stationID\":2366845094},\"type\":{\"role\":\"publicTransport\"},\"position\":{\"position\":{\"latitude\":39.5904915,\"longitude\":-105.0913829,\"elevation\":1685.4},\"heading\":175.9000}}},\"dataType\":\"us.dot.its.jpo.ode.plugin.j2735.J2735SRM\"}}";

    public static List<OdeSrmData> getJsonSrms(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        ArrayList<OdeSrmData> srms = new ArrayList<>();
        
        try {
            OdeSrmData srm = objectMapper.readValue(srmString, OdeSrmData.class);
            srms.add(srm);
        } catch (JsonMappingException e) {
            System.out.println("A Json Mapping Exception Occurred while trying to get data from mocked srm.");
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            System.out.println("A Json Processing Exception Occurred while trying to get data from a mocked srm.");
            e.printStackTrace();
        }
        return srms;
    }

}



    