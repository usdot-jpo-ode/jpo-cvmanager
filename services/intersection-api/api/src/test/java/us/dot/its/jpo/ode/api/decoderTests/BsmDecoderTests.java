package us.dot.its.jpo.ode.api.decoderTests;

import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import us.dot.its.jpo.geojsonconverter.DateJsonMapper;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.Point;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.bsm.ProcessedBsm;
import us.dot.its.jpo.ode.api.asn1.BsmDecoder;
import us.dot.its.jpo.ode.model.OdeMessageFrameData;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class BsmDecoderTests {

    private final BsmDecoder bsmDecoder;

    private String rawBsmReference = "0022e12d18466c65c1493800000e00e4616183e85a8f0100c000038081bc001480b8494c4c950cd8cde6e9651116579f22a424dd78fffff00761e4fd7eb7d07f7fff80005f11d1020214c1c0ffc7c016aff4017a0ff65403b0fd204c20ffccc04f8fe40c420ffe6404cefe60e9a10133408fcfde1438103ab4138f00e1eec1048ec160103e237410445c171104e26bc103dc4154305c2c84103b1c1c8f0a82f42103f34262d1123198103dac25fb12034ce10381c259f12038ca103574251b10e3b2210324c23ad0f23d8efffe0000209340d10000004264bf00";
    private String odeBsmDecodedXmlReference = "<MessageFrame><messageId>20</messageId><value><BasicSafetyMessage><coreData><msgCnt>37</msgCnt><id>31325433</id><secMark>25399</secMark><lat>405659938</lat><long>-1050317754</long><elev>14409</elev><accuracy><semiMajor>186</semiMajor><semiMinor>241</semiMinor><orientation>65535</orientation></accuracy><transmission><unavailable/></transmission><speed>14</speed><heading>25060</heading><angle>127</angle><accelSet><long>27</long><lat>0</lat><vert>0</vert><yaw>0</yaw></accelSet><brakes><wheelBrakes>10000</wheelBrakes><traction><unavailable/></traction><abs><unavailable/></abs><scs><unavailable/></scs><brakeBoost><unavailable/></brakeBoost><auxBrakes><unavailable/></auxBrakes></brakes><size><width>190</width><length>570</length></size></coreData><partII><BSMpartIIExtension><partII-Id>0</partII-Id><partII-Value><VehicleSafetyExtensions><pathHistory><crumbData><PathHistoryPoint><latOffset>-113</latOffset><lonOffset>181</lonOffset><elevationOffset>-6</elevationOffset><timeOffset>190</timeOffset></PathHistoryPoint><PathHistoryPoint><latOffset>-310</latOffset><lonOffset>472</lonOffset><elevationOffset>-23</elevationOffset><timeOffset>610</timeOffset></PathHistoryPoint><PathHistoryPoint><latOffset>-103</latOffset><lonOffset>636</lonOffset><elevationOffset>-14</elevationOffset><timeOffset>1570</timeOffset></PathHistoryPoint><PathHistoryPoint><latOffset>-52</latOffset><lonOffset>615</lonOffset><elevationOffset>-13</elevationOffset><timeOffset>1870</timeOffset></PathHistoryPoint><PathHistoryPoint><latOffset>614</latOffset><lonOffset>1150</lonOffset><elevationOffset>-17</elevationOffset><timeOffset>2589</timeOffset></PathHistoryPoint><PathHistoryPoint><latOffset>1878</latOffset><lonOffset>2503</lonOffset><elevationOffset>7</elevationOffset><timeOffset>3959</timeOffset></PathHistoryPoint><PathHistoryPoint><latOffset>2333</latOffset><lonOffset>2816</lonOffset><elevationOffset>31</elevationOffset><timeOffset>4539</timeOffset></PathHistoryPoint><PathHistoryPoint><latOffset>2187</latOffset><lonOffset>2952</lonOffset><elevationOffset>39</elevationOffset><timeOffset>4959</timeOffset></PathHistoryPoint><PathHistoryPoint><latOffset>1976</latOffset><lonOffset>2721</lonOffset><elevationOffset>46</elevationOffset><timeOffset>5699</timeOffset></PathHistoryPoint><PathHistoryPoint><latOffset>1891</latOffset><lonOffset>3655</lonOffset><elevationOffset>84</elevationOffset><timeOffset>6050</timeOffset></PathHistoryPoint><PathHistoryPoint><latOffset>2022</latOffset><lonOffset>4886</lonOffset><elevationOffset>137</elevationOffset><timeOffset>6349</timeOffset></PathHistoryPoint><PathHistoryPoint><latOffset>1973</latOffset><lonOffset>4861</lonOffset><elevationOffset>144</elevationOffset><timeOffset>6760</timeOffset></PathHistoryPoint><PathHistoryPoint><latOffset>1795</latOffset><lonOffset>4815</lonOffset><elevationOffset>144</elevationOffset><timeOffset>7270</timeOffset></PathHistoryPoint><PathHistoryPoint><latOffset>1710</latOffset><lonOffset>4749</lonOffset><elevationOffset>135</elevationOffset><timeOffset>7570</timeOffset></PathHistoryPoint><PathHistoryPoint><latOffset>1609</latOffset><lonOffset>4566</lonOffset><elevationOffset>121</elevationOffset><timeOffset>7880</timeOffset></PathHistoryPoint></crumbData></pathHistory><pathPrediction><radiusOfCurve>32767</radiusOfCurve><confidence>0</confidence></pathPrediction></VehicleSafetyExtensions></partII-Value></BSMpartIIExtension><BSMpartIIExtension><partII-Id>2</partII-Id><partII-Value><SupplementalVehicleExtensions><classDetails><keyType>0</keyType><role><basicVehicle/></role><hpmsType><none/></hpmsType><fuelType>0</fuelType></classDetails><vehicleData><height>38</height></vehicleData><doNotUse2><airTemp>191</airTemp></doNotUse2></SupplementalVehicleExtensions></partII-Value></BSMpartIIExtension></partII></BasicSafetyMessage></value></MessageFrame>";
    private String odeBsmDecodedJsonReference = "{\"metadata\":{\"recordType\":\"bsmTx\",\"securityResultCode\":\"success\",\"payloadType\":\"us.dot.its.jpo.ode.model.OdeMessageFramePayload\",\"serialId\":{\"streamId\":\"44a6d71c-8af1-4f45-848c-10bd7f919be8\",\"bundleSize\":1,\"bundleId\":0,\"recordId\":0,\"serialNumber\":0},\"odeReceivedAt\":\"2025-08-29T16:09:34.416Z\",\"schemaVersion\":9,\"maxDurationTime\":0,\"recordGeneratedBy\":\"OBU\",\"sanitized\":false,\"source\":\"EV\",\"isCertPresent\":false},\"payload\":{\"data\":{\"messageId\":20,\"value\":{\"BasicSafetyMessage\":{\"coreData\":{\"msgCnt\":37,\"id\":\"31325433\",\"secMark\":25399,\"lat\":405659938,\"long\":-1050317754,\"elev\":14409,\"accuracy\":{\"semiMajor\":186,\"semiMinor\":241,\"orientation\":65535},\"transmission\":\"unavailable\",\"speed\":14,\"heading\":25060,\"angle\":127,\"accelSet\":{\"long\":27,\"lat\":0,\"vert\":0,\"yaw\":0},\"brakes\":{\"wheelBrakes\":\"80\",\"traction\":\"unavailable\",\"abs\":\"unavailable\",\"scs\":\"unavailable\",\"brakeBoost\":\"unavailable\",\"auxBrakes\":\"unavailable\"},\"size\":{\"width\":190,\"length\":570}},\"partII\":[{\"partII-Id\":0,\"partII-Value\":{\"VehicleSafetyExtensions\":{\"pathHistory\":{\"crumbData\":[{\"latOffset\":-113,\"lonOffset\":181,\"elevationOffset\":-6,\"timeOffset\":190},{\"latOffset\":-310,\"lonOffset\":472,\"elevationOffset\":-23,\"timeOffset\":610},{\"latOffset\":-103,\"lonOffset\":636,\"elevationOffset\":-14,\"timeOffset\":1570},{\"latOffset\":-52,\"lonOffset\":615,\"elevationOffset\":-13,\"timeOffset\":1870},{\"latOffset\":614,\"lonOffset\":1150,\"elevationOffset\":-17,\"timeOffset\":2589},{\"latOffset\":1878,\"lonOffset\":2503,\"elevationOffset\":7,\"timeOffset\":3959},{\"latOffset\":2333,\"lonOffset\":2816,\"elevationOffset\":31,\"timeOffset\":4539},{\"latOffset\":2187,\"lonOffset\":2952,\"elevationOffset\":39,\"timeOffset\":4959},{\"latOffset\":1976,\"lonOffset\":2721,\"elevationOffset\":46,\"timeOffset\":5699},{\"latOffset\":1891,\"lonOffset\":3655,\"elevationOffset\":84,\"timeOffset\":6050},{\"latOffset\":2022,\"lonOffset\":4886,\"elevationOffset\":137,\"timeOffset\":6349},{\"latOffset\":1973,\"lonOffset\":4861,\"elevationOffset\":144,\"timeOffset\":6760},{\"latOffset\":1795,\"lonOffset\":4815,\"elevationOffset\":144,\"timeOffset\":7270},{\"latOffset\":1710,\"lonOffset\":4749,\"elevationOffset\":135,\"timeOffset\":7570},{\"latOffset\":1609,\"lonOffset\":4566,\"elevationOffset\":121,\"timeOffset\":7880}]},\"pathPrediction\":{\"radiusOfCurve\":32767,\"confidence\":0}}}},{\"partII-Id\":2,\"partII-Value\":{\"SupplementalVehicleExtensions\":{\"classDetails\":{\"keyType\":0,\"role\":\"basicVehicle\",\"hpmsType\":\"none\",\"fuelType\":0},\"vehicleData\":{\"height\":38},\"doNotUse2\":{\"airTemp\":191}}}}]}}},\"dataType\":\"us.dot.its.jpo.asn.j2735.r2024.BasicSafetyMessage.BasicSafetyMessageMessageFrame\"}}";
    private String processedBsmReference = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[-105.0317754,40.5659938]},\"properties\":{\"schemaVersion\":2,\"messageType\":\"BSM\",\"odeReceivedAt\":\"2025-08-29T16:09:34.416Z\",\"timeStamp\":\"2025-08-29T16:09:25.399Z\",\"validationMessages\":[{\"message\":\"$.metadata.logFileName: is missing but it is required\",\"jsonPath\":\"$.metadata\",\"schemaPath\":\"#/properties/metadata/required\"},{\"message\":\"$.metadata.receivedMessageDetails: is missing but it is required\",\"jsonPath\":\"$.metadata\",\"schemaPath\":\"#/properties/metadata/required\"},{\"message\":\"$.metadata.recordGeneratedAt: is missing but it is required\",\"jsonPath\":\"$.metadata\",\"schemaPath\":\"#/properties/metadata/required\"},{\"message\":\"$.metadata.asn1: is missing but it is required\",\"jsonPath\":\"$.metadata\",\"schemaPath\":\"#/properties/metadata/required\"}],\"accelSet\":{\"accelLat\":0.0,\"accelLong\":0.27,\"accelVert\":0.0,\"accelYaw\":0.0},\"accuracy\":{\"semiMajor\":9.3,\"semiMinor\":12.05},\"brakes\":{\"wheelBrakes\":{\"unavailable\":true,\"leftFront\":false,\"leftRear\":false,\"rightFront\":false,\"rightRear\":false},\"traction\":\"UNAVAILABLE\",\"abs\":\"UNAVAILABLE\",\"scs\":\"UNAVAILABLE\",\"brakeBoost\":\"UNAVAILABLE\",\"auxBrakes\":\"UNAVAILABLE\"},\"heading\":313.25,\"id\":\"31325433\",\"msgCnt\":37,\"secMark\":25399,\"size\":{\"width\":190,\"length\":570},\"speed\":0.28,\"transmission\":\"UNAVAILABLE\"}}";

    @Autowired
    public BsmDecoderTests(BsmDecoder bsmDecoder) {
        this.bsmDecoder = bsmDecoder;
    }

    /**
     * Test to Decode a raw BSM into an XML String. May not work if host machine is
     * Windows and .so library is not properly linked.
     * If system is missing required libraries, the test will be skipped.
     */
    @Test
    public void testDecodeAsnToXERString() {
        try {
            String result = bsmDecoder.decodeAsnToXERString(rawBsmReference);
            assertEquals(result, odeBsmDecodedXmlReference);
        } catch (java.lang.ExceptionInInitializerError e) {
            // Ignore errors due to missing native libraries during testing.
            assumeTrue("Skipping testDecodeAsnToXERString test because system is missing required libraries", false);
        }
    }

    /**
     * Test verifying the conversion from String XML data to OdeMessageFrame
     * Object
     */
    @Test
    public void testGetAsMessageFrame() {
        try {
            OdeMessageFrameData bsm = bsmDecoder.convertXERToMessageFrame(odeBsmDecodedXmlReference);

            bsm.getMetadata().setOdeReceivedAt("2025-08-29T16:09:34.416Z");
            bsm.getMetadata()
                    .setSerialId(bsm.getMetadata().getSerialId().setStreamId("44a6d71c-8af1-4f45-848c-10bd7f919be8"));

            assertEquals(bsm.toJson(), odeBsmDecodedJsonReference);
        } catch (JsonProcessingException e) {
            assertEquals(true, false);
        }
    }

    /**
     * Test to verify Conversion from a OdeMessageFrame object to a ProcessedBSM
     * Object
     */
    @Test
    public void testConvertMessageFrameToProcessedBsm() {
        ObjectMapper objectMapper = DateJsonMapper.getInstance();

        try {
            OdeMessageFrameData bsmMessageFrame = objectMapper.readValue(odeBsmDecodedJsonReference,
                    OdeMessageFrameData.class);

            ProcessedBsm<Point> bsm = bsmDecoder.convertMessageFrameToProcessedBsm(bsmMessageFrame);

            bsm.getProperties().setOdeReceivedAt("2025-08-29T16:09:34.416Z");
            // bsm.getProperties().setSerialId("44a6d71c-8af1-4f45-848c-10bd7f919be8");

            assertEquals(bsm.toString(), processedBsmReference);
        } catch (JsonProcessingException e) {
            assertEquals(true, false);
        }
    }
}
