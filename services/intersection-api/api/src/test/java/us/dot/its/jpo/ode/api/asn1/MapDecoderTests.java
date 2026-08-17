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
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.geojsonconverter.validator.MapJsonValidator;
import us.dot.its.jpo.ode.model.OdeMessageFrameData;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

@ExtendWith(MockitoExtension.class)
public class MapDecoderTests {

    private MapDecoder mapDecoder;

    private String odeMapDecodedXmlReference;
    private String odeMapDecodedJsonReference;
    private String processedMapReference;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    MessageFrameCodec messageFrameCodec;

    @BeforeEach
    void setUp() throws IOException {
        mapDecoder = new MapDecoder(messageFrameCodec, new MapJsonValidator());

        odeMapDecodedXmlReference = new String(
                Files.readAllBytes(Paths.get("src/test/resources/xml/Ode.ReferenceMapXER.xml")));

        odeMapDecodedJsonReference = new String(
                Files.readAllBytes(Paths.get("src/test/resources/json/map/Ode.ReferenceMapJson.json")));

        processedMapReference = new String(
                Files.readAllBytes(Paths.get("src/test/resources/json/map/GJC.ReferenceProcessedMapJson.json")));
    }

    /**
     * Test verifying the conversion from String XML data to OdeMessageFrame Object
     */
    @Test
    public void testGetAsMessageFrame() throws JsonProcessingException {
        OdeMessageFrameData spat = mapDecoder.convertXERToMessageFrame(odeMapDecodedXmlReference);

        spat.getMetadata().setOdeReceivedAt("2025-08-29T16:09:34.416Z");
        spat.getMetadata()
                .setSerialId(spat.getMetadata().getSerialId().setStreamId("44a6d71c-8af1-4f45-848c-10bd7f919be8"));

        assertThatJson(odeMapDecodedJsonReference).isEqualTo(spat.toJson());
    }

    /**
     * Test to verify Conversion from a OdeMessageFrame object to a ProcessedMAP Object
     */
    @Test
    public void testConvertMessageFrameToProcessedMap() throws JsonProcessingException {
        OdeMessageFrameData mapMessageFrame = objectMapper.readValue(odeMapDecodedJsonReference,
                OdeMessageFrameData.class);

        ProcessedMap<LineString> map = mapDecoder.convertMessageFrameToProcessedMap(mapMessageFrame);

        map.getProperties().setOdeReceivedAt(ZonedDateTime.parse("2025-08-29T16:09:34.416Z"));

        assertThatJson(processedMapReference).isEqualTo(map.toString());
    }
}
