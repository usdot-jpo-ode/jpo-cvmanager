package us.dot.its.jpo.ode.mockdata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.asn.j2735.r2024.SignalRequestMessage.SignalRequestMessage;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.srm.ProcessedSrm;

@Slf4j
public class MockSrmGenerator {

    public static List<SignalRequestMessage> getJsonSrms() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ArrayList<SignalRequestMessage> srms = new ArrayList<>();

        try {
            String srmString = new String(Files.readAllBytes(Paths.get("src/main/resources/mockdata/srm.json")));
            SignalRequestMessage srm = objectMapper.readValue(srmString,
                    SignalRequestMessage.class);
            srms.add(srm);
        } catch (JsonMappingException e) {
            log.error("JsonMappingException", e);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException", e);
        } catch (IOException e) {
            log.error("IOException", e);
            e.printStackTrace();
        }
        return srms;
    }

    public static List<ProcessedSrm> getProcessedSrms() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ArrayList<ProcessedSrm> srms = new ArrayList<>();

        try {
            String processedSrmString = new String(
                    Files.readAllBytes(Paths.get("src/main/resources/mockdata/processed_srm.json")));
            ProcessedSrm srm = objectMapper.readValue(processedSrmString, ProcessedSrm.class);
            srms.add(srm);
        } catch (JsonMappingException e) {
            log.error("JsonMappingException", e);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException", e);
        } catch (IOException e) {
            log.error("IOException", e);
            e.printStackTrace();
        }
        return srms;
    }

}
