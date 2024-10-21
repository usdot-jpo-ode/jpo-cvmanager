package us.dot.its.jpo.ode.api.models.messages;

import us.dot.its.jpo.ode.api.models.MessageType;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TimDecodedMessage extends DecodedMessage{
    public ObjectNode tim;

    public TimDecodedMessage(ObjectNode tim, String asn1Text, String decodeErrors) {
        super(asn1Text, MessageType.TIM, decodeErrors);
        this.tim = tim;
    }

}
