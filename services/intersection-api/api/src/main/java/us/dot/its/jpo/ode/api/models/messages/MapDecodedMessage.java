package us.dot.its.jpo.ode.api.models.messages;

import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.models.MessageType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MapDecodedMessage extends DecodedMessage {
    private ProcessedMap<LineString> processedMap;

    public MapDecodedMessage(ProcessedMap<LineString> processedMap, String asn1Text,
            String decodeErrors) {
        super(asn1Text, MessageType.MAP, decodeErrors);
        this.processedMap = processedMap;
    }

}
