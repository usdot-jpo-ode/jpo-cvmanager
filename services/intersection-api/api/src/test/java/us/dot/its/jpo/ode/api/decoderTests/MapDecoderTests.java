package us.dot.its.jpo.ode.api.decoderTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import us.dot.its.jpo.geojsonconverter.DateJsonMapper;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.asn1.MapDecoder;
import us.dot.its.jpo.ode.model.OdeMessageFrameData;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class MapDecoderTests {

    private final MapDecoder mapDecoder;

    private String odeMapDecodedXmlReference = "";
    private String odeMapDecodedJsonReference = "";
    private String processedMapReference = "";

    ObjectMapper objectMapper;

    @Autowired
    public MapDecoderTests(MapDecoder mapDecoder) {
        this.mapDecoder = mapDecoder;

        objectMapper = DateJsonMapper.getInstance();

        try {

            odeMapDecodedXmlReference = new String(
                    Files.readAllBytes(Paths.get("src/test/resources/xml/Ode.ReferenceMapXER.xml")));

            odeMapDecodedJsonReference = new String(
                    Files.readAllBytes(Paths
                            .get("src/test/resources/json/map/Ode.ReferenceMapJson.json")))
                    .replaceAll("\n", "").replaceAll(" ", "");

            processedMapReference = new String(
                    Files.readAllBytes(Paths
                            .get("src/test/resources/json/map/GJC.ReferenceProcessedMapJson.json")))
                    .replaceAll("\n", "").replaceAll(" ", "");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Test verifying the conversion from String XML data to OdeMessageFrame
     * Object
     */
    @Test
    public void testGetAsMessageFrame() {
        try {
            OdeMessageFrameData spat = mapDecoder.convertXERToMessageFrame(odeMapDecodedXmlReference);

            spat.getMetadata().setOdeReceivedAt("2025-08-29T16:09:34.416Z");
            spat.getMetadata()
                    .setSerialId(spat.getMetadata().getSerialId().setStreamId("44a6d71c-8af1-4f45-848c-10bd7f919be8"));

            assertEquals(spat.toJson().replaceAll("\n", "").replaceAll(" ", ""), odeMapDecodedJsonReference);
        } catch (JsonProcessingException e) {
            assertEquals(true, false);
        }
    }

    /**
     * Test to verify Conversion from a OdeMessageFrame object to a ProcessedMAP
     * Object
     */
    @Test
    public void testConvertMessageFrameToProcessedMap() {
        ObjectMapper objectMapper = DateJsonMapper.getInstance();

        try {
            OdeMessageFrameData mapMessageFrame = objectMapper.readValue(odeMapDecodedJsonReference,
                    OdeMessageFrameData.class);

            ProcessedMap<LineString> map = mapDecoder.convertMessageFrameToProcessedMap(mapMessageFrame);

            map.getProperties().setOdeReceivedAt(ZonedDateTime.parse("2025-08-29T16:09:34.416Z"));

            assertEquals(map.toString().replaceAll("\n", "").replaceAll(" ", ""), processedMapReference);
        } catch (JsonProcessingException e) {
            assertEquals(true, false);
        }
    }
}
