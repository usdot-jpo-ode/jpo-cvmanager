

package us.dot.its.jpo.ode.api.models.messages;

import java.time.Instant;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import us.dot.its.jpo.ode.api.models.MessageType;

@ToString
@Setter
@EqualsAndHashCode
@Getter
public class DecodedMessage {
    String asn1Text;
    long decodeTime;
    String decodeErrors;
    String type;

    public DecodedMessage(String asn1Text, MessageType type, String decodeErrors){
        this.asn1Text = asn1Text;
        this.decodeTime = Instant.now().toEpochMilli();
        this.decodeErrors = decodeErrors;
        this.type = type.name();
    }
}



