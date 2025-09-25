package us.dot.its.jpo.ode.api.asn1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import us.dot.its.jpo.ode.api.models.messages.DecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.EncodedMessage;
import us.dot.its.jpo.ode.model.OdeMessageFrameData;

public interface Decoder {

    /**
     * This function manages the decoding for the corresponding J2735 Data type.
     * This includes transforming the data to the correct ODE data format, and
     * Processed formats where applicable
     * 
     * @return DecodedMessage Object including all intermediate formats during the
     *         decoding procedure.
     */
    public DecodedMessage decode(EncodedMessage message);

    /**
     * This is a helper function to assist in decoding ASN.1. This function converts
     * ASN.1 to an XML string representing the decoded result
     * 
     * @return String representation of the decoded ASN.1 message in XML
     */
    public String decodeAsnToXERString(String asnHex);

    /**
     * This is a helper function to assist in decoding ASN.1. This function converts
     * the decoded XML string to an OdeMessageFrameData object
     * 
     * @return OdeMessageFrameData representation of the decoded ASN.1 message in
     *         XML
     */
    public OdeMessageFrameData convertXERToMessageFrame(String encodedXml)
            throws JsonMappingException, JsonProcessingException;

}
