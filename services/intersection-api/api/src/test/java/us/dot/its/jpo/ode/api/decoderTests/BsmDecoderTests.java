package us.dot.its.jpo.ode.api.decoderTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
import us.dot.its.jpo.geojsonconverter.pojos.geojson.Point;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.bsm.ProcessedBsm;
import us.dot.its.jpo.ode.api.asn1.BsmDecoder;
import us.dot.its.jpo.ode.model.OdeMessageFrameData;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class BsmDecoderTests {

    private final BsmDecoder bsmDecoder;

    private String odeBsmDecodedXmlReference = "";
    private String odeBsmDecodedJsonReference = "";
    private String processedBsmReference = "";

    ObjectMapper objectMapper;

    @Autowired
    public BsmDecoderTests(BsmDecoder bsmDecoder) {
        this.bsmDecoder = bsmDecoder;

        objectMapper = DateJsonMapper.getInstance();

        try {
            odeBsmDecodedXmlReference = new String(
                    Files.readAllBytes(Paths.get("src/test/resources/xml/Ode.ReferenceBsmXER.xml")));

            odeBsmDecodedJsonReference = new String(
                    Files.readAllBytes(Paths
                            .get("src/test/resources/json/bsm/Ode.ReferenceBsmJson.json")))
                    .replaceAll("\n", "").replaceAll(" ", "");

            processedBsmReference = new String(
                    Files.readAllBytes(Paths
                            .get("src/test/resources/json/bsm/GJC.ReferenceProcessedBsmJson.json")))
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
            OdeMessageFrameData bsm = bsmDecoder.convertXERToMessageFrame(odeBsmDecodedXmlReference);

            bsm.getMetadata().setOdeReceivedAt("2025-08-29T16:09:34.416Z");
            bsm.getMetadata()
                    .setSerialId(bsm.getMetadata().getSerialId().setStreamId("44a6d71c-8af1-4f45-848c-10bd7f919be8"));

            assertEquals(bsm.toJson().replaceAll("\n", "").replaceAll(" ", ""), odeBsmDecodedJsonReference);
        } catch (JsonProcessingException e) {
            assertEquals(true, false);
        }
    }

    /**
     * Test to verify Conversion from a OdeMessageFrame object to a ProcessedBSM
     * Object
     */
    @Test
    public void testConvertMessageFrameToProcessedBsm() {
        ObjectMapper objectMapper = DateJsonMapper.getInstance();

        try {
            OdeMessageFrameData bsmMessageFrame = objectMapper.readValue(odeBsmDecodedJsonReference,
                    OdeMessageFrameData.class);

            ProcessedBsm<Point> bsm = bsmDecoder.convertMessageFrameToProcessedBsm(bsmMessageFrame);

            bsm.getProperties().setOdeReceivedAt("2025-08-29T16:09:34.416Z");

            assertEquals(bsm.toString().replaceAll("\n", "").replaceAll(" ", ""), processedBsmReference);
        } catch (JsonProcessingException e) {
            assertEquals(true, false);
        }
    }
}
