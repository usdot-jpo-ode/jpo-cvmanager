

package us.dot.its.jpo.ode.api.models;

import lombok.Data;

@Data
public class Message<T> {
    String asn1Text;
    // String xmlText;
    // String odeJsonText;
    T decodedMessage;
}