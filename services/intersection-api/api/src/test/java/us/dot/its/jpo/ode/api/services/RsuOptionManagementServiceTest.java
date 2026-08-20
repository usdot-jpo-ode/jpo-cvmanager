package us.dot.its.jpo.ode.api.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import us.dot.its.jpo.ode.api.models.devices.management.RsuPatch;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuOption;
import us.dot.its.jpo.ode.api.repositories.RsuOptionRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RsuOptionManagementServiceTest {

    @Mock
    private RsuRepository rsuRepository;

    @Mock
    private RsuOptionRepository rsuOptionRepository;

    @InjectMocks
    private RsuOptionManagementService rsuOptionManagementService;

    // ==================== CREATE NEW RSU OPTION TESTS ====================

    @Test
    void testModifyRsuOption_CreateNewRsuOption_BothFields() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setTimDeposit(true);
        patch.setSnmpMonitoring(true);

        Rsu existingRsu = new Rsu();
        existingRsu.setId(1);
        existingRsu.setIpv4Address(inetAddress);

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuOptionRepository.findByRsuId(1)).thenReturn(Optional.empty());

        rsuOptionManagementService.modifyRsuOption(rsuIp, patch);

        // Verify that RsuOption was created and saved with correct values
        verify(rsuOptionRepository).findByRsuId(1);

        ArgumentCaptor<RsuOption> optionCaptor = ArgumentCaptor.forClass(RsuOption.class);
        verify(rsuOptionRepository).save(optionCaptor.capture());

        RsuOption savedOption = optionCaptor.getValue();
        assertNotNull(savedOption);
        assertEquals(true, savedOption.getTimDeposit());
        assertEquals(true, savedOption.getSnmpMonitoring());
        assertEquals(existingRsu, savedOption.getRsu());
    }

    @Test
    void testModifyRsuOption_CreateNewRsuOption_TimDepositOnly() throws UnknownHostException {
        String rsuIp = "192.168.1.101";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setTimDeposit(true);
        // snmpMonitoring is not set

        Rsu existingRsu = new Rsu();
        existingRsu.setId(2);
        existingRsu.setIpv4Address(inetAddress);

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuOptionRepository.findByRsuId(2)).thenReturn(Optional.empty());

        rsuOptionManagementService.modifyRsuOption(rsuIp, patch);

        // Verify that RsuOption was created with tim_deposit set and default for snmp_monitoring
        verify(rsuOptionRepository).findByRsuId(2);

        ArgumentCaptor<RsuOption> optionCaptor = ArgumentCaptor.forClass(RsuOption.class);
        verify(rsuOptionRepository).save(optionCaptor.capture());

        RsuOption savedOption = optionCaptor.getValue();
        assertNotNull(savedOption);
        assertEquals(true, savedOption.getTimDeposit());
        assertEquals(false, savedOption.getSnmpMonitoring()); // default value
        assertEquals(existingRsu, savedOption.getRsu());
    }

    @Test
    void testModifyRsuOption_CreateNewRsuOption_SnmpMonitoringOnly() throws UnknownHostException {
        String rsuIp = "192.168.1.102";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setSnmpMonitoring(true);
        // timDeposit is not set

        Rsu existingRsu = new Rsu();
        existingRsu.setId(3);
        existingRsu.setIpv4Address(inetAddress);

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuOptionRepository.findByRsuId(3)).thenReturn(Optional.empty());

        rsuOptionManagementService.modifyRsuOption(rsuIp, patch);

        // Verify that RsuOption was created with snmp_monitoring set and default for tim_deposit
        verify(rsuOptionRepository).findByRsuId(3);

        ArgumentCaptor<RsuOption> optionCaptor = ArgumentCaptor.forClass(RsuOption.class);
        verify(rsuOptionRepository).save(optionCaptor.capture());

        RsuOption savedOption = optionCaptor.getValue();
        assertNotNull(savedOption);
        assertEquals(false, savedOption.getTimDeposit()); // default value
        assertEquals(true, savedOption.getSnmpMonitoring());
        assertEquals(existingRsu, savedOption.getRsu());
    }

    // ==================== UPDATE EXISTING RSU OPTION TESTS ====================

    @Test
    void testModifyRsuOption_UpdateExistingOption_BothFields() throws UnknownHostException {
        String rsuIp = "192.168.1.103";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setTimDeposit(false);
        patch.setSnmpMonitoring(true);

        Rsu existingRsu = new Rsu();
        existingRsu.setId(4);
        existingRsu.setIpv4Address(inetAddress);

        RsuOption existingOption = new RsuOption();
        existingOption.setId(4);
        existingOption.setRsu(existingRsu);
        existingOption.setTimDeposit(true); // old value
        existingOption.setSnmpMonitoring(false); // old value

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuOptionRepository.findByRsuId(4)).thenReturn(Optional.of(existingOption));

        rsuOptionManagementService.modifyRsuOption(rsuIp, patch);

        // Verify that existing RsuOption was updated
        verify(rsuOptionRepository).findByRsuId(4);
        verify(rsuOptionRepository).save(existingOption);
        assertEquals(false, existingOption.getTimDeposit());
        assertEquals(true, existingOption.getSnmpMonitoring());
    }

    @Test
    void testModifyRsuOption_UpdateExistingOption_TimDepositOnly() throws UnknownHostException {
        String rsuIp = "192.168.1.104";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setTimDeposit(false);
        // snmpMonitoring is not set, should not change existing value

        Rsu existingRsu = new Rsu();
        existingRsu.setId(5);
        existingRsu.setIpv4Address(inetAddress);

        RsuOption existingOption = new RsuOption();
        existingOption.setId(5);
        existingOption.setRsu(existingRsu);
        existingOption.setTimDeposit(true); // should be updated
        existingOption.setSnmpMonitoring(true); // should remain unchanged

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuOptionRepository.findByRsuId(5)).thenReturn(Optional.of(existingOption));

        rsuOptionManagementService.modifyRsuOption(rsuIp, patch);

        // Verify that only timDeposit was updated
        verify(rsuOptionRepository).findByRsuId(5);
        verify(rsuOptionRepository).save(existingOption);
        assertEquals(false, existingOption.getTimDeposit()); // updated
        assertEquals(true, existingOption.getSnmpMonitoring()); // unchanged
    }

    @Test
    void testModifyRsuOption_UpdateExistingOption_SnmpMonitoringOnly() throws UnknownHostException {
        String rsuIp = "192.168.1.105";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setSnmpMonitoring(false);
        // timDeposit is not set, should not change existing value

        Rsu existingRsu = new Rsu();
        existingRsu.setId(6);
        existingRsu.setIpv4Address(inetAddress);

        RsuOption existingOption = new RsuOption();
        existingOption.setId(6);
        existingOption.setRsu(existingRsu);
        existingOption.setTimDeposit(true); // should remain unchanged
        existingOption.setSnmpMonitoring(true); // should be updated

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuOptionRepository.findByRsuId(6)).thenReturn(Optional.of(existingOption));

        rsuOptionManagementService.modifyRsuOption(rsuIp, patch);

        // Verify that only snmpMonitoring was updated
        verify(rsuOptionRepository).findByRsuId(6);
        verify(rsuOptionRepository).save(existingOption);
        assertEquals(true, existingOption.getTimDeposit()); // unchanged
        assertEquals(false, existingOption.getSnmpMonitoring()); // updated
    }

    @Test
    void testModifyRsuOption_UpdateExistingOption_NoChanges() throws UnknownHostException {
        String rsuIp = "192.168.1.106";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setTimDeposit(true);
        patch.setSnmpMonitoring(false);

        Rsu existingRsu = new Rsu();
        existingRsu.setId(7);
        existingRsu.setIpv4Address(inetAddress);

        RsuOption existingOption = new RsuOption();
        existingOption.setId(7);
        existingOption.setRsu(existingRsu);
        existingOption.setTimDeposit(true); // same as patch
        existingOption.setSnmpMonitoring(false); // same as patch

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuOptionRepository.findByRsuId(7)).thenReturn(Optional.of(existingOption));

        rsuOptionManagementService.modifyRsuOption(rsuIp, patch);

        // Verify that no save occurred since values are unchanged
        verify(rsuOptionRepository).findByRsuId(7);
        verify(rsuOptionRepository, never()).save(any());
    }

    // ==================== EARLY RETURN TESTS ====================

    @Test
    void testModifyRsuOption_NoOptionsProvided_EarlyReturn() throws UnknownHostException {
        String rsuIp = "192.168.1.107";

        RsuPatch patch = new RsuPatch();
        // No timDeposit or snmpMonitoring set

        rsuOptionManagementService.modifyRsuOption(rsuIp, patch);

        // Verify that repositories were never accessed due to early return
        verify(rsuRepository, never()).findByIpv4Address(any());
        verify(rsuOptionRepository, never()).findByRsuId(any());
        verify(rsuOptionRepository, never()).save(any());
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    void testModifyRsuOption_InvalidIpAddress_ThrowsBadRequest() {
        String invalidIp = "invalid-ip";

        RsuPatch patch = new RsuPatch();
        patch.setTimDeposit(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            rsuOptionManagementService.modifyRsuOption(invalidIp, patch);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid IP address"));
        verify(rsuOptionRepository, never()).findByRsuId(any());
        verify(rsuOptionRepository, never()).save(any());
    }

    @Test
    void testModifyRsuOption_RsuNotFound_ThrowsNotFound() throws UnknownHostException {
        String rsuIp = "192.168.1.108";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setTimDeposit(true);

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            rsuOptionManagementService.modifyRsuOption(rsuIp, patch);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("RSU not found"));
        verify(rsuOptionRepository, never()).findByRsuId(any());
        verify(rsuOptionRepository, never()).save(any());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    void testModifyRsuOption_SetBothFieldsToFalse() throws UnknownHostException {
        String rsuIp = "192.168.1.109";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setTimDeposit(false);
        patch.setSnmpMonitoring(false);

        Rsu existingRsu = new Rsu();
        existingRsu.setId(9);
        existingRsu.setIpv4Address(inetAddress);

        RsuOption existingOption = new RsuOption();
        existingOption.setId(9);
        existingOption.setRsu(existingRsu);
        existingOption.setTimDeposit(true);
        existingOption.setSnmpMonitoring(true);

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuOptionRepository.findByRsuId(9)).thenReturn(Optional.of(existingOption));

        rsuOptionManagementService.modifyRsuOption(rsuIp, patch);

        // Verify that both fields were set to false
        verify(rsuOptionRepository).save(existingOption);
        assertEquals(false, existingOption.getTimDeposit());
        assertEquals(false, existingOption.getSnmpMonitoring());
    }

    @Test
    void testModifyRsuOption_ToggleValues() throws UnknownHostException {
        String rsuIp = "192.168.1.110";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setTimDeposit(false);
        patch.setSnmpMonitoring(true);

        Rsu existingRsu = new Rsu();
        existingRsu.setId(10);
        existingRsu.setIpv4Address(inetAddress);

        RsuOption existingOption = new RsuOption();
        existingOption.setId(10);
        existingOption.setRsu(existingRsu);
        existingOption.setTimDeposit(true);
        existingOption.setSnmpMonitoring(false);

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuOptionRepository.findByRsuId(10)).thenReturn(Optional.of(existingOption));

        rsuOptionManagementService.modifyRsuOption(rsuIp, patch);

        // Verify that values were toggled
        verify(rsuOptionRepository).save(existingOption);
        assertEquals(false, existingOption.getTimDeposit());
        assertEquals(true, existingOption.getSnmpMonitoring());
    }
}

