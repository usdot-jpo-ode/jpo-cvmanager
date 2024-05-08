package us.dot.its.jpo.ode.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import us.dot.its.jpo.ode.api.asn1.Decoder;
import us.dot.its.jpo.ode.api.models.messages.BsmDecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.DecodedMessage;
import us.dot.its.jpo.ode.model.OdeBsmData;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableWebMvc
@SpringBootApplication
@EnableScheduling
public class ConflictApiApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ConflictApiApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ConflictApiApplication.class, args);
        System.out.println("Started Conflict Monitor API");
        System.out.println("Conflict Monitor API docs page found here: http://localhost:8081/swagger-ui/index.html");
        System.out.println("Startup Complete");

        BsmDecodedMessage bsm = (BsmDecodedMessage)Decoder.decode("00145144ad0b7947c2ed9ad2748035a4e8ff880000000fd2229199307d7d07d0b17fff05407d12720038c000fe72c107b001ea88fffeb4002127c0009000000fdfffe3ffff9407344704000041910120100000000efc10609c26e900e11f61a947802127c0009000000fdfffe3ffff9407453304000041910120100000008ffffe501ca508100000000000a508100000404804000000849f00024000003f7fff8ffffe501ca508100000fe501ca508100000fffe501ca51c10000000024000003f7fff8ffffe501ca51c1");

        

        System.out.println("Fully Decoded BSM" + bsm.getBsm());
        // OdeBsmData decodeTest;
        // try {
        //     decodeTest = OdeBsmDataCreatorHelper.createOdeBsmData("<?xml version=\"1.0\"?><OdeAsn1Data><metadata><bsmSource>RV</bsmSource><logFileName/><recordType>bsmTx</recordType><securityResultCode>success</securityResultCode><receivedMessageDetails><locationData><latitude/><longitude/><elevation/><speed/><heading/></locationData><rxSource>RV</rxSource></receivedMessageDetails><encodings><encodings><elementName>unsecuredData</elementName><elementType>MessageFrame</elementType><encodingRule>UPER</encodingRule></encodings></encodings><payloadType>us.dot.its.jpo.ode.model.OdeAsn1Payload</payloadType><serialId><streamId>f48cd9fa-cf32-4542-9cfd-4ff0542d56b0</streamId><bundleSize>1</bundleSize><bundleId>0</bundleId><recordId>0</recordId><serialNumber>0</serialNumber></serialId><odeReceivedAt>2024-05-07T18:45:29.796137Z</odeReceivedAt><schemaVersion>6</schemaVersion><maxDurationTime>0</maxDurationTime><recordGeneratedAt/><recordGeneratedBy/><sanitized>false</sanitized><odePacketID/><odeTimStartDateTime/><originIp>10.165.5.230</originIp></metadata><payload><dataType>MessageFrame</dataType><data><MessageFrame><messageId>20</messageId><value><BasicSafetyMessage><coreData><msgCnt>105</msgCnt><id>21234841</id><secMark>29609</secMark><lat>403192909</lat><long>-1117049264</long><elev>14321</elev><accuracy><semiMajor>40</semiMajor><semiMinor>40</semiMinor><orientation>8100</orientation></accuracy><transmission><forwardGears/></transmission><speed>542</speed><heading>13208</heading><angle>127</angle><accelSet><long>2001</long><lat>2001</lat><vert>-127</vert><yaw>0</yaw></accelSet><brakes><wheelBrakes>01111</wheelBrakes><traction><unavailable/></traction><abs><unavailable/></abs><scs><unavailable/></scs><brakeBoost><unavailable/></brakeBoost><auxBrakes><unavailable/></auxBrakes></brakes><size><width>320</width><length>1280</length></size></coreData><partII><PartIIcontent><partII-Id>0</partII-Id><partII-Value><VehicleSafetyExtensions><pathHistory><crumbData><PathHistoryPoint><latOffset>-14434</latOffset><lonOffset>6167</lonOffset><elevationOffset>8</elevationOffset><timeOffset>1268</timeOffset></PathHistoryPoint><PathHistoryPoint><latOffset>-32277</latOffset><lonOffset>13981</lonOffset><elevationOffset>12</elevationOffset><timeOffset>2522</timeOffset></PathHistoryPoint></crumbData></pathHistory><pathPrediction><radiusOfCurve>4079</radiusOfCurve><confidence>120</confidence></pathPrediction></VehicleSafetyExtensions></partII-Value></PartIIcontent><PartIIcontent><partII-Id>2</partII-Id><partII-Value><SupplementalVehicleExtensions><classification>54</classification><classDetails><keyType>54</keyType><hpmsType><none/></hpmsType></classDetails><vehicleData><height>58</height><mass>109</mass></vehicleData><weatherReport><isRaining><error/></isRaining><rainRate>65535</rainRate><precipSituation><unknown/></precipSituation><solarRadiation>65535</solarRadiation><friction>101</friction><roadFriction>0</roadFriction></weatherReport><weatherProbe><airTemp>51</airTemp></weatherProbe></SupplementalVehicleExtensions></partII-Value></PartIIcontent></partII></BasicSafetyMessage></value></MessageFrame></data></payload></OdeAsn1Data>");
        //     System.out.println(decodeTest);
        // } catch (XmlUtilsException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }   
    }

//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                ConflictMonitorApiProperties props = new ConflictMonitorApiProperties();
//                registry.addMapping("/**").allowedOrigins(props.getCors());
//                // registry.addMapping("/**").allowedMethods("*");
//            }
//        };
//    }

    
    
}
