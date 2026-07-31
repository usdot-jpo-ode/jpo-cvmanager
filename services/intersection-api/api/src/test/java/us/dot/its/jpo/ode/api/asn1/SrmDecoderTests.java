package us.dot.its.jpo.ode.api.asn1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;

import j2735ffm.MessageFrameCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.geojsonconverter.DateJsonMapper;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.srm.ProcessedSrm;
import us.dot.its.jpo.ode.model.OdeMessageFrameData;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class SrmDecoderTests {

    private SrmDecoder srmDecoder;
    private String odeSrmDecodedXmlReference = "";
    private String odeSrmDecodedJsonReference = "";
    private String processedSrmReference = "";
    private String odeReceivedAt = "2025-08-29T16:09:34.416Z";

    ObjectMapper objectMapper;

    @Mock
    MessageFrameCodec messageFrameCodec;

    @BeforeEach
    void setUp() throws IOException {
        srmDecoder = new SrmDecoder(messageFrameCodec);

        objectMapper = DateJsonMapper.getInstance();

        try {
            odeSrmDecodedXmlReference = new String(
                    Files.readAllBytes(Paths.get("src/test/resources/xml/Ode.ReferenceSrmXER.xml")));

            odeSrmDecodedJsonReference = new String(
                    Files.readAllBytes(Paths
                            .get("src/test/resources/json/srm/Ode.ReferenceSrmJson.json")))
                    .replaceAll("\n", "").replaceAll(" ", "");

            processedSrmReference = new String(
                    Files.readAllBytes(Paths
                            .get("src/test/resources/json/srm/GJC.ReferenceProcessedSrmJson.json")))
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
            OdeMessageFrameData srm = srmDecoder.convertXERToMessageFrame(odeSrmDecodedXmlReference);

            srm.getMetadata().setOdeReceivedAt(odeReceivedAt);
            srm.getMetadata()
                    .setSerialId(srm.getMetadata().getSerialId().setStreamId("44a6d71c-8af1-4f45-848c-10bd7f919be8"));

            assertThatJson(odeSrmDecodedJsonReference).isEqualTo(srm.toJson());
        } catch (JsonProcessingException e) {
            assertEquals(true, false);
        }
    }

    /**
     * Test to verify Conversion from a OdeMessageFrame object to a ProcessedSrm
     * Object
     */
    @Test
    public void testConvertMessageFrameToProcessedSrm() {

        try {
            OdeMessageFrameData srmMessageFrame = objectMapper.readValue(odeSrmDecodedJsonReference,
                    OdeMessageFrameData.class);

            srmMessageFrame.getMetadata().setOdeReceivedAt(odeReceivedAt);

            ProcessedSrm srm = srmDecoder.convertMessageFrameToProcessedSrm(srmMessageFrame);

            srm.getProperties().setOdeReceivedAt(ZonedDateTime.parse(odeReceivedAt));

            assertEquals(srm.toString().replaceAll("\n", "").replaceAll(" ", ""), processedSrmReference);
        } catch (JsonProcessingException e) {
            assertEquals(true, false);
        }
    }
}
