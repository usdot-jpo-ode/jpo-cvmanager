package us.dot.its.jpo.ode.api.asn1;

import us.dot.its.jpo.ode.api.models.messages.DecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.EncodedMessage;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.util.XmlUtils.XmlUtilsException;

public interface Decoder {
    
    public DecodedMessage decode(EncodedMessage message);
    public OdeData getAsOdeData(String encodedData);
    public OdeData getAsOdeJson(String encodedXml) throws XmlUtilsException;

    

}
