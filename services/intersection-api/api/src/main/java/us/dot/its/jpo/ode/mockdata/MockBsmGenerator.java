package us.dot.its.jpo.ode.mockdata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.Point;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.bsm.ProcessedBsm;
import us.dot.its.jpo.ode.model.OdeMessageFrameData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Slf4j
public class MockBsmGenerator {
    static TypeReference<ProcessedBsm<Point>> processedBsmTypeReference = new TypeReference<>() {
    };

    public static List<OdeMessageFrameData> getJsonBsms() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ArrayList<OdeMessageFrameData> bsms = new ArrayList<>();

        try {
            String bsmString = new String(Files.readAllBytes(Paths.get("src/main/resources/mockdata/bsm.json")));
            OdeMessageFrameData bsm = objectMapper.readValue(bsmString,
                    OdeMessageFrameData.class);
            bsms.add(bsm);
        } catch (JsonMappingException e) {
            log.error("JsonMappingException", e);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException", e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bsms;
    }

    public static List<ProcessedBsm<Point>> getProcessedBsms() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ArrayList<ProcessedBsm<Point>> bsms = new ArrayList<>();

        try {
            String processedBsmString = new String(
                    Files.readAllBytes(Paths.get("src/main/resources/mockdata/processed_bsm.json")));
            ProcessedBsm<Point> bsm = objectMapper.readValue(processedBsmString,
                    processedBsmTypeReference);

            bsms.add(bsm);
        } catch (JsonMappingException e) {
            log.error("JsonMappingException", e);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException", e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bsms;
    }
}
