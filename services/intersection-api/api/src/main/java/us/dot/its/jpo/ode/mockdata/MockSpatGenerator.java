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
import us.dot.its.jpo.asn.j2735.r2024.SPAT.SPAT;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;

@Slf4j
public class MockSpatGenerator {

    public static List<ProcessedSpat> getProcessedSpats() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ArrayList<ProcessedSpat> spats = new ArrayList<>();

        try {
            String processedSpatString = new String(
                    Files.readAllBytes(Paths.get("src/main/resources/mockdata/processed_spat.json")));
            ProcessedSpat spat = objectMapper.readValue(processedSpatString, ProcessedSpat.class);
            spats.add(spat);
        } catch (JsonMappingException e) {
            log.error("JsonMappingException", e);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException", e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return spats;
    }

    public static List<SPAT> getJsonSpats() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ArrayList<SPAT> spats = new ArrayList<>();

        try {
            String spatString = new String(Files.readAllBytes(Paths.get("src/main/resources/mockdata/spat.json")));
            SPAT spat = objectMapper.readValue(spatString, SPAT.class);
            spats.add(spat);
        } catch (JsonMappingException e) {
            log.error("JsonMappingException", e);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException", e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return spats;
    }

}
