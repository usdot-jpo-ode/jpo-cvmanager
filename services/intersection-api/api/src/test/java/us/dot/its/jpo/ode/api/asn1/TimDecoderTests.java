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

import us.dot.its.jpo.ode.model.OdeMessageFrameData;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

@ExtendWith(MockitoExtension.class)
public class TimDecoderTests {

    private TimDecoder timDecoder;

    private String odeTimDecodedXmlReference;
    private String odeTimDecodedJsonReference;

    @Mock
    MessageFrameCodec messageFrameCodec;

    @BeforeEach
    void setUp() throws IOException {
        timDecoder = new TimDecoder(messageFrameCodec);

        odeTimDecodedXmlReference = new String(
                Files.readAllBytes(Paths.get("src/test/resources/xml/Ode.ReferenceTimXER.xml")));

        odeTimDecodedJsonReference = new String(
                Files.readAllBytes(Paths.get("src/test/resources/json/tim/Ode.ReferenceTimJson.json")));
    }

    /**
     * Test verifying the conversion from String XML data to OdeMessageFrame Object
     */
    @Test
    public void testGetAsMessageFrame() throws JsonProcessingException {
        OdeMessageFrameData tim = timDecoder.convertXERToMessageFrame(odeTimDecodedXmlReference);

        tim.getMetadata().setOdeReceivedAt("2025-08-29T16:09:34.416Z");
        tim.getMetadata()
                .setSerialId(tim.getMetadata().getSerialId().setStreamId("44a6d71c-8af1-4f45-848c-10bd7f919be8"));

        assertThatJson(odeTimDecodedJsonReference).isEqualTo(tim.toJson());
    }
}
