
package us.dot.its.jpo.ode.api.models.messages;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import us.dot.its.jpo.geojsonconverter.DateJsonMapper;
import us.dot.its.jpo.ode.api.models.MessageType;

@Setter
@EqualsAndHashCode
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BsmDecodedMessage.class, name = "BSM"),
        @JsonSubTypes.Type(value = MapDecodedMessage.class, name = "MAP"),
        @JsonSubTypes.Type(value = SpatDecodedMessage.class, name = "SPAT"),
        @JsonSubTypes.Type(value = SrmDecodedMessage.class, name = "SRM"),
        @JsonSubTypes.Type(value = SsmDecodedMessage.class, name = "SSM"),
        @JsonSubTypes.Type(value = TimDecodedMessage.class, name = "TIM")
})
public class DecodedMessage {
    private static final Logger logger = LoggerFactory.getLogger(DecodedMessage.class);

    private String asn1Text;
    private long decodeTime;
    private String decodeErrors;
    private String type;

    public DecodedMessage(String asn1Text, MessageType type, String decodeErrors) {
        this.asn1Text = asn1Text;
        this.decodeTime = Instant.now().toEpochMilli();
        this.decodeErrors = decodeErrors;
        this.type = type.name();
    }

    @Override
    public String toString() {
        try {
            return DateJsonMapper.getInstance().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.debug("Error: Exception serializing {} Event to JSON", this, e);
        }
        return "";
    }
}
