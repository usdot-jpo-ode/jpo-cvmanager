package us.dot.its.jpo.ode.api.asn1;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import us.dot.its.jpo.ode.api.models.messages.MapDecodedMessage;
import us.dot.its.jpo.geojsonconverter.converter.map.MapProcessedJsonConverter;
import us.dot.its.jpo.geojsonconverter.partitioner.RsuIntersectionKey;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.connectinglanes.ConnectingLanesFeatureCollection;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.MapFeatureCollection;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.MapSharedProperties;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.geojsonconverter.validator.JsonValidatorResult;
import us.dot.its.jpo.geojsonconverter.validator.MapJsonValidator;
import us.dot.its.jpo.ode.api.models.messages.DecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.EncodedMessage;
import us.dot.its.jpo.ode.context.AppContext;
import us.dot.its.jpo.ode.model.Asn1Encoding;
import us.dot.its.jpo.ode.model.Asn1Encoding.EncodingRule;
import us.dot.its.jpo.ode.model.OdeAsn1Data;
import us.dot.its.jpo.ode.model.OdeAsn1Payload;
import us.dot.its.jpo.ode.model.OdeMapData;
import us.dot.its.jpo.ode.model.OdeMapMetadata;
import us.dot.its.jpo.ode.model.OdeMapMetadata.MapSource;
import us.dot.its.jpo.ode.model.OdeMapPayload;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeHexByteArray;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeMsgPayload;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.model.RxSource;
import us.dot.its.jpo.ode.plugin.j2735.J2735IntersectionGeometry;
import us.dot.its.jpo.ode.plugin.j2735.builders.MAPBuilder;
import us.dot.its.jpo.ode.util.JsonUtils;
import us.dot.its.jpo.ode.util.XmlUtils;
import us.dot.its.jpo.ode.util.XmlUtils.XmlUtilsException;

@Component
public class MapDecoder implements Decoder {
    private static final Logger logger = LoggerFactory.getLogger(MapDecoder.class);

    @Autowired
    MapJsonValidator mapJsonValidator;

    public MapProcessedJsonConverter converter = new MapProcessedJsonConverter();

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
            OdeMapData map = getAsOdeJson(decodedXml);

            try{
                ProcessedMap<LineString> processedMap = createProcessedMap(map);
                // build output data structure
                return new MapDecodedMessage(processedMap, map, message.getAsn1Message(), "");
            }catch (Exception e) {
                logger.error("XML Exception: {}", e.getMessage());
                return new MapDecodedMessage(null, map, message.getAsn1Message(), e.getMessage());
            }
            
        } catch (JsonProcessingException e) {
            logger.error("JSON Processing Exception: {}", e.getMessage(), e);
            return new MapDecodedMessage(null, null, message.getAsn1Message(), e.getMessage());
        } catch (Exception e) {
            logger.error("Generic Exception: {}", e.getMessage(), e);
            return new MapDecodedMessage(null, null, message.getAsn1Message(), e.getMessage());
        }
    }

    @Override
    public OdeData getAsOdeData(String encodedData) {
        OdeMsgPayload payload = new OdeAsn1Payload(new OdeHexByteArray(encodedData));

        // construct metadata
        OdeMapMetadata metadata = new OdeMapMetadata(payload);

        metadata.setOdeReceivedAt(DecoderManager.getCurrentIsoTimestamp());
        metadata.setOriginIp(DecoderManager.getStaticUserOriginIp());
        metadata.setRecordType(RecordType.mapTx);
        metadata.setSecurityResultCode(SecurityResultCode.success);

        metadata.setMapSource(MapSource.RSU);

        Asn1Encoding unsecuredDataEncoding = new Asn1Encoding("unsecuredData", "MessageFrame",
                EncodingRule.UPER);
        metadata.addEncoding(unsecuredDataEncoding);

        // construct odeData
        return new OdeAsn1Data(metadata, payload);
    }

    @Override
    public OdeMapData getAsOdeJson(String consumedData) throws XmlUtilsException {
        ObjectNode consumed = XmlUtils.toObjectNode(consumedData);

		JsonNode metadataNode = consumed.findValue(AppContext.METADATA_STRING);
        if (metadataNode instanceof ObjectNode object) {
            // Removing encodings to match ODE behavior
            object.remove(AppContext.ENCODINGS_STRING);

			// Map header file does not have a location and use predefined set required
			// RxSource
			ReceivedMessageDetails receivedMessageDetails = new ReceivedMessageDetails();
			receivedMessageDetails.setRxSource(RxSource.NA);
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode;
			try {
				jsonNode = objectMapper.readTree(receivedMessageDetails.toJson());
				object.set(AppContext.RECEIVEDMSGDETAILS_STRING, jsonNode);
			} catch (JsonProcessingException e) {
                logger.error("Exception deserializing MAP message", e);
			}
		}

		OdeMapMetadata metadata = (OdeMapMetadata) JsonUtils.fromJson(metadataNode.toString(), OdeMapMetadata.class);

		if (metadata.getSchemaVersion() <= 4) {
			metadata.setReceivedMessageDetails(null);
		}

		OdeMapPayload payload = new OdeMapPayload(MAPBuilder.genericMAP(consumed.findValue("MapData")));
		return new OdeMapData(metadata, payload);
    }

    public ProcessedMap<LineString> createProcessedMap(OdeMapData odeMap){
            JsonValidatorResult validationResults = mapJsonValidator.validate(odeMap.toString());
            OdeMapMetadata mapMetadata = (OdeMapMetadata)odeMap.getMetadata();
            OdeMapPayload mapPayload = (OdeMapPayload)odeMap.getPayload();
            J2735IntersectionGeometry intersection = mapPayload.getMap().getIntersections().getIntersections().getFirst();

            MapSharedProperties sharedProps = converter.createProperties(mapPayload, mapMetadata, intersection, validationResults);
            MapFeatureCollection<LineString> mapFeatureCollection = converter.createFeatureCollection(intersection);
            ConnectingLanesFeatureCollection<LineString> connectingLanesFeatureCollection = converter.createConnectingLanesFeatureCollection(mapMetadata, intersection);

            ProcessedMap<LineString> processedMapObject = new ProcessedMap<LineString>();
            processedMapObject.setMapFeatureCollection(mapFeatureCollection);
            processedMapObject.setConnectingLanesFeatureCollection(connectingLanesFeatureCollection);
            processedMapObject.setProperties(sharedProps);

            var key = new RsuIntersectionKey();
            key.setRsuId(mapMetadata.getOriginIp());
            key.setIntersectionReferenceID(intersection.getId());
            return processedMapObject;
    }
}
