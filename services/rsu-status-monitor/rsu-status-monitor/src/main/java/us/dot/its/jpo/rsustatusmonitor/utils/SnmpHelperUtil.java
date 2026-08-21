package us.dot.its.jpo.rsustatusmonitor.utils;

import java.time.LocalDateTime;

import org.snmp4j.smi.OctetString;

public class SnmpHelperUtil {
    public static String generateNtcip1218HexDateTimeString(LocalDateTime dateTime) {
        String year = String.format("%04x", dateTime.getYear());
        String month = String.format("%02x", dateTime.getMonthValue());
        String day = String.format("%02x", dateTime.getDayOfMonth());
        String hour = String.format("%02x", dateTime.getHour());
        String minute = String.format("%02x", dateTime.getMinute());
        String second = String.format("%02x", dateTime.getSecond());
        return String.format("%s%s%s%s%s%s00", year, month, day, hour, minute, second);
    }

    /**
     * Helper method to convert hex string to OctetString for SNMP - add comment
     * explaining nuance logic
     * 
     * @param hexString The hex string to convert (e.g., "07e9010100000000")
     * @return OctetString with the converted byte array
     */
    public static OctetString hexStringToOctetString(String hexString) {
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hexString.substring(2 * i, 2 * i + 2), 16);
        }
        return new OctetString(bytes);
    }
}
