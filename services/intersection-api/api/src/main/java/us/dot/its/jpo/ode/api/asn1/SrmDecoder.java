package us.dot.its.jpo.ode.api.asn1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import us.dot.its.jpo.ode.api.models.messages.SrmDecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.DecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.EncodedMessage;
import us.dot.its.jpo.ode.model.OdeMsgMetadata;
import us.dot.its.jpo.ode.model.Asn1Encoding;
import us.dot.its.jpo.ode.model.Asn1Encoding.EncodingRule;
import us.dot.its.jpo.ode.model.OdeAsn1Data;
import us.dot.its.jpo.ode.model.OdeAsn1Payload;
import us.dot.its.jpo.ode.model.OdeSrmData;
import us.dot.its.jpo.ode.model.OdeSrmMetadata;
import us.dot.its.jpo.ode.model.OdeSrmPayload;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeHexByteArray;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeMsgPayload;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.model.RxSource;
import us.dot.its.jpo.ode.plugin.j2735.builders.SRMBuilder;
import us.dot.its.jpo.ode.util.JsonUtils;
import us.dot.its.jpo.ode.util.XmlUtils;
import us.dot.its.jpo.ode.util.XmlUtils.XmlUtilsException;

@Slf4j
@Component
public class SrmDecoder implements Decoder {

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
            OdeSrmData srm = getAsOdeJson(decodedXml);

            // build output data structure
            return new SrmDecodedMessage(srm, message.getAsn1Message(), "");

        } catch (Exception e) {
            log.error("Exception decoding SRM message", e);
            return new SrmDecodedMessage(null, message.getAsn1Message(), e.getMessage());
        }
    }

    @Override
    public OdeData getAsOdeData(String encodedData) {
        OdeMsgPayload payload = new OdeAsn1Payload(new OdeHexByteArray(encodedData));

        // construct metadata
        OdeSrmMetadata metadata = new OdeSrmMetadata(payload);
        metadata.setOdeReceivedAt(DecoderManager.getCurrentIsoTimestamp());
        metadata.setOriginIp(DecoderManager.getStaticUserOriginIp());
        metadata.setRecordType(RecordType.srmTx);

        Asn1Encoding unsecuredDataEncoding = new Asn1Encoding("unsecuredData", "MessageFrame", EncodingRule.UPER);
        metadata.addEncoding(unsecuredDataEncoding);

        // construct odeData
        return new OdeAsn1Data(metadata, payload);

    }

    @Override
    public OdeSrmData getAsOdeJson(String consumedData) throws XmlUtilsException {
        ObjectNode consumed = XmlUtils.toObjectNode(consumedData);

        JsonNode metadataNode = consumed.findValue(OdeMsgMetadata.METADATA_STRING);
        if (metadataNode instanceof ObjectNode object) {
            // Removing encodings to match ODE behavior
            object.remove(OdeMsgMetadata.ENCODINGS_STRING);

            // Ssm header file does not have a location and use predefined set required
            // RxSource
            ReceivedMessageDetails receivedMessageDetails = new ReceivedMessageDetails();
            receivedMessageDetails.setRxSource(RxSource.NA);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode;
            try {
                jsonNode = objectMapper.readTree(receivedMessageDetails.toJson());
                object.set(OdeMsgMetadata.RECEIVEDMSGDETAILS_STRING, jsonNode);
            } catch (JsonProcessingException e) {
                log.error("Exception decoding SRM to ODE json", e);
            }
        }

        OdeSrmMetadata metadata = (OdeSrmMetadata) JsonUtils.fromJson(metadataNode.toString(), OdeSrmMetadata.class);

        if (metadata.getSchemaVersion() <= 4) {
            metadata.setReceivedMessageDetails(null);
        }

        OdeSrmPayload payload = new OdeSrmPayload(SRMBuilder.genericSRM(consumed.findValue("SignalRequestMessage")));
        return new OdeSrmData(metadata, payload);
    }

}
