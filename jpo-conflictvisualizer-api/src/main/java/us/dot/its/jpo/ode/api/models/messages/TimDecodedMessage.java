package us.dot.its.jpo.ode.api.models.messages;

import us.dot.its.jpo.ode.api.models.MessageType;
import us.dot.its.jpo.ode.model.OdeTimData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class TimDecodedMessage extends DecodedMessage{
    public OdeTimData tim;

    public TimDecodedMessage(OdeTimData tim, String asn1Text, MessageType type, String decodeErrors) {
        super(asn1Text, type, decodeErrors);
        this.tim = tim;
    }

}
