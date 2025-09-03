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
import us.dot.its.jpo.ode.api.asn1.TimDecoder;
import us.dot.its.jpo.ode.model.OdeMessageFrameData;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class TimDecoderTests {

    private final TimDecoder timDecoder;

    private String rawTimReference = "";
    private String odeTimDecodedXmlReference = "";
    private String odeTimDecodedJsonReference = "";

    ObjectMapper objectMapper;

    @Autowired
    public TimDecoderTests(TimDecoder timDecoder) {
        this.timDecoder = timDecoder;

        objectMapper = DateJsonMapper.getInstance();

        try {
            rawTimReference = new String(
                    Files.readAllBytes(Paths.get("src/test/resources/uper/ReferenceTimUPER.txt")));
            odeTimDecodedXmlReference = new String(
                    Files.readAllBytes(Paths.get("src/test/resources/xml/Ode.ReferenceTimXER.xml")));

            odeTimDecodedJsonReference = new String(
                    Files.readAllBytes(Paths
                            .get("src/test/resources/json/tim/Ode.ReferenceTimJson.json")))
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
            OdeMessageFrameData tim = timDecoder.convertXERToMessageFrame(odeTimDecodedXmlReference);

            tim.getMetadata().setOdeReceivedAt("2025-08-29T16:09:34.416Z");
            tim.getMetadata()
                    .setSerialId(tim.getMetadata().getSerialId().setStreamId("44a6d71c-8af1-4f45-848c-10bd7f919be8"));

            assertEquals(tim.toJson().replaceAll("\n", "").replaceAll(" ", ""), odeTimDecodedJsonReference);
        } catch (JsonProcessingException e) {
            assertEquals(true, false);
        }
    }
}
