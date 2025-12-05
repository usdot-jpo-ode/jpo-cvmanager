package us.dot.its.jpo.rsustatusmonitor.models.postgres.derived;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RsuSnmpCredentialsTest {

    @Test
    public void testConstructor() {
        RsuSnmpCredentials credentials = new RsuSnmpCredentials(
            1, "192.168.1.1", "user", "pass", "encryptPass", "SHA", "12345"
        );
        
        assertEquals(1, credentials.getRsu_id());
        assertEquals("192.168.1.1", credentials.getIpv4_address());
        assertEquals("user", credentials.getUsername());
        assertEquals("pass", credentials.getPassword());
        assertEquals("encryptPass", credentials.getEncrypt_password());
        assertEquals("SHA", credentials.getProtocol_code());
        assertEquals("12345", credentials.getIntersection_id());
    }

    @Test
    public void testGettersAndSetters() {
        RsuSnmpCredentials credentials = new RsuSnmpCredentials(
            1, "192.168.1.1", "user", "pass", "encryptPass", "SHA", "12345"
        );
        
        credentials.setRsu_id(2);
        credentials.setIpv4_address("10.0.0.1");
        credentials.setUsername("newUser");
        credentials.setPassword("newPass");
        credentials.setEncrypt_password("newEncrypt");
        credentials.setProtocol_code("MD5");
        credentials.setIntersection_id("67890");
        
        assertEquals(2, credentials.getRsu_id());
        assertEquals("10.0.0.1", credentials.getIpv4_address());
        assertEquals("newUser", credentials.getUsername());
        assertEquals("newPass", credentials.getPassword());
        assertEquals("newEncrypt", credentials.getEncrypt_password());
        assertEquals("MD5", credentials.getProtocol_code());
        assertEquals("67890", credentials.getIntersection_id());
    }

    @Test
    public void testEqualsAndHashCode() {
        RsuSnmpCredentials credentials1 = new RsuSnmpCredentials(
            1, "192.168.1.1", "user", "pass", "encryptPass", "SHA", "12345"
        );
        RsuSnmpCredentials credentials2 = new RsuSnmpCredentials(
            1, "192.168.1.1", "user", "pass", "encryptPass", "SHA", "12345"
        );
        RsuSnmpCredentials credentials3 = new RsuSnmpCredentials(
            2, "10.0.0.1", "user2", "pass2", "encryptPass2", "MD5", "67890"
        );
        
        assertEquals(credentials1, credentials2);
        assertNotEquals(credentials1, credentials3);
        assertEquals(credentials1.hashCode(), credentials2.hashCode());
    }

    @Test
    public void testToString() {
        RsuSnmpCredentials credentials = new RsuSnmpCredentials(
            1, "192.168.1.1", "user", "pass", "encryptPass", "SHA", "12345"
        );
        String result = credentials.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("192.168.1.1"));
    }
}
