package us.dot.its.jpo.ode.mockdata;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.ode.model.OdeBsmData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class MockBsmGenerator {

    static String bsmString = "{\"metadata\": {\"bsmSource\": \"RV\", \"logFileName\": \"\", \"recordType\": \"bsmTx\", \"securityResultCode\": \"success\", \"receivedMessageDetails\": {\"locationData\": {\"latitude\": \"\", \"longitude\": \"\", \"elevation\": \"\", \"speed\": \"\", \"heading\": \"\"}, \"rxSource\": \"RV\"}, \"payloadType\": \"us.dot.its.jpo.ode.model.OdeBsmPayload\", \"serialId\": {\"streamId\": \"849c3ac6-4747-426f-ae5e-28ac8531b32c\", \"bundleSize\": 1, \"bundleId\": 0, \"recordId\": 0, \"serialNumber\": 0}, \"odeReceivedAt\": \"2022-06-17T19:14:21.124599Z\", \"schemaVersion\": 6, \"maxDurationTime\": 0, \"recordGeneratedAt\": \"\", \"sanitized\": false, \"odePacketID\": \"\", \"odeTimStartDateTime\": \"\", \"originIp\": \"10.11.81.12\"}, \"payload\": {\"data\": {\"coreData\": {\"msgCnt\": 45, \"id\": \"E6A99808\", \"secMark\": 20962, \"position\": {\"latitude\": 39.5881502, \"longitude\": -105.091045, \"elevation\": 1691.9}, \"accelSet\": {\"accelLong\": -0.05, \"accelYaw\": -0.65}, \"accuracy\": {\"semiMajor\": 2.0, \"semiMinor\": 2.0, \"orientation\": 0.0}, \"transmission\": \"UNAVAILABLE\", \"speed\": 22.62, \"heading\": 169.2, \"brakes\": {\"wheelBrakes\": {\"leftFront\": false, \"rightFront\": false, \"unavailable\": true, \"leftRear\": false, \"rightRear\": false}, \"traction\": \"unavailable\", \"abs\": \"off\", \"scs\": \"unavailable\", \"brakeBoost\": \"unavailable\", \"auxBrakes\": \"unavailable\"}, \"size\": {\"width\": 180, \"length\": 480}}}, \"dataType\": \"us.dot.its.jpo.ode.plugin.j2735.J2735Bsm\"}}";


    public static List<OdeBsmData> getJsonBsms(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        ArrayList<OdeBsmData> bsms = new ArrayList<>();
        
        try {
            OdeBsmData bsm = objectMapper.readValue(bsmString, OdeBsmData.class);
            bsms.add(bsm);
        } catch (JsonMappingException e) {
            System.out.println("A Json Mapping Exception Occurred while trying to get data from mocked BSM.");
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            System.out.println("A Json Processing Exception Occurred while trying to get data from a mocked BSM.");
            e.printStackTrace();
        }
        return bsms;
    }
}
