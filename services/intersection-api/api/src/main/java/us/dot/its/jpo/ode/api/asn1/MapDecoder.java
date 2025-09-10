package us.dot.its.jpo.ode.api.asn1;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import lombok.extern.slf4j.Slf4j;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import j2735ffm.MessageFrameCodec;
import us.dot.its.jpo.asn.j2735.r2024.MapData.IntersectionGeometry;
import us.dot.its.jpo.asn.j2735.r2024.MapData.MapDataMessageFrame;
import us.dot.its.jpo.ode.api.models.messages.MapDecodedMessage;
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
import us.dot.its.jpo.geojsonconverter.converter.map.MapProcessedJsonConverter;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.connectinglanes.ConnectingLanesFeatureCollection;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.DeserializedRawMap;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.MapFeatureCollection;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.MapSharedProperties;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.geojsonconverter.utils.ProcessedSchemaVersions;
import us.dot.its.jpo.geojsonconverter.validator.MapJsonValidator;
import us.dot.its.jpo.geojsonconverter.validator.JsonValidatorResult;

/**
 * Decoder implementation for Basic Safety Message (MAP) messages.
 * Converts ASN.1 encoded MAP messages to processed MAP objects.
 */
@Slf4j
@Component
public class MapDecoder implements Decoder {

    MessageFrameCodec codec;
    MapJsonValidator mapJsonValidator;
    public static final MapProcessedJsonConverter converter = new MapProcessedJsonConverter();
    public static final XmlMapper xmlMapper = new XmlMapper();

    /**
     * Constructs a MapDecoder with required dependencies.
     *
     * @param codec            MessageFrameCodec for ASN.1 decoding
     * @param mapJsonValidator Validator for MAP JSON messages
     */
    @Autowired
    MapDecoder(MessageFrameCodec codec, MapJsonValidator mapJsonValidator) {
        this.codec = codec;
        this.mapJsonValidator = mapJsonValidator;
    }

    /**
     * Decodes an ASN.1 encoded MAP message to a processed MAP object.
     *
     * @param message EncodedMessage containing ASN.1 MAP data
     * @return DecodedMessage containing processed MAP or error details
     */
    @Override
    public DecodedMessage decode(EncodedMessage message) {

        String xer = decodeAsnToXERString(message.getAsn1Message());

        try {
            OdeMessageFrameData odeMessageFrameData = convertXERToMessageFrame(xer);
            ProcessedMap<LineString> processedMap = convertMessageFrameToProcessedMap(odeMessageFrameData);
            return new MapDecodedMessage(processedMap, message.getAsn1Message(), "");

        } catch (JsonProcessingException e) {
            return new MapDecodedMessage(null, message.getAsn1Message(), e.getMessage());
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
        metadata.setRecordType(RecordType.mapTx);
        metadata.setSecurityResultCode(SecurityResultCode.success);
        metadata.setRecordGeneratedBy(GeneratedBy.RSU);
        metadata.setSource(Source.EV);

        JsonNode rootNode = xmlMapper.readTree(encodedXml);

        MessageFrame<?> messageFrame = xmlMapper.convertValue(rootNode, MessageFrame.class);

        OdeMessageFramePayload payload = new OdeMessageFramePayload(messageFrame);

        return new OdeMessageFrameData(metadata, payload);

    }

    /**
     * Converts OdeMessageFrameData to a processed MAP object.
     * Validates the message and returns either a processed or failure MAP.
     *
     * @param odeMessageFrameData OdeMessageFrameData to process
     * @return ProcessedMap object containing MAP data or validation failure
     */
    public ProcessedMap<LineString> convertMessageFrameToProcessedMap(OdeMessageFrameData odeMessageFrameData) {
        DeserializedRawMap deserializedMap = new DeserializedRawMap();
        try {
            JsonValidatorResult validationResults = mapJsonValidator.validate(odeMessageFrameData.toJson());
            deserializedMap.setOdeMapMessageFrameData(odeMessageFrameData);
            deserializedMap.setValidatorResults(validationResults);
        } catch (Exception e) {
            JsonValidatorResult validatorResult = new JsonValidatorResult();

            validatorResult.addException(e);
            deserializedMap.setValidationFailure(true);
            deserializedMap.setValidatorResults(validatorResult);
            deserializedMap.setFailedMessage(e.getMessage());
        }

        if (!deserializedMap.isValidationFailure()) {
            OdeMessageFrameData rawValue = new OdeMessageFrameData();
            rawValue.setMetadata(odeMessageFrameData.getMetadata());
            OdeMessageFrameMetadata mapMetadata = rawValue.getMetadata();

            rawValue.setPayload(odeMessageFrameData.getPayload());
            MapDataMessageFrame mapMessageFrame = (MapDataMessageFrame) rawValue.getPayload().getData();
            IntersectionGeometry intersection = mapMessageFrame.getValue().getIntersections().get(0);

            MapSharedProperties sharedProps = converter.createProperties(mapMessageFrame.getValue(), mapMetadata,
                    intersection, deserializedMap.getValidatorResults());

            // Set the schema version
            sharedProps.setSchemaVersion(ProcessedSchemaVersions.PROCESSED_MAP_SCHEMA_VERSION);

            MapFeatureCollection<LineString> mapFeatureCollection = converter.createFeatureCollection(intersection);
            ConnectingLanesFeatureCollection<LineString> connectingLanesFeatureCollection = converter
                    .createConnectingLanesFeatureCollection(
                            mapMetadata, intersection);

            ProcessedMap<LineString> processedMapObject = new ProcessedMap<LineString>();
            processedMapObject.setMapFeatureCollection(mapFeatureCollection);
            processedMapObject.setConnectingLanesFeatureCollection(connectingLanesFeatureCollection);
            processedMapObject.setProperties(sharedProps);
            return processedMapObject;
        } else {
            ProcessedMap<LineString> processedMap = converter.createFailureProcessedMap(
                    deserializedMap.getValidatorResults(),
                    deserializedMap.getFailedMessage());
            processedMap.getProperties().setSchemaVersion(ProcessedSchemaVersions.PROCESSED_MAP_SCHEMA_VERSION);
            return processedMap;
        }

    }
}
