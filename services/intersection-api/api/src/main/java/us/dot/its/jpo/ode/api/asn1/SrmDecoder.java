package us.dot.its.jpo.ode.api.asn1;

import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.HexFormat;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import j2735ffm.MessageFrameCodec;
import us.dot.its.jpo.ode.api.models.messages.SrmDecodedMessage;
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
import us.dot.its.jpo.asn.j2735.r2024.SignalRequestMessage.SignalRequestMessageMessageFrame;
import us.dot.its.jpo.geojsonconverter.converter.srm.SrmConverter;
import us.dot.its.jpo.geojsonconverter.pojos.common.DeserializedRawMessageFrame;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.srm.ProcessedSrm;
import us.dot.its.jpo.geojsonconverter.utils.ProcessedSchemaVersions;
import us.dot.its.jpo.geojsonconverter.validator.JsonValidatorResult;
import us.dot.its.jpo.geojsonconverter.validator.SrmJsonValidator;

/**
 * Decoder implementation for Basic Safety Message (SRM) messages.
 * Converts ASN.1 encoded SRM messages to processed SRM objects.
 */
@Slf4j
@ConditionalOnBean(MessageFrameCodec.class)
@Component
public class SrmDecoder implements Decoder {

    MessageFrameCodec codec;
    SrmJsonValidator srmJsonValidator;
    public static final SrmConverter converter = new SrmConverter();
    public static final XmlMapper xmlMapper = new XmlMapper();

    /**
     * Constructs a SrmDecoder with required dependencies.
     *
     * @param codec MessageFrameCodec for ASN.1 decoding
     */
    @Autowired
    SrmDecoder(MessageFrameCodec codec) {
        this.codec = codec;
    }

    /**
     * Decodes an ASN.1 encoded SRM message to a processed SRM object.
     *
     * @param message EncodedMessage containing ASN.1 SRM data
     * @return DecodedMessage containing processed SRM or error details
     */
    @Override
    public DecodedMessage decode(EncodedMessage message) {

        String xer = decodeAsnToXERString(message.getAsn1Message());

        try {
            OdeMessageFrameData odeMessageFrameData = convertXERToMessageFrame(xer);
            ProcessedSrm processedSrm = convertMessageFrameToProcessedSrm(odeMessageFrameData);
            processedSrm.getProperties().setAsn1(message.getAsn1Message());
            processedSrm.getProperties().setOriginIp(odeMessageFrameData.getMetadata().getOriginIp());
            processedSrm.getProperties()
                    .setOdeReceivedAt(ZonedDateTime.parse(odeMessageFrameData.getMetadata().getOdeReceivedAt()));
            return new SrmDecodedMessage(
                    processedSrm,
                    message.getAsn1Message(), "");

        } catch (JsonProcessingException e) {
            return new SrmDecodedMessage(null, message.getAsn1Message(), e.getMessage());
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
        metadata.setRecordType(RecordType.srmTx);
        metadata.setSecurityResultCode(SecurityResultCode.success);
        metadata.setRecordGeneratedBy(GeneratedBy.OBU);
        metadata.setSource(Source.EV);

        JsonNode rootNode = xmlMapper.readTree(encodedXml);

        MessageFrame<?> messageFrame = xmlMapper.convertValue(rootNode, MessageFrame.class);

        OdeMessageFramePayload payload = new OdeMessageFramePayload(messageFrame);

        return new OdeMessageFrameData(metadata, payload);

    }

    /**
     * Converts OdeMessageFrameData to a processed SRM object.
     * Validates the message and returns either a processed or failure SRM.
     *
     * @param odeMessageFrameData OdeMessageFrameData to process
     * @return ProcessedSrm object containing SRM data or validation failure
     */
    public ProcessedSrm convertMessageFrameToProcessedSrm(OdeMessageFrameData odeMessageFrameData) {
        DeserializedRawMessageFrame deserializedSrm = new DeserializedRawMessageFrame();
        try {
            JsonValidatorResult validationResults = srmJsonValidator.validate(odeMessageFrameData.toJson());
            deserializedSrm.setOdeMessageFrameData(odeMessageFrameData);
            deserializedSrm.setValidationResults(validationResults);
        } catch (Exception e) {
            JsonValidatorResult validatorResult = new JsonValidatorResult();

            validatorResult.addException(e);
            deserializedSrm.setValidationFailure(true);
            deserializedSrm.setValidationResults(validatorResult);
            deserializedSrm.setFailedMessage(e.getMessage());
        }

        OdeMessageFrameData rawValue = new OdeMessageFrameData();
        rawValue.setMetadata(odeMessageFrameData.getMetadata());
        rawValue.setPayload(odeMessageFrameData.getPayload());

        SignalRequestMessageMessageFrame srmMessageFrame = (SignalRequestMessageMessageFrame) rawValue.getPayload()
                .getData();

        ZonedDateTime zdt = ZonedDateTime.parse(rawValue.getMetadata().getOdeReceivedAt());

        ProcessedSrm processedSrm = converter.processSrm(srmMessageFrame, zdt);

        // Set the schema version
        processedSrm.getProperties().setSchemaVersion(ProcessedSchemaVersions.PROCESSED_SRM_SCHEMA_VERSION);
        return processedSrm;

    }
}
