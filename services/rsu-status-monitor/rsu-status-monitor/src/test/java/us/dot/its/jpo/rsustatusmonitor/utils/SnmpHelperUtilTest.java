package us.dot.its.jpo.rsustatusmonitor.utils;

import org.junit.jupiter.api.Test;
import org.snmp4j.smi.OctetString;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SnmpHelperUtilTest {

    @Test
    public void testGenerateNtcip1218HexDateTimeString_BasicDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 11, 21, 14, 30, 45);

        String result = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dateTime);

        // Format: YYYYMMDDHHMMSS00
        // 2025 = 0x07e9, 11 = 0x0b, 21 = 0x15, 14 = 0x0e, 30 = 0x1e, 45 = 0x2d
        assertEquals("07e90b150e1e2d00", result);
        assertEquals(16, result.length());
    }

    @Test
    public void testGenerateNtcip1218HexDateTimeString_Midnight() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0);

        String result = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dateTime);

        // 2025 = 0x07e9, 01 = 0x01, 01 = 0x01, 00 = 0x00, 00 = 0x00, 00 = 0x00
        assertEquals("07e90101000000", result.substring(0, 14));
        assertEquals("00", result.substring(14));
        assertEquals(16, result.length());
    }

    @Test
    public void testGenerateNtcip1218HexDateTimeString_MaxValues() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 12, 31, 23, 59, 59);

        String result = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dateTime);

        // 2025 = 0x07e9, 12 = 0x0c, 31 = 0x1f, 23 = 0x17, 59 = 0x3b, 59 = 0x3b
        assertEquals("07e90c1f173b3b00", result);
        assertEquals(16, result.length());
    }

    @Test
    public void testGenerateNtcip1218HexDateTimeString_LeapYearDate() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 2, 29, 12, 0, 0);

        String result = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dateTime);

        // 2024 = 0x07e8, 02 = 0x02, 29 = 0x1d, 12 = 0x0c, 00 = 0x00, 00 = 0x00
        assertEquals("07e8021d0c0000", result.substring(0, 14));
        assertEquals(16, result.length());
    }

    @Test
    public void testGenerateNtcip1218HexDateTimeString_Year2000() {
        LocalDateTime dateTime = LocalDateTime.of(2000, 6, 15, 10, 20, 30);

        String result = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dateTime);

        // 2000 = 0x07d0, 06 = 0x06, 15 = 0x0f, 10 = 0x0a, 20 = 0x14, 30 = 0x1e
        assertEquals("07d0060f0a141e00", result);
    }

    @Test
    public void testGenerateNtcip1218HexDateTimeString_FarFutureYear() {
        LocalDateTime dateTime = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

        String result = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dateTime);

        // 9999 = 0x270f, 12 = 0x0c, 31 = 0x1f, 23 = 0x17, 59 = 0x3b, 59 = 0x3b
        assertEquals("270f0c1f173b3b00", result);
        assertEquals(16, result.length());
    }

    @Test
    public void testGenerateNtcip1218HexDateTimeString_AlwaysEndsWithZeros() {
        LocalDateTime dt1 = LocalDateTime.of(2025, 5, 10, 8, 15, 22);
        LocalDateTime dt2 = LocalDateTime.of(2023, 1, 1, 0, 0, 1);
        LocalDateTime dt3 = LocalDateTime.of(2030, 12, 25, 18, 45, 30);

        String result1 = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dt1);
        String result2 = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dt2);
        String result3 = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dt3);

        assertTrue(result1.endsWith("00"), "Result should end with 00");
        assertTrue(result2.endsWith("00"), "Result should end with 00");
        assertTrue(result3.endsWith("00"), "Result should end with 00");
    }

    @Test
    public void testGenerateNtcip1218HexDateTimeString_AllLowercaseHex() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 10, 31, 15, 45, 59);

        String result = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dateTime);

        assertEquals(result.toLowerCase(), result, "Hex string should be lowercase");
        assertTrue(result.matches("[0-9a-f]+"), "Should only contain lowercase hex characters");
    }

    @Test
    public void testGenerateNtcip1218HexDateTimeString_PaddingForSingleDigits() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 5, 3, 7, 9);

        String result = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dateTime);

        // 2025 = 0x07e9, 01 = 0x01, 05 = 0x05, 03 = 0x03, 07 = 0x07, 09 = 0x09
        assertEquals("07e90105030709", result.substring(0, 14));
        assertEquals(16, result.length());
    }

    @Test
    public void testHexStringToOctetString_BasicConversion() {
        String hexString = "07e90b150e1e2d00";

        OctetString result = SnmpHelperUtil.hexStringToOctetString(hexString);

        assertNotNull(result);
        assertEquals(8, result.length()); // 16 hex chars = 8 bytes

        byte[] bytes = result.toByteArray();
        assertEquals((byte) 0x07, bytes[0]);
        assertEquals((byte) 0xe9, bytes[1]);
        assertEquals((byte) 0x0b, bytes[2]);
        assertEquals((byte) 0x15, bytes[3]);
        assertEquals((byte) 0x0e, bytes[4]);
        assertEquals((byte) 0x1e, bytes[5]);
        assertEquals((byte) 0x2d, bytes[6]);
        assertEquals((byte) 0x00, bytes[7]);
    }

    @Test
    public void testHexStringToOctetString_EmptyString() {
        String hexString = "";

        OctetString result = SnmpHelperUtil.hexStringToOctetString(hexString);

        assertNotNull(result);
        assertEquals(0, result.length());
    }

    @Test
    public void testHexStringToOctetString_SingleByte() {
        String hexString = "FF";

        OctetString result = SnmpHelperUtil.hexStringToOctetString(hexString);

        assertNotNull(result);
        assertEquals(1, result.length());
        assertEquals((byte) 0xFF, result.toByteArray()[0]);
    }

    @Test
    public void testHexStringToOctetString_AllZeros() {
        String hexString = "00000000";

        OctetString result = SnmpHelperUtil.hexStringToOctetString(hexString);

        assertNotNull(result);
        assertEquals(4, result.length());
        byte[] bytes = result.toByteArray();
        for (byte b : bytes) {
            assertEquals((byte) 0x00, b);
        }
    }

    @Test
    public void testHexStringToOctetString_AllOnes() {
        String hexString = "FFFFFFFF";

        OctetString result = SnmpHelperUtil.hexStringToOctetString(hexString);

        assertNotNull(result);
        assertEquals(4, result.length());
        byte[] bytes = result.toByteArray();
        for (byte b : bytes) {
            assertEquals((byte) 0xFF, b);
        }
    }

    @Test
    public void testHexStringToOctetString_MixedCase() {
        String hexString = "AbCdEf01";

        OctetString result = SnmpHelperUtil.hexStringToOctetString(hexString);

        assertNotNull(result);
        assertEquals(4, result.length());
        byte[] bytes = result.toByteArray();
        assertEquals((byte) 0xAB, bytes[0]);
        assertEquals((byte) 0xCD, bytes[1]);
        assertEquals((byte) 0xEF, bytes[2]);
        assertEquals((byte) 0x01, bytes[3]);
    }

    @Test
    public void testHexStringToOctetString_LongString() {
        String hexString = "0123456789ABCDEF0123456789ABCDEF";

        OctetString result = SnmpHelperUtil.hexStringToOctetString(hexString);

        assertNotNull(result);
        assertEquals(16, result.length());
    }

    @Test
    public void testRoundTrip_GenerateAndConvert() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 11, 21, 14, 30, 45);

        String hexString = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dateTime);
        OctetString octetString = SnmpHelperUtil.hexStringToOctetString(hexString);

        assertNotNull(hexString);
        assertNotNull(octetString);
        assertEquals(16, hexString.length());
        assertEquals(8, octetString.length());

        byte[] bytes = octetString.toByteArray();
        assertEquals((byte) 0x07, bytes[0]); // Year high byte
        assertEquals((byte) 0xe9, bytes[1]); // Year low byte (2025 = 0x07e9)
    }

    @Test
    public void testGenerateNtcip1218HexDateTimeString_ConsistentFormat() {
        LocalDateTime dateTime1 = LocalDateTime.of(2025, 6, 15, 10, 20, 30);
        LocalDateTime dateTime2 = LocalDateTime.of(2025, 6, 15, 10, 20, 30);

        String result1 = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dateTime1);
        String result2 = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dateTime2);

        assertEquals(result1, result2, "Same date/time should produce identical results");
    }

    @Test
    public void testGenerateNtcip1218HexDateTimeString_DifferentTimes() {
        LocalDateTime dateTime1 = LocalDateTime.of(2025, 6, 15, 10, 20, 30);
        LocalDateTime dateTime2 = LocalDateTime.of(2025, 6, 15, 10, 20, 31);

        String result1 = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dateTime1);
        String result2 = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dateTime2);

        assertNotEquals(result1, result2, "Different times should produce different results");
    }

    @Test
    public void testHexStringToOctetString_WithLeadingZeros() {
        String hexString = "00010203";

        OctetString result = SnmpHelperUtil.hexStringToOctetString(hexString);

        assertNotNull(result);
        byte[] bytes = result.toByteArray();
        assertEquals((byte) 0x00, bytes[0]);
        assertEquals((byte) 0x01, bytes[1]);
        assertEquals((byte) 0x02, bytes[2]);
        assertEquals((byte) 0x03, bytes[3]);
    }

    @Test
    public void testGenerateNtcip1218HexDateTimeString_EarlyMorningHour() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 3, 10, 1, 5, 8);

        String result = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dateTime);

        // 2025 = 0x07e9, 03 = 0x03, 10 = 0x0a, 01 = 0x01, 05 = 0x05, 08 = 0x08
        assertEquals("07e9030a01050800", result);
    }

    @Test
    public void testGenerateNtcip1218HexDateTimeString_NoonExactly() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 7, 4, 12, 0, 0);

        String result = SnmpHelperUtil.generateNtcip1218HexDateTimeString(dateTime);

        // 2025 = 0x07e9, 07 = 0x07, 04 = 0x04, 12 = 0x0c, 00 = 0x00, 00 = 0x00
        assertEquals("07e907040c000000", result);
    }

    @Test
    public void testHexStringToOctetString_ByteValueRange() {
        String hexString = "00FF7F80";

        OctetString result = SnmpHelperUtil.hexStringToOctetString(hexString);

        byte[] bytes = result.toByteArray();
        assertEquals((byte) 0x00, bytes[0]); // Min unsigned
        assertEquals((byte) 0xFF, bytes[1]); // Max unsigned
        assertEquals((byte) 0x7F, bytes[2]); // Max positive signed
        assertEquals((byte) 0x80, bytes[3]); // Min negative signed
    }
}
