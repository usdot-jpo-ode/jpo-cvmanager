package us.dot.its.jpo.rsustatusmonitor.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import us.dot.its.jpo.rsustatusmonitor.snmp.SnmpProperties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings({ "unchecked", "unused" })
@ExtendWith(MockitoExtension.class)
public class SNMPServiceTest {

    @Mock
    private SnmpProperties snmpProperties;

    @Mock
    private Snmp snmp;

    @Mock
    private DefaultUdpTransportMapping transport;

    @Mock
    private ResponseEvent<UdpAddress> responseEvent;

    @Mock
    private PDU responsePdu;

    @Mock
    private ScopedPDU scopedResponsePdu;

    private SNMPService service;

    @BeforeEach
    public void setup() {
        lenient().when(snmpProperties.getPort()).thenReturn(161);
        lenient().when(snmpProperties.getRetries()).thenReturn(2);
        lenient().when(snmpProperties.getTimeout()).thenReturn(5000);

        service = new SNMPService(snmpProperties);
    }

    @Test
    public void testGetSnmpV2Value_Success() throws Exception {
        String ipAddress = "192.168.1.100";
        String community = "public";
        String oid = "1.3.6.1.2.1.1.1.0";
        String expectedValue = "Test Device";

        VariableBinding vb = new VariableBinding(new OID(oid), new OctetString(expectedValue));
        when(responsePdu.get(0)).thenReturn(vb);
        when(responseEvent.getResponse()).thenReturn(responsePdu);

        try (MockedConstruction<DefaultUdpTransportMapping> transportMock = mockConstruction(
                DefaultUdpTransportMapping.class,
                (mock, context) -> doNothing().when(mock).listen());
                MockedConstruction<Snmp> snmpMock = mockConstruction(Snmp.class, (mock, context) -> {
                    when(mock.send(any(PDU.class), any(Target.class))).thenReturn(responseEvent);
                    doNothing().when(mock).close();
                })) {

            String result = service.getSnmpV2Value(ipAddress, community, oid);

            assertEquals(expectedValue, result);
        }
    }

    @Test
    public void testGetSnmpV2Value_NullResponse() throws Exception {
        String ipAddress = "192.168.1.100";
        String community = "public";
        String oid = "1.3.6.1.2.1.1.1.0";

        try (MockedConstruction<DefaultUdpTransportMapping> transportMock = mockConstruction(
                DefaultUdpTransportMapping.class,
                (mock, context) -> doNothing().when(mock).listen());
                MockedConstruction<Snmp> snmpMock = mockConstruction(Snmp.class, (mock, context) -> {
                    when(mock.send(any(PDU.class), any(Target.class))).thenReturn(null);
                    doNothing().when(mock).close();
                })) {

            assertThrows(RuntimeException.class, () -> {
                service.getSnmpV2Value(ipAddress, community, oid);
            });
        }
    }

    @Test
    public void testGetSnmpV2Value_NullResponsePdu() throws Exception {
        String ipAddress = "192.168.1.100";
        String community = "public";
        String oid = "1.3.6.1.2.1.1.1.0";

        when(responseEvent.getResponse()).thenReturn(null);

        try (MockedConstruction<DefaultUdpTransportMapping> transportMock = mockConstruction(
                DefaultUdpTransportMapping.class,
                (mock, context) -> doNothing().when(mock).listen());
                MockedConstruction<Snmp> snmpMock = mockConstruction(Snmp.class, (mock, context) -> {
                    when(mock.send(any(PDU.class), any(Target.class))).thenReturn(responseEvent);
                    doNothing().when(mock).close();
                })) {

            assertThrows(RuntimeException.class, () -> {
                service.getSnmpV2Value(ipAddress, community, oid);
            });
        }
    }

