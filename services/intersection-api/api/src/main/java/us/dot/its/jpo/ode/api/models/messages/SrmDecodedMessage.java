package us.dot.its.jpo.ode.api.models.messages;

import us.dot.its.jpo.ode.api.models.MessageType;
import us.dot.its.jpo.ode.model.OdeSrmData;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SrmDecodedMessage extends DecodedMessage{
    public OdeSrmData srm;

    public SrmDecodedMessage(OdeSrmData srm, String asn1Text, String decodeErrors) {
        super(asn1Text, MessageType.SRM, decodeErrors);
        this.srm = srm;
    }

}
