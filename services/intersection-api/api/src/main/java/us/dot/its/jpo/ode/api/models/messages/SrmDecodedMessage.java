package us.dot.its.jpo.ode.api.models.messages;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.srm.ProcessedSrm;
import us.dot.its.jpo.ode.api.models.MessageType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SrmDecodedMessage extends DecodedMessage {
    private ProcessedSrm srm;

    public SrmDecodedMessage(ProcessedSrm srm, String asn1Text, String decodeErrors) {
        super(asn1Text, MessageType.SRM, decodeErrors);
        this.srm = srm;
    }

}
