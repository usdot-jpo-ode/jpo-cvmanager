package us.dot.its.jpo.ode.api.decoderTests;

import static org.junit.Assume.assumeTrue;
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
import us.dot.its.jpo.ode.api.asn1.SrmDecoder;
import us.dot.its.jpo.ode.model.OdeMessageFrameData;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class SrmDecoderTests {

    private final SrmDecoder srmDecoder;

    private String rawSrmReference = "";
    private String odeSrmDecodedXmlReference = "";
    private String odeSrmDecodedJsonReference = "";

    ObjectMapper objectMapper;

    @Autowired
    public SrmDecoderTests(SrmDecoder srmDecoder) {
        this.srmDecoder = srmDecoder;

        objectMapper = DateJsonMapper.getInstance();

        try {
            rawSrmReference = new String(
                    Files.readAllBytes(Paths.get("src/test/resources/uper/ReferenceSrmUPER.txt")));
            odeSrmDecodedXmlReference = new String(
                    Files.readAllBytes(Paths.get("src/test/resources/xml/Ode.ReferenceSrmXER.xml")));

            odeSrmDecodedJsonReference = new String(
                    Files.readAllBytes(Paths
                            .get("src/test/resources/json/srm/Ode.ReferenceSrmJson.json")))
                    .replaceAll("\n", "").replaceAll(" ", "");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Test to Decode a raw SRM into an XML String. May not work if host machine is
     * Windows and .so library is not properly linked.
     * If system is missing required libraries, the test will be skipped.
     */
    @Test
    public void testDecodeAsnToXERString() {
        try {
            String result = srmDecoder.decodeAsnToXERString(rawSrmReference);
            assertEquals(result, odeSrmDecodedXmlReference);
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
            OdeMessageFrameData srm = srmDecoder.convertXERToMessageFrame(odeSrmDecodedXmlReference);

            srm.getMetadata().setOdeReceivedAt("2025-08-29T16:09:34.416Z");
            srm.getMetadata()
                    .setSerialId(srm.getMetadata().getSerialId().setStreamId("44a6d71c-8af1-4f45-848c-10bd7f919be8"));

            assertEquals(srm.toJson().replaceAll("\n", "").replaceAll(" ", ""), odeSrmDecodedJsonReference);
        } catch (JsonProcessingException e) {
            assertEquals(true, false);
        }
    }
}
