package us.dot.its.jpo.ode.api.models.messages;

import us.dot.its.jpo.asn.j2735.r2024.PersonalSafetyMessage.PersonalSafetyMessageMessageFrame;
import us.dot.its.jpo.ode.api.models.MessageType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PsmDecodedMessage extends DecodedMessage {
    private PersonalSafetyMessageMessageFrame psm;

    public PsmDecodedMessage(PersonalSafetyMessageMessageFrame psm, String asn1Text, String decodeErrors) {
        super(asn1Text, MessageType.PSM, decodeErrors);
        this.psm = psm;
    }

}
