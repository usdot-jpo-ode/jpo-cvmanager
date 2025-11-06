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
import us.dot.its.jpo.ode.api.asn1.SsmDecoder;
import us.dot.its.jpo.ode.model.OdeMessageFrameData;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class SsmDecoderTests {

    private final SsmDecoder ssmDecoder;

    private String odeSsmDecodedXmlReference = "";
    private String odeSsmDecodedJsonReference = "";

    ObjectMapper objectMapper;

    @Autowired
    public SsmDecoderTests(SsmDecoder ssmDecoder) {
        this.ssmDecoder = ssmDecoder;

        objectMapper = DateJsonMapper.getInstance();

        try {
            odeSsmDecodedXmlReference = new String(
                    Files.readAllBytes(Paths.get("src/test/resources/xml/Ode.ReferenceSsmXER.xml")));

            odeSsmDecodedJsonReference = new String(
                    Files.readAllBytes(Paths
                            .get("src/test/resources/json/ssm/Ode.ReferenceSsmJson.json")));

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
            OdeMessageFrameData ssm = ssmDecoder.convertXERToMessageFrame(odeSsmDecodedXmlReference);

            ssm.getMetadata().setOdeReceivedAt("2025-08-29T16:09:34.416Z");
            ssm.getMetadata()
                    .setSerialId(ssm.getMetadata().getSerialId().setStreamId("44a6d71c-8af1-4f45-848c-10bd7f919be8"));

            assertThatJson(odeSsmDecodedJsonReference).isEqualTo(ssm.toJson());
        } catch (JsonProcessingException e) {
            assertEquals(true, false);
        }
    }
}
