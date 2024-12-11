package us.dot.its.jpo.ode.api.asn1;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import us.dot.its.jpo.ode.api.models.messages.BsmDecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.DecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.EncodedMessage;
import us.dot.its.jpo.ode.context.AppContext;
import us.dot.its.jpo.ode.model.Asn1Encoding;
import us.dot.its.jpo.ode.model.Asn1Encoding.EncodingRule;
import us.dot.its.jpo.ode.model.OdeAsn1Data;
import us.dot.its.jpo.ode.model.OdeAsn1Payload;
import us.dot.its.jpo.ode.model.OdeBsmData;
import us.dot.its.jpo.ode.model.OdeBsmMetadata;
import us.dot.its.jpo.ode.model.OdeBsmMetadata.BsmSource;
import us.dot.its.jpo.ode.model.OdeBsmPayload;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeHexByteArray;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeLogMsgMetadataLocation;
import us.dot.its.jpo.ode.model.OdeMsgPayload;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.model.RxSource;
import us.dot.its.jpo.ode.plugin.j2735.builders.BsmBuilder;
import us.dot.its.jpo.ode.util.JsonUtils;
import us.dot.its.jpo.ode.util.XmlUtils;
import us.dot.its.jpo.ode.util.XmlUtils.XmlUtilsException;


@Component
public class BsmDecoder implements Decoder {


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
            // String decodedXml = mockDecodeXmlWithAcm(xml);

            // Convert to Ode Json 
            OdeBsmData bsm = getAsOdeJson(decodedXml);

            // build output data structure
            DecodedMessage decodedMessage = new BsmDecodedMessage(bsm, message.getAsn1Message(), "");
            return decodedMessage;
            
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new BsmDecodedMessage(null, message.getAsn1Message(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new BsmDecodedMessage(null, message.getAsn1Message(), e.getMessage());
        }
    }

    @Override
    public OdeData getAsOdeData(String encodedData) {
        OdeMsgPayload payload = new OdeAsn1Payload(new OdeHexByteArray(encodedData));

        // construct metadata
        OdeBsmMetadata metadata = new OdeBsmMetadata(payload);
        metadata.setOdeReceivedAt(DecoderManager.getOdeReceivedAt());
        metadata.setRecordType(RecordType.bsmTx);
        metadata.setSecurityResultCode(SecurityResultCode.success);

        // construct metadata: receivedMessageDetails
        ReceivedMessageDetails receivedMessageDetails = new ReceivedMessageDetails();
        receivedMessageDetails.setRxSource(RxSource.RV);

        // construct metadata: locationData
        OdeLogMsgMetadataLocation locationData = new OdeLogMsgMetadataLocation();
        receivedMessageDetails.setLocationData(locationData);

        metadata.setReceivedMessageDetails(receivedMessageDetails);
        metadata.setBsmSource(BsmSource.RV);

        Asn1Encoding unsecuredDataEncoding = new Asn1Encoding("unsecuredData", "MessageFrame",
                EncodingRule.UPER);
        metadata.addEncoding(unsecuredDataEncoding);

        // construct odeData
        return new OdeAsn1Data(metadata, payload);
    }

    @Override
    public OdeBsmData getAsOdeJson(String consumedData) throws XmlUtilsException {
        ObjectNode consumed = XmlUtils.toObjectNode(consumedData);

        JsonNode metadataNode = consumed.findValue(AppContext.METADATA_STRING);
        if (metadataNode instanceof ObjectNode) {
            ObjectNode object = (ObjectNode) metadataNode;
            object.remove(AppContext.ENCODINGS_STRING);
        }
        
        OdeBsmMetadata metadata = (OdeBsmMetadata) JsonUtils.fromJson(
            metadataNode.toString(), OdeBsmMetadata.class);

        /*
        *  ODE-755 and ODE-765 Starting with schemaVersion=5 receivedMessageDetails 
        *  will be present in BSM metadata. None should be present in prior versions.
        */
        if (metadata.getSchemaVersion() <= 4) {
            metadata.setReceivedMessageDetails(null);
        }
        
        OdeBsmPayload payload = new OdeBsmPayload(
            BsmBuilder.genericBsm(consumed.findValue("BasicSafetyMessage")));
        return new OdeBsmData(metadata, payload);
    }

