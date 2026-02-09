package us.dot.its.jpo.ode.api.controllers.organizations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import us.dot.its.jpo.ode.api.repositories.RsuOrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationControllerTest {

    @Mock
    private RsuRepository rsuRepository;

    @Mock
    private RsuOrganizationRepository rsuOrganizationRepository;

    @InjectMocks
    private OrganizationController organizationController;

    // ==================== GET RSU IPS BY ORGANIZATION TESTS ====================

    @Test
    void testGetRsuIpsByOrganization_Success() throws UnknownHostException {
        // Arrange
        String organization = "TestOrg";

        List<InetAddress> mockIpAddresses = Arrays.asList(
                InetAddress.getByName("192.168.1.100"),
                InetAddress.getByName("192.168.1.101"),
                InetAddress.getByName("192.168.1.102"));

        when(rsuOrganizationRepository.findAllRsuIpsByOrganizationName(organization))
                .thenReturn(mockIpAddresses);

        // Act
        List<String> result = organizationController.getRsuIpsByOrganization(organization);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("192.168.1.100", result.get(0));
        assertEquals("192.168.1.101", result.get(1));
        assertEquals("192.168.1.102", result.get(2));

        verify(rsuOrganizationRepository).findAllRsuIpsByOrganizationName(organization);
    }

    @Test
    void testGetRsuIpsByOrganization_EmptyResult() {
        // Arrange
        String organization = "EmptyOrg";

        when(rsuOrganizationRepository.findAllRsuIpsByOrganizationName(organization))
                .thenReturn(Arrays.asList());

        // Act
        List<String> result = organizationController.getRsuIpsByOrganization(organization);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(rsuOrganizationRepository).findAllRsuIpsByOrganizationName(organization);
    }

    @Test
    void testGetRsuIpsByOrganization_SingleRsu() throws UnknownHostException {
        // Arrange
        String organization = "SingleRsuOrg";

        List<InetAddress> mockIpAddresses = Arrays.asList(
                InetAddress.getByName("192.168.1.100"));

        when(rsuOrganizationRepository.findAllRsuIpsByOrganizationName(organization))
                .thenReturn(mockIpAddresses);

        // Act
        List<String> result = organizationController.getRsuIpsByOrganization(organization);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("192.168.1.100", result.get(0));

        verify(rsuOrganizationRepository).findAllRsuIpsByOrganizationName(organization);
    }

    @Test
    void testGetRsuIpsByOrganization_MultiplePages() throws UnknownHostException {
        // Arrange
        String organization = "LargeOrg";

        List<InetAddress> mockIpAddresses = Arrays.asList(
                InetAddress.getByName("192.168.1.1"),
                InetAddress.getByName("192.168.1.2"),
                InetAddress.getByName("192.168.1.3"),
                InetAddress.getByName("192.168.1.4"),
                InetAddress.getByName("192.168.1.5"));

        when(rsuOrganizationRepository.findAllRsuIpsByOrganizationName(organization))
                .thenReturn(mockIpAddresses);

        // Act
        List<String> result = organizationController.getRsuIpsByOrganization(organization);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());

        verify(rsuOrganizationRepository).findAllRsuIpsByOrganizationName(organization);
    }

    @Test
    void testGetRsuIpsByOrganization_IpAddressFormatting() throws UnknownHostException {
        // Arrange
        String organization = "TestOrg";

        List<InetAddress> mockIpAddresses = Arrays.asList(
                InetAddress.getByName("10.0.0.1"),
                InetAddress.getByName("172.16.0.1"),
                InetAddress.getByName("192.168.0.1"));

        when(rsuOrganizationRepository.findAllRsuIpsByOrganizationName(organization))
                .thenReturn(mockIpAddresses);

        // Act
        List<String> result = organizationController.getRsuIpsByOrganization(organization);

        // Assert
        assertEquals("10.0.0.1", result.get(0));
        assertEquals("172.16.0.1", result.get(1));
        assertEquals("192.168.0.1", result.get(2));
    }

    // ==================== GET RSU ORGANIZATION ASSIGNMENTS TESTS
    // ====================

    @Test
    void testGetRsuOrganizationAssignments_Success() throws UnknownHostException {
        // Arrange
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        List<String> mockOrganizations = Arrays.asList("Org1", "Org2", "Org3");

        when(rsuRepository.findAllOrganizationNamesByIpv4Address(inetAddress))
                .thenReturn(mockOrganizations);

        // Act
        List<String> result = organizationController.getRsuOrganizationAssignments(rsuIp);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Org1", result.get(0));
        assertEquals("Org2", result.get(1));
        assertEquals("Org3", result.get(2));

        verify(rsuRepository).findAllOrganizationNamesByIpv4Address(inetAddress);
    }

    @Test
    void testGetRsuOrganizationAssignments_SingleOrganization() throws UnknownHostException {
        // Arrange
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        List<String> mockOrganizations = Arrays.asList("OnlyOrg");

        when(rsuRepository.findAllOrganizationNamesByIpv4Address(inetAddress))
                .thenReturn(mockOrganizations);

        // Act
        List<String> result = organizationController.getRsuOrganizationAssignments(rsuIp);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("OnlyOrg", result.get(0));

        verify(rsuRepository).findAllOrganizationNamesByIpv4Address(inetAddress);
    }

    @Test
    void testGetRsuOrganizationAssignments_NoOrganizations() throws UnknownHostException {
        // Arrange
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        when(rsuRepository.findAllOrganizationNamesByIpv4Address(inetAddress))
                .thenReturn(Arrays.asList());

        // Act
        List<String> result = organizationController.getRsuOrganizationAssignments(rsuIp);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(rsuRepository).findAllOrganizationNamesByIpv4Address(inetAddress);
    }

    @Test
    void testGetRsuOrganizationAssignments_InvalidIpAddress() {
        // Arrange
        String invalidIp = "invalid-ip-address";

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> organizationController.getRsuOrganizationAssignments(invalidIp));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid RSU IP address"));
        assertTrue(exception.getReason().contains(invalidIp));

        verify(rsuRepository, never()).findAllOrganizationNamesByIpv4Address(any());
    }

    @Test
    void testGetRsuOrganizationAssignments_MalformedIpAddress() {
        // Arrange
        String malformedIp = "999.999.999.999";

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> organizationController.getRsuOrganizationAssignments(malformedIp));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid RSU IP address"));

        verify(rsuRepository, never()).findAllOrganizationNamesByIpv4Address(any());
    }

    @Test
    void testGetRsuOrganizationAssignments_IpWithHostname() {
        // Arrange
        String hostnameIp = "localhost";

        // Act & Assert - This should work as InetAddress.getByName() can resolve
        // hostnames
        // But in practice, you might want to validate IP format before calling the
        // method
        assertDoesNotThrow(() -> {
            organizationController.getRsuOrganizationAssignments(hostnameIp);
        });
    }

    @Test
    void testGetRsuOrganizationAssignments_IPv6Address() throws UnknownHostException {
        // Arrange
        String ipv6Address = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        InetAddress inetAddress = InetAddress.getByName(ipv6Address);

        List<String> mockOrganizations = Arrays.asList("Org1");

        when(rsuRepository.findAllOrganizationNamesByIpv4Address(inetAddress))
                .thenReturn(mockOrganizations);

        // Act
        List<String> result = organizationController.getRsuOrganizationAssignments(ipv6Address);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(rsuRepository).findAllOrganizationNamesByIpv4Address(inetAddress);
    }

    @Test
    void testGetRsuOrganizationAssignments_DifferentIpRanges() throws UnknownHostException {
        // Arrange
        String[] testIps = {
                "10.0.0.1",
                "172.16.0.1",
                "192.168.1.1",
                "8.8.8.8"
        };

        for (String ip : testIps) {
            InetAddress inetAddress = InetAddress.getByName(ip);
            when(rsuRepository.findAllOrganizationNamesByIpv4Address(inetAddress))
                    .thenReturn(Arrays.asList("TestOrg"));

            // Act
            List<String> result = organizationController.getRsuOrganizationAssignments(ip);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("TestOrg", result.get(0));
        }

        verify(rsuRepository, times(4)).findAllOrganizationNamesByIpv4Address(any());
    }

    @Test
    void testGetRsuOrganizationAssignments_RepositoryThrowsException() throws UnknownHostException {
        // Arrange
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        when(rsuRepository.findAllOrganizationNamesByIpv4Address(inetAddress))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> organizationController.getRsuOrganizationAssignments(rsuIp));

        verify(rsuRepository).findAllOrganizationNamesByIpv4Address(inetAddress);
    }
}