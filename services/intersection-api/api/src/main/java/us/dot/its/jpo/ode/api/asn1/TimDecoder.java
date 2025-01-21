package us.dot.its.jpo.ode.api.asn1;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

            // Convert to Ode Json 
            ObjectNode tim = XmlUtils.toObjectNode(decodedXml);

            // build output data structure
            return new TimDecodedMessage(tim, message.getAsn1Message(), "");
            
        } catch (JsonProcessingException e) {
            log.error("JSON Processing Exception: {}", e.getMessage(), e);
            return new TimDecodedMessage(null, message.getAsn1Message(), e.getMessage());
        } catch (Exception e) {
            log.error("Generic Exception: {}", e.getMessage(), e);
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
        metadata.setOdeReceivedAt(DecoderManager.getCurrentIsoTimestamp());
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
        // There is no proper deserializer for TIM data into the ODE tim format. This function is not used here.
        return null;
    }

}
