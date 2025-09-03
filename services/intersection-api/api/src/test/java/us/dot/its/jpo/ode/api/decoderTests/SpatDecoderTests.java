package us.dot.its.jpo.ode.api.decoderTests;

import static org.junit.Assume.assumeTrue;
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
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.asn1.SpatDecoder;
import us.dot.its.jpo.ode.model.OdeMessageFrameData;

import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class SpatDecoderTests {

    private final SpatDecoder spatDecoder;

    private String rawSpatReference = "";
    private String odeSpatDecodedXmlReference = "";
    private String odeSpatDecodedJsonReference = "";
    private String processedSpatReference = "";

    ObjectMapper objectMapper;

    @Autowired
    public SpatDecoderTests(SpatDecoder spatDecoder) {
        this.spatDecoder = spatDecoder;

        objectMapper = DateJsonMapper.getInstance();

        try {
            rawSpatReference = new String(
                    Files.readAllBytes(Paths.get("src/test/resources/uper/ReferenceSpatUPER.txt")));

            odeSpatDecodedXmlReference = new String(
                    Files.readAllBytes(Paths.get("src/test/resources/xml/Ode.ReferenceSpatXER.xml")));

            odeSpatDecodedJsonReference = new String(
                    Files.readAllBytes(Paths
                            .get("src/test/resources/json/spat/Ode.ReferenceSpatJson.json")))
                    .replaceAll("\n", "").replaceAll(" ", "");

            processedSpatReference = new String(
                    Files.readAllBytes(Paths
                            .get("src/test/resources/json/spat/GJC.ReferenceProcessedSpatJson.json")))
                    .replaceAll("\n", "").replaceAll(" ", "");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Test to Decode a raw SPAT into an XML String. May not work if host machine is
     * Windows and .so library is not properly linked.
     * If system is missing required libraries, the test will be skipped.
     */
    @Test
    public void testDecodeAsnToXERString() {
        try {
            String result = spatDecoder.decodeAsnToXERString(rawSpatReference);
            // System.out.println(result);
            assertEquals(result, odeSpatDecodedXmlReference);
        } catch (java.lang.ExceptionInInitializerError e) {
            // Ignore errors due to missing native libraries during testing.
            assumeTrue("Skipping testDecodeAsnToXERString test because system is missing required libraries", false);
        }
    }

    /**
     * Test verifying the conversion from String XML data to OdeMessageFrame
     * Object
     */
    @Test
    public void testGetAsMessageFrame() {
        try {
            OdeMessageFrameData spat = spatDecoder.convertXERToMessageFrame(odeSpatDecodedXmlReference);

            spat.getMetadata().setOdeReceivedAt("2025-08-29T16:09:34.416Z");
            spat.getMetadata()
                    .setSerialId(spat.getMetadata().getSerialId().setStreamId("44a6d71c-8af1-4f45-848c-10bd7f919be8"));

            assertEquals(spat.toJson().replaceAll("\n", "").replaceAll(" ", ""), odeSpatDecodedJsonReference);
        } catch (JsonProcessingException e) {
            assertEquals(true, false);
        }
    }

    /**
     * Test to verify Conversion from a OdeMessageFrame object to a ProcessedSPAT
     * Object
     */
    @Test
    public void testConvertMessageFrameToProcessedSpat() {

        try {
            OdeMessageFrameData spatMessageFrame = objectMapper.readValue(odeSpatDecodedJsonReference,
                    OdeMessageFrameData.class);

            spatMessageFrame.getMetadata().setOdeReceivedAt("2025-08-29T16:09:34.416Z");

            ProcessedSpat spat = spatDecoder.convertMessageFrameToProcessedSpat(spatMessageFrame);

            spat.setOdeReceivedAt("2025-08-29T16:09:34.416Z");

            assertEquals(spat.toString().replaceAll("\n", "").replaceAll(" ", ""), processedSpatReference);
        } catch (JsonProcessingException e) {
            assertEquals(true, false);
        }
    }
}
