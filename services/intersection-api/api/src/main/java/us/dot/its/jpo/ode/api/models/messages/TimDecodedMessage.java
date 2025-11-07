package us.dot.its.jpo.ode.api.models.messages;

import us.dot.its.jpo.asn.j2735.r2024.TravelerInformation.TravelerInformation;
import us.dot.its.jpo.ode.api.models.MessageType;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TimDecodedMessage extends DecodedMessage {
    private TravelerInformation tim;

    public TimDecodedMessage(TravelerInformation tim, String asn1Text, String decodeErrors) {
        super(asn1Text, MessageType.TIM, decodeErrors);
        this.tim = tim;
    }

}
