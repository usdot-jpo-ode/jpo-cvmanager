package us.dot.its.jpo.ode.api.asn1;

import lombok.extern.slf4j.Slf4j;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import j2735ffm.MessageFrameCodec;
import us.dot.its.jpo.ode.api.models.messages.PsmDecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.DecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.EncodedMessage;
import us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeMessageFrameData;
import us.dot.its.jpo.ode.model.OdeMessageFrameMetadata.Source;
import us.dot.its.jpo.ode.model.OdeMessageFramePayload;
import us.dot.its.jpo.ode.model.OdeMessageFrameMetadata;
import us.dot.its.jpo.ode.util.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import us.dot.its.jpo.asn.j2735.r2024.MessageFrame.MessageFrame;
import us.dot.its.jpo.asn.j2735.r2024.PersonalSafetyMessage.PersonalSafetyMessageMessageFrame;

/**
 * Decoder implementation for Basic Safety Message (PSM) messages.
 * Converts ASN.1 encoded PSM messages to processed PSM objects.
 */
@Slf4j
@Component
public class PsmDecoder implements Decoder {

    MessageFrameCodec codec;
    XmlMapper xmlMapper = new XmlMapper();

    /**
     * Constructs a PsmDecoder with required dependencies.
     *
     * @param codec MessageFrameCodec for ASN.1 decoding
     */
    @Autowired
    PsmDecoder(MessageFrameCodec codec) {
        this.codec = codec;
    }

    /**
     * Decodes an ASN.1 encoded PSM message to a processed PSM object.
     *
     * @param message EncodedMessage containing ASN.1 PSM data
     * @return DecodedMessage containing processed PSM or error details
     */
    @Override
    public DecodedMessage decode(EncodedMessage message) {

        String xer = decodeAsnToXERString(message.getAsn1Message());

        try {
            OdeMessageFrameData odeMessageFrameData = convertXERToMessageFrame(xer);
            return new PsmDecodedMessage(
                    ((PersonalSafetyMessageMessageFrame) odeMessageFrameData.getPayload().getData()).getValue(),
                    message.getAsn1Message(), "");

        } catch (JsonProcessingException e) {
            return new PsmDecodedMessage(null, message.getAsn1Message(), e.getMessage());
        }

    }

    /**
     * Converts an ASN.1 hex string to XER (XML Encoding Rules) string.
     *
     * @param asnHex ASN.1 encoded hex string
     * @return XER string representation of the message
     */
    @Override
    public String decodeAsnToXERString(String asnHex) {
        byte[] bytes = HexFormat.of().parseHex(asnHex);
        String xer = codec.uperToXer(bytes);
        return xer;
    }

    /**
     * Converts an XER-encoded XML string to an OdeMessageFrameData object.
     *
     * @param encodedXml XER-encoded XML string
     * @return OdeMessageFrameData object
     * @throws JsonMappingException    if XML mapping fails
     * @throws JsonProcessingException if XML processing fails
     */
    @Override
    public OdeMessageFrameData convertXERToMessageFrame(String encodedXml)
            throws JsonMappingException, JsonProcessingException {
        OdeMessageFrameMetadata metadata = new OdeMessageFrameMetadata();
        metadata.setOdeReceivedAt(DateTimeUtils.now());
        metadata.setRecordType(RecordType.psmTx);
        metadata.setSecurityResultCode(SecurityResultCode.success);
        metadata.setRecordGeneratedBy(GeneratedBy.OBU);
        metadata.setSource(Source.EV);

        JsonNode rootNode = xmlMapper.readTree(encodedXml);

        MessageFrame<?> messageFrame = xmlMapper.convertValue(rootNode, MessageFrame.class);

        OdeMessageFramePayload payload = new OdeMessageFramePayload(messageFrame);

        return new OdeMessageFrameData(metadata, payload);

    }
}
