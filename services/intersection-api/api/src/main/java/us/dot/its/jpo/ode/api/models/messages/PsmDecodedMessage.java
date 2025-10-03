package us.dot.its.jpo.ode.api.models.messages;

import us.dot.its.jpo.asn.j2735.r2024.PersonalSafetyMessage.PersonalSafetyMessage;
import us.dot.its.jpo.ode.api.models.MessageType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PsmDecodedMessage extends DecodedMessage {
    private PersonalSafetyMessage psm;

    public PsmDecodedMessage(PersonalSafetyMessage psm, String asn1Text, String decodeErrors) {
        super(asn1Text, MessageType.PSM, decodeErrors);
        this.psm = psm;
    }

}
