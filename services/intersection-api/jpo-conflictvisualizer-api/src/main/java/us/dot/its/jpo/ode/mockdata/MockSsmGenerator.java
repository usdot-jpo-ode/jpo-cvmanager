package us.dot.its.jpo.ode.mockdata;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import us.dot.its.jpo.ode.model.OdeSsmData;
public class MockSsmGenerator {

    static String ssmString = "{\"metadata\":{\"logFileName\":\"\",\"recordType\":\"ssmTx\",\"receivedMessageDetails\":{\"rxSource\":\"NA\"},\"payloadType\":\"us.dot.its.jpo.ode.model.OdeSsmPayload\",\"serialId\":{\"streamId\":\"e6f21a57-6952-402a-86c8-49e8fcb727c1\",\"bundleSize\":1,\"bundleId\":0,\"recordId\":0,\"serialNumber\":0},\"odeReceivedAt\":\"2024-05-09T15:27:17.557565954Z\",\"schemaVersion\":6,\"maxDurationTime\":0,\"recordGeneratedAt\":\"\",\"sanitized\":false,\"odePacketID\":\"\",\"odeTimStartDateTime\":\"\",\"originIp\":\"172.18.0.1\",\"ssmSource\":\"RSU\"},\"payload\":{\"data\":{\"second\":0,\"status\":{\"signalStatus\":[{\"sequenceNumber\":0,\"id\":{\"id\":12110},\"sigStatus\":{\"signalStatusPackage\":[{\"requester\":{\"id\":{\"stationID\":2366845094},\"request\":3,\"sequenceNumber\":0,\"typeData\":{\"role\":\"publicTransport\"}},\"inboundOn\":{\"lane\":23},\"status\":\"granted\"}]}}]}},\"dataType\":\"us.dot.its.jpo.ode.plugin.j2735.J2735SSM\"}}";

    public static List<OdeSsmData> getJsonSsms(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        ArrayList<OdeSsmData> ssms = new ArrayList<>();
        
        try {
            OdeSsmData ssm = objectMapper.readValue(ssmString, OdeSsmData.class);
            ssms.add(ssm);
        } catch (JsonMappingException e) {
            System.out.println("A Json Mapping Exception Occurred while trying to get data from mocked ssm.");
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            System.out.println("A Json Processing Exception Occurred while trying to get data from a mocked ssm.");
            e.printStackTrace();
        }
        return ssms;
    }

}



    
