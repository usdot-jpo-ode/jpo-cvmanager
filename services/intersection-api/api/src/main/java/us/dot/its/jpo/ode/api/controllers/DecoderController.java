package us.dot.its.jpo.ode.api.controllers;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import us.dot.its.jpo.ode.api.models.MessageType;
import us.dot.its.jpo.ode.api.models.messages.DecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.EncodedMessage;
import us.dot.its.jpo.ode.mockdata.MockDecodedMessageGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import us.dot.its.jpo.ode.api.asn1.DecoderManager;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
public class DecoderController {

        private final DecoderManager decoderManager;

        @Autowired
        public DecoderController(
                        DecoderManager decoderManager) {
                this.decoderManager = decoderManager;
        }

        @Operation(summary = "Decode an Uploaded ASN.1-Encoded Message", description = "Decodes an uploaded ASN.1 encoded message and returns the decoded message")
        @RequestMapping(value = "/decoder/upload", method = RequestMethod.POST, produces = "application/json")
        @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Success"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role"),
                        @ApiResponse(responseCode = "400", description = "Message type not supported for asn.1 decoding"),
        })
        public @ResponseBody ResponseEntity<String> decode_request(
                        @RequestBody EncodedMessage encodedMessage,
                        @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
                try {
                        if (testData) {
                                return switch (encodedMessage.getType()) {
                                        case BSM, UNKNOWN -> ResponseEntity.status(HttpStatus.OK)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .body(MockDecodedMessageGenerator.getBsmDecodedMessage()
                                                                        .toString());
                                        case MAP -> ResponseEntity.status(HttpStatus.OK)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .body(MockDecodedMessageGenerator.getMapDecodedMessage()
                                                                        .toString());
                                        case SPAT -> ResponseEntity.status(HttpStatus.OK)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .body(MockDecodedMessageGenerator.getSpatDecodedMessage()
                                                                        .toString());
                                        case SRM -> ResponseEntity.status(HttpStatus.OK)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .body(MockDecodedMessageGenerator.getSrmDecodedMessage()
                                                                        .toString());
                                        case SSM -> ResponseEntity.status(HttpStatus.OK)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .body(MockDecodedMessageGenerator.getSsmDecodedMessage()
                                                                        .toString());
                                        case TIM -> ResponseEntity.status(HttpStatus.OK)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .body(MockDecodedMessageGenerator.getTimDecodedMessage()
                                                                        .toString());
                                        case null ->
                                                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                                String.format("No test data available for Message Type %s",
                                                                                encodedMessage.getType()));
                                };
                        } else {
                                if (encodedMessage.getType() == MessageType.UNKNOWN) {
                                        EncodedMessage newEncodedMessage = DecoderManager
                                                        .identifyAsn1(encodedMessage.getAsn1Message());

                                        if (newEncodedMessage.getType() != MessageType.UNKNOWN) {
                                                encodedMessage = newEncodedMessage;
                                        } else {
                                                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                                "Unable to identify Message Type from ASN.1");
                                        }
                                }

                                DecodedMessage decodedMessage = decoderManager.decode(encodedMessage);

                                return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                                                .body(decodedMessage.toString());
                        }

                } catch (Exception e) {
                        log.warn("Failed to decode data: {}", e.getMessage(), e);
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                        String.format("Exception handling encoded data: %s", e.getMessage()), e);
                }
        }
}