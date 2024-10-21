package us.dot.its.jpo.ode.api.asn1;

import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.ode.api.models.MessageType;
import us.dot.its.jpo.ode.api.models.messages.DecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.EncodedMessage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


@Component
@Slf4j
public class DecoderManager {

    public static final MessageType[] types = {MessageType.BSM, MessageType.MAP, MessageType.SPAT, MessageType.SRM, MessageType.SSM, MessageType.TIM};
    public static final String[] startFlags = {"0014", "0012", "0013", "001d", "001e", "001f"}; //BSM, MAP, SPAT, SRM, SSM, TIM
    public static final int[] maxSizes = {500, 2048, 1000, 500, 500, 500};
    public static final int HEADER_MINIMUM_SIZE = 20;
    public static final int bufferSize = 2048;

    @Autowired 
    public BsmDecoder bsmDecoder;

    @Autowired 
    public MapDecoder mapDecoder;

    @Autowired 
    public SpatDecoder spatDecoder;

    @Autowired 
    public SrmDecoder srmDecoder;

    @Autowired 
    public SsmDecoder ssmDecoder;

    @Autowired 
    public TimDecoder timDecoder;

    // public static DecodedMessage decode(String inputAsn1){

    //     // Identify Message Type and Cut off any extra characters
    //     TypePayload payload = identifyAsn1(inputAsn1);

        
    //     // Convert Payload to Pojo and add Metadata
    //     OdeData data = getAsOdeData(payload);

    //     XmlUtils xmlUtils = new XmlUtils();

    //     try {
    //         // Convert to XML for ASN.1 Decoder
    //         String xml = xmlUtils.toXml(data);
    //         System.out.println("XML" + xml);

    //         // Send String through ASN.1 Decoder to get Decoded XML Data
    //         // String decodedXml = decodeXmlWithAcm(xml);
    //         String decodedXml = mockDecodeXmlWithAcm(xml);

    //         if(payload.getType().equals(MessageType.BSM)){
    //             OdeBsmData bsm = createOdeBsmData(decodedXml);
    //             DecodedMessage message = new BsmDecodedMessage(bsm, inputAsn1, MessageType.BSM, "");
    //             return message;
    //         }

    //     } catch (JsonProcessingException e) {
    //         e.printStackTrace();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }

        

    //     return null;
    // }

    public DecodedMessage decode(EncodedMessage message){
        String payload = removeHeader(message.getAsn1Message(), message.getType());
        message.setAsn1Message(payload);

        Decoder decoder = null;

        if(payload != null){
            if(message.getType() == MessageType.BSM){
                decoder = new BsmDecoder();
            }
            else if(message.getType() == MessageType.MAP){
                decoder = mapDecoder;
            }else if(message.getType() == MessageType.SPAT){
                decoder = spatDecoder;
            }else if(message.getType() == MessageType.SRM){
                decoder = srmDecoder;
            }else if(message.getType() == MessageType.SSM){
                decoder = ssmDecoder;
            }else if(message.getType() == MessageType.TIM){
                decoder = timDecoder;
                // return new DecodedMessage(payload, message.getType(), "Ode Does not support ODE Serialization / Deserialization");
            }else{
                return new DecodedMessage(payload, message.getType(), "No Valid Decoder found for Message Type");
            }

            
            return decoder.decode(message);
            
            
        }
        
        return new DecodedMessage(payload, message.getType(), "Unable to find valid message start flag within input data");

    }

    public static String getOdeReceivedAt(){
        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
        String timestamp = utc.format(DateTimeFormatter.ISO_INSTANT);
        return timestamp;
    }

    public static String getOriginIp(){
        return "user-upload";
    }

    public static String removeHeader(String hexPacket, MessageType type) {
  
        String startFlag = startFlags[ArrayUtils.indexOf(types, type)];

        int startIndex = hexPacket.indexOf(startFlag);
        if (startIndex == 0) {
           // Raw Message no Headers
        } else if (startIndex == -1) {

           return null;
        } else {
           // We likely found a message with a header, look past the first 20
           // bytes for the start of the BSM
           int trueStartIndex = HEADER_MINIMUM_SIZE
                 + hexPacket.substring(HEADER_MINIMUM_SIZE, hexPacket.length()).indexOf(startFlag);
           hexPacket = hexPacket.substring(trueStartIndex, hexPacket.length());
        }
  
        return hexPacket;
     }


    public static EncodedMessage identifyAsn1(String hexPacket){
        // Compute the Effective End Location of the real data.
        //int endIndex = hexPacket.indexOf("0000000000000000");
        //if(endIndex == -1){
        int endIndex = hexPacket.length()-1;
        //}

        int closestStartIndex = endIndex;
        MessageType closestMessageType = MessageType.UNKNOWN;
        // int closestBufferSize = bufferSize;


        for(int i = 0; i< startFlags.length; i++){

            String startFlag = startFlags[i];
            MessageType mType = types[i];
            int typeBufferSize = maxSizes[i];
            
            
            // Skip possible message type if packet is too big
            if(endIndex > typeBufferSize*2){
                continue;
            }

	    
            int startIndex = hexPacket.indexOf(startFlag);
	    
	    if (startIndex == 0) {
                return new EncodedMessage(hexPacket, mType);
            }else if (startIndex == -1) {
                continue;
            } else{
	    	int trueStartIndex = hexPacket.substring(HEADER_MINIMUM_SIZE, hexPacket.length()).indexOf(startFlag);
		if(trueStartIndex ==-1){
			continue;
		}
		trueStartIndex += HEADER_MINIMUM_SIZE;

		while (trueStartIndex != -1 && (trueStartIndex % 2 == 1) && trueStartIndex < hexPacket.length()-4){
		    int newStartIndex = hexPacket.substring(trueStartIndex+1, hexPacket.length()).indexOf(startFlag);
		    if(newStartIndex == -1){
			    trueStartIndex = -1;
			    break;
		    }else{
			trueStartIndex += newStartIndex+1;
		    }  
		}
 
		if(trueStartIndex != -1 && trueStartIndex < closestStartIndex){
                    closestStartIndex = trueStartIndex;
                    closestMessageType = mType;
                    // closestBufferSize = typeBufferSize;
                }
            }
        }

        if(closestMessageType == MessageType.UNKNOWN){
            return new EncodedMessage(hexPacket, MessageType.UNKNOWN);
        }else{
            return new EncodedMessage(hexPacket.substring(closestStartIndex, hexPacket.length()), closestMessageType);
        }   
    }


    


    public static String decodeXmlWithAcm(String xmlMessage) throws Exception {


        System.out.println("Decoding Message: " + xmlMessage);
        log.info("Decoding message: {}", xmlMessage);

        // Save XML to temp file
        String tempDir = FileUtils.getTempDirectoryPath();
        String tempFileName = "asn1-codec-java-" + UUID.randomUUID().toString() + ".xml";
        log.info("Temp file name: {}", tempFileName);
        System.out.println("Temp File Name: " + tempFileName);
        Path tempFilePath = Path.of(tempDir, tempFileName);
        File tempFile = new File(tempFilePath.toString());
        FileUtils.writeStringToFile(tempFile, xmlMessage, StandardCharsets.UTF_8);

        // Run ACM tool to decode message
        var pb = new ProcessBuilder(
                "/build/acm", "-F", "-c", "/build/config/example.properties", "-T", "decode", tempFile.getAbsolutePath());
        pb.directory(new File("/build"));
        Process process = pb.start();
        String result = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
        log.info("Result: {}", result);
        System.out.println("Decode Result: " + result);

        // Clean up temp file
        tempFile.delete();

        return result;
    }
}