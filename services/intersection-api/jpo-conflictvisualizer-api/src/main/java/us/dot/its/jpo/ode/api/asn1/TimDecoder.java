package us.dot.its.jpo.ode.api.asn1;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import us.dot.its.jpo.ode.api.models.messages.TimDecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.DecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.EncodedMessage;
import us.dot.its.jpo.ode.model.Asn1Encoding;
import us.dot.its.jpo.ode.model.Asn1Encoding.EncodingRule;
import us.dot.its.jpo.ode.model.OdeAsn1Data;
import us.dot.its.jpo.ode.model.OdeAsn1Payload;
import us.dot.its.jpo.ode.model.OdeTimData;
import us.dot.its.jpo.ode.model.OdeTimMetadata;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeHexByteArray;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy;
import us.dot.its.jpo.ode.model.OdeMsgPayload;
import us.dot.its.jpo.ode.util.XmlUtils;
import us.dot.its.jpo.ode.util.XmlUtils.XmlUtilsException;

@Component
public class TimDecoder implements Decoder {
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DecodedMessage decode(EncodedMessage message) {
        
        // Convert to Ode Data type and Add Metadata
        OdeData data = getAsOdeData(message.getAsn1Message());

        XmlUtils xmlUtils = new XmlUtils();

        try {
            // Convert to XML for ASN.1 Decoder
            String xml = xmlUtils.toXml(data);

            // Send String through ASN.1 Decoder to get Decoded XML Data
            String decodedXml = DecoderManager.decodeXmlWithAcm(xml);
            // String decodedXml = mockTim();

            // Convert to Ode Json 
            ObjectNode tim = XmlUtils.toObjectNode(decodedXml);

            // build output data structure
            DecodedMessage decodedMessage = new TimDecodedMessage(tim, message.getAsn1Message(), "");
            return decodedMessage;
            
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new TimDecodedMessage(null, message.getAsn1Message(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new TimDecodedMessage(null, message.getAsn1Message(), e.getMessage());
        }
    }

    @Override
    public OdeData getAsOdeData(String encodedData) {
        OdeMsgPayload payload = new OdeAsn1Payload(new OdeHexByteArray(encodedData));

        // construct metadata
        OdeTimMetadata metadata = new OdeTimMetadata(payload);

        //construct metadata
        metadata = new OdeTimMetadata(payload);
        metadata.setOdeReceivedAt(DecoderManager.getOdeReceivedAt());
        metadata.setOriginIp("user-upload");
        metadata.setRecordType(RecordType.timMsg);
        metadata.setRecordGeneratedBy(GeneratedBy.RSU);
        
        Asn1Encoding unsecuredDataEncoding = new Asn1Encoding("unsecuredData", "MessageFrame",EncodingRule.UPER);
        metadata.addEncoding(unsecuredDataEncoding);
        
        //construct odeData
        return new OdeAsn1Data(metadata, payload);
    }

    @Override
    public OdeTimData getAsOdeJson(String consumedData) throws XmlUtilsException {

        // There is no proper deserilizer for TIM data into the ODE tim format. This function is not used here.

        return null;
    }

    public String mockTim(){
        return "<?xml version=\"1.0\"?><OdeAsn1Data><metadata><logFileName/><recordType>timMsg</recordType><securityResultCode/><receivedMessageDetails/><encodings><encodings><elementName>unsecuredData</elementName><elementType>MessageFrame</elementType><encodingRule>UPER</encodingRule></encodings></encodings><payloadType>us.dot.its.jpo.ode.model.OdeAsn1Payload</payloadType><serialId><streamId>d768dfa6-9b37-4a98-b16c-c0c68e5989c5</streamId><bundleSize>1</bundleSize><bundleId>0</bundleId><recordId>0</recordId><serialNumber>0</serialNumber></serialId><odeReceivedAt>2024-05-10T17:43:23.093127553Z</odeReceivedAt><schemaVersion>6</schemaVersion><maxDurationTime>0</maxDurationTime><recordGeneratedAt/><recordGeneratedBy>RSU</recordGeneratedBy><sanitized>false</sanitized><odePacketID/><odeTimStartDateTime/><originIp>172.18.0.1</originIp></metadata><payload><dataType>MessageFrame</dataType><data><MessageFrame><messageId>31</messageId><value><TravelerInformation><msgCnt>1</msgCnt><timeStamp>449089</timeStamp><packetID>0000000000000BBC2B</packetID><urlB>null</urlB><dataFrames><TravelerDataFrame><sspTimRights>1</sspTimRights><frameType><advisory/></frameType><msgId><roadSignID><position><lat>411269876</lat><long>-1047269563</long></position><viewAngle>1111111111111111</viewAngle><mutcdCode><warning/></mutcdCode></roadSignID></msgId><startYear>2018</startYear><startTime>448260</startTime><duratonTime>1440</duratonTime><priority>5</priority><sspLocationRights>1</sspLocationRights><regions><GeographicalPath><name>westbound_I-80_366.0_365.0_RSU-10.145.1.100_RW_4456</name><id><region>0</region><id>0</id></id><anchor><lat>411269876</lat><long>-1047269563</long></anchor><laneWidth>32700</laneWidth><directionality><both/></directionality><closedPath><false/></closedPath><direction>0000000000010000</direction><description><path><scale>0</scale><offset><xy><nodes><NodeXY><delta><node-LatLon><lon>-1047287423</lon><lat>411264686</lat></node-LatLon></delta></NodeXY><NodeXY><delta><node-LatLon><lon>-1047305390</lon><lat>411260104</lat></node-LatLon></delta></NodeXY><NodeXY><delta><node-LatLon><lon>-1047323629</lon><lat>411256185</lat></node-LatLon></delta></NodeXY><NodeXY><delta><node-LatLon><lon>-1047342080</lon><lat>411252886</lat></node-LatLon></delta></NodeXY><NodeXY><delta><node-LatLon><lon>-1047360706</lon><lat>411250207</lat></node-LatLon></delta></NodeXY><NodeXY><delta><node-LatLon><lon>-1047379480</lon><lat>411248201</lat></node-LatLon></delta></NodeXY><NodeXY><delta><node-LatLon><lon>-1047398354</lon><lat>411246839</lat></node-LatLon></delta></NodeXY><NodeXY><delta><node-LatLon><lon>-1047417290</lon><lat>411246133</lat></node-LatLon></delta></NodeXY><NodeXY><delta><node-LatLon><lon>-1047436246</lon><lat>411245796</lat></node-LatLon></delta></NodeXY><NodeXY><delta><node-LatLon><lon>-1047455202</lon><lat>411245470</lat></node-LatLon></delta></NodeXY><NodeXY><delta><node-LatLon><lon>-1047474159</lon><lat>411245173</lat></node-LatLon></delta></NodeXY></nodes></xy></offset></path></description></GeographicalPath></regions><sspMsgRights1>1</sspMsgRights1><sspMsgRights2>1</sspMsgRights2><content><advisory><SEQUENCE><item><itis>777</itis></item></SEQUENCE><SEQUENCE><item><itis>13579</itis></item></SEQUENCE></advisory></content><url>null</url></TravelerDataFrame></dataFrames></TravelerInformation></value></MessageFrame></data></payload></OdeAsn1Data>";
    }

}
