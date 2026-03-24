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
public class SrmDecoderTests {

    private SrmDecoder srmDecoder;

    private String odeSrmDecodedXmlReference;
    private String odeSrmDecodedJsonReference;

    @Mock
    MessageFrameCodec messageFrameCodec;

    @BeforeEach
    void setUp() throws IOException {
        srmDecoder = new SrmDecoder(messageFrameCodec);

        odeSrmDecodedXmlReference = new String(
                Files.readAllBytes(Paths.get("src/test/resources/xml/Ode.ReferenceSrmXER.xml")));

        odeSrmDecodedJsonReference = new String(
                Files.readAllBytes(Paths.get("src/test/resources/json/srm/Ode.ReferenceSrmJson.json")))
                .replaceAll("\n", "").replaceAll(" ", "");
    }

    /**
     * Test verifying the conversion from String XML data to OdeMessageFrame Object
     */
    @Test
    public void testGetAsMessageFrame() throws JsonProcessingException {
        OdeMessageFrameData srm = srmDecoder.convertXERToMessageFrame(odeSrmDecodedXmlReference);

        srm.getMetadata().setOdeReceivedAt("2025-08-29T16:09:34.416Z");
        srm.getMetadata()
                .setSerialId(srm.getMetadata().getSerialId().setStreamId("44a6d71c-8af1-4f45-848c-10bd7f919be8"));

        assertThatJson(odeSrmDecodedJsonReference).isEqualTo(srm.toJson());
    }
}