    @Test
    public void testGetSnmpV3Value_Success() throws Exception {
        String ip = "192.168.1.100";
        String username = "testUser";
        String authPass = "authPassword";
        String privPass = "privPassword";
        String oid = "1.3.6.1.4.1.1206.4.2.3.5.3.0";

        Variable expectedVariable = new Integer32(12345);
        VariableBinding vb = new VariableBinding(new OID(oid), expectedVariable);
        when(scopedResponsePdu.get(0)).thenReturn(vb);
        when(responseEvent.getResponse()).thenReturn(scopedResponsePdu);

        try (MockedConstruction<DefaultUdpTransportMapping> transportMock = mockConstruction(
                DefaultUdpTransportMapping.class,
                (mock, context) -> doNothing().when(mock).listen());
                MockedConstruction<Snmp> snmpMock = mockConstruction(Snmp.class, (mock, context) -> {
                    when(mock.send(any(ScopedPDU.class), any(Target.class))).thenReturn(responseEvent);
                    when(mock.getUSM()).thenReturn(mock(USM.class));
                    doNothing().when(mock).close();
                });
                MockedConstruction<USM> usmMock = mockConstruction(USM.class);
                MockedStatic<SecurityModels> securityModelsMock = mockStatic(SecurityModels.class);
                MockedStatic<SecurityProtocols> securityProtocolsMock = mockStatic(SecurityProtocols.class);
                MockedStatic<MPv3> mpv3Mock = mockStatic(MPv3.class)) {

            SecurityModels mockSecurityModels = mock(SecurityModels.class);
            SecurityProtocols mockSecurityProtocols = mock(SecurityProtocols.class);

            securityModelsMock.when(SecurityModels::getInstance).thenReturn(mockSecurityModels);
            securityProtocolsMock.when(SecurityProtocols::getInstance).thenReturn(mockSecurityProtocols);
            mpv3Mock.when(MPv3::createLocalEngineID).thenReturn(new byte[] { 1, 2, 3, 4, 5 });

            Variable result = service.getSnmpV3Value(ip, username, authPass, privPass, oid);

            assertNotNull(result);
            assertEquals(12345, result.toInt());
        }
    }

    @Test
    public void testGetSnmpV3Value_NullResponse() throws Exception {
        String ip = "192.168.1.100";
        String username = "testUser";
        String authPass = "authPassword";
        String privPass = "privPassword";
        String oid = "1.3.6.1.4.1.1206.4.2.3.5.3.0";

        try (MockedConstruction<DefaultUdpTransportMapping> transportMock = mockConstruction(
                DefaultUdpTransportMapping.class,
                (mock, context) -> doNothing().when(mock).listen());
                MockedConstruction<Snmp> snmpMock = mockConstruction(Snmp.class, (mock, context) -> {
                    when(mock.send(any(ScopedPDU.class), any(Target.class))).thenReturn(null);
                    when(mock.getUSM()).thenReturn(mock(USM.class));
                    doNothing().when(mock).close();
                });
                MockedConstruction<USM> usmMock = mockConstruction(USM.class);
                MockedStatic<SecurityModels> securityModelsMock = mockStatic(SecurityModels.class);
                MockedStatic<SecurityProtocols> securityProtocolsMock = mockStatic(SecurityProtocols.class);
                MockedStatic<MPv3> mpv3Mock = mockStatic(MPv3.class)) {

            SecurityModels mockSecurityModels = mock(SecurityModels.class);
            SecurityProtocols mockSecurityProtocols = mock(SecurityProtocols.class);

            securityModelsMock.when(SecurityModels::getInstance).thenReturn(mockSecurityModels);
            securityProtocolsMock.when(SecurityProtocols::getInstance).thenReturn(mockSecurityProtocols);
            mpv3Mock.when(MPv3::createLocalEngineID).thenReturn(new byte[] { 1, 2, 3, 4, 5 });

            Variable result = service.getSnmpV3Value(ip, username, authPass, privPass, oid);

            assertNull(result);
        }
    }

