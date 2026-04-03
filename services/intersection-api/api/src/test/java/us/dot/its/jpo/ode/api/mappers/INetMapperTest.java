package us.dot.its.jpo.ode.api.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

class INetMapperTest {

    private INetMapper iNetMapper;

    @BeforeEach
    void setUp() {
        iNetMapper = new INetMapperImpl();
    }

    @Nested
    @DisplayName("Tests for mapInetAddressToString mapper method")
    class MapInetAddressToStringTests {
        @Test
        void testMapInetAddressToString_ValidIpv4() throws UnknownHostException {
            InetAddress address = InetAddress.getByName("192.168.1.100");

            String result = iNetMapper.mapInetAddressToString(address);

            assertNotNull(result);
            assertEquals("192.168.1.100", result);
        }

        @Test
        void testMapInetAddressToString_Null() {
            String result = iNetMapper.mapInetAddressToString(null);

            assertNull(result);
        }

        @Test
        void testMapInetAddressToString_Localhost() throws UnknownHostException {
            InetAddress address = InetAddress.getByName("127.0.0.1");

            String result = iNetMapper.mapInetAddressToString(address);

            assertNotNull(result);
            assertEquals("127.0.0.1", result);
        }
    }

    @Nested
    @DisplayName("Tests for mapStringToInetAddress mapper method")
    class MapStringToInetAddressTests {
        @Test
        void testMapStringToInetAddress_ValidIpv4() {
            InetAddress result = iNetMapper.mapStringToInetAddress("192.168.1.100");

            assertNotNull(result);
            assertEquals("192.168.1.100", result.getHostAddress());
        }

        @Test
        void testMapStringToInetAddress_Null() {
            InetAddress result = iNetMapper.mapStringToInetAddress(null);

            assertNull(result);
        }

        @Test
        void testMapStringToInetAddress_InvalidIp() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> iNetMapper.mapStringToInetAddress("invalid-ip"));

            assertTrue(exception.getMessage().contains("Invalid IP address"));
            assertTrue(exception.getMessage().contains("invalid-ip"));
        }
    }

    @Nested
    @DisplayName("Round-trip tests for InetAddress <-> String mapping")
    class RoundTripTests {
        @Test
        void testRoundTrip_ValidIpv4() {
            String originalIp = "192.168.1.100";

            InetAddress inetAddress = iNetMapper.mapStringToInetAddress(originalIp);
            String resultIp = iNetMapper.mapInetAddressToString(inetAddress);

            assertNotNull(resultIp);
            assertEquals(originalIp, resultIp);
        }
    }
}