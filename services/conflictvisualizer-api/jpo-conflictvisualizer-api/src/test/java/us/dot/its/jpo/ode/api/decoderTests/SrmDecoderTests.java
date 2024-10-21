package us.dot.its.jpo.ode.api.decoderTests;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import us.dot.its.jpo.ode.api.asn1.SrmDecoder;
import us.dot.its.jpo.ode.api.models.messages.SrmDecodedMessage;
import us.dot.its.jpo.ode.mockdata.MockDecodedMessageGenerator;
import us.dot.its.jpo.ode.model.OdeSrmData;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeMsgMetadata;

import us.dot.its.jpo.ode.util.XmlUtils.XmlUtilsException;


@SpringBootTest
@RunWith(SpringRunner.class)
public class SrmDecoderTests {
    

    @Autowired
    SrmDecoder srmDecoder;

    private String odeSrmDataReference = "{\"metadata\":{\"recordType\":\"srmTx\",\"encodings\":[{\"elementName\":\"unsecuredData\",\"elementType\":\"MessageFrame\",\"encodingRule\":\"UPER\"}],\"payloadType\":\"us.dot.its.jpo.ode.model.OdeAsn1Payload\",\"serialId\":{\"streamId\":\"1809ff18-9251-42f0-a579-d629c1f6d60f\",\"bundleSize\":1,\"bundleId\":0,\"recordId\":0,\"serialNumber\":0},\"odeReceivedAt\":\"2024-05-15T20:02:18.273873Z\",\"schemaVersion\":6,\"maxDurationTime\":0,\"sanitized\":false,\"originIp\":\"user-upload\"},\"payload\":{\"dataType\":\"us.dot.its.jpo.ode.model.OdeHexByteArray\",\"data\":{\"bytes\":\"001d2130000010090bd341080d00855c6c0c6899853000a534f7c24cb29897694759b7c0\"}}}";
    private String odeSrmDecodedXmlReference = "<?xml version=\"1.0\"?><OdeAsn1Data><metadata><logFileName/><recordType>srmTx</recordType><securityResultCode/><receivedMessageDetails/><encodings><encodings><elementName>unsecuredData</elementName><elementType>MessageFrame</elementType><encodingRule>UPER</encodingRule></encodings></encodings><payloadType>us.dot.its.jpo.ode.model.OdeAsn1Payload</payloadType><serialId><streamId>747378da-48a6-4868-a757-8aaa7771c329</streamId><bundleSize>1</bundleSize><bundleId>0</bundleId><recordId>0</recordId><serialNumber>0</serialNumber></serialId><odeReceivedAt>2024-05-15T19:59:40.208172634Z</odeReceivedAt><schemaVersion>6</schemaVersion><maxDurationTime>0</maxDurationTime><recordGeneratedAt/><recordGeneratedBy/><sanitized>false</sanitized><odePacketID/><odeTimStartDateTime/><originIp>172.18.0.1</originIp><srmSource>RSU</srmSource></metadata><payload><dataType>MessageFrame</dataType><data><MessageFrame><messageId>29</messageId><value><SignalRequestMessage><second>0</second><sequenceNumber>1</sequenceNumber><requests><SignalRequestPackage><request><id><id>12109</id></id><requestID>4</requestID><requestType><priorityRequest/></requestType><inBoundLane><lane>13</lane></inBoundLane><outBoundLane><lane>4</lane></outBoundLane></request><duration>10979</duration></SignalRequestPackage></requests><requestor><id><stationID>2366845094</stationID></id><type><role><publicTransport/></role></type><position><position><lat>395904915</lat><long>-1050913829</long><elevation>16854</elevation></position><heading>14072</heading></position></requestor></SignalRequestMessage></value></MessageFrame></data></payload></OdeAsn1Data>";
    private String odeSrmDecodedDataReference = "{\"metadata\":{\"logFileName\":\"\",\"recordType\":\"srmTx\",\"receivedMessageDetails\":{\"rxSource\":\"NA\"},\"payloadType\":\"us.dot.its.jpo.ode.model.OdeSrmPayload\",\"serialId\":{\"streamId\":\"747378da-48a6-4868-a757-8aaa7771c329\",\"bundleSize\":1,\"bundleId\":0,\"recordId\":0,\"serialNumber\":0},\"odeReceivedAt\":\"2024-05-15T19:59:40.208172634Z\",\"schemaVersion\":6,\"maxDurationTime\":0,\"recordGeneratedAt\":\"\",\"sanitized\":false,\"odePacketID\":\"\",\"odeTimStartDateTime\":\"\",\"originIp\":\"172.18.0.1\",\"srmSource\":\"RSU\"},\"payload\":{\"data\":{\"second\":0,\"sequenceNumber\":1,\"requests\":{\"signalRequestPackage\":[{\"request\":{\"id\":{\"id\":12109},\"requestID\":4,\"requestType\":\"priorityRequest\",\"inBoundLane\":{\"lane\":13},\"outBoundLane\":{\"lane\":4}},\"duration\":10979}]},\"requestor\":{\"id\":{\"stationID\":2366845094},\"type\":{\"role\":\"publicTransport\"},\"position\":{\"position\":{\"latitude\":39.5904915,\"longitude\":-105.0913829,\"elevation\":1685.4},\"heading\":175.9000}}},\"dataType\":\"us.dot.its.jpo.ode.plugin.j2735.J2735SRM\"}}";


    @Test
    public void testSrmGetAsOdeData() {

        SrmDecodedMessage srm = MockDecodedMessageGenerator.getSrmDecodedMessage();
        OdeData data = srmDecoder.getAsOdeData(srm.getAsn1Text());

        OdeMsgMetadata metadata = data.getMetadata();


        // Copy over fields that might be different
        metadata.setOdeReceivedAt("2024-05-15T20:02:18.273873Z");
        metadata.setSerialId(metadata.getSerialId().setStreamId("1809ff18-9251-42f0-a579-d629c1f6d60f"));

        assertEquals(data.toJson(), odeSrmDataReference);
    
    }

    @Test
    public void testSrmGetAsOdeJson() throws XmlUtilsException{
        OdeSrmData srm = srmDecoder.getAsOdeJson(odeSrmDecodedXmlReference);
        assertEquals(srm.toJson(), odeSrmDecodedDataReference);
    }

}
