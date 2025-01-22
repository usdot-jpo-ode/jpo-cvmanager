package us.dot.its.jpo.ode.api.asn1;

import us.dot.its.jpo.ode.api.models.messages.DecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.EncodedMessage;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.util.XmlUtils.XmlUtilsException;

public interface Decoder {
    
    /**
     * This function manages the decoding for the corresponding J2735 Data type. This includes transforming the data to the correct ODE data format, and Processed formats where applicable
     * @return DecodedMessage Object including all intermediate formats during the decoding procedure.
     */
    public DecodedMessage decode(EncodedMessage message);


    /**
     * This function adds metadata and transforms the included string into an OdeAsn1Data Object
     * @return OdeAsn1Data Object with Metadata for the corresponding message
     */
    public OdeData getAsOdeData(String encodedData);
    
    /**
     * This Method takes in an XML string containing a J2735 message format, and returns an equivalent JSON object representing the same J2735 message.
     * @return Returns an a Json Serializable Object representing the J2735 Data 
     */
    public OdeData getAsOdeJson(String encodedXml) throws XmlUtilsException;

    

}
