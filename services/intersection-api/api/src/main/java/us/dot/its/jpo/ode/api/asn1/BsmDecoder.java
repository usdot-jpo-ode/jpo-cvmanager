package us.dot.its.jpo.ode.api.asn1;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;

import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import j2735ffm.MessageFrameCodec;
import us.dot.its.jpo.asn.j2735.r2024.BasicSafetyMessage.BasicSafetyMessageMessageFrame;
import us.dot.its.jpo.ode.api.models.messages.BsmDecodedMessage;
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
import us.dot.its.jpo.geojsonconverter.converter.bsm.BsmProcessedJsonConverter;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.Point;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.bsm.DeserializedRawBsm;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.bsm.ProcessedBsm;
import us.dot.its.jpo.geojsonconverter.utils.ProcessedSchemaVersions;
import us.dot.its.jpo.geojsonconverter.validator.BsmJsonValidator;
import us.dot.its.jpo.geojsonconverter.validator.JsonValidatorResult;

/**
 * Decoder implementation for Basic Safety Message (BSM) messages.
 * Converts ASN.1 encoded BSM messages to processed BSM objects.
 */
@Slf4j
@Component
public class BsmDecoder implements Decoder {

    MessageFrameCodec codec;
    BsmJsonValidator bsmJsonValidator;
    BsmProcessedJsonConverter converter = new BsmProcessedJsonConverter();
    XmlMapper xmlMapper = new XmlMapper();

    /**
     * Constructs a BsmDecoder with required dependencies.
     *
     * @param codec            MessageFrameCodec for ASN.1 decoding
     * @param bsmJsonValidator Validator for BSM JSON messages
     */
    @Autowired
    BsmDecoder(MessageFrameCodec codec, BsmJsonValidator bsmJsonValidator) {
        this.codec = codec;
        this.bsmJsonValidator = bsmJsonValidator;
    }

    /**
     * Decodes an ASN.1 encoded BSM message to a processed BSM object.
     *
     * @param message EncodedMessage containing ASN.1 BSM data
     * @return DecodedMessage containing processed BSM or error details
     */
    @Override
    public DecodedMessage decode(EncodedMessage message) {

        String xer = decodeAsnToXERString(message.getAsn1Message());
        OdeMessageFrameData odeMessageFrameData = null;

        try {
            odeMessageFrameData = convertXERToMessageFrame(xer);
            ProcessedBsm<Point> processedBsm = convertMessageFrameToProcessedBsm(odeMessageFrameData);
            return new BsmDecodedMessage(processedBsm, message.getAsn1Message(), "");

        } catch (JsonProcessingException e) {
            return new BsmDecodedMessage(null, message.getAsn1Message(), e.getMessage());
        }

    }

    /**
     * Converts an ASN.1 hex string to XER (XML Encoding Rules) string.
     *
     * @param asnHex ASN.1 encoded hex string
     * @return XER string representation of the message
     */
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
    public OdeMessageFrameData convertXERToMessageFrame(String encodedXml)
            throws JsonMappingException, JsonProcessingException {
        OdeMessageFrameMetadata metadata = new OdeMessageFrameMetadata();
        metadata.setOdeReceivedAt(DateTimeUtils.now());
        metadata.setRecordType(RecordType.bsmTx);
        metadata.setSecurityResultCode(SecurityResultCode.success);
        metadata.setRecordGeneratedBy(GeneratedBy.OBU);
        metadata.setSource(Source.EV);

        JsonNode rootNode = xmlMapper.readTree(encodedXml);

        MessageFrame<?> messageFrame = xmlMapper.convertValue(rootNode, MessageFrame.class);

        OdeMessageFramePayload payload = new OdeMessageFramePayload(messageFrame);

        return new OdeMessageFrameData(metadata, payload);

    }

    /**
     * Converts OdeMessageFrameData to a processed BSM object.
     * Validates the message and returns either a processed or failure BSM.
     *
     * @param odeMessageFrameData OdeMessageFrameData to process
     * @return ProcessedBsm object containing BSM data or validation failure
     */
    public ProcessedBsm<Point> convertMessageFrameToProcessedBsm(OdeMessageFrameData odeMessageFrameData) {
        DeserializedRawBsm deserializedBsm = new DeserializedRawBsm();
        try {
            JsonValidatorResult validationResults = bsmJsonValidator.validate(odeMessageFrameData.toJson());
            deserializedBsm.setOdeBsmMessageFrameData(odeMessageFrameData);
            deserializedBsm.setValidatorResults(validationResults);
        } catch (Exception e) {
            JsonValidatorResult validatorResult = new JsonValidatorResult();

            validatorResult.addException(e);
            deserializedBsm.setValidationFailure(true);
            deserializedBsm.setValidatorResults(validatorResult);
            deserializedBsm.setFailedMessage(e.getMessage());
        }

        if (!deserializedBsm.isValidationFailure()) {
            OdeMessageFrameData rawValue = new OdeMessageFrameData();
            rawValue.setMetadata(odeMessageFrameData.getMetadata());
            OdeMessageFrameMetadata bsmMetadata = rawValue.getMetadata();

            rawValue.setPayload(odeMessageFrameData.getPayload());
            BasicSafetyMessageMessageFrame bsmMessageFrame = (BasicSafetyMessageMessageFrame) rawValue.getPayload()
                    .getData();

            ProcessedBsm<Point> processedBsm = converter.createProcessedBsm(bsmMetadata, bsmMessageFrame,
                    deserializedBsm.getValidatorResults());

            processedBsm.getProperties().setSchemaVersion(ProcessedSchemaVersions.PROCESSED_BSM_SCHEMA_VERSION);
            return processedBsm;
        } else {
            ProcessedBsm<Point> processedBsm = converter.createFailureProcessedBsm(
                    deserializedBsm.getValidatorResults(),
                    deserializedBsm.getFailedMessage());
            processedBsm.getProperties().setSchemaVersion(ProcessedSchemaVersions.PROCESSED_BSM_SCHEMA_VERSION);
            return processedBsm;
        }

    }
}
