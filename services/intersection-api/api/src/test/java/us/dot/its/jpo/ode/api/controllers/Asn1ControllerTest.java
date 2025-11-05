package us.dot.its.jpo.ode.api.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.api.asn1.DecoderManager;
import us.dot.its.jpo.ode.api.models.MessageType;
import us.dot.its.jpo.ode.api.models.messages.DecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.EncodedMessage;

public class Asn1ControllerTest {

    @Mock
    DecoderManager decoderManager;

    Asn1Controller controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new Asn1Controller(decoderManager);
    }

    @Test
    void testDecodeRequestTestDataBSM() {
        EncodedMessage encodedMessage = new EncodedMessage();
        encodedMessage.setType(MessageType.BSM);

        ResponseEntity<String> response = controller.decode_request(encodedMessage, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("bsm"); // assuming mock data contains "bsm"
    }

    @Test
    void testDecodeRequestTestDataUnknown() {
        EncodedMessage encodedMessage = new EncodedMessage();
        encodedMessage.setType(MessageType.UNKNOWN);

        ResponseEntity<String> response = controller.decode_request(encodedMessage, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("bsm"); // UNKNOWN returns BSM mock
    }

    @Test
    void testDecodeRequestNormalDecode() {
        EncodedMessage encodedMessage = new EncodedMessage();
        encodedMessage.setType(MessageType.BSM);
        encodedMessage.setAsn1Message("test");

        DecodedMessage decodedMessage = mock(DecodedMessage.class);
        when(decodedMessage.toString()).thenReturn("{\"decoded\":true}");

        when(decoderManager.decode(any(EncodedMessage.class))).thenReturn(decodedMessage);

        ResponseEntity<String> response = controller.decode_request(encodedMessage, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"decoded\":true}");
        verify(decoderManager).decode(encodedMessage);
    }
}