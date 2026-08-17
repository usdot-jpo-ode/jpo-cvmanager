package us.dot.its.jpo.rsustatusmonitor.models.postgres.tables;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SnmpCredentialsTest {

    @Test
    public void testGettersAndSetters() {
        SnmpCredentials credentials = new SnmpCredentials();
        
        credentials.setSnmp_credential_id(1);
        credentials.setUsername("testUser");
        credentials.setPassword("testPass");
        credentials.setEncrypt_password("encryptedPass");
        credentials.setNickname("TestCred");
        
        assertEquals(1, credentials.getSnmp_credential_id());
        assertEquals("testUser", credentials.getUsername());
        assertEquals("testPass", credentials.getPassword());
        assertEquals("encryptedPass", credentials.getEncrypt_password());
        assertEquals("TestCred", credentials.getNickname());
    }

    @Test
    public void testEqualsAndHashCode() {
        SnmpCredentials credentials1 = new SnmpCredentials();
        credentials1.setSnmp_credential_id(1);
        credentials1.setUsername("testUser");
        
        SnmpCredentials credentials2 = new SnmpCredentials();
        credentials2.setSnmp_credential_id(1);
        credentials2.setUsername("testUser");
        
        assertEquals(credentials1, credentials2);
        assertEquals(credentials1.hashCode(), credentials2.hashCode());
    }

    @Test
    public void testToString() {
        SnmpCredentials credentials = new SnmpCredentials();
        credentials.setSnmp_credential_id(1);
        
        String result = credentials.toString();
        assertNotNull(result);
    }
}