    @Test
    public void testGetSnmpV3Value_NullResponsePdu() throws Exception {
        String ip = "192.168.1.100";
        String username = "testUser";
        String authPass = "authPassword";
        String privPass = "privPassword";
        String oid = "1.3.6.1.4.1.1206.4.2.3.5.3.0";

        when(responseEvent.getResponse()).thenReturn(null);

        try (MockedConstruction<DefaultUdpTransportMapping> transportMock = mockConstruction(
                DefaultUdpTransportMapping.class,
                (mock, context) -> doNothing().when(mock).listen());
                MockedConstruction<Snmp> snmpMock = mockConstruction(Snmp.class, (mock, context) -> {
                    when(mock.send(any(ScopedPDU.class), any(Target.class))).thenReturn(responseEvent);
                    when(mock.getUSM()).thenReturn(mock(USM.class));
                    doNothing().when(mock).close();
                });
                MockedConstruction<USM> usmMock = mockConstruction(USM.class);
                MockedStatic<SecurityModels> securityModelsMock = mockStatic(SecurityModels.class);
                MockedStatic<SecurityProtocols> securityProtocolsMock = mockStatic(SecurityProtocols.class);
                MockedStatic<MPv3> mpv3Mock = mockStatic(MPv3.class)) {

            SecurityModels mockSecurityModels = mock(SecurityModels.class);
            SecurityProtocols mockSecurityProtocols = mock(SecurityProtocols.class);

            securityModelsMock.when(SecurityModels::getInstance).thenReturn(mockSecurityModels);
            securityProtocolsMock.when(SecurityProtocols::getInstance).thenReturn(mockSecurityProtocols);
            mpv3Mock.when(MPv3::createLocalEngineID).thenReturn(new byte[] { 1, 2, 3, 4, 5 });

            Variable result = service.getSnmpV3Value(ip, username, authPass, privPass, oid);

            assertNull(result);
        }
    }

    @Test
    public void testGetSnmpV3Value_IOException() throws Exception {
        String ip = "192.168.1.100";
        String username = "testUser";
        String authPass = "authPassword";
        String privPass = "privPassword";
        String oid = "1.3.6.1.4.1.1206.4.2.3.5.3.0";

        try (MockedConstruction<DefaultUdpTransportMapping> transportMock = mockConstruction(
                DefaultUdpTransportMapping.class,
                (mock, context) -> doThrow(new IOException("Network error")).when(mock).listen());
                MockedConstruction<Snmp> snmpMock = mockConstruction(Snmp.class);
                MockedConstruction<USM> usmMock = mockConstruction(USM.class);
                MockedStatic<SecurityModels> securityModelsMock = mockStatic(SecurityModels.class);
                MockedStatic<SecurityProtocols> securityProtocolsMock = mockStatic(SecurityProtocols.class);
                MockedStatic<MPv3> mpv3Mock = mockStatic(MPv3.class)) {

            SecurityModels mockSecurityModels = mock(SecurityModels.class);
            SecurityProtocols mockSecurityProtocols = mock(SecurityProtocols.class);

            securityModelsMock.when(SecurityModels::getInstance).thenReturn(mockSecurityModels);
            securityProtocolsMock.when(SecurityProtocols::getInstance).thenReturn(mockSecurityProtocols);
            mpv3Mock.when(MPv3::createLocalEngineID).thenReturn(new byte[] { 1, 2, 3, 4, 5 });

            assertThrows(IOException.class, () -> {
                service.getSnmpV3Value(ip, username, authPass, privPass, oid);
            });
        }
    }

    @Test
    public void testSetSnmpV3Value_Success() throws Exception {
        String ipAddress = "192.168.1.100";
        String username = "testUser";
        String authPass = "authPassword";
        String oid = "1.3.6.1.4.1.1206.4.2.3.5.5.0";
        int intValue = 42;

        when(scopedResponsePdu.getErrorStatus()).thenReturn(PDU.noError);
        when(responseEvent.getResponse()).thenReturn(scopedResponsePdu);

        try (MockedConstruction<DefaultUdpTransportMapping> transportMock = mockConstruction(
                DefaultUdpTransportMapping.class,
                (mock, context) -> doNothing().when(mock).listen());
                MockedConstruction<Snmp> snmpMock = mockConstruction(Snmp.class, (mock, context) -> {
                    when(mock.send(any(ScopedPDU.class), any(Target.class))).thenReturn(responseEvent);
                    when(mock.getUSM()).thenReturn(mock(USM.class));
                    doNothing().when(mock).close();
                });
                MockedConstruction<USM> usmMock = mockConstruction(USM.class);
                MockedStatic<SecurityModels> securityModelsMock = mockStatic(SecurityModels.class);
                MockedStatic<SecurityProtocols> securityProtocolsMock = mockStatic(SecurityProtocols.class);
                MockedStatic<MPv3> mpv3Mock = mockStatic(MPv3.class)) {

            SecurityModels mockSecurityModels = mock(SecurityModels.class);
            SecurityProtocols mockSecurityProtocols = mock(SecurityProtocols.class);

            securityModelsMock.when(SecurityModels::getInstance).thenReturn(mockSecurityModels);
            securityProtocolsMock.when(SecurityProtocols::getInstance).thenReturn(mockSecurityProtocols);
            mpv3Mock.when(MPv3::createLocalEngineID).thenReturn(new byte[] { 1, 2, 3, 4, 5 });

            service.setSnmpV3Value(ipAddress, username, authPass, oid, intValue);

            assertTrue(true);
        }
    }

    @Test
    public void testSetSnmpV3Value_NullResponse() throws Exception {
        String ipAddress = "192.168.1.100";
        String username = "testUser";
        String authPass = "authPassword";
        String oid = "1.3.6.1.4.1.1206.4.2.3.5.5.0";
        int intValue = 42;

        try (MockedConstruction<DefaultUdpTransportMapping> transportMock = mockConstruction(
                DefaultUdpTransportMapping.class,
                (mock, context) -> doNothing().when(mock).listen());
                MockedConstruction<Snmp> snmpMock = mockConstruction(Snmp.class, (mock, context) -> {
                    when(mock.send(any(ScopedPDU.class), any(Target.class))).thenReturn(null);
                    when(mock.getUSM()).thenReturn(mock(USM.class));
                    doNothing().when(mock).close();
                });
                MockedConstruction<USM> usmMock = mockConstruction(USM.class);
                MockedStatic<SecurityModels> securityModelsMock = mockStatic(SecurityModels.class);
                MockedStatic<SecurityProtocols> securityProtocolsMock = mockStatic(SecurityProtocols.class);
                MockedStatic<MPv3> mpv3Mock = mockStatic(MPv3.class)) {

            SecurityModels mockSecurityModels = mock(SecurityModels.class);
            SecurityProtocols mockSecurityProtocols = mock(SecurityProtocols.class);

            securityModelsMock.when(SecurityModels::getInstance).thenReturn(mockSecurityModels);
            securityProtocolsMock.when(SecurityProtocols::getInstance).thenReturn(mockSecurityProtocols);
            mpv3Mock.when(MPv3::createLocalEngineID).thenReturn(new byte[] { 1, 2, 3, 4, 5 });

            assertThrows(NullPointerException.class, () -> {
                service.setSnmpV3Value(ipAddress, username, authPass, oid, intValue);
            });
        }
    }

    @Test
    public void testSetSnmpV3Value_ErrorStatus() throws Exception {
        String ipAddress = "192.168.1.100";
        String username = "testUser";
        String authPass = "authPassword";
        String oid = "1.3.6.1.4.1.1206.4.2.3.5.5.0";
        int intValue = 42;

        when(scopedResponsePdu.getErrorStatus()).thenReturn(PDU.genErr);
        when(responseEvent.getResponse()).thenReturn(scopedResponsePdu);

        try (MockedConstruction<DefaultUdpTransportMapping> transportMock = mockConstruction(
                DefaultUdpTransportMapping.class,
                (mock, context) -> doNothing().when(mock).listen());
                MockedConstruction<Snmp> snmpMock = mockConstruction(Snmp.class, (mock, context) -> {
                    when(mock.send(any(ScopedPDU.class), any(Target.class))).thenReturn(responseEvent);
                    when(mock.getUSM()).thenReturn(mock(USM.class));
                    doNothing().when(mock).close();
                });
                MockedConstruction<USM> usmMock = mockConstruction(USM.class);
                MockedStatic<SecurityModels> securityModelsMock = mockStatic(SecurityModels.class);
                MockedStatic<SecurityProtocols> securityProtocolsMock = mockStatic(SecurityProtocols.class);
                MockedStatic<MPv3> mpv3Mock = mockStatic(MPv3.class)) {

            SecurityModels mockSecurityModels = mock(SecurityModels.class);
            SecurityProtocols mockSecurityProtocols = mock(SecurityProtocols.class);

            securityModelsMock.when(SecurityModels::getInstance).thenReturn(mockSecurityModels);
            securityProtocolsMock.when(SecurityProtocols::getInstance).thenReturn(mockSecurityProtocols);
            mpv3Mock.when(MPv3::createLocalEngineID).thenReturn(new byte[] { 1, 2, 3, 4, 5 });

            assertDoesNotThrow(() -> {
                service.setSnmpV3Value(ipAddress, username, authPass, oid, intValue);
            });
        }
    }

