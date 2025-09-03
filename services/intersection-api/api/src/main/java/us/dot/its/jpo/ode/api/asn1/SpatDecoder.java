package us.dot.its.jpo.ode.api.asn1;

import lombok.extern.slf4j.Slf4j;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import j2735ffm.MessageFrameCodec;
import us.dot.its.jpo.ode.api.models.messages.SpatDecodedMessage;
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
import us.dot.its.jpo.asn.j2735.r2024.SPAT.SPATMessageFrame;
import us.dot.its.jpo.geojsonconverter.converter.spat.SpatProcessedJsonConverter;
import us.dot.its.jpo.geojsonconverter.pojos.spat.DeserializedRawSpat;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.geojsonconverter.utils.ProcessedSchemaVersions;
import us.dot.its.jpo.geojsonconverter.validator.SpatJsonValidator;
import us.dot.its.jpo.geojsonconverter.validator.JsonValidatorResult;

/**
 * Decoder implementation for Basic Safety Message (SPAT) messages.
 * Converts ASN.1 encoded SPAT messages to processed SPAT objects.
 */
@Slf4j
@Component
public class SpatDecoder implements Decoder {

    MessageFrameCodec codec;
    SpatJsonValidator spatJsonValidator;
    SpatProcessedJsonConverter converter = new SpatProcessedJsonConverter();
    XmlMapper xmlMapper = new XmlMapper();

    /**
     * Constructs a SpatDecoder with required dependencies.
     *
     * @param codec             MessageFrameCodec for ASN.1 decoding
     * @param spatJsonValidator Validator for SPAT JSON messages
     */
    @Autowired
    SpatDecoder(MessageFrameCodec codec, SpatJsonValidator spatJsonValidator) {
        this.codec = codec;
        this.spatJsonValidator = spatJsonValidator;
    }

    /**
     * Decodes an ASN.1 encoded SPAT message to a processed SPAT object.
     *
     * @param message EncodedMessage containing ASN.1 SPAT data
     * @return DecodedMessage containing processed SPAT or error details
     */
    @Override
    public DecodedMessage decode(EncodedMessage message) {

        String xer = decodeAsnToXERString(message.getAsn1Message());

        try {
            OdeMessageFrameData odeMessageFrameData = convertXERToMessageFrame(xer);
            ProcessedSpat processedSpat = convertMessageFrameToProcessedSpat(odeMessageFrameData);
            return new SpatDecodedMessage(processedSpat, message.getAsn1Message(), "");

        } catch (JsonProcessingException e) {
            return new SpatDecodedMessage(null, message.getAsn1Message(), e.getMessage());
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
        metadata.setRecordType(RecordType.spatTx);
        metadata.setSecurityResultCode(SecurityResultCode.success);
        metadata.setRecordGeneratedBy(GeneratedBy.RSU);
        metadata.setSource(Source.EV);

        JsonNode rootNode = xmlMapper.readTree(encodedXml);

        MessageFrame<?> messageFrame = xmlMapper.convertValue(rootNode, MessageFrame.class);

        OdeMessageFramePayload payload = new OdeMessageFramePayload(messageFrame);

        return new OdeMessageFrameData(metadata, payload);

    }

    /**
     * Converts OdeMessageFrameData to a processed SPAT object.
     * Validates the message and returns either a processed or failure SPAT.
     *
     * @param odeMessageFrameData OdeMessageFrameData to process
     * @return ProcessedSpat object containing SPAT data or validation failure
     */
    public ProcessedSpat convertMessageFrameToProcessedSpat(OdeMessageFrameData odeMessageFrameData) {
        DeserializedRawSpat deserializedSpat = new DeserializedRawSpat();
        try {
            JsonValidatorResult validationResults = spatJsonValidator.validate(odeMessageFrameData.toJson());
            deserializedSpat.setOdeSpatMessageFrameData(odeMessageFrameData);
            deserializedSpat.setValidatorResults(validationResults);
        } catch (Exception e) {
            JsonValidatorResult validatorResult = new JsonValidatorResult();

            validatorResult.addException(e);
            deserializedSpat.setValidationFailure(true);
            deserializedSpat.setValidatorResults(validatorResult);
            deserializedSpat.setFailedMessage(e.getMessage());
        }

        if (!deserializedSpat.isValidationFailure()) {
            OdeMessageFrameData rawValue = new OdeMessageFrameData();
            rawValue.setMetadata(odeMessageFrameData.getMetadata());
            OdeMessageFrameMetadata spatMetadata = rawValue.getMetadata();

            rawValue.setPayload(odeMessageFrameData.getPayload());
            SPATMessageFrame spatMessageFrame = (SPATMessageFrame) rawValue.getPayload().getData();

            ProcessedSpat processedSpat = converter.createProcessedSpat(spatMessageFrame.getValue(), spatMetadata,
                    deserializedSpat.getValidatorResults());

            // Set the schema version
            processedSpat.setSchemaVersion(ProcessedSchemaVersions.PROCESSED_SPAT_SCHEMA_VERSION);
            return processedSpat;
        } else {
            ProcessedSpat processedSpat = converter.createFailureProcessedSpat(
                    deserializedSpat.getValidatorResults(),
                    deserializedSpat.getFailedMessage());
            processedSpat.setSchemaVersion(ProcessedSchemaVersions.PROCESSED_SPAT_SCHEMA_VERSION);
            return processedSpat;
        }

    }
}
