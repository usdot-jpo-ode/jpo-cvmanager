package us.dot.its.jpo.ode.api.models.messages;

import us.dot.its.jpo.ode.api.models.MessageType;
import us.dot.its.jpo.ode.model.OdeSsmData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class SsmDecodedMessage extends DecodedMessage{
    public OdeSsmData ssm;

    public SsmDecodedMessage(OdeSsmData ssm, String asn1Text, MessageType type, String decodeErrors) {
        super(asn1Text, type, decodeErrors);
        this.ssm = ssm;
    }

}
