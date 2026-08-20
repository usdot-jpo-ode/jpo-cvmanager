package us.dot.its.jpo.ode.api.asn1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import j2735ffm.MessageFrameCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.geojsonconverter.DateJsonMapper;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.geojsonconverter.validator.SpatJsonValidator;
import us.dot.its.jpo.ode.model.OdeMessageFrameData;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

@ExtendWith(MockitoExtension.class)
public class SpatDecoderTests {

    private SpatDecoder spatDecoder;

    private String odeSpatDecodedXmlReference;
    private String odeSpatDecodedJsonReference;
    private String processedSpatReference;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    MessageFrameCodec messageFrameCodec;

    @BeforeEach
    void setUp() throws IOException {
        spatDecoder = new SpatDecoder(messageFrameCodec, new SpatJsonValidator());

        odeSpatDecodedXmlReference = new String(
                Files.readAllBytes(Paths.get("src/test/resources/xml/Ode.ReferenceSpatXER.xml")));

        odeSpatDecodedJsonReference = new String(
                Files.readAllBytes(Paths.get("src/test/resources/json/spat/Ode.ReferenceSpatJson.json")));

        processedSpatReference = new String(
                Files.readAllBytes(Paths.get("src/test/resources/json/spat/GJC.ReferenceProcessedSpatJson.json")));
    }

    /**
     * Test verifying the conversion from String XML data to OdeMessageFrame Object
     */
    @Test
    public void testGetAsMessageFrame() throws JsonProcessingException {
        OdeMessageFrameData spat = spatDecoder.convertXERToMessageFrame(odeSpatDecodedXmlReference);

        spat.getMetadata().setOdeReceivedAt("2025-08-29T16:09:34.416Z");
        spat.getMetadata()
                .setSerialId(spat.getMetadata().getSerialId().setStreamId("44a6d71c-8af1-4f45-848c-10bd7f919be8"));

        assertThatJson(odeSpatDecodedJsonReference).isEqualTo(spat.toJson());
    }

    /**
     * Test to verify Conversion from a OdeMessageFrame object to a ProcessedSPAT Object
     */
    @Test
    public void testConvertMessageFrameToProcessedSpat() throws JsonProcessingException {
        OdeMessageFrameData spatMessageFrame = objectMapper.readValue(odeSpatDecodedJsonReference,
                OdeMessageFrameData.class);

        spatMessageFrame.getMetadata().setOdeReceivedAt("2025-08-29T16:09:34.416Z");

        ProcessedSpat spat = spatDecoder.convertMessageFrameToProcessedSpat(spatMessageFrame);

        spat.setOdeReceivedAt("2025-08-29T16:09:34.416Z");

        assertThatJson(processedSpatReference).isEqualTo(spat.toString());
    }
}
