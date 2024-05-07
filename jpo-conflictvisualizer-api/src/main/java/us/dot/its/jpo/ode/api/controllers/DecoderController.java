package us.dot.its.jpo.ode.api.controllers;
 
import java.time.ZonedDateTime;

 
// import jakarta.ws.rs.core.Response;
 
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
 
import us.dot.its.jpo.ode.api.models.UploadData;
import us.dot.its.jpo.ode.mockdata.MockUploadDataGenerator;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;

// import us.dot.its.jpo.ode.coder.StringPublisher;
 
@RestController
public class DecoderController {
 
    private static final Logger logger = LoggerFactory.getLogger(AssessmentController.class);
 
    ObjectMapper objectMapper = new ObjectMapper();
 
    @Autowired
    ConflictMonitorApiProperties props;


    // private StringPublisher bsmPublisher;
 
    public String getCurrentTime() {
        return ZonedDateTime.now().toInstant().toEpochMilli() + "";
    }
 
    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/decoder/upload", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody ResponseEntity<String> new_bulk_upload_request(
            @RequestBody UploadData newUploadData,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        try {
            logger.info("Uploading Bulk Data");

            if (testData) {
               newUploadData = MockUploadDataGenerator.getUploadData();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).contentType(MediaType.TEXT_PLAIN)
                    .body(newUploadData.toString());
            }
 
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                    .body(newUploadData.toString());
        } catch (Exception e) {
            logger.info("Failed to Upload Bulk Data");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN)
                    .body(ExceptionUtils.getStackTrace(e));
        }
    }

    // public void publishBSM(String asn1Bsm){
    //     String payloadHexString = HexUtils.toHexString(payload);
    //     logger.debug("Packet: {}", payloadHexString);
        
    //     // Add header data for the decoding process
    //     ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
    //     String timestamp = utc.format(DateTimeFormatter.ISO_INSTANT);

    //     JSONObject metadataObject = new JSONObject();
    //     metadataObject.put("utctimestamp", timestamp);
    //     metadataObject.put("originRsu", senderIp);

    //     JSONObject messageObject = new JSONObject();
    //     messageObject.put("metadata", metadataObject);
    //     messageObject.put("payload", payloadHexString);

    //     JSONArray messageList = new JSONArray();
    //     messageList.put(messageObject);

    //     JSONObject jsonObject = new JSONObject();
    //     jsonObject.put("BsmMessageContent", messageList);

    //     logger.debug("BSM JSON Object: {}", jsonObject.toString());

    //     // Submit JSON to the OdeRawEncodedMessageJson Kafka Topic
    //     this.bsmPublisher.publish(jsonObject.toString(), this.bsmPublisher.getOdeProperties().getKafkaTopicOdeRawEncodedBSMJson());

    // }
}