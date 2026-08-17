package us.dot.its.jpo.rsustatusmonitor.services;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Variable;

import us.dot.its.jpo.geojsonconverter.partitioner.RsuIntersectionKey;
import us.dot.its.jpo.rsustatusmonitor.models.postgres.derived.RsuSnmpCredentials;
import us.dot.its.jpo.rsustatusmonitor.models.snmp.OID;
import us.dot.its.jpo.rsustatusmonitor.models.snmp.RsuState;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RsuQueryServiceTest {

    @Mock
    private SNMPService snmpService;

    @Mock
    private KafkaProducerService kafkaService;

    private MeterRegistry meterRegistry;

    @InjectMocks
    private RsuQueryService service;

    private RsuSnmpCredentials credentials;

    @BeforeEach
    public void setup() {
        // Use a real SimpleMeterRegistry instead of mocking to avoid stubbing issues
        meterRegistry = new SimpleMeterRegistry();
        service = new RsuQueryService(snmpService, kafkaService, meterRegistry);

        credentials = new RsuSnmpCredentials(1, "192.168.1.100", "testUser", "testPass", "encryptPass", "SNMPv3",
                "12345");
    }

    @Test
    public void testGetRsuInformation_Success() throws Exception {
        Variable uptimeVar = new Integer32(3600);
        Variable tempVar = new Integer32(45);
        Variable modeVar = new Integer32(1);

        when(snmpService.getSnmpV3Value(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(uptimeVar, tempVar, modeVar);

        doNothing().when(kafkaService).sendRsuStatus(any(RsuIntersectionKey.class), any(RsuState.class));

        service.getRsuInformation(credentials);

        // Allow async method to complete
        Thread.sleep(100);

        ArgumentCaptor<RsuIntersectionKey> keyCaptor = ArgumentCaptor.forClass(RsuIntersectionKey.class);
        ArgumentCaptor<RsuState> stateCaptor = ArgumentCaptor.forClass(RsuState.class);

        verify(kafkaService).sendRsuStatus(keyCaptor.capture(), stateCaptor.capture());

        RsuIntersectionKey capturedKey = keyCaptor.getValue();
        assertEquals(12345, capturedKey.getIntersectionId());
        assertEquals("192.168.1.100", capturedKey.getRsuId());
        assertEquals(-1, capturedKey.getRegion());

        RsuState capturedState = stateCaptor.getValue();
        assertEquals("192.168.1.100", capturedState.rsuIP);
        assertEquals("12345", capturedState.intersectionID);
        assertEquals(3600, capturedState.uptime);
        assertEquals(45, capturedState.temperature);
        assertEquals(1, capturedState.mode);
        assertTrue(capturedState.timestamp > 0);

        verify(snmpService, times(3)).getSnmpV3Value(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testGetRsuInformation_WithNullIntersectionId() throws Exception {
        credentials = new RsuSnmpCredentials(2, "192.168.1.50", "user", "pass", "encPass", "SNMPv3", null);

        Variable uptimeVar = new Integer32(7200);
        Variable tempVar = new Integer32(50);
        Variable modeVar = new Integer32(2);

        when(snmpService.getSnmpV3Value(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(uptimeVar, tempVar, modeVar);

        doNothing().when(kafkaService).sendRsuStatus(any(RsuIntersectionKey.class), any(RsuState.class));

        service.getRsuInformation(credentials);

        // Allow async method to complete
        Thread.sleep(100);

        ArgumentCaptor<RsuIntersectionKey> keyCaptor = ArgumentCaptor.forClass(RsuIntersectionKey.class);
        ArgumentCaptor<RsuState> stateCaptor = ArgumentCaptor.forClass(RsuState.class);

        verify(kafkaService).sendRsuStatus(keyCaptor.capture(), stateCaptor.capture());

        RsuIntersectionKey capturedKey = keyCaptor.getValue();
        assertEquals(-1, capturedKey.getIntersectionId());

        RsuState capturedState = stateCaptor.getValue();
        assertEquals("-1", capturedState.intersectionID);
    }

    @Test
    public void testGetRsuInformation_MissingUsername() throws Exception {
        credentials = new RsuSnmpCredentials(3, "192.168.1.75", null, "pass", "encPass", "SNMPv3", "54321");

        service.getRsuInformation(credentials);

        // Allow async method to complete
        Thread.sleep(100);

        verify(snmpService, never()).getSnmpV3Value(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(kafkaService, never()).sendRsuStatus(any(RsuIntersectionKey.class), any(RsuState.class));
    }

    @Test
    public void testGetRsuInformation_MissingPassword() throws Exception {
        credentials = new RsuSnmpCredentials(4, "192.168.1.80", "user", null, "encPass", "SNMPv3", "99999");

        service.getRsuInformation(credentials);

        // Allow async method to complete
        Thread.sleep(100);

        verify(snmpService, never()).getSnmpV3Value(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(kafkaService, never()).sendRsuStatus(any(RsuIntersectionKey.class), any(RsuState.class));
    }

    @Test
    public void testGetRsuInformation_MissingIpAddress() throws Exception {
        credentials = new RsuSnmpCredentials(5, null, "user", "pass", "encPass", "SNMPv3", "11111");

        // The service will throw NullPointerException when trying to create a metric
        // tag with null IP
        // This happens before the null check in the service
        assertThrows(NullPointerException.class, () -> {
            service.getRsuInformation(credentials);
            // Allow async method to complete
            Thread.sleep(100);
        });
    }

    @Test
    public void testGetRsuInformation_NullEncryptPassword() throws Exception {
        credentials = new RsuSnmpCredentials(6, "192.168.1.90", "user", "pass", null, "SNMPv3", "22222");

        Variable uptimeVar = new Integer32(1800);
        Variable tempVar = new Integer32(40);
        Variable modeVar = new Integer32(0);

        when(snmpService.getSnmpV3Value(anyString(), anyString(), eq("pass"), eq("pass"), anyString()))
                .thenReturn(uptimeVar, tempVar, modeVar);

        doNothing().when(kafkaService).sendRsuStatus(any(RsuIntersectionKey.class), any(RsuState.class));

        service.getRsuInformation(credentials);

        // Allow async method to complete
        Thread.sleep(100);

        verify(snmpService, times(3)).getSnmpV3Value(anyString(), anyString(), eq("pass"), eq("pass"), anyString());
        verify(kafkaService).sendRsuStatus(any(RsuIntersectionKey.class), any(RsuState.class));
    }

    @Test
    public void testGetIntOID_Success() throws Exception {
        OID testOid = new OID("testOid", null, "1.2.3.4.5");
        Variable var = new Integer32(100);

        when(snmpService.getSnmpV3Value(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(var);

        int result = service.getIntOID("192.168.1.100", "user", "pass", "encPass", testOid);

        assertEquals(100, result);
        verify(snmpService).getSnmpV3Value("192.168.1.100", "user", "pass", "encPass", "1.2.3.4.5");
    }

    @Test
    public void testGetIntOID_NullVariable() throws Exception {
        OID testOid = new OID("testOid", null, "1.2.3.4.5");

        when(snmpService.getSnmpV3Value(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);

        int result = service.getIntOID("192.168.1.100", "user", "pass", "encPass", testOid);

        assertEquals(-1, result);
        verify(snmpService).getSnmpV3Value("192.168.1.100", "user", "pass", "encPass", "1.2.3.4.5");
    }

    @Test
    public void testGetIntOID_IOException() throws Exception {
        OID testOid = new OID("testOid", null, "1.2.3.4.5");

        when(snmpService.getSnmpV3Value(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IOException("Connection timeout"));

        int result = service.getIntOID("192.168.1.100", "user", "pass", "encPass", testOid);

        assertEquals(-1, result);
        verify(snmpService).getSnmpV3Value("192.168.1.100", "user", "pass", "encPass", "1.2.3.4.5");
    }

    @Test
    public void testGetIntOID_DifferentValues() throws Exception {
        OID testOid = new OID("testOid", null, "1.2.3.4.5");
        Variable var1 = new Integer32(0);
        Variable var2 = new Integer32(-5);
        Variable var3 = new Integer32(Integer.MAX_VALUE);

        when(snmpService.getSnmpV3Value(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(var1, var2, var3);

        int result1 = service.getIntOID("192.168.1.100", "user", "pass", "encPass", testOid);
        int result2 = service.getIntOID("192.168.1.100", "user", "pass", "encPass", testOid);
        int result3 = service.getIntOID("192.168.1.100", "user", "pass", "encPass", testOid);

        assertEquals(0, result1);
        assertEquals(-5, result2);
        assertEquals(Integer.MAX_VALUE, result3);
        verify(snmpService, times(3)).getSnmpV3Value(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testGetRsuInformation_PartialSnmpFailures() throws Exception {
        Variable uptimeVar = new Integer32(5000);

        when(snmpService.getSnmpV3Value(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(uptimeVar, null, null);

        doNothing().when(kafkaService).sendRsuStatus(any(RsuIntersectionKey.class), any(RsuState.class));

        service.getRsuInformation(credentials);

        // Allow async method to complete
        Thread.sleep(100);

        ArgumentCaptor<RsuState> stateCaptor = ArgumentCaptor.forClass(RsuState.class);
        verify(kafkaService).sendRsuStatus(any(RsuIntersectionKey.class), stateCaptor.capture());

        RsuState capturedState = stateCaptor.getValue();
        assertEquals(5000, capturedState.uptime);
        assertEquals(-1, capturedState.temperature);
        assertEquals(-1, capturedState.mode);
    }

    @Test
    public void testGetRsuInformation_AllSnmpFailures() throws Exception {
        when(snmpService.getSnmpV3Value(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IOException("SNMP timeout"));

        doNothing().when(kafkaService).sendRsuStatus(any(RsuIntersectionKey.class), any(RsuState.class));

        service.getRsuInformation(credentials);

        // Allow async method to complete
        Thread.sleep(100);

        ArgumentCaptor<RsuState> stateCaptor = ArgumentCaptor.forClass(RsuState.class);
        verify(kafkaService).sendRsuStatus(any(RsuIntersectionKey.class), stateCaptor.capture());

        RsuState capturedState = stateCaptor.getValue();
        assertEquals(-1, capturedState.uptime);
        assertEquals(-1, capturedState.temperature);
        assertEquals(-1, capturedState.mode);
    }

    @Test
    public void testGetRsuInformation_AsyncExecution() throws Exception {
        Variable var = new Integer32(100);
        when(snmpService.getSnmpV3Value(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(var);
        doNothing().when(kafkaService).sendRsuStatus(any(RsuIntersectionKey.class), any(RsuState.class));

        long startTime = System.currentTimeMillis();
        service.getRsuInformation(credentials);
        long endTime = System.currentTimeMillis();

        assertTrue(endTime - startTime < 50, "Async method should return immediately");

        // Allow async method to complete
        Thread.sleep(100);

        verify(kafkaService).sendRsuStatus(any(RsuIntersectionKey.class), any(RsuState.class));
    }

    @Test
    public void testGetRsuInformation_MultipleRsusSequentially() throws Exception {
        RsuSnmpCredentials cred1 = new RsuSnmpCredentials(1, "192.168.1.10", "user1", "pass1", "enc1", "SNMPv3",
                "1000");
        RsuSnmpCredentials cred2 = new RsuSnmpCredentials(2, "192.168.1.20", "user2", "pass2", "enc2", "SNMPv3",
                "2000");

        Variable var = new Integer32(100);
        when(snmpService.getSnmpV3Value(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(var);
        doNothing().when(kafkaService).sendRsuStatus(any(RsuIntersectionKey.class), any(RsuState.class));

        service.getRsuInformation(cred1);
        service.getRsuInformation(cred2);

        // Allow async methods to complete
        Thread.sleep(100);

        verify(kafkaService, times(2)).sendRsuStatus(any(RsuIntersectionKey.class), any(RsuState.class));
        verify(snmpService, times(6)).getSnmpV3Value(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testGetIntOID_WithDifferentOIDs() throws Exception {
        OID oid1 = new OID("uptime", null, "1.3.6.1.4.1.1206.4.2.3.5.3.0");
        OID oid2 = new OID("temperature", null, "1.3.6.1.4.1.1206.4.2.3.5.4.0");
        OID oid3 = new OID("mode", null, "1.3.6.1.4.1.1206.4.2.3.5.5.0");

        Variable var1 = new Integer32(1000);
        Variable var2 = new Integer32(35);
        Variable var3 = new Integer32(1);

        when(snmpService.getSnmpV3Value(eq("192.168.1.100"), eq("user"), eq("pass"), eq("encPass"), eq(oid1.getOid())))
                .thenReturn(var1);
        when(snmpService.getSnmpV3Value(eq("192.168.1.100"), eq("user"), eq("pass"), eq("encPass"), eq(oid2.getOid())))
                .thenReturn(var2);
        when(snmpService.getSnmpV3Value(eq("192.168.1.100"), eq("user"), eq("pass"), eq("encPass"), eq(oid3.getOid())))
                .thenReturn(var3);

        int uptime = service.getIntOID("192.168.1.100", "user", "pass", "encPass", oid1);
        int temp = service.getIntOID("192.168.1.100", "user", "pass", "encPass", oid2);
        int mode = service.getIntOID("192.168.1.100", "user", "pass", "encPass", oid3);

        assertEquals(1000, uptime);
        assertEquals(35, temp);
        assertEquals(1, mode);
    }
}
