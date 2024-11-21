package us.dot.its.jpo.ode.api.decoderTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import us.dot.its.jpo.ode.api.asn1.SsmDecoder;
import us.dot.its.jpo.ode.api.models.messages.SsmDecodedMessage;
import us.dot.its.jpo.ode.mockdata.MockDecodedMessageGenerator;
import us.dot.its.jpo.ode.model.OdeSsmData;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeMsgMetadata;

import us.dot.its.jpo.ode.util.XmlUtils.XmlUtilsException;


@SpringBootTest
@RunWith(SpringRunner.class)
public class SsmDecoderTests {
    

    @Autowired
    SsmDecoder ssmDecoder;

    private String odeSsmDataReference = "{\"metadata\":{\"recordType\":\"ssmTx\",\"encodings\":[{\"elementName\":\"unsecuredData\",\"elementType\":\"MessageFrame\",\"encodingRule\":\"UPER\"}],\"payloadType\":\"us.dot.its.jpo.ode.model.OdeAsn1Payload\",\"serialId\":{\"streamId\":\"fc430f29-b761-4a2c-90fb-dc4c9f5d4e9c\",\"bundleSize\":1,\"bundleId\":0,\"recordId\":0,\"serialNumber\":0},\"odeReceivedAt\":\"2024-05-14T23:01:21.516531700Z\",\"schemaVersion\":6,\"maxDurationTime\":0,\"sanitized\":false,\"asn1\":\"001e120000000005e9c04071a26614c06000040ba0\",\"originIp\":\"user-upload\"},\"payload\":{\"dataType\":\"us.dot.its.jpo.ode.model.OdeHexByteArray\",\"data\":{\"bytes\":\"001e120000000005e9c04071a26614c06000040ba0\"}}}";
    private String odeSsmDecodedXmlReference = "<?xml version=\"1.0\"?><OdeAsn1Data><metadata><logFileName/><recordType>ssmTx</recordType><securityResultCode/><receivedMessageDetails/><encodings><encodings><elementName>unsecuredData</elementName><elementType>MessageFrame</elementType><encodingRule>UPER</encodingRule></encodings></encodings><payloadType>us.dot.its.jpo.ode.model.OdeAsn1Payload</payloadType><serialId><streamId>f9eed695-0e43-4272-90a1-b879c663766d</streamId><bundleSize>1</bundleSize><bundleId>0</bundleId><recordId>0</recordId><serialNumber>0</serialNumber></serialId><odeReceivedAt>2024-05-15T20:20:44.648878014Z</odeReceivedAt><schemaVersion>6</schemaVersion><maxDurationTime>0</maxDurationTime><recordGeneratedAt/><recordGeneratedBy/><sanitized>false</sanitized><odePacketID/><odeTimStartDateTime/><originIp>172.18.0.1</originIp><ssmSource>RSU</ssmSource></metadata><payload><dataType>MessageFrame</dataType><data><MessageFrame><messageId>30</messageId><value><SignalStatusMessage><second>0</second><status><SignalStatus><sequenceNumber>0</sequenceNumber><id><id>12110</id></id><sigStatus><SignalStatusPackage><requester><id><stationID>2366845094</stationID></id><request>3</request><sequenceNumber>0</sequenceNumber><typeData><role><publicTransport/></role></typeData></requester><inboundOn><lane>23</lane></inboundOn><status><granted/></status></SignalStatusPackage></sigStatus></SignalStatus></status></SignalStatusMessage></value></MessageFrame></data></payload></OdeAsn1Data>";
    private String odeSsmDecodedDataReference = "{\"metadata\":{\"logFileName\":\"\",\"recordType\":\"ssmTx\",\"receivedMessageDetails\":{\"rxSource\":\"NA\"},\"payloadType\":\"us.dot.its.jpo.ode.model.OdeSsmPayload\",\"serialId\":{\"streamId\":\"f9eed695-0e43-4272-90a1-b879c663766d\",\"bundleSize\":1,\"bundleId\":0,\"recordId\":0,\"serialNumber\":0},\"odeReceivedAt\":\"2024-05-15T20:20:44.648878014Z\",\"schemaVersion\":6,\"maxDurationTime\":0,\"recordGeneratedAt\":\"\",\"sanitized\":false,\"odePacketID\":\"\",\"odeTimStartDateTime\":\"\",\"originIp\":\"172.18.0.1\",\"ssmSource\":\"RSU\"},\"payload\":{\"data\":{\"second\":0,\"status\":{\"signalStatus\":[{\"sequenceNumber\":0,\"id\":{\"id\":12110},\"sigStatus\":{\"signalStatusPackage\":[{\"requester\":{\"id\":{\"stationID\":2366845094},\"request\":3,\"sequenceNumber\":0,\"typeData\":{\"role\":\"publicTransport\"}},\"inboundOn\":{\"lane\":23},\"status\":\"granted\"}]}}]}},\"dataType\":\"us.dot.its.jpo.ode.plugin.j2735.J2735SSM\"}}";


    @Test
    public void testSsmGetAsOdeData() {

        SsmDecodedMessage ssm = MockDecodedMessageGenerator.getSsmDecodedMessage();
        OdeData data = ssmDecoder.getAsOdeData(ssm.getAsn1Text());

        OdeMsgMetadata metadata = data.getMetadata();

        System.out.println(data);
        // Copy over fields that might be different
        metadata.setOdeReceivedAt("2024-05-14T23:01:21.516531700Z");
        metadata.setSerialId(metadata.getSerialId().setStreamId("fc430f29-b761-4a2c-90fb-dc4c9f5d4e9c"));

        assertEquals(data.toJson(), odeSsmDataReference);
    
    }

    @Test
    public void testSsmGetAsOdeJson() throws XmlUtilsException{
        OdeSsmData ssm = ssmDecoder.getAsOdeJson(odeSsmDecodedXmlReference);
        assertEquals(ssm.toJson(), odeSsmDecodedDataReference);
        System.out.println(ssm);
    }

}
