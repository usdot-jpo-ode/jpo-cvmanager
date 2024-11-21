package us.dot.its.jpo.ode.api.asn1;


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
            // String decodedXml = mockMapDecode();

            // Convert to Ode Json 
            OdeMapData map = getAsOdeJson(decodedXml);

            try{
                ProcessedMap<LineString> processedMap = createProcessedMap(map);
                // build output data structure
                return new MapDecodedMessage(processedMap, map, message.getAsn1Message(), "");
            }catch (Exception e) {
                return new MapDecodedMessage(null, map, message.getAsn1Message(), e.getMessage());
            }

            
            
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new MapDecodedMessage(null, null, message.getAsn1Message(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new MapDecodedMessage(null, null, message.getAsn1Message(), e.getMessage());
        }
    }

    @Override
    public OdeData getAsOdeData(String encodedData) {
        OdeMsgPayload payload = new OdeAsn1Payload(new OdeHexByteArray(encodedData));

        // construct metadata
        OdeMapMetadata metadata = new OdeMapMetadata(payload);

        metadata.setOdeReceivedAt(DecoderManager.getOdeReceivedAt());
        metadata.setOriginIp(DecoderManager.getOriginIp());
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
		if (metadataNode instanceof ObjectNode) {
			ObjectNode object = (ObjectNode) metadataNode;
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
				e.printStackTrace();
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
            J2735IntersectionGeometry intersection = mapPayload.getMap().getIntersections().getIntersections().get(0);



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

    public String mockMapDecode(){
        return "<?xml version=\"1.0\"?><OdeAsn1Data><metadata><logFileName/><recordType>mapTx</recordType><securityResultCode>success</securityResultCode><receivedMessageDetails/><encodings><encodings><elementName>unsecuredData</elementName><elementType>MessageFrame</elementType><encodingRule>UPER</encodingRule></encodings></encodings><payloadType>us.dot.its.jpo.ode.model.OdeAsn1Payload</payloadType><serialId><streamId>af11b4ee-e2eb-48ef-82c6-97172b6cda7a</streamId><bundleSize>1</bundleSize><bundleId>0</bundleId><recordId>0</recordId><serialNumber>0</serialNumber></serialId><odeReceivedAt>2024-05-10T15:57:46.531370277Z</odeReceivedAt><schemaVersion>6</schemaVersion><maxDurationTime>0</maxDurationTime><recordGeneratedAt/><recordGeneratedBy/><sanitized>false</sanitized><odePacketID/><odeTimStartDateTime/><mapSource>RSU</mapSource><originIp>172.18.0.1</originIp></metadata><payload><dataType>MessageFrame</dataType><data><MessageFrame><messageId>18</messageId><value><MapData><msgIssueRevision>0</msgIssueRevision><layerType><intersectionData/></layerType><layerID>0</layerID><intersections><IntersectionGeometry><id><id>12110</id></id><revision>0</revision><refPoint><lat>395952649</lat><long>-1050914122</long><elevation>16770</elevation></refPoint><laneWidth>366</laneWidth><laneSet><GenericLane><laneID>2</laneID><ingressApproach>1</ingressApproach><laneAttributes><directionalUse>10</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><maneuvers>100000000000</maneuvers><nodeList><nodes><NodeXY><delta><node-XY6><x>2225</x><y>808</y></node-XY6></delta><attributes><dElevation>-10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>10517</x><y>-161</y></node-XY6></delta><attributes><dElevation>-60</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>2769</x><y>112</y></node-XY6></delta><attributes><dElevation>-10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>6142</x><y>-180</y></node-XY6></delta><attributes><dElevation>-30</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>6636</x><y>-12</y></node-XY6></delta><attributes><dElevation>-20</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>3804</x><y>-7</y></node-XY6></delta></NodeXY></nodes></nodeList><connectsTo><Connection><connectingLane><lane>19</lane><maneuver>100000000000</maneuver></connectingLane><signalGroup>4</signalGroup><connectionID>1</connectionID></Connection></connectsTo></GenericLane><GenericLane><laneID>3</laneID><ingressApproach>1</ingressApproach><laneAttributes><directionalUse>10</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><maneuvers>100000000000</maneuvers><nodeList><nodes><NodeXY><delta><node-XY6><x>2222</x><y>515</y></node-XY6></delta><attributes><dElevation>-10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>2933</x><y>-82</y></node-XY6></delta><attributes><dElevation>-10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>6259</x><y>-85</y></node-XY6></delta><attributes><dElevation>-40</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>3416</x><y>-98</y></node-XY6></delta><attributes><dElevation>-20</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>6867</x><y>-39</y></node-XY6></delta><attributes><dElevation>-30</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>2657</x><y>-55</y></node-XY6></delta><attributes><dElevation>-10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>4259</x><y>67</y></node-XY6></delta><attributes><dElevation>-10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>3481</x><y>0</y></node-XY6></delta></NodeXY></nodes></nodeList><connectsTo><Connection><connectingLane><lane>18</lane><maneuver>100000000000</maneuver></connectingLane><signalGroup>4</signalGroup><connectionID>1</connectionID></Connection></connectsTo></GenericLane><GenericLane><laneID>1</laneID><ingressApproach>1</ingressApproach><laneAttributes><directionalUse>10</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><maneuvers>001000000000</maneuvers><nodeList><nodes><NodeXY><delta><node-XY6><x>2215</x><y>1218</y></node-XY6></delta></NodeXY><NodeXY><delta><node-XY6><x>10451</x><y>-223</y></node-XY6></delta><attributes><dElevation>-70</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>1852</x><y>-108</y></node-XY6></delta><attributes><dElevation>-10</dElevation></attributes></NodeXY></nodes></nodeList><connectsTo><Connection><connectingLane><lane>28</lane><maneuver>001000000000</maneuver></connectingLane><connectionID>1</connectionID></Connection></connectsTo></GenericLane><GenericLane><laneID>4</laneID><ingressApproach>1</ingressApproach><laneAttributes><directionalUse>10</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><maneuvers>010000000000</maneuvers><nodeList><nodes><NodeXY><delta><node-XY6><x>2213</x><y>-124</y></node-XY6></delta><attributes><dElevation>-10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>7187</x><y>20</y></node-XY6></delta><attributes><dElevation>-40</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>541</x><y>40</y></node-XY6></delta><attributes><dElevation>-10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>1298</x><y>229</y></node-XY6></delta></NodeXY></nodes></nodeList><connectsTo><Connection><connectingLane><lane>12</lane><maneuver>010000000000</maneuver></connectingLane><signalGroup>7</signalGroup><connectionID>1</connectionID></Connection></connectsTo></GenericLane><GenericLane><laneID>6</laneID><egressApproach>2</egressApproach><laneAttributes><directionalUse>01</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><nodeList><nodes><NodeXY><delta><node-XY6><x>2267</x><y>-1019</y></node-XY6></delta><attributes><dElevation>-10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>365</x><y>7</y></node-XY6></delta></NodeXY></nodes></nodeList></GenericLane><GenericLane><laneID>5</laneID><egressApproach>2</egressApproach><laneAttributes><directionalUse>01</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><nodeList><nodes><NodeXY><delta><node-XY6><x>2267</x><y>-662</y></node-XY6></delta><attributes><dElevation>-10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>338</x><y>0</y></node-XY6></delta></NodeXY></nodes></nodeList></GenericLane><GenericLane><laneID>7</laneID><egressApproach>2</egressApproach><laneAttributes><directionalUse>01</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><nodeList><nodes><NodeXY><delta><node-XY6><x>2294</x><y>-1463</y></node-XY6></delta><attributes><dElevation>-10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>365</x><y>27</y></node-XY6></delta></NodeXY></nodes></nodeList></GenericLane><GenericLane><laneID>12</laneID><egressApproach>4</egressApproach><laneAttributes><directionalUse>01</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><nodeList><nodes><NodeXY><delta><node-XY6><x>-952</x><y>-1898</y></node-XY6></delta></NodeXY><NodeXY><delta><node-XY6><x>0</x><y>-377</y></node-XY6></delta></NodeXY></nodes></nodeList></GenericLane><GenericLane><laneID>13</laneID><egressApproach>4</egressApproach><laneAttributes><directionalUse>01</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><nodeList><nodes><NodeXY><delta><node-XY6><x>-1440</x><y>-1898</y></node-XY6></delta></NodeXY><NodeXY><delta><node-XY6><x>7</x><y>-390</y></node-XY6></delta></NodeXY><NodeXY><delta><node-XY6><x>0</x><y>0</y></node-XY6></delta></NodeXY><NodeXY><delta><node-XY6><x>0</x><y>7</y></node-XY6></delta></NodeXY></nodes></nodeList></GenericLane><GenericLane><laneID>10</laneID><ingressApproach>3</ingressApproach><laneAttributes><directionalUse>10</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><maneuvers>100000000000</maneuvers><nodeList><nodes><NodeXY><delta><node-XY6><x>705</x><y>-1919</y></node-XY6></delta></NodeXY><NodeXY><delta><node-XY6><x>-1</x><y>-7515</y></node-XY6></delta><attributes><dElevation>-40</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-41</x><y>-5272</y></node-XY6></delta><attributes><dElevation>10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-17</x><y>-11337</y></node-XY6></delta></NodeXY><NodeXY><delta><node-XY6><x>17</x><y>-5938</y></node-XY6></delta><attributes><dElevation>10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>0</x><y>0</y></node-XY6></delta></NodeXY><NodeXY><delta><node-XY6><x>-7</x><y>-7</y></node-XY6></delta></NodeXY></nodes></nodeList><connectsTo><Connection><connectingLane><lane>26</lane><maneuver>100000000000</maneuver></connectingLane><signalGroup>6</signalGroup><connectionID>1</connectionID></Connection></connectsTo></GenericLane><GenericLane><laneID>9</laneID><ingressApproach>3</ingressApproach><laneAttributes><directionalUse>10</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><maneuvers>100000000000</maneuvers><nodeList><nodes><NodeXY><delta><node-XY6><x>1090</x><y>-1930</y></node-XY6></delta><attributes><dElevation>-10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>9</x><y>-4764</y></node-XY6></delta><attributes><dElevation>-10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-20</x><y>-6402</y></node-XY6></delta><attributes><dElevation>-20</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-34</x><y>-7953</y></node-XY6></delta><attributes><dElevation>-10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>16</x><y>-10940</y></node-XY6></delta><attributes><dElevation>20</dElevation></attributes></NodeXY></nodes></nodeList><connectsTo><Connection><connectingLane><lane>27</lane><maneuver>100000000000</maneuver></connectingLane><signalGroup>6</signalGroup><connectionID>1</connectionID></Connection></connectsTo></GenericLane><GenericLane><laneID>11</laneID><ingressApproach>3</ingressApproach><laneAttributes><directionalUse>10</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><maneuvers>010000000000</maneuvers><nodeList><nodes><NodeXY><delta><node-XY6><x>350</x><y>-1925</y></node-XY6></delta></NodeXY><NodeXY><delta><node-XY6><x>-32</x><y>-6698</y></node-XY6></delta><attributes><dElevation>-30</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>156</x><y>-2616</y></node-XY6></delta></NodeXY></nodes></nodeList><connectsTo><Connection><connectingLane><lane>18</lane><maneuver>010000000000</maneuver></connectingLane><signalGroup>1</signalGroup><connectionID>1</connectionID></Connection></connectsTo></GenericLane><GenericLane><laneID>8</laneID><ingressApproach>3</ingressApproach><laneAttributes><directionalUse>10</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><maneuvers>001000000000</maneuvers><nodeList><nodes><NodeXY><delta><node-XY6><x>1557</x><y>-1956</y></node-XY6></delta><attributes><dElevation>-10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-101</x><y>-1956</y></node-XY6></delta></NodeXY><NodeXY><delta><node-XY6><x>-5</x><y>-7393</y></node-XY6></delta><attributes><dElevation>-40</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-121</x><y>-1636</y></node-XY6></delta></NodeXY></nodes></nodeList><connectsTo><Connection><connectingLane><lane>7</lane><maneuver>001000000000</maneuver></connectingLane><connectionID>1</connectionID></Connection></connectsTo></GenericLane><GenericLane><laneID>15</laneID><ingressApproach>5</ingressApproach><laneAttributes><directionalUse>10</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><maneuvers>100000000000</maneuvers><nodeList><nodes><NodeXY><delta><node-XY6><x>-2405</x><y>-1018</y></node-XY6></delta><attributes><dElevation>10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-9339</x><y>26</y></node-XY6></delta><attributes><dElevation>30</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-11895</x><y>-38</y></node-XY6></delta><attributes><dElevation>50</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-8814</x><y>13</y></node-XY6></delta></NodeXY></nodes></nodeList><connectsTo><Connection><connectingLane><lane>6</lane><maneuver>100000000000</maneuver></connectingLane><signalGroup>8</signalGroup><connectionID>1</connectionID></Connection></connectsTo></GenericLane><GenericLane><laneID>16</laneID><ingressApproach>5</ingressApproach><laneAttributes><directionalUse>10</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><maneuvers>100000000000</maneuvers><nodeList><nodes><NodeXY><delta><node-XY6><x>-2337</x><y>-656</y></node-XY6></delta><attributes><dElevation>10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-12229</x><y>15</y></node-XY6></delta><attributes><dElevation>50</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-11362</x><y>-37</y></node-XY6></delta><attributes><dElevation>30</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-6537</x><y>1</y></node-XY6></delta></NodeXY></nodes></nodeList><connectsTo><Connection><connectingLane><lane>5</lane><maneuver>100000000000</maneuver></connectingLane><signalGroup>8</signalGroup><connectionID>1</connectionID></Connection></connectsTo></GenericLane><GenericLane><laneID>17</laneID><ingressApproach>5</ingressApproach><laneAttributes><directionalUse>10</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><maneuvers>010000000000</maneuvers><nodeList><nodes><NodeXY><delta><node-XY6><x>-2338</x><y>-284</y></node-XY6></delta><attributes><dElevation>10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-8892</x><y>-18</y></node-XY6></delta><attributes><dElevation>30</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-11959</x><y>-2</y></node-XY6></delta><attributes><dElevation>50</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-1564</x><y>-180</y></node-XY6></delta></NodeXY></nodes></nodeList><connectsTo><Connection><connectingLane><lane>26</lane><maneuver>010000000000</maneuver></connectingLane><signalGroup>3</signalGroup><connectionID>1</connectionID></Connection></connectsTo></GenericLane><GenericLane><laneID>14</laneID><ingressApproach>5</ingressApproach><laneAttributes><directionalUse>10</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><maneuvers>001000000000</maneuvers><nodeList><nodes><NodeXY><delta><node-XY6><x>-2401</x><y>-1380</y></node-XY6></delta><attributes><dElevation>10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-19947</x><y>8</y></node-XY6></delta><attributes><dElevation>80</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-10108</x><y>-7</y></node-XY6></delta></NodeXY></nodes></nodeList><connectsTo><Connection><connectingLane><lane>13</lane><maneuver>001000000000</maneuver></connectingLane><connectionID>1</connectionID></Connection></connectsTo></GenericLane><GenericLane><laneID>18</laneID><egressApproach>6</egressApproach><laneAttributes><directionalUse>01</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><nodeList><nodes><NodeXY><delta><node-XY6><x>-2264</x><y>479</y></node-XY6></delta><attributes><dElevation>20</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-580</x><y>-14</y></node-XY6></delta></NodeXY></nodes></nodeList></GenericLane><GenericLane><laneID>19</laneID><egressApproach>6</egressApproach><laneAttributes><directionalUse>01</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><nodeList><nodes><NodeXY><delta><node-XY6><x>-2257</x><y>796</y></node-XY6></delta><attributes><dElevation>20</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-518</x><y>0</y></node-XY6></delta></NodeXY></nodes></nodeList></GenericLane><GenericLane><laneID>20</laneID><egressApproach>6</egressApproach><laneAttributes><directionalUse>01</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><nodeList><nodes><NodeXY><delta><node-XY6><x>-2236</x><y>1229</y></node-XY6></delta><attributes><dElevation>20</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-484</x><y>0</y></node-XY6></delta></NodeXY></nodes></nodeList></GenericLane><GenericLane><laneID>23</laneID><ingressApproach>7</ingressApproach><laneAttributes><directionalUse>10</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><maneuvers>100000000000</maneuvers><nodeList><nodes><NodeXY><delta><node-XY6><x>-914</x><y>1903</y></node-XY6></delta><attributes><dElevation>20</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>225</x><y>12288</y></node-XY6></delta><attributes><dElevation>30</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>40</x><y>9634</y></node-XY6></delta><attributes><dElevation>-20</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>32</x><y>8035</y></node-XY6></delta><attributes><dElevation>-30</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>0</x><y>0</y></node-XY6></delta></NodeXY><NodeXY><delta><node-XY6><x>-7</x><y>28</y></node-XY6></delta></NodeXY></nodes></nodeList><connectsTo><Connection><connectingLane><lane>12</lane><maneuver>100000000000</maneuver></connectingLane><signalGroup>2</signalGroup><connectionID>1</connectionID></Connection></connectsTo></GenericLane><GenericLane><laneID>22</laneID><ingressApproach>7</ingressApproach><laneAttributes><directionalUse>10</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><maneuvers>100000000000</maneuvers><nodeList><nodes><NodeXY><delta><node-XY6><x>-1221</x><y>1914</y></node-XY6></delta><attributes><dElevation>20</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>92</x><y>10475</y></node-XY6></delta><attributes><dElevation>30</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>89</x><y>11582</y></node-XY6></delta><attributes><dElevation>-20</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>48</x><y>7912</y></node-XY6></delta><attributes><dElevation>-30</dElevation></attributes></NodeXY></nodes></nodeList><connectsTo><Connection><connectingLane><lane>13</lane><maneuver>100000000000</maneuver></connectingLane><signalGroup>2</signalGroup><connectionID>1</connectionID></Connection></connectsTo></GenericLane><GenericLane><laneID>21</laneID><ingressApproach>7</ingressApproach><laneAttributes><directionalUse>10</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><maneuvers>001000000000</maneuvers><nodeList><nodes><NodeXY><delta><node-XY6><x>-1649</x><y>1935</y></node-XY6></delta><attributes><dElevation>20</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>16</x><y>3182</y></node-XY6></delta><attributes><dElevation>10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>21</x><y>5991</y></node-XY6></delta><attributes><dElevation>20</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>102</x><y>4810</y></node-XY6></delta></NodeXY><NodeXY><delta><node-XY6><x>85</x><y>11803</y></node-XY6></delta><attributes><dElevation>-30</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>19</x><y>4161</y></node-XY6></delta><attributes><dElevation>-20</dElevation></attributes></NodeXY></nodes></nodeList><connectsTo><Connection><connectingLane><lane>20</lane><maneuver>001000000000</maneuver></connectingLane><connectionID>1</connectionID></Connection></connectsTo></GenericLane><GenericLane><laneID>24</laneID><ingressApproach>7</ingressApproach><laneAttributes><directionalUse>10</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><maneuvers>010000000000</maneuvers><nodeList><nodes><NodeXY><delta><node-XY6><x>-535</x><y>1926</y></node-XY6></delta><attributes><dElevation>20</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>28</x><y>4582</y></node-XY6></delta><attributes><dElevation>10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>88</x><y>3793</y></node-XY6></delta><attributes><dElevation>10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-143</x><y>1866</y></node-XY6></delta><attributes><dElevation>10</dElevation></attributes></NodeXY></nodes></nodeList><connectsTo><Connection><connectingLane><lane>6</lane><maneuver>010000000000</maneuver></connectingLane><signalGroup>5</signalGroup><connectionID>1</connectionID></Connection></connectsTo></GenericLane><GenericLane><laneID>25</laneID><ingressApproach>7</ingressApproach><laneAttributes><directionalUse>10</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><maneuvers>010000000000</maneuvers><nodeList><nodes><NodeXY><delta><node-XY6><x>-213</x><y>1913</y></node-XY6></delta><attributes><dElevation>20</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>14</x><y>4594</y></node-XY6></delta><attributes><dElevation>10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-91</x><y>2238</y></node-XY6></delta><attributes><dElevation>10</dElevation></attributes></NodeXY></nodes></nodeList><connectsTo><Connection><connectingLane><lane>5</lane><maneuver>010000000000</maneuver></connectingLane><signalGroup>5</signalGroup><connectionID>1</connectionID></Connection></connectsTo></GenericLane><GenericLane><laneID>26</laneID><egressApproach>8</egressApproach><laneAttributes><directionalUse>01</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><nodeList><nodes><NodeXY><delta><node-XY6><x>724</x><y>1902</y></node-XY6></delta><attributes><dElevation>10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-7</x><y>352</y></node-XY6></delta><attributes><dElevation>10</dElevation></attributes></NodeXY></nodes></nodeList></GenericLane><GenericLane><laneID>27</laneID><egressApproach>8</egressApproach><laneAttributes><directionalUse>01</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><nodeList><nodes><NodeXY><delta><node-XY6><x>1105</x><y>1888</y></node-XY6></delta><attributes><dElevation>10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-7</x><y>332</y></node-XY6></delta><attributes><dElevation>10</dElevation></attributes></NodeXY></nodes></nodeList></GenericLane><GenericLane><laneID>28</laneID><egressApproach>8</egressApproach><laneAttributes><directionalUse>01</directionalUse><sharedWith>0000000000</sharedWith><laneType><vehicle>00000000</vehicle></laneType></laneAttributes><nodeList><nodes><NodeXY><delta><node-XY6><x>1591</x><y>1874</y></node-XY6></delta><attributes><dElevation>10</dElevation></attributes></NodeXY><NodeXY><delta><node-XY6><x>-14</x><y>346</y></node-XY6></delta></NodeXY><NodeXY><delta><node-XY6><x>0</x><y>7</y></node-XY6></delta></NodeXY></nodes></nodeList></GenericLane></laneSet></IntersectionGeometry></intersections></MapData></value></MessageFrame></data></payload></OdeAsn1Data>";
    }

}
