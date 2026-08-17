package us.dot.its.jpo.ode.api.models.messages;

import us.dot.its.jpo.ode.api.models.MessageType;
import us.dot.its.jpo.geojsonconverter.pojos.ssm.ProcessedSsm;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SsmDecodedMessage extends DecodedMessage {
    private ProcessedSsm ssm;

    public SsmDecodedMessage(ProcessedSsm ssm, String asn1Text, String decodeErrors) {
        super(asn1Text, MessageType.SSM, decodeErrors);
        this.ssm = ssm;
    }

}
