package us.dot.its.jpo.ode.api.models.messages;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import us.dot.its.jpo.ode.api.models.MessageType;


@ToString
@Setter
@EqualsAndHashCode
@Getter
public class EncodedMessage {
    String asn1Message;
    MessageType type;

    public EncodedMessage(){
        
    }

    public EncodedMessage(String asn1Message, MessageType type){
        this.asn1Message = asn1Message;
        this.type = type;
    }

    public EncodedMessage(String asn1Message, String type){
        this.asn1Message = asn1Message;
        this.type = MessageType.valueOf(type);
    }
}