    @Test
    public void testSetSnmpV3Values_Success() throws Exception {
        String ipAddress = "192.168.1.100";
        String username = "testUser";
        String authPass = "authPassword";
        Map<String, Variable> oidValuePairs = new HashMap<>();
        oidValuePairs.put("1.3.6.1.4.1.1206.4.2.3.5.5.0", new Integer32(1));
        oidValuePairs.put("1.3.6.1.4.1.1206.4.2.3.5.6.0", new Integer32(2));
        oidValuePairs.put("1.3.6.1.4.1.1206.4.2.3.5.7.0", new OctetString("test"));

        when(scopedResponsePdu.getErrorStatus()).thenReturn(PDU.noError);
        when(responseEvent.getResponse()).thenReturn(scopedResponsePdu);

        try (MockedConstruction<DefaultUdpTransportMapping> transportMock = mockConstruction(
                DefaultUdpTransportMapping.class,
                (mock, context) -> doNothing().when(mock).listen());
                MockedConstruction<Snmp> snmpMock = mockConstruction(Snmp.class, (mock, context) -> {
                    when(mock.send(any(ScopedPDU.class), any(Target.class))).thenReturn(responseEvent);
                    when(mock.getUSM()).thenReturn(mock(USM.class));
                    doNothing().when(mock).close();
                });
                MockedConstruction<USM> usmMock = mockConstruction(USM.class);
                MockedStatic<SecurityModels> securityModelsMock = mockStatic(SecurityModels.class);
                MockedStatic<SecurityProtocols> securityProtocolsMock = mockStatic(SecurityProtocols.class);
                MockedStatic<MPv3> mpv3Mock = mockStatic(MPv3.class)) {

            SecurityModels mockSecurityModels = mock(SecurityModels.class);
            SecurityProtocols mockSecurityProtocols = mock(SecurityProtocols.class);

            securityModelsMock.when(SecurityModels::getInstance).thenReturn(mockSecurityModels);
            securityProtocolsMock.when(SecurityProtocols::getInstance).thenReturn(mockSecurityProtocols);
            mpv3Mock.when(MPv3::createLocalEngineID).thenReturn(new byte[] { 1, 2, 3, 4, 5 });

            service.setSnmpV3Values(ipAddress, username, authPass, oidValuePairs);

            assertTrue(true);
        }
    }

    @Test
    public void testSetSnmpV3Values_EmptyMap() throws Exception {
        String ipAddress = "192.168.1.100";
        String username = "testUser";
        String authPass = "authPassword";
        Map<String, Variable> oidValuePairs = new HashMap<>();

        when(scopedResponsePdu.getErrorStatus()).thenReturn(PDU.noError);
        when(responseEvent.getResponse()).thenReturn(scopedResponsePdu);

        try (MockedConstruction<DefaultUdpTransportMapping> transportMock = mockConstruction(
                DefaultUdpTransportMapping.class,
                (mock, context) -> doNothing().when(mock).listen());
                MockedConstruction<Snmp> snmpMock = mockConstruction(Snmp.class, (mock, context) -> {
                    when(mock.send(any(ScopedPDU.class), any(Target.class))).thenReturn(responseEvent);
                    when(mock.getUSM()).thenReturn(mock(USM.class));
                    doNothing().when(mock).close();
                });
                MockedConstruction<USM> usmMock = mockConstruction(USM.class);
                MockedStatic<SecurityModels> securityModelsMock = mockStatic(SecurityModels.class);
                MockedStatic<SecurityProtocols> securityProtocolsMock = mockStatic(SecurityProtocols.class);
                MockedStatic<MPv3> mpv3Mock = mockStatic(MPv3.class)) {

            SecurityModels mockSecurityModels = mock(SecurityModels.class);
            SecurityProtocols mockSecurityProtocols = mock(SecurityProtocols.class);

            securityModelsMock.when(SecurityModels::getInstance).thenReturn(mockSecurityModels);
            securityProtocolsMock.when(SecurityProtocols::getInstance).thenReturn(mockSecurityProtocols);
            mpv3Mock.when(MPv3::createLocalEngineID).thenReturn(new byte[] { 1, 2, 3, 4, 5 });

            service.setSnmpV3Values(ipAddress, username, authPass, oidValuePairs);

            assertTrue(true);
        }
    }

