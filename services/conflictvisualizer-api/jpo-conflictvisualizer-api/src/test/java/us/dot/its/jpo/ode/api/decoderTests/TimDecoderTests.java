package us.dot.its.jpo.ode.api.decoderTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import us.dot.its.jpo.ode.api.asn1.TimDecoder;
import us.dot.its.jpo.ode.api.models.messages.TimDecodedMessage;
import us.dot.its.jpo.ode.mockdata.MockDecodedMessageGenerator;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeMsgMetadata;


@SpringBootTest
@RunWith(SpringRunner.class)
public class TimDecoderTests {
    

    @Autowired
    TimDecoder timDecoder;

    private String odeTimDataReference = "{\"metadata\":{\"recordType\":\"timMsg\",\"encodings\":[{\"elementName\":\"unsecuredData\",\"elementType\":\"MessageFrame\",\"encodingRule\":\"UPER\"}],\"payloadType\":\"us.dot.its.jpo.ode.model.OdeAsn1Payload\",\"serialId\":{\"streamId\":\"fc430f29-b761-4a2c-90fb-dc4c9f5d4e9c\",\"bundleSize\":1,\"bundleId\":0,\"recordId\":0,\"serialNumber\":0},\"odeReceivedAt\":\"2024-05-14T23:01:21.516531700Z\",\"schemaVersion\":6,\"maxDurationTime\":0,\"recordGeneratedBy\":\"RSU\",\"sanitized\":false,\"asn1\":\"005f498718cca69ec1a04600000100105d9b46ec5be401003a0103810040038081d4001f80d07016da410000000000000bbc2b0f775d9b0309c271431fa166ee0a27fff93f136b8205a0a107fb2ef979f4c5bfaeec97e4ad70c2fb36cd9730becdb355cc2fd2a7556b160b98b46ab98ae62c185fa55efb468d5b4000000004e2863f42cddc144ff7980040401262cdd7b809c509f5c62cdd35519c507b9062cdcee129c505cf262cdca5ff9c50432c62cdc5d3d9c502e3e62cdc13e79c501e9262cdbca2d9c5013ee62cdb80359c500e6a62cdb36299c500bc862cdaec1d9c50093c62cdaa2109c5006ea1080203091a859eeebb36006001830001aad27f4ff7580001aad355e39b5880a30029d6585009ef808332d8d9f80c3855151b38c772f765007967ec1170bcb7937f5cb880a25a52863493bcb87570dbcb5abc6bfb2faec606cfa34eb95a24790b2017366d3aabe7729e\",\"originIp\":\"user-upload\"},\"payload\":{\"dataType\":\"us.dot.its.jpo.ode.model.OdeHexByteArray\",\"data\":{\"bytes\":\"005f498718cca69ec1a04600000100105d9b46ec5be401003a0103810040038081d4001f80d07016da410000000000000bbc2b0f775d9b0309c271431fa166ee0a27fff93f136b8205a0a107fb2ef979f4c5bfaeec97e4ad70c2fb36cd9730becdb355cc2fd2a7556b160b98b46ab98ae62c185fa55efb468d5b4000000004e2863f42cddc144ff7980040401262cdd7b809c509f5c62cdd35519c507b9062cdcee129c505cf262cdca5ff9c50432c62cdc5d3d9c502e3e62cdc13e79c501e9262cdbca2d9c5013ee62cdb80359c500e6a62cdb36299c500bc862cdaec1d9c50093c62cdaa2109c5006ea1080203091a859eeebb36006001830001aad27f4ff7580001aad355e39b5880a30029d6585009ef808332d8d9f80c3855151b38c772f765007967ec1170bcb7937f5cb880a25a52863493bcb87570dbcb5abc6bfb2faec606cfa34eb95a24790b2017366d3aabe7729e\"}}}";

    @Test
    public void testTimGetAsOdeData() {

        TimDecodedMessage tim = MockDecodedMessageGenerator.getTimDecodedMessage();
        OdeData data = timDecoder.getAsOdeData(tim.getAsn1Text());

        OdeMsgMetadata metadata = data.getMetadata();

        System.out.println(data);
        // Copy over fields that might be different
        metadata.setOdeReceivedAt("2024-05-14T23:01:21.516531700Z");
        metadata.setSerialId(metadata.getSerialId().setStreamId("fc430f29-b761-4a2c-90fb-dc4c9f5d4e9c"));

        assertEquals(data.toJson(), odeTimDataReference);
    
    }

}
