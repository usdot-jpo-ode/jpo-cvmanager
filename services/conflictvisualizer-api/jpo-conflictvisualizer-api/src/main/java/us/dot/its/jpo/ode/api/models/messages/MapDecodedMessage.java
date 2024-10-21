package us.dot.its.jpo.ode.api.models.messages;


import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.models.MessageType;
import us.dot.its.jpo.ode.model.OdeMapData;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MapDecodedMessage extends DecodedMessage{
    public OdeMapData map;
    public ProcessedMap<LineString> processedMap;

    public MapDecodedMessage(ProcessedMap<LineString> processedMap, OdeMapData map, String asn1Text, String decodeErrors) {
        super(asn1Text, MessageType.MAP, decodeErrors);
        this.processedMap = processedMap;
        this.map = map;
    }

}
