package us.dot.its.jpo.ode.api.asn1;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import us.dot.its.jpo.ode.api.models.messages.TimDecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.DecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.EncodedMessage;
import us.dot.its.jpo.ode.context.AppContext;
import us.dot.its.jpo.ode.model.Asn1Encoding;
import us.dot.its.jpo.ode.model.Asn1Encoding.EncodingRule;
import us.dot.its.jpo.ode.model.OdeAsn1Data;
import us.dot.its.jpo.ode.model.OdeAsn1Payload;
import us.dot.its.jpo.ode.model.OdeTimData;
import us.dot.its.jpo.ode.model.OdeTimMetadata;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeHexByteArray;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy;
import us.dot.its.jpo.ode.model.OdeMsgPayload;
import us.dot.its.jpo.ode.util.JsonUtils;
import us.dot.its.jpo.ode.util.XmlUtils;
import us.dot.its.jpo.ode.util.XmlUtils.XmlUtilsException;

@Component
public class TimDecoder implements Decoder {


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
            

            // Convert to Ode Json 
            OdeTimData tim = getAsOdeJson(decodedXml);

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
        ObjectNode consumed = XmlUtils.toObjectNode(consumedData);

        JsonNode metadataNode = consumed.findValue(AppContext.METADATA_STRING);
        if (metadataNode instanceof ObjectNode) {
            ObjectNode object = (ObjectNode) metadataNode;
            object.remove(AppContext.ENCODINGS_STRING);
        }

        JsonNode payloadNode = consumed.findValue("TravelerInformationMessage");
        if (payloadNode instanceof ObjectNode) {
            ObjectNode object = (ObjectNode) payloadNode;
            object.remove(AppContext.ENCODINGS_STRING);
        }
        
        OdeTimMetadata metadata = (OdeTimMetadata) JsonUtils.fromJson(
            metadataNode.toString(), OdeTimMetadata.class);

        OdeTimPayload payload = (OdeTimPayload) JsonUtils.fromJson(
            payloadNode.toString(), OdeTimPayload.class);

        /*
        *  ODE-755 and ODE-765 Starting with schemaVersion=5 receivedMessageDetails 
        *  will be present in BSM metadata. None should be present in prior versions.
        */
        if (metadata.getSchemaVersion() <= 4) {
            metadata.setReceivedMessageDetails(null);
        }
        
        // OdeTimPayload payload = new OdeTimPayload(
        //     TimBuilder.genericTim(consumed.findValue("TravelerInformationMessage")));
        return new OdeTimData(metadata, payload);
    }

}
