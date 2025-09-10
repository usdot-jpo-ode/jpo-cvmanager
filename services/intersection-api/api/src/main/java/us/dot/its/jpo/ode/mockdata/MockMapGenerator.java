package us.dot.its.jpo.ode.mockdata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.asn.j2735.r2024.MapData.MapData;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;

@Slf4j
public class MockMapGenerator {

    static TypeReference<ProcessedMap<LineString>> typeReference = new TypeReference<>() {
    };

    public static List<ProcessedMap<LineString>> getProcessedMaps() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ArrayList<ProcessedMap<LineString>> maps = new ArrayList<>();

        try {
            String processedMapString = new String(
                    Files.readAllBytes(Paths.get("src/main/resources/mockdata/processed_map.json")));
            ProcessedMap<LineString> map = objectMapper.readValue(processedMapString, typeReference);
            maps.add(map);
        } catch (JsonMappingException e) {
            log.error("JsonMappingException", e);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException", e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return maps;
    }

    public static List<MapData> getJsonMaps() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ArrayList<MapData> maps = new ArrayList<>();

        try {
            String mapString = new String(Files.readAllBytes(Paths.get("src/main/resources/mockdata/map.json")));
            MapData map = objectMapper.readValue(mapString, MapData.class);
            maps.add(map);
        } catch (JsonMappingException e) {
            log.error("JsonMappingException", e);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException", e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return maps;
    }
}
