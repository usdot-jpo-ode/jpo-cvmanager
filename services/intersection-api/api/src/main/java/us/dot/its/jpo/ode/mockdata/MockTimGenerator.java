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
import us.dot.its.jpo.asn.j2735.r2024.TravelerInformation.TravelerInformation;

@Slf4j
public class MockTimGenerator {

    public static List<TravelerInformation> getJsonTims() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ArrayList<TravelerInformation> tims = new ArrayList<>();

        try {
            String timString = new String(Files.readAllBytes(Paths.get("src/main/resources/mockdata/tim.json")));
            TravelerInformation tim = objectMapper.readValue(timString,
                    TravelerInformation.class);
            tims.add(tim);
        } catch (JsonMappingException e) {
            log.error("JsonMappingException", e);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException", e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return tims;
    }

}
