package us.dot.its.jpo.ode.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import us.dot.its.jpo.ode.api.asn1.DecoderManager;
import us.dot.its.jpo.ode.api.models.MessageType;
import us.dot.its.jpo.ode.api.models.messages.BsmDecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.DecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.EncodedMessage;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableWebMvc
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"us.dot.its.jpo.ode.api", "us.dot.its.jpo.geojsonconverter.validator"})
public class ConflictApiApplication extends SpringBootServletInitializer {

    @Autowired DecoderManager manager;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ConflictApiApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ConflictApiApplication.class, args);
        System.out.println("Started Conflict Monitor API");
        System.out.println("Conflict Monitor API docs page found here: http://localhost:8081/swagger-ui/index.html");
        System.out.println("Startup Complete");

        // DecodedMessage bsm = manager.decode(new EncodedMessage("00145144ad0b7947c2ed9ad2748035a4e8ff880000000fd2229199307d7d07d0b17fff05407d12720038c000fe72c107b001ea88fffeb4002127c0009000000fdfffe3ffff9407344704000041910120100000000efc10609c26e900e11f61a947802127c0009000000fdfffe3ffff9407453304000041910120100000008ffffe501ca508100000000000a508100000404804000000849f00024000003f7fff8ffffe501ca508100000fe501ca508100000fffe501ca51c10000000024000003f7fff8ffffe501ca51c1", MessageType.BSM));

        // System.out.println(bsm);

        
        

        // System.out.println("Fully Decoded BSM" + bsm.getBsm());
        // OdeBsmData decodeTest;
        // try {
        //     decodeTest = OdeBsmDataCreatorHelper.createOdeBsmData("<?xml version=\"1.0\"?><OdeAsn1Data><metadata><bsmSource>RV</bsmSource><logFileName/><recordType>bsmTx</recordType><securityResultCode>success</securityResultCode><receivedMessageDetails><locationData><latitude/><longitude/><elevation/><speed/><heading/></locationData><rxSource>RV</rxSource></receivedMessageDetails><encodings><encodings><elementName>unsecuredData</elementName><elementType>MessageFrame</elementType><encodingRule>UPER</encodingRule></encodings></encodings><payloadType>us.dot.its.jpo.ode.model.OdeAsn1Payload</payloadType><serialId><streamId>f48cd9fa-cf32-4542-9cfd-4ff0542d56b0</streamId><bundleSize>1</bundleSize><bundleId>0</bundleId><recordId>0</recordId><serialNumber>0</serialNumber></serialId><odeReceivedAt>2024-05-07T18:45:29.796137Z</odeReceivedAt><schemaVersion>6</schemaVersion><maxDurationTime>0</maxDurationTime><recordGeneratedAt/><recordGeneratedBy/><sanitized>false</sanitized><odePacketID/><odeTimStartDateTime/><originIp>10.165.5.230</originIp></metadata><payload><dataType>MessageFrame</dataType><data><MessageFrame><messageId>20</messageId><value><BasicSafetyMessage><coreData><msgCnt>105</msgCnt><id>21234841</id><secMark>29609</secMark><lat>403192909</lat><long>-1117049264</long><elev>14321</elev><accuracy><semiMajor>40</semiMajor><semiMinor>40</semiMinor><orientation>8100</orientation></accuracy><transmission><forwardGears/></transmission><speed>542</speed><heading>13208</heading><angle>127</angle><accelSet><long>2001</long><lat>2001</lat><vert>-127</vert><yaw>0</yaw></accelSet><brakes><wheelBrakes>01111</wheelBrakes><traction><unavailable/></traction><abs><unavailable/></abs><scs><unavailable/></scs><brakeBoost><unavailable/></brakeBoost><auxBrakes><unavailable/></auxBrakes></brakes><size><width>320</width><length>1280</length></size></coreData><partII><PartIIcontent><partII-Id>0</partII-Id><partII-Value><VehicleSafetyExtensions><pathHistory><crumbData><PathHistoryPoint><latOffset>-14434</latOffset><lonOffset>6167</lonOffset><elevationOffset>8</elevationOffset><timeOffset>1268</timeOffset></PathHistoryPoint><PathHistoryPoint><latOffset>-32277</latOffset><lonOffset>13981</lonOffset><elevationOffset>12</elevationOffset><timeOffset>2522</timeOffset></PathHistoryPoint></crumbData></pathHistory><pathPrediction><radiusOfCurve>4079</radiusOfCurve><confidence>120</confidence></pathPrediction></VehicleSafetyExtensions></partII-Value></PartIIcontent><PartIIcontent><partII-Id>2</partII-Id><partII-Value><SupplementalVehicleExtensions><classification>54</classification><classDetails><keyType>54</keyType><hpmsType><none/></hpmsType></classDetails><vehicleData><height>58</height><mass>109</mass></vehicleData><weatherReport><isRaining><error/></isRaining><rainRate>65535</rainRate><precipSituation><unknown/></precipSituation><solarRadiation>65535</solarRadiation><friction>101</friction><roadFriction>0</roadFriction></weatherReport><weatherProbe><airTemp>51</airTemp></weatherProbe></SupplementalVehicleExtensions></partII-Value></PartIIcontent></partII></BasicSafetyMessage></value></MessageFrame></data></payload></OdeAsn1Data>");
        //     System.out.println(decodeTest);
        // } catch (XmlUtilsException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }   
    }

    @Bean
    public void test(){
        System.out.println("Running Decoder");
        DecodedMessage map = manager.decode(new EncodedMessage("0012839338023000205e96094d40df4c2ca626c8516e02dc3c2010640000000289e01c009f603f42e88039900000000a41107b027d80fd0a4200c6400000002973021c09f603de0c16029200000080002a8a008d027d98fee805404fb0e1085f60588200028096021200000080002aa0007d027d98fe9802e04fb1200c214456228000a02b1240005022c03240000020000d56b40bc04fb35ff655e2c09f623fb81c835fec0db240a0a2bff4aebf82c660000804b0089000000800025670034013ecd7fb9578e027d9aff883c4e050515ffa567a41635000040258024800000400012b8f81f409f663fac094013ecd7fc83ddb02829affa480bc04fb02c6e0000804b09c5000000200035ea98a9604f60da6c7c113d505c35ffe941d409f65c05034c050500c9880004409bc800000006d2bd3cec813c40cde062c1fd400000200008791ea3db3cf380a009f666f05005813d80ffe0a0588c00040092106a00000000bc75cac009f66db54c04a813d80a100801241ed40000000078ebae3b6da7a008809e2050904008811f100000000bc72389009f60eca8002049c400000002f1b2ca3027d93a71fa813ec204bc400000002f1b2b34027b0397608880cd10000000039b8e1a51036820505080d51000000003a7461ed1036760505080dd1000000003b2f62311006260505160bca00000080002b785e2a80a0a6c028de728145037f1f9e456488000202b2540001022c1894000001000057058c5b81414d806dbcd4028a18f4df23a050502c8d0000404b05a5000000800035b6471bc05053602431f380a2864087bdb0141458064ab0d6c00053fc013ec0b0680006012c15940000020000d6c06c6581414d807fb972028a1901d78dc050536020ec1800a0a6c039d639813d80b0780006012c1494000002000096ab8c6581414d8062be32028a1b01417e04050a360172d77009e2058440003009409c200000040006b3486a480a0a1cab7134c8117dcc02879b018fae2c050f3601ced54809e21012720000000067fbad0007e7e84045c80000000100661580958004041c8000000019f3658401cdfa2c0d64000002000144016c02c36ddfff0282984acc1ee05052c36f0ac02828669d82da8f821480a0a10f140002c8e0001004b03190000008000519fd190c43b2e0066108b08401428c342a0ce02828258a0604a6be959aee0e6050502c920001004b02d90000008000459fa164404fb30a8580a00a14619c306701414c32ce10e02829659081f814141029030164b000080200",MessageType.MAP));
        System.out.println(map);

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
