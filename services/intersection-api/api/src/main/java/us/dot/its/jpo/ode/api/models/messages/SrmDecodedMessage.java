package us.dot.its.jpo.ode.api.models.messages;

import us.dot.its.jpo.asn.j2735.r2024.SignalRequestMessage.SignalRequestMessage;
import us.dot.its.jpo.ode.api.models.MessageType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SrmDecodedMessage extends DecodedMessage {
    private SignalRequestMessage srm;

    public SrmDecodedMessage(SignalRequestMessage srm, String asn1Text, String decodeErrors) {
        super(asn1Text, MessageType.SRM, decodeErrors);
        this.srm = srm;
    }

}
