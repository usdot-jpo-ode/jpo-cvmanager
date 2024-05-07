package us.dot.its.jpo.ode.api.asn1;

import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.ode.api.models.Message;
import us.dot.its.jpo.ode.api.models.MessageType;
import us.dot.its.jpo.ode.api.models.TypePayload;
import us.dot.its.jpo.ode.coder.OdeBsmDataCreatorHelper;
import us.dot.its.jpo.ode.model.Asn1Encoding;
import us.dot.its.jpo.ode.model.Asn1Encoding.EncodingRule;
import us.dot.its.jpo.ode.model.OdeAsn1Data;
import us.dot.its.jpo.ode.model.OdeAsn1Payload;
import us.dot.its.jpo.ode.model.OdeBsmData;
import us.dot.its.jpo.ode.model.OdeBsmMetadata;
import us.dot.its.jpo.ode.model.OdeBsmMetadata.BsmSource;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeHexByteArray;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeLogMsgMetadataLocation;
import us.dot.its.jpo.ode.model.OdeMsgPayload;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.model.RxSource;
import us.dot.its.jpo.ode.util.XmlUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

// import static us.dot.its.jpo.ode.api.asn1.extractMessageFrame;



@Component
@Slf4j
public class Decoder {

    public static final MessageType[] types = {MessageType.BSM, MessageType.MAP, MessageType.SPAT, MessageType.SRM, MessageType.SSM, MessageType.TIM};
    public static final String[] startFlags = {"0014", "0012", "0013", "001d", "001e", "001f"}; //BSM, MAP, SPAT, SRM, SSM, TIM
    public static final int[] maxSizes = {500, 2048, 1000, 500, 500, 500};
    public static final int HEADER_MINIMUM_SIZE = 20;
    public static final int bufferSize = 2048;

    public static Message decode(String inputAsn1){

        // Identify Message Type and Cut off any extra characters
        TypePayload payload = identifyAsn1(inputAsn1);

        
        // Convert Payload to Pojo and add Metadata
        OdeData data = getAsOdeData(payload);

        

        XmlUtils xmlUtils = new XmlUtils();
        try {
            // Convert to XML for ASN.1 Decoder
            String xml = xmlUtils.toXml(data);
            System.out.println("XML" + xml);

            // Send String through ASN.1 Decoder to get Decoded XML Data
            String decodedXml = decodeXmlWithAcm(xml);

            if(payload.getType() == MessageType.BSM){
                OdeBsmData bsm = OdeBsmDataCreatorHelper.createOdeBsmData(decodedXml);
                Message<OdeBsmData> message = new Message();
                message.setAsn1Text(inputAsn1);
                message.setDecodedMessage(bsm);
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        

        return null;
    }

    public static Message decodeAsType(String inputAsn1, MessageType type){
        String payload = removeHeader(inputAsn1, type);

        if(payload != null){

        }

        return null;

    }

    public static String getOdeReceivedAt(){
        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
        String timestamp = utc.format(DateTimeFormatter.ISO_INSTANT);
        return timestamp;
    }

    public static OdeData getAsOdeData(TypePayload payload){

        if(payload.getType() == MessageType.BSM){
            return getBsmAsOdeData(payload.getPayload());
        }

        return null;

    }

    public static OdeData getBsmAsOdeData(String encodedPayload){
        OdeMsgPayload payload = new OdeAsn1Payload(new OdeHexByteArray(encodedPayload));

        // construct metadata
        OdeBsmMetadata metadata = new OdeBsmMetadata(payload);
        metadata.setOdeReceivedAt(getOdeReceivedAt());
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

    

    



    public static String removeHeader(String hexPacket, MessageType type) {
  
        // logger.debug("BSM packet length: {}, start index: {}",
        // hexPacket.length(), startIndex);
  
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


    public static TypePayload identifyAsn1(String hexPacket){


        // Compute the Effective End Location of the real data.
        //int endIndex = hexPacket.indexOf("0000000000000000");
        //if(endIndex == -1){
        int endIndex = hexPacket.length()-1;
        //}

        int closestStartIndex = endIndex;
        MessageType closestMessageType = MessageType.UNKNOWN;
        int closestBufferSize = bufferSize;


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
                return new TypePayload(mType, hexPacket);
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
                    closestBufferSize = typeBufferSize;
                }
            }
        }

        if(closestMessageType == MessageType.UNKNOWN){
            return new TypePayload(MessageType.UNKNOWN, hexPacket);
        }else{
            return new TypePayload(closestMessageType, hexPacket.substring(closestStartIndex, hexPacket.length()));
        }   
    }



    public static String decodeXmlWithAcm(String xmlMessage) throws Exception {

        log.info("Decoding message: {}", xmlMessage);

        // Save XML to temp file
        String tempDir = FileUtils.getTempDirectoryPath();
        String tempFileName = "asn1-codec-java-" + UUID.randomUUID().toString() + ".xml";
        log.info("Temp file name: {}", tempFileName);
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

        // Clean up temp file
        tempFile.delete();

        return result;

        // // Remove wrapping from result to just return the XER
        // try {
        //     String messageFrame = extractMessageFrame(result);
        //     log.info("Message frame: {}", messageFrame);
        //     return messageFrame;
        // } catch (Exception e) {
        //     log.error("Error extracting message frame: {}, returning result which is probably an error message", e);
        //     return result;
        // }
    }
}