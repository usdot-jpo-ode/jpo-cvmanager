package us.dot.its.jpo.rsustatusmonitor.models.snmp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import us.dot.its.jpo.rsustatusmonitor.models.postgres.derived.RsuSnmpCredentials;

// NTCIP-1218
@Data
@Generated
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RsuMsgfwdValues {
    private RsuSnmpCredentials rsuSnmpCredentials;
    private String rsuReceivedMsgPsid;
    private String rsuReceivedMsgDestIpAddr;
    private Integer rsuReceivedMsgDestPort;
    private Integer rsuReceivedMsgProtocol;
    private Integer rsuReceivedMsgRssi;
    private Integer rsuReceivedMsgInterval;
    // Formatted in an 8-byte hex string: YYYYMMddHHmmssSS
    // Example: '2025-01-01 00:00:00.00' would be formatted like '07e9010100000000'
    private String rsuReceivedMsgDeliveryStart;
    private String rsuReceivedMsgDeliveryStop;
    private Integer rsuReceivedMsgStatus;
    private Integer rsuReceivedMsgSecure;
    private Integer rsuReceivedMsgAuthMsgInterval;
}