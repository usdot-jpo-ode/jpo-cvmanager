package us.dot.its.jpo.ode.api.asn1;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import us.dot.its.jpo.ode.api.models.MessageType;
import us.dot.its.jpo.ode.api.models.messages.DecodedMessage;
import us.dot.its.jpo.ode.api.models.messages.EncodedMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DecoderManager {

    public static final Map<String, Pair<MessageType, Integer>> startFlagsToTypesAndSizes = Map.of(
            "0014", Pair.of(MessageType.BSM, 500),
            "0012", Pair.of(MessageType.MAP, 2048),
            "0013", Pair.of(MessageType.SPAT, 1000),
            "001d", Pair.of(MessageType.SRM, 500),
            "001e", Pair.of(MessageType.SSM, 500),
            "001f", Pair.of(MessageType.TIM, 2048),
            "0020", Pair.of(MessageType.PSM, 500));
    public static final Map<MessageType, String> typesToStartFlags = startFlagsToTypesAndSizes.entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getValue().getLeft(), Map.Entry::getKey));
    public static final int HEADER_MINIMUM_SIZE = 20;

    private final BsmDecoder bsmDecoder;
    private final MapDecoder mapDecoder;
    private final SpatDecoder spatDecoder;
    private final SrmDecoder srmDecoder;
    private final SsmDecoder ssmDecoder;
    private final TimDecoder timDecoder;
    private final PsmDecoder psmDecoder;

    @Autowired
    public DecoderManager(
            BsmDecoder bsmDecoder,
            MapDecoder mapDecoder,
            SpatDecoder spatDecoder,
            SrmDecoder srmDecoder,
            SsmDecoder ssmDecoder,
            TimDecoder timDecoder,
            PsmDecoder psmDecoder) {
        this.bsmDecoder = bsmDecoder;
        this.mapDecoder = mapDecoder;
        this.spatDecoder = spatDecoder;
        this.srmDecoder = srmDecoder;
        this.ssmDecoder = ssmDecoder;
        this.timDecoder = timDecoder;
        this.psmDecoder = psmDecoder;

    }

    /**
     * This function takes in an Encoded message object, and decodes it into a
     * DecodedMessage Object.
     * During the decoding process this function performs the following
     * Remove Message Headers
     * Pass the Message to the ACM module for Decoding
     * Pass the message to the appropriate Message type decoder to be converted to
     * the correct J2735 and Processed- message formats.
     * 
     * @return A DecodedMessage object representing the object in its multiple
     *         representations. This includes, asn.1, ODEJsonFormat, and Processed
     *         formats for available message types.
     */
    public DecodedMessage decode(EncodedMessage message) {
        String asn1 = message.getAsn1Message().toLowerCase();
        final String payload = removeHeader(asn1, message.getType()).toLowerCase();

        message.setAsn1Message(payload);

        if (payload == null) {
            return new DecodedMessage(null, message.getType(),
                    "Unable to find valid message start flag within input data");
        }

        final Decoder decoder = switch (message.getType()) {
            case MessageType.BSM:
                yield bsmDecoder;
            case MessageType.MAP:
                yield mapDecoder;
            case MessageType.SPAT:
                yield spatDecoder;
            case MessageType.SRM:
                yield srmDecoder;
            case MessageType.SSM:
                yield ssmDecoder;
            case MessageType.TIM:
                yield timDecoder;
            case MessageType.PSM:
                yield psmDecoder; // PSM decoder not implemented yet
            case MessageType.UNKNOWN:
                yield null;
        };
        if (decoder == null) {
            return new DecodedMessage(payload, message.getType(), "No Valid Decoder found for Message Type UNKNOWN");
        } else {
            return decoder.decode(message);
        }
    }

    /**
     * This is a helper function to return the current time as an ISO formatted
     * String
     * 
     * @return An ISO formatted string representing the current time
     */
    public static String getCurrentIsoTimestamp() {
        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
        return utc.format(DateTimeFormatter.ISO_INSTANT);
    }

    /**
     * This returns a static string representing the "Origin IP" for user-uploaded
     * data
     * 
     * @return "user-upload"
     */
    public static String getStaticUserOriginIp() {
        return "user-upload";
    }

    /**
     * This returns a Hex Encoded ASN.1 String where any header bytes before the
     * message frame type bytes have been removed.
     * 
     * @return A hexadecimal string representing an ASN.1 encoded message. The first
     *         4 characters of the hex string should correspond to an ASN.1 message
     *         type.
     */
    public static String removeHeader(String hexPacket, MessageType type) {

        String startFlag = typesToStartFlags.get(type);

        int startIndex = hexPacket.indexOf(startFlag);

        return switch (startIndex) {
            case 0:
                yield hexPacket; // Raw Message no Headers
            case -1:
                yield null;
            default:
                // We likely found a message with a header, look past the first 20
                // bytes for the start of the message
                int trueStartIndex = HEADER_MINIMUM_SIZE
                        + hexPacket.substring(HEADER_MINIMUM_SIZE).indexOf(startFlag);
                yield hexPacket.substring(trueStartIndex);
        };
    }

    /**
     * This method takes in a hex encoded ASN.1 packet and returns the message type
     * that matches the corresponding method.
     * 
     * @return An EncodedMessage object containing a String representing the hex
     *         encoded asn.1 and MessageType object representing MAP, SPaT, BSM,
     *         etc.
     */
    public static EncodedMessage identifyAsn1(String hexPacket) {

        int endIndex = hexPacket.length() - 1;

        int closestStartIndex = endIndex;
        MessageType closestMessageType = MessageType.UNKNOWN;

        for (Map.Entry<String, Pair<MessageType, Integer>> entry : startFlagsToTypesAndSizes.entrySet()) {

            String startFlag = entry.getKey();
            MessageType mType = entry.getValue().getLeft();
            int typeBufferSize = entry.getValue().getRight();

            // Skip possible message type if packet is too big
            if (endIndex > typeBufferSize * 2) {
                continue;
            }

            int startIndex = hexPacket.indexOf(startFlag);

            if (startIndex == 0) {
                return new EncodedMessage(hexPacket, mType);
            } else if (startIndex != -1) {
                int trueStartIndex = hexPacket.substring(HEADER_MINIMUM_SIZE).indexOf(startFlag);
                if (trueStartIndex == -1) {
                    continue;
                }
                trueStartIndex += HEADER_MINIMUM_SIZE;

                while ((trueStartIndex % 2 == 1) && trueStartIndex < hexPacket.length() - 4) {
                    // trueStartIndex != -1 is required by trueStartIndex % 2 == 1
                    int newStartIndex = hexPacket.substring(trueStartIndex + 1).indexOf(startFlag);
                    if (newStartIndex == -1) {
                        trueStartIndex = -1;
                        break;
                    } else {
                        trueStartIndex += newStartIndex + 1;
                    }
                }

                if (trueStartIndex != -1 && trueStartIndex < closestStartIndex) {
                    closestStartIndex = trueStartIndex;
                    closestMessageType = mType;
                }
            }
        }

        if (closestMessageType == MessageType.UNKNOWN) {
            return new EncodedMessage(hexPacket, MessageType.UNKNOWN);
        } else {
            return new EncodedMessage(hexPacket.substring(closestStartIndex), closestMessageType);
        }
    }
}