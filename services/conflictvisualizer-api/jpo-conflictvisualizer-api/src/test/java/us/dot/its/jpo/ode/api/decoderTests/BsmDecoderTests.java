package us.dot.its.jpo.ode.api.decoderTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import us.dot.its.jpo.ode.api.asn1.BsmDecoder;
import us.dot.its.jpo.ode.api.models.messages.BsmDecodedMessage;
import us.dot.its.jpo.ode.mockdata.MockDecodedMessageGenerator;
import us.dot.its.jpo.ode.model.OdeBsmData;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeMsgMetadata;

import us.dot.its.jpo.ode.util.XmlUtils.XmlUtilsException;


@SpringBootTest
@RunWith(SpringRunner.class)
public class BsmDecoderTests {
    

    @Autowired
    BsmDecoder bsmDecoder;

    private String odeBsmDataReference = "{\"metadata\":{\"bsmSource\":\"RV\",\"recordType\":\"bsmTx\",\"securityResultCode\":\"success\",\"receivedMessageDetails\":{\"locationData\":{},\"rxSource\":\"RV\"},\"encodings\":[{\"elementName\":\"unsecuredData\",\"elementType\":\"MessageFrame\",\"encodingRule\":\"UPER\"}],\"payloadType\":\"us.dot.its.jpo.ode.model.OdeAsn1Payload\",\"serialId\":{\"streamId\":\"fc430f29-b761-4a2c-90fb-dc4c9f5d4e9c\",\"bundleSize\":1,\"bundleId\":0,\"recordId\":0,\"serialNumber\":0},\"odeReceivedAt\":\"2024-05-14T23:01:21.516531700Z\",\"schemaVersion\":6,\"maxDurationTime\":0,\"sanitized\":false},\"payload\":{\"dataType\":\"us.dot.its.jpo.ode.model.OdeHexByteArray\",\"data\":{\"bytes\":\"00145144ad0b7947c2ed9ad2748035a4e8ff880000000fd2229199307d7d07d0b17fff05407d12720038c000fe72c107b001ea88fffeb4002127c0009000000fdfffe3ffff9407344704000041910120100000000efc10609c26e900e11f61a947802127c0009000000fdfffe3ffff9407453304000041910120100000008ffffe501ca508100000000000a508100000404804000000849f00024000003f7fff8ffffe501ca508100000fe501ca508100000fffe501ca51c10000000024000003f7fff8ffffe501ca51c1\"}}}";
    private String odeBsmDecodedXmlReference = "<?xml version=\\\"1.0\\\"?><OdeAsn1Data><metadata><bsmSource>RV</bsmSource><logFileName/><recordType>bsmTx</recordType><securityResultCode>success</securityResultCode><receivedMessageDetails><locationData><latitude/><longitude/><elevation/><speed/><heading/></locationData><rxSource>RV</rxSource></receivedMessageDetails><encodings><encodings><elementName>unsecuredData</elementName><elementType>MessageFrame</elementType><encodingRule>UPER</encodingRule></encodings></encodings><payloadType>us.dot.its.jpo.ode.model.OdeAsn1Payload</payloadType><serialId><streamId>8829c539-e684-40b7-a786-692acd3f897a</streamId><bundleSize>1</bundleSize><bundleId>0</bundleId><recordId>0</recordId><serialNumber>0</serialNumber></serialId><odeReceivedAt>2024-05-08T20:47:53.830130272Z</odeReceivedAt><schemaVersion>6</schemaVersion><maxDurationTime>0</maxDurationTime><recordGeneratedAt/><recordGeneratedBy/><sanitized>false</sanitized><odePacketID/><odeTimStartDateTime/><originIp/></metadata><payload><dataType>MessageFrame</dataType><data><MessageFrame><messageId>20</messageId><value><BasicSafetyMessage><coreData><msgCnt>18</msgCnt><id>B42DE51F</id><secMark>2998</secMark><lat>0</lat><long>0</long><elev>0</elev><accuracy><semiMajor>0</semiMajor><semiMinor>0</semiMinor><orientation>8100</orientation></accuracy><transmission><forwardGears/></transmission><speed>1315</speed><heading>6448</heading><angle>-1</angle><accelSet><long>0</long><lat>0</lat><vert>50</vert><yaw>0</yaw></accelSet><brakes><wheelBrakes>00000</wheelBrakes><traction><on/></traction><abs><on/></abs><scs><on/></scs><brakeBoost><unavailable/></brakeBoost><auxBrakes><unavailable/></auxBrakes></brakes><size><width>250</width><length>590</length></size></coreData><partII><BSMpartIIExtension><partII-Id>0</partII-Id><partII-Value><VehicleSafetyExtensions><pathHistory><crumbData><PathHistoryPoint><latOffset>-795</latOffset><lonOffset>2109</lonOffset><elevationOffset>0</elevationOffset><timeOffset>62789</timeOffset></PathHistoryPoint></crumbData></pathHistory><pathPrediction><radiusOfCurve>32767</radiusOfCurve><confidence>180</confidence></pathPrediction></VehicleSafetyExtensions></partII-Value></BSMpartIIExtension><BSMpartIIExtension><partII-Id>2</partII-Id><partII-Value><SupplementalVehicleExtensions><classification>0</classification><classDetails><keyType>0</keyType><hpmsType><none/></hpmsType></classDetails><vehicleData/><weatherReport><isRaining><error/></isRaining><rainRate>65535</rainRate><precipSituation><unknown/></precipSituation><solarRadiation>65535</solarRadiation><friction>101</friction><roadFriction>0</roadFriction></weatherReport><weatherProbe><airTemp>52</airTemp><airPressure>71</airPressure><rainRates><statusFront><off/></statusFront><rateFront>0</rateFront></rainRates></weatherProbe></SupplementalVehicleExtensions></partII-Value></BSMpartIIExtension><BSMpartIIExtension><partII-Id>1</partII-Id><partII-Value><SpecialVehicleExtensions><vehicleAlerts><notUsed>0</notUsed><sirenUse><notInUse/></sirenUse><lightsUse><notInUse/></lightsUse><multi><unavailable/></multi><events><notUsed>0</notUsed><event>1000000000000000</event></events></vehicleAlerts></SpecialVehicleExtensions></partII-Value></BSMpartIIExtension></partII></BasicSafetyMessage></value></MessageFrame></data></payload></OdeAsn1Data>";
    private String odeBsmDecodedDataReference = "{\"metadata\":{\"bsmSource\":\"RV\",\"logFileName\":\"\",\"recordType\":\"bsmTx\",\"securityResultCode\":\"success\",\"receivedMessageDetails\":{\"locationData\":{\"latitude\":\"\",\"longitude\":\"\",\"elevation\":\"\",\"speed\":\"\",\"heading\":\"\"},\"rxSource\":\"RV\"},\"payloadType\":\"us.dot.its.jpo.ode.model.OdeBsmPayload\",\"serialId\":{\"streamId\":\"8829c539-e684-40b7-a786-692acd3f897a\",\"bundleSize\":1,\"bundleId\":0,\"recordId\":0,\"serialNumber\":0},\"odeReceivedAt\":\"2024-05-08T20:47:53.830130272Z\",\"schemaVersion\":6,\"maxDurationTime\":0,\"recordGeneratedAt\":\"\",\"sanitized\":false,\"odePacketID\":\"\",\"odeTimStartDateTime\":\"\",\"originIp\":\"\"},\"payload\":{\"data\":{\"coreData\":{\"msgCnt\":18,\"id\":\"B42DE51F\",\"secMark\":2998,\"position\":{\"latitude\":0E-7,\"longitude\":0E-7,\"elevation\":0.0},\"accelSet\":{\"accelLat\":0.00,\"accelLong\":0.00,\"accelVert\":1.00,\"accelYaw\":0.00},\"accuracy\":{\"semiMajor\":0.00,\"semiMinor\":0.00,\"orientation\":44.4953079900},\"transmission\":\"FORWARDGEARS\",\"speed\":26.30,\"heading\":80.6000,\"angle\":-1.5,\"brakes\":{\"wheelBrakes\":{\"leftFront\":false,\"rightFront\":false,\"unavailable\":true,\"leftRear\":false,\"rightRear\":false},\"traction\":\"on\",\"abs\":\"on\",\"scs\":\"on\",\"brakeBoost\":\"unavailable\",\"auxBrakes\":\"unavailable\"},\"size\":{\"width\":250,\"length\":590}},\"partII\":[]},\"dataType\":\"us.dot.its.jpo.ode.plugin.j2735.J2735Bsm\"}}";


    @Test
    public void testBsmGetAsOdeData() {

        BsmDecodedMessage bsm = MockDecodedMessageGenerator.getBsmDecodedMessage();
        OdeData data = bsmDecoder.getAsOdeData(bsm.getAsn1Text());

        OdeMsgMetadata metadata = data.getMetadata();


        // Copy over fields that might be different
        metadata.setOdeReceivedAt("2024-05-14T23:01:21.516531700Z");
        metadata.setSerialId(metadata.getSerialId().setStreamId("fc430f29-b761-4a2c-90fb-dc4c9f5d4e9c"));

        assertEquals(data.toJson(), odeBsmDataReference);
    
    }

    @Test
    public void testBsmGetAsOdeJson() throws XmlUtilsException{
        OdeBsmData bsm = bsmDecoder.getAsOdeJson(odeBsmDecodedXmlReference);
        assertEquals(bsm.toJson(), odeBsmDecodedDataReference);
    }

}
