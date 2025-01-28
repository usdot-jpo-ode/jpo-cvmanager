package us.dot.its.jpo.ode.api.models.messages;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import us.dot.its.jpo.ode.api.models.MessageType;

@ToString
@Setter
@EqualsAndHashCode
@Getter
public class EncodedMessage {
    private String asn1Message;
    private MessageType type;

    // Default constructor
    public EncodedMessage() {
    }

    public EncodedMessage(String asn1Message, MessageType type) {
        this.asn1Message = asn1Message;
        this.type = type;
    }
}
