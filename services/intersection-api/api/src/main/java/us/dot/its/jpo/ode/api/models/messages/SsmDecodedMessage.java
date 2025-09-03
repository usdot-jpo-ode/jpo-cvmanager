package us.dot.its.jpo.ode.api.models.messages;

import us.dot.its.jpo.asn.j2735.r2024.SignalStatusMessage.SignalStatusMessageMessageFrame;
import us.dot.its.jpo.ode.api.models.MessageType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SsmDecodedMessage extends DecodedMessage {
    private SignalStatusMessageMessageFrame ssm;

    public SsmDecodedMessage(SignalStatusMessageMessageFrame ssm, String asn1Text, String decodeErrors) {
        super(asn1Text, MessageType.SSM, decodeErrors);
        this.ssm = ssm;
    }

}
