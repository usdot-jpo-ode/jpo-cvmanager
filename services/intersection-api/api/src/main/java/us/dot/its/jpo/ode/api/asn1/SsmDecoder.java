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
import us.dot.its.jpo.ode.api.models.messages.SsmDecodedMessage;
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
import us.dot.its.jpo.asn.j2735.r2024.SignalStatusMessage.SignalStatusMessageMessageFrame;
import us.dot.its.jpo.geojsonconverter.converter.ssm.SsmConverter;
import us.dot.its.jpo.geojsonconverter.pojos.common.DeserializedRawMessageFrame;
import us.dot.its.jpo.geojsonconverter.pojos.ssm.ProcessedSsm;
import us.dot.its.jpo.geojsonconverter.utils.ProcessedSchemaVersions;
import us.dot.its.jpo.geojsonconverter.validator.JsonValidatorResult;
import us.dot.its.jpo.geojsonconverter.validator.SrmJsonValidator;

/**
 * Decoder implementation for Basic Safety Message (SSM) messages.
 * Converts ASN.1 encoded SSM messages to processed SSM objects.
 */
@Slf4j
@ConditionalOnBean(MessageFrameCodec.class)
@Component
public class SsmDecoder implements Decoder {

    MessageFrameCodec codec;
    SrmJsonValidator ssmJsonValidator;
    public static final SsmConverter converter = new SsmConverter();
    public static final XmlMapper xmlMapper = new XmlMapper();

    /**
     * Constructs a SsmDecoder with required dependencies.
     *
     * @param codec MessageFrameCodec for ASN.1 decoding
     */
    @Autowired
    SsmDecoder(MessageFrameCodec codec) {
        this.codec = codec;
    }

    /**
     * Decodes an ASN.1 encoded SSM message to a processed SSM object.
     *
     * @param message EncodedMessage containing ASN.1 SSM data
     * @return DecodedMessage containing processed SSM or error details
     */
    @Override
    public DecodedMessage decode(EncodedMessage message) {

        String xer = decodeAsnToXERString(message.getAsn1Message());

        try {
            OdeMessageFrameData odeMessageFrameData = convertXERToMessageFrame(xer);
            ProcessedSsm processedSsm = convertMessageFrameToProcessedSsm(odeMessageFrameData);
            processedSsm.setAsn1(message.getAsn1Message());
            processedSsm.setOriginIp(odeMessageFrameData.getMetadata().getOriginIp());
            processedSsm.setOdeReceivedAt(ZonedDateTime.parse(odeMessageFrameData.getMetadata().getOdeReceivedAt()));
            return new SsmDecodedMessage(processedSsm, message.getAsn1Message(), "");

        } catch (JsonProcessingException e) {
            return new SsmDecodedMessage(null, message.getAsn1Message(), e.getMessage());
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
        metadata.setRecordType(RecordType.ssmTx);
        metadata.setSecurityResultCode(SecurityResultCode.success);
        metadata.setRecordGeneratedBy(GeneratedBy.RSU);
        metadata.setSource(Source.EV);

        JsonNode rootNode = xmlMapper.readTree(encodedXml);

        MessageFrame<?> messageFrame = xmlMapper.convertValue(rootNode, MessageFrame.class);

        OdeMessageFramePayload payload = new OdeMessageFramePayload(messageFrame);

        return new OdeMessageFrameData(metadata, payload);

    }

    /**
     * Converts OdeMessageFrameData to a processed SSM object.
     * Validates the message and returns either a processed or failure SSM.
     *
     * @param odeMessageFrameData OdeMessageFrameData to process
     * @return ProcessedSsm object containing SSM data or validation failure
     */
    public ProcessedSsm convertMessageFrameToProcessedSsm(OdeMessageFrameData odeMessageFrameData) {
        DeserializedRawMessageFrame deserializedSsm = new DeserializedRawMessageFrame();
        try {
            JsonValidatorResult validationResults = ssmJsonValidator.validate(odeMessageFrameData.toJson());
            deserializedSsm.setOdeMessageFrameData(odeMessageFrameData);
            deserializedSsm.setValidationResults(validationResults);
        } catch (Exception e) {
            JsonValidatorResult validatorResult = new JsonValidatorResult();

            validatorResult.addException(e);
            deserializedSsm.setValidationFailure(true);
            deserializedSsm.setValidationResults(validatorResult);
            deserializedSsm.setFailedMessage(e.getMessage());
        }

        OdeMessageFrameData rawValue = new OdeMessageFrameData();
        rawValue.setMetadata(odeMessageFrameData.getMetadata());
        rawValue.setPayload(odeMessageFrameData.getPayload());

        SignalStatusMessageMessageFrame ssmMessageFrame = (SignalStatusMessageMessageFrame) rawValue.getPayload()
                .getData();

        ProcessedSsm processedSsm = converter.processSsm(ssmMessageFrame);

        // Set the schema version
        processedSsm.setSchemaVersion(ProcessedSchemaVersions.PROCESSED_SSM_SCHEMA_VERSION);
        return processedSsm;

    }
}