    @Test
    public void testSetSnmpV3Values_NullResponse() throws Exception {
        String ipAddress = "192.168.1.100";
        String username = "testUser";
        String authPass = "authPassword";
        Map<String, Variable> oidValuePairs = new HashMap<>();
        oidValuePairs.put("1.3.6.1.4.1.1206.4.2.3.5.5.0", new Integer32(1));

        try (MockedConstruction<DefaultUdpTransportMapping> transportMock = mockConstruction(
                DefaultUdpTransportMapping.class,
                (mock, context) -> doNothing().when(mock).listen());
                MockedConstruction<Snmp> snmpMock = mockConstruction(Snmp.class, (mock, context) -> {
                    when(mock.send(any(ScopedPDU.class), any(Target.class))).thenReturn(null);
                    when(mock.getUSM()).thenReturn(mock(USM.class));
                    doNothing().when(mock).close();
                });
                MockedConstruction<USM> usmMock = mockConstruction(USM.class);
                MockedStatic<SecurityModels> securityModelsMock = mockStatic(SecurityModels.class);
                MockedStatic<SecurityProtocols> securityProtocolsMock = mockStatic(SecurityProtocols.class);
                MockedStatic<MPv3> mpv3Mock = mockStatic(MPv3.class)) {

            SecurityModels mockSecurityModels = mock(SecurityModels.class);
            SecurityProtocols mockSecurityProtocols = mock(SecurityProtocols.class);

            securityModelsMock.when(SecurityModels::getInstance).thenReturn(mockSecurityModels);
            securityProtocolsMock.when(SecurityProtocols::getInstance).thenReturn(mockSecurityProtocols);
            mpv3Mock.when(MPv3::createLocalEngineID).thenReturn(new byte[] { 1, 2, 3, 4, 5 });

            assertThrows(NullPointerException.class, () -> {
                service.setSnmpV3Values(ipAddress, username, authPass, oidValuePairs);
            });
        }
    }

    @Test
    public void testSetSnmpV3Values_ErrorStatus() throws Exception {
        String ipAddress = "192.168.1.100";
        String username = "testUser";
        String authPass = "authPassword";
        Map<String, Variable> oidValuePairs = new HashMap<>();
        oidValuePairs.put("1.3.6.1.4.1.1206.4.2.3.5.5.0", new Integer32(1));

        when(scopedResponsePdu.getErrorStatus()).thenReturn(PDU.badValue);
        when(scopedResponsePdu.getErrorStatusText()).thenReturn("Bad value");
        when(responseEvent.getResponse()).thenReturn(scopedResponsePdu);

        try (MockedConstruction<DefaultUdpTransportMapping> transportMock = mockConstruction(
                DefaultUdpTransportMapping.class,
                (mock, context) -> doNothing().when(mock).listen());
                MockedConstruction<Snmp> snmpMock = mockConstruction(Snmp.class, (mock, context) -> {
                    when(mock.send(any(ScopedPDU.class), any(Target.class))).thenReturn(responseEvent);
                    when(mock.getUSM()).thenReturn(mock(USM.class));
                    doNothing().when(mock).close();
                });
                MockedConstruction<USM> usmMock = mockConstruction(USM.class);
                MockedStatic<SecurityModels> securityModelsMock = mockStatic(SecurityModels.class);
                MockedStatic<SecurityProtocols> securityProtocolsMock = mockStatic(SecurityProtocols.class);
                MockedStatic<MPv3> mpv3Mock = mockStatic(MPv3.class)) {

            SecurityModels mockSecurityModels = mock(SecurityModels.class);
            SecurityProtocols mockSecurityProtocols = mock(SecurityProtocols.class);

            securityModelsMock.when(SecurityModels::getInstance).thenReturn(mockSecurityModels);
            securityProtocolsMock.when(SecurityProtocols::getInstance).thenReturn(mockSecurityProtocols);
            mpv3Mock.when(MPv3::createLocalEngineID).thenReturn(new byte[] { 1, 2, 3, 4, 5 });

            // should log warning but not throw exception
            assertDoesNotThrow(() -> {
                service.setSnmpV3Values(ipAddress, username, authPass, oidValuePairs);
            });
        }
    }

    @Test
    public void testConstructor() {
        SNMPService newService = new SNMPService(snmpProperties);
        assertNotNull(newService);
    }
}
