package us.dot.its.jpo.ode.api.models.messages;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.Point;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.bsm.ProcessedBsm;
import us.dot.its.jpo.ode.api.models.MessageType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BsmDecodedMessage extends DecodedMessage {
    private ProcessedBsm<Point> bsm;

    public BsmDecodedMessage(ProcessedBsm<Point> bsm, String asn1Text, String decodeErrors) {
        super(asn1Text, MessageType.BSM, decodeErrors);
        this.bsm = bsm;
    }

}