    public String mockDecodeXmlWithAcm(String xmlMessage){
        return "<?xml version=\"1.0\"?><OdeAsn1Data><metadata><bsmSource>RV</bsmSource><logFileName/><recordType>bsmTx</recordType><securityResultCode>success</securityResultCode><receivedMessageDetails><locationData><latitude/><longitude/><elevation/><speed/><heading/></locationData><rxSource>RV</rxSource></receivedMessageDetails><encodings><encodings><elementName>unsecuredData</elementName><elementType>MessageFrame</elementType><encodingRule>UPER</encodingRule></encodings></encodings><payloadType>us.dot.its.jpo.ode.model.OdeAsn1Payload</payloadType><serialId><streamId>8829c539-e684-40b7-a786-692acd3f897a</streamId><bundleSize>1</bundleSize><bundleId>0</bundleId><recordId>0</recordId><serialNumber>0</serialNumber></serialId><odeReceivedAt>2024-05-08T20:47:53.830130272Z</odeReceivedAt><schemaVersion>6</schemaVersion><maxDurationTime>0</maxDurationTime><recordGeneratedAt/><recordGeneratedBy/><sanitized>false</sanitized><odePacketID/><odeTimStartDateTime/><originIp/></metadata><payload><dataType>MessageFrame</dataType><data><MessageFrame><messageId>20</messageId><value><BasicSafetyMessage><coreData><msgCnt>18</msgCnt><id>B42DE51F</id><secMark>2998</secMark><lat>0</lat><long>0</long><elev>0</elev><accuracy><semiMajor>0</semiMajor><semiMinor>0</semiMinor><orientation>8100</orientation></accuracy><transmission><forwardGears/></transmission><speed>1315</speed><heading>6448</heading><angle>-1</angle><accelSet><long>0</long><lat>0</lat><vert>50</vert><yaw>0</yaw></accelSet><brakes><wheelBrakes>00000</wheelBrakes><traction><on/></traction><abs><on/></abs><scs><on/></scs><brakeBoost><unavailable/></brakeBoost><auxBrakes><unavailable/></auxBrakes></brakes><size><width>250</width><length>590</length></size></coreData><partII><BSMpartIIExtension><partII-Id>0</partII-Id><partII-Value><VehicleSafetyExtensions><pathHistory><crumbData><PathHistoryPoint><latOffset>-795</latOffset><lonOffset>2109</lonOffset><elevationOffset>0</elevationOffset><timeOffset>62789</timeOffset></PathHistoryPoint></crumbData></pathHistory><pathPrediction><radiusOfCurve>32767</radiusOfCurve><confidence>180</confidence></pathPrediction></VehicleSafetyExtensions></partII-Value></BSMpartIIExtension><BSMpartIIExtension><partII-Id>2</partII-Id><partII-Value><SupplementalVehicleExtensions><classification>0</classification><classDetails><keyType>0</keyType><hpmsType><none/></hpmsType></classDetails><vehicleData/><weatherReport><isRaining><error/></isRaining><rainRate>65535</rainRate><precipSituation><unknown/></precipSituation><solarRadiation>65535</solarRadiation><friction>101</friction><roadFriction>0</roadFriction></weatherReport><weatherProbe><airTemp>52</airTemp><airPressure>71</airPressure><rainRates><statusFront><off/></statusFront><rateFront>0</rateFront></rainRates></weatherProbe></SupplementalVehicleExtensions></partII-Value></BSMpartIIExtension><BSMpartIIExtension><partII-Id>1</partII-Id><partII-Value><SpecialVehicleExtensions><vehicleAlerts><notUsed>0</notUsed><sirenUse><notInUse/></sirenUse><lightsUse><notInUse/></lightsUse><multi><unavailable/></multi><events><notUsed>0</notUsed><event>1000000000000000</event></events></vehicleAlerts></SpecialVehicleExtensions></partII-Value></BSMpartIIExtension></partII></BasicSafetyMessage></value></MessageFrame></data></payload></OdeAsn1Data>";
    }

}
