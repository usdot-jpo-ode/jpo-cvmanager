package us.dot.its.jpo.ode.api.models.messages;

import us.dot.its.jpo.asn.j2735.r2024.SignalRequestMessage.SignalRequestMessageMessageFrame;
import us.dot.its.jpo.ode.api.models.MessageType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SrmDecodedMessage extends DecodedMessage {
    private SignalRequestMessageMessageFrame srm;

    public SrmDecodedMessage(SignalRequestMessageMessageFrame srm, String asn1Text, String decodeErrors) {
        super(asn1Text, MessageType.SRM, decodeErrors);
        this.srm = srm;
    }

}
