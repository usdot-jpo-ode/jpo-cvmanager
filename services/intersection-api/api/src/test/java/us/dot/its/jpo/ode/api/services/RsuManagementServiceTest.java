package us.dot.its.jpo.ode.api.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import us.dot.its.jpo.ode.api.mappers.RsuInfoMapper;
import us.dot.its.jpo.ode.api.mappers.RsuPatchMapper;
import us.dot.its.jpo.ode.api.models.devices.RsuInfoDto;
import us.dot.its.jpo.ode.api.models.devices.management.ModifyRsuAllowedSelections;
import us.dot.its.jpo.ode.api.models.devices.management.RsuPatch;
import us.dot.its.jpo.ode.api.models.keycloak.CvManagerAuthToken;
import us.dot.its.jpo.ode.api.models.SimplePosition;
import us.dot.its.jpo.ode.api.models.UserRole;
import us.dot.its.jpo.ode.api.models.postgres.tables.Organization;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuCredential;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuModel;
import us.dot.its.jpo.ode.api.models.postgres.tables.RsuOrganization;
import us.dot.its.jpo.ode.api.models.postgres.tables.SnmpCredential;
import us.dot.its.jpo.ode.api.models.postgres.tables.SnmpProtocol;
import us.dot.its.jpo.ode.api.repositories.ConsecutiveFirmwareUpgradeFailureRepository;
import us.dot.its.jpo.ode.api.repositories.MaxRetryLimitReachedInstanceRepository;
import us.dot.its.jpo.ode.api.repositories.OrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.PingRepository;
import us.dot.its.jpo.ode.api.repositories.RsuCredentialRepository;
import us.dot.its.jpo.ode.api.repositories.RsuIntersectionRepository;
import us.dot.its.jpo.ode.api.repositories.RsuOrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.RsuModelRepository;
import us.dot.its.jpo.ode.api.repositories.RsuOptionRepository;
import us.dot.its.jpo.ode.api.repositories.RsuRepository;
import us.dot.its.jpo.ode.api.repositories.ScmsHealthRepository;
import us.dot.its.jpo.ode.api.repositories.SnmpCredentialRepository;
import us.dot.its.jpo.ode.api.repositories.SnmpMsgfwdConfigRepository;
import us.dot.its.jpo.ode.api.repositories.SnmpProtocolRepository;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RsuManagementServiceTest {

    @Mock
    private ConsecutiveFirmwareUpgradeFailureRepository consecutiveFirmwareUpgradeFailureRepository;

    @Mock
    private MaxRetryLimitReachedInstanceRepository maxRetryLimitReachedInstanceRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private PermissionService permissionService;

    @Mock
    private PingRepository pingRepository;

    @Mock
    private RsuCredentialRepository rsuCredentialRepository;

    @Mock
    private RsuIntersectionRepository rsuIntersectionRepository;

    @Mock
    private RsuOrganizationRepository rsuOrganizationRepository;

    @Mock
    private RsuModelRepository rsuModelRepository;

    @Mock
    private RsuRepository rsuRepository;

    @Mock
    private RsuOptionRepository rsuOptionRepository;

    @Mock
    private ScmsHealthRepository scmsHealthRepository;

    @Mock
    private SnmpCredentialRepository snmpCredentialRepository;

    @Mock
    private SnmpMsgfwdConfigRepository snmpMsgfwdConfigRepository;

    @Mock
    private SnmpProtocolRepository snmpProtocolRepository;

    @Mock
    private RsuInfoMapper rsuMapper;

    @Mock
    private RsuPatchMapper rsuPatchMapper;

    @Mock
    private CvManagerAuthToken authToken;

    @InjectMocks
    private RsuManagementService rsuManagementService;

    // ==================== GET RSU INFO TESTS ====================

    @Test
    void testGetRsuInfo_Success() throws UnknownHostException {
        String ipAddress = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(ipAddress);

        Rsu mockRsu = new Rsu();
        RsuInfoDto mockDto = new RsuInfoDto(
                ipAddress,
                new SimplePosition(39.7392, -105.0844),
                123.4,
                "I-25",
                "RSU123",
                "SCMS123",
                "Model X",
                "ssh-group",
                "snmp-group",
                "v3",
                Arrays.asList("Org1", "Org2"),
                Boolean.TRUE,
                Boolean.TRUE);

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(mockRsu);
        when(rsuMapper.toDto(mockRsu)).thenReturn(mockDto);

        RsuInfoDto result = rsuManagementService.getRsuInfo(ipAddress);

        assertNotNull(result);
        assertEquals(ipAddress, result.getIpv4Address());
        assertEquals(123.4, result.getMilepost());
        assertEquals("I-25", result.getPrimaryRoute());
        verify(rsuRepository).findByIpv4Address(inetAddress);
        verify(rsuMapper).toDto(mockRsu);
    }

    @Test
    void testGetRsuInfo_NotFound() throws UnknownHostException {
        String ipAddress = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(ipAddress);

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(null);

        RsuInfoDto result = rsuManagementService.getRsuInfo(ipAddress);

        assertNull(result);
        verify(rsuRepository).findByIpv4Address(inetAddress);
        verify(rsuMapper, never()).toDto(any());
    }

    @Test
    void testGetRsuInfo_InvalidIpAddress() {
        String invalidIpAddress = "invalid-ip";

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> rsuManagementService.getRsuInfo(invalidIpAddress));

        assertTrue(exception.getMessage().contains("Invalid IP address"));
        assertInstanceOf(UnknownHostException.class, exception.getCause());
        verify(rsuRepository, never()).findByIpv4Address(any());
    }

    // ==================== GET ALL RSU INFO TESTS ====================

    @Test
    void testGetAllRsuInfo_Success() {
        String orgName = "TestOrg";
        String search = "Search Term";
        Pageable pageable = PageRequest.of(0, 10);

        Rsu rsu1 = new Rsu();
        Rsu rsu2 = new Rsu();
        List<Rsu> rsuList = Arrays.asList(rsu1, rsu2);
        Page<Rsu> rsuPage = new PageImpl<>(rsuList, pageable, 2);

        RsuInfoDto dto1 = new RsuInfoDto(
                "192.168.1.100",
                new SimplePosition(39.7392, -105.0844),
                123.4,
                "I-25",
                "RSU1",
                "SCMS1",
                "Model X",
                "ssh1",
                "snmp1",
                "v3",
                Arrays.asList("TestOrg"),
                Boolean.TRUE,
                Boolean.TRUE);
        RsuInfoDto dto2 = new RsuInfoDto(
                "192.168.1.101",
                new SimplePosition(39.7400, -105.0850),
                124.5,
                "I-70",
                "RSU2",
                "SCMS2",
                "Model Y",
                "ssh2",
                "snmp2",
                "v2c",
                Arrays.asList("TestOrg"),
                Boolean.TRUE,
                Boolean.TRUE);

        when(rsuRepository.findAllByOrganization(orgName, search, pageable)).thenReturn(rsuPage);
        when(rsuMapper.toDto(rsu1)).thenReturn(dto1);
        when(rsuMapper.toDto(rsu2)).thenReturn(dto2);

        Page<RsuInfoDto> result = rsuManagementService.getAllRsuInfo(orgName, search, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("192.168.1.100", result.getContent().get(0).getIpv4Address());
        assertEquals("192.168.1.101", result.getContent().get(1).getIpv4Address());
        verify(rsuRepository).findAllByOrganization(orgName, search, pageable);
        verify(rsuMapper, times(2)).toDto(any(Rsu.class));
    }

    @Test
    void testGetAllRsuInfo_EmptyResult() {
        String orgName = "EmptyOrg";
        String search = "Search Term";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Rsu> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(rsuRepository.findAllByOrganization(orgName, search, pageable)).thenReturn(emptyPage);

        Page<RsuInfoDto> result = rsuManagementService.getAllRsuInfo(orgName, search, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(rsuRepository).findAllByOrganization(orgName, search, pageable);
        verify(rsuMapper, never()).toDto(any());
    }

    // ==================== GET ALLOWED SELECTIONS TESTS ====================

    @Test
    void testGetAllowedSelections_Success() {

        List<String> primaryRoutes = Arrays.asList("I-25", "I-70", "US-36");

        List<RsuRepository.RsuModelProjection> rsuModels = Arrays.asList(
                createRsuModelProjection("Commsignia", "ITS-RS4-M"),
                createRsuModelProjection("Yunex", "RSU-2X"));

        List<String> sshCredentials = Arrays.asList("ssh-group-1", "ssh-group-2");
        List<String> snmpCredentials = Arrays.asList("snmp-group-1", "snmp-group-2");
        List<String> snmpVersions = Arrays.asList("v2c", "v3");

        when(rsuRepository.findAllPrimaryRoutes()).thenReturn(primaryRoutes);
        when(rsuRepository.findAllRsuModels()).thenReturn(rsuModels);
        when(rsuCredentialRepository.findAllNicknames()).thenReturn(sshCredentials);
        when(snmpCredentialRepository.findAllNicknames()).thenReturn(snmpCredentials);
        when(snmpProtocolRepository.findAllNicknames()).thenReturn(snmpVersions);
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(Arrays.asList("Org1", "Org2"));

        ModifyRsuAllowedSelections result = rsuManagementService.getAllowedSelections(authToken);

        assertNotNull(result);

        assertEquals(3, result.getPrimaryRoutes().size());
        assertTrue(result.getPrimaryRoutes().contains("I-25"));

        assertEquals(2, result.getRsuModels().size());
        assertTrue(result.getRsuModels().contains("Commsignia ITS-RS4-M"));
        assertTrue(result.getRsuModels().contains("Yunex RSU-2X"));

        assertEquals(2, result.getSshCredentialGroups().size());
        assertTrue(result.getSshCredentialGroups().contains("ssh-group-1"));

        assertEquals(2, result.getSnmpCredentialGroups().size());
        assertTrue(result.getSnmpCredentialGroups().contains("snmp-group-1"));

        assertEquals(2, result.getSnmpVersionGroups().size());
        assertTrue(result.getSnmpVersionGroups().contains("v2c"));

        assertEquals(2, result.getOrganizations().size());
        assertTrue(result.getOrganizations().contains("Org1"));
        assertTrue(result.getOrganizations().contains("Org2"));

        verify(rsuRepository).findAllPrimaryRoutes();
        verify(rsuRepository).findAllRsuModels();
        verify(rsuCredentialRepository).findAllNicknames();
        verify(snmpCredentialRepository).findAllNicknames();
        verify(snmpProtocolRepository).findAllNicknames();
    }

    @Test
    void testGetAllowedSelections_EmptyResults() {

        when(rsuRepository.findAllPrimaryRoutes()).thenReturn(List.of());
        when(rsuRepository.findAllRsuModels()).thenReturn(List.of());
        when(rsuCredentialRepository.findAllNicknames()).thenReturn(List.of());
        when(snmpCredentialRepository.findAllNicknames()).thenReturn(List.of());
        when(snmpProtocolRepository.findAllNicknames()).thenReturn(List.of());
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of());

        ModifyRsuAllowedSelections result = rsuManagementService.getAllowedSelections(authToken);

        assertNotNull(result);
        assertTrue(result.getPrimaryRoutes().isEmpty());
        assertTrue(result.getRsuModels().isEmpty());
        assertTrue(result.getSshCredentialGroups().isEmpty());
        assertTrue(result.getSnmpCredentialGroups().isEmpty());
        assertTrue(result.getSnmpVersionGroups().isEmpty());
        assertTrue(result.getOrganizations().isEmpty());
    }

    // ==================== MODIFY RSU TESTS ====================

    @Test
    void testModifyRsu_Success() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setMilepost(150.0);
        patch.setPrimaryRoute("I-70");

        Rsu existingRsu = new Rsu();
        existingRsu.setIpv4Address(inetAddress);
        existingRsu.setMilepost(123.4);
        existingRsu.setPrimaryRoute("I-25");
        existingRsu.setRsuOrganizations(new HashSet<>());

        RsuInfoDto expectedDto = new RsuInfoDto(
                rsuIp,
                new SimplePosition(39.7392, -105.0844),
                150.0,
                "I-70",
                "RSU123",
                "SCMS123",
                "Model X",
                "ssh-group",
                "snmp-group",
                "v3",
                Arrays.asList("Org1"),
                Boolean.TRUE,
                Boolean.TRUE);

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        doNothing().when(rsuPatchMapper).updateRsuFromPatch(patch, existingRsu);
        when(rsuRepository.save(existingRsu)).thenReturn(existingRsu);
        when(rsuMapper.toDto(existingRsu)).thenReturn(expectedDto);
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of("Org1"));

        RsuInfoDto result = rsuManagementService.modifyRsu(rsuIp, patch, authToken);

        assertNotNull(result);
        assertEquals(150.0, result.getMilepost());
        assertEquals("I-70", result.getPrimaryRoute());
        verify(rsuRepository).findByIpv4Address(inetAddress);
        verify(rsuPatchMapper).updateRsuFromPatch(patch, existingRsu);
        verify(rsuRepository).save(existingRsu);
        verify(rsuMapper).toDto(existingRsu);
    }

    @Test
    void testModifyRsu_WithModelUpdate() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setModel("Yunex RSU-2X");

        Rsu existingRsu = new Rsu();
        existingRsu.setIpv4Address(inetAddress);
        existingRsu.setRsuOrganizations(new HashSet<>());

        RsuModel newModel = new RsuModel();
        newModel.setName("RSU-2X");

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuModelRepository.findByNameAndManufacturerName("RSU-2X", "Yunex"))
                .thenReturn(Optional.of(newModel));
        when(rsuRepository.save(existingRsu)).thenReturn(existingRsu);
        when(rsuMapper.toDto(existingRsu)).thenReturn(null);
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of("Org1"));

        rsuManagementService.modifyRsu(rsuIp, patch, authToken);

        verify(rsuModelRepository).findByNameAndManufacturerName("RSU-2X", "Yunex");
        verify(rsuRepository).save(existingRsu);
    }

    @Test
    void testModifyRsu_WithCredentialUpdates() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setSshCredentialGroup("ssh-group-new");
        patch.setSnmpCredentialGroup("snmp-group-new");
        patch.setSnmpVersionGroup("v3");

        Rsu existingRsu = new Rsu();
        existingRsu.setIpv4Address(inetAddress);
        existingRsu.setRsuOrganizations(new HashSet<>());

        RsuCredential sshCred = new RsuCredential();
        SnmpCredential snmpCred = new SnmpCredential();
        SnmpProtocol snmpProtocol = new SnmpProtocol();

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuCredentialRepository.findByNickname("ssh-group-new")).thenReturn(Optional.of(sshCred));
        when(snmpCredentialRepository.findByNickname("snmp-group-new")).thenReturn(Optional.of(snmpCred));
        when(snmpProtocolRepository.findByNickname("v3")).thenReturn(Optional.of(snmpProtocol));
        when(rsuRepository.save(existingRsu)).thenReturn(existingRsu);
        when(rsuMapper.toDto(existingRsu)).thenReturn(null);
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of("Org1"));

        rsuManagementService.modifyRsu(rsuIp, patch, authToken);

        verify(rsuCredentialRepository).findByNickname("ssh-group-new");
        verify(snmpCredentialRepository).findByNickname("snmp-group-new");
        verify(snmpProtocolRepository).findByNickname("v3");
    }

    @Test
    void testModifyRsu_RsuNotFound() throws UnknownHostException {
        String rsuIp = "192.168.1.123";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);
        RsuPatch patch = new RsuPatch();

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> rsuManagementService.modifyRsu(rsuIp, patch, authToken));

        assertTrue(exception.getMessage().contains("RSU not found"));
        verify(rsuRepository, never()).save(any());
    }

    @Test
    void testModifyRsu_InvalidIpAddress() {
        String invalidIp = "invalid-ip";
        RsuPatch patch = new RsuPatch();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> rsuManagementService.modifyRsu(invalidIp, patch, authToken));

        assertTrue(exception.getMessage().contains("Invalid IP address"));
        verify(rsuRepository, never()).save(any());
    }

    @Test
    void testModifyRsu_ModelNotFound() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setModel("Unknown Model");

        Rsu existingRsu = new Rsu();
        existingRsu.setRsuOrganizations(new HashSet<>());

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuModelRepository.findByNameAndManufacturerName(anyString(), anyString()))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> rsuManagementService.modifyRsu(rsuIp, patch, authToken));

        assertTrue(exception.getMessage().contains("Model not found"));
    }

    @Test
    void testModifyRsu_InvalidModelFormat() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setModel("InvalidFormat");

        Rsu existingRsu = new Rsu();
        existingRsu.setRsuOrganizations(new HashSet<>());

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> rsuManagementService.modifyRsu(rsuIp, patch, authToken));

        assertTrue(exception.getMessage().contains("Invalid model format"));
    }

    // ==================== HANDLE ORGANIZATION CHANGES TESTS ====================

    @Test
    void testHandleOrganizationChanges_AddOrganizations_Success() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setOrganizationsToAdd(Arrays.asList("Org1", "Org2"));

        Rsu existingRsu = new Rsu();
        existingRsu.setIpv4Address(inetAddress);
        existingRsu.setRsuOrganizations(new HashSet<>());

        Organization org1 = new Organization();
        org1.setName("Org1");
        Organization org2 = new Organization();
        org2.setName("Org2");

        List<String> authorizedOrgs = Arrays.asList("Org1", "Org2", "Org3");

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuRepository.existsByIpAndOrganizations(inetAddress, List.of("Org1")))
                .thenReturn(false);
        when(rsuRepository.existsByIpAndOrganizations(inetAddress, List.of("Org2")))
                .thenReturn(false);
        when(organizationRepository.findByName("Org1")).thenReturn(Optional.of(org1));
        when(organizationRepository.findByName("Org2")).thenReturn(Optional.of(org2));
        when(rsuRepository.save(existingRsu)).thenReturn(existingRsu);
        when(rsuMapper.toDto(existingRsu)).thenReturn(null);
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(authorizedOrgs);

        rsuManagementService.modifyRsu(rsuIp, patch, authToken);

        verify(rsuOrganizationRepository, times(2)).save(any(RsuOrganization.class));
        verify(organizationRepository).findByName("Org1");
        verify(organizationRepository).findByName("Org2");
    }

    @Test
    void testHandleOrganizationChanges_AddOrganizations_AlreadyExists() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setOrganizationsToAdd(Arrays.asList("Org1"));

        Rsu existingRsu = new Rsu();
        existingRsu.setIpv4Address(inetAddress);
        existingRsu.setRsuOrganizations(new HashSet<>());

        List<String> authorizedOrgs = Arrays.asList("Org1");

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuRepository.existsByIpAndOrganizations(inetAddress, List.of("Org1")))
                .thenReturn(true);
        when(rsuRepository.save(existingRsu)).thenReturn(existingRsu);
        when(rsuMapper.toDto(existingRsu)).thenReturn(null);
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(authorizedOrgs);

        rsuManagementService.modifyRsu(rsuIp, patch, authToken);

        verify(rsuOrganizationRepository, never()).save(any(RsuOrganization.class));
        verify(organizationRepository, never()).findByName(anyString());
    }

    @Test
    void testHandleOrganizationChanges_AddOrganizations_Unauthorized() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setOrganizationsToAdd(Arrays.asList("UnauthorizedOrg"));

        Rsu existingRsu = new Rsu();
        existingRsu.setIpv4Address(inetAddress);
        existingRsu.setRsuOrganizations(new HashSet<>());

        List<String> authorizedOrgs = Arrays.asList("Org1", "Org2");

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(authorizedOrgs);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> rsuManagementService.modifyRsu(rsuIp, patch, authToken));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("User does not have permission to add RSU to organization(s)"));
        assertTrue(exception.getMessage().contains("UnauthorizedOrg"));
        verify(rsuOrganizationRepository, never()).save(any(RsuOrganization.class));
    }

    @Test
    void testHandleOrganizationChanges_AddOrganizations_OrganizationNotFound() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setOrganizationsToAdd(Arrays.asList("NonExistentOrg"));

        Rsu existingRsu = new Rsu();
        existingRsu.setIpv4Address(inetAddress);
        existingRsu.setRsuOrganizations(new HashSet<>());

        List<String> authorizedOrgs = Arrays.asList("NonExistentOrg");

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuRepository.existsByIpAndOrganizations(inetAddress, List.of("NonExistentOrg")))
                .thenReturn(false);
        when(organizationRepository.findByName("NonExistentOrg")).thenReturn(Optional.empty());
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(authorizedOrgs);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> rsuManagementService.modifyRsu(rsuIp, patch, authToken));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Organization not found: NonExistentOrg"));
        verify(rsuOrganizationRepository, never()).save(any(RsuOrganization.class));
    }

    @Test
    void testHandleOrganizationChanges_RemoveOrganizations_Success() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setOrganizationsToRemove(Arrays.asList("Org1", "Org2"));

        Rsu existingRsu = new Rsu();
        existingRsu.setIpv4Address(inetAddress);
        existingRsu.setRsuOrganizations(new HashSet<>());

        Organization org1 = new Organization();
        org1.setName("Org1");
        Organization org2 = new Organization();
        org2.setName("Org2");

        RsuOrganization rsuOrg1 = new RsuOrganization();
        rsuOrg1.setRsu(existingRsu);
        rsuOrg1.setOrganization(org1);

        RsuOrganization rsuOrg2 = new RsuOrganization();
        rsuOrg2.setRsu(existingRsu);
        rsuOrg2.setOrganization(org2);

        List<String> authorizedOrgs = Arrays.asList("Org1", "Org2", "Org3");

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuOrganizationRepository.findByRsuIpv4AddressAndOrganization_Name(inetAddress, "Org1"))
                .thenReturn(Optional.of(rsuOrg1));
        when(rsuOrganizationRepository.findByRsuIpv4AddressAndOrganization_Name(inetAddress, "Org2"))
                .thenReturn(Optional.of(rsuOrg2));
        when(rsuRepository.save(existingRsu)).thenReturn(existingRsu);
        when(rsuMapper.toDto(existingRsu)).thenReturn(null);
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(authorizedOrgs);

        rsuManagementService.modifyRsu(rsuIp, patch, authToken);

        verify(rsuOrganizationRepository).delete(rsuOrg1);
        verify(rsuOrganizationRepository).delete(rsuOrg2);
    }

    @Test
    void testHandleOrganizationChanges_RemoveOrganizations_NotFound() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setOrganizationsToRemove(Arrays.asList("Org1"));

        Rsu existingRsu = new Rsu();
        existingRsu.setIpv4Address(inetAddress);
        existingRsu.setRsuOrganizations(new HashSet<>());

        List<String> authorizedOrgs = Arrays.asList("Org1");

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuOrganizationRepository.findByRsuIpv4AddressAndOrganization_Name(inetAddress, "Org1"))
                .thenReturn(Optional.empty());
        when(rsuRepository.save(existingRsu)).thenReturn(existingRsu);
        when(rsuMapper.toDto(existingRsu)).thenReturn(null);
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(authorizedOrgs);

        rsuManagementService.modifyRsu(rsuIp, patch, authToken);

        verify(rsuOrganizationRepository, never()).delete(any(RsuOrganization.class));
    }

    @Test
    void testHandleOrganizationChanges_RemoveOrganizations_Unauthorized() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setOrganizationsToRemove(Arrays.asList("UnauthorizedOrg"));

        Rsu existingRsu = new Rsu();
        existingRsu.setIpv4Address(inetAddress);
        existingRsu.setRsuOrganizations(new HashSet<>());

        List<String> authorizedOrgs = Arrays.asList("Org1", "Org2");

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(authorizedOrgs);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> rsuManagementService.modifyRsu(rsuIp, patch, authToken));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("User does not have permission to remove RSU from organization(s)"));
        assertTrue(exception.getMessage().contains("UnauthorizedOrg"));
        verify(rsuOrganizationRepository, never()).delete(any(RsuOrganization.class));
    }

    @Test
    void testHandleOrganizationChanges_AddAndRemoveOrganizations() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setOrganizationsToAdd(Arrays.asList("NewOrg"));
        patch.setOrganizationsToRemove(Arrays.asList("OldOrg"));

        Rsu existingRsu = new Rsu();
        existingRsu.setIpv4Address(inetAddress);
        existingRsu.setRsuOrganizations(new HashSet<>());

        Organization newOrg = new Organization();
        newOrg.setName("NewOrg");
        Organization oldOrg = new Organization();
        oldOrg.setName("OldOrg");

        RsuOrganization rsuOrgToRemove = new RsuOrganization();
        rsuOrgToRemove.setRsu(existingRsu);
        rsuOrgToRemove.setOrganization(oldOrg);

        List<String> authorizedOrgs = Arrays.asList("NewOrg", "OldOrg");

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuRepository.existsByIpAndOrganizations(inetAddress, List.of("NewOrg")))
                .thenReturn(false);
        when(organizationRepository.findByName("NewOrg")).thenReturn(Optional.of(newOrg));
        when(rsuOrganizationRepository.findByRsuIpv4AddressAndOrganization_Name(inetAddress, "OldOrg"))
                .thenReturn(Optional.of(rsuOrgToRemove));
        when(rsuRepository.save(existingRsu)).thenReturn(existingRsu);
        when(rsuMapper.toDto(existingRsu)).thenReturn(null);
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(authorizedOrgs);

        rsuManagementService.modifyRsu(rsuIp, patch, authToken);

        verify(rsuOrganizationRepository).save(any(RsuOrganization.class));
        verify(rsuOrganizationRepository).delete(rsuOrgToRemove);
    }

    @Test
    void testHandleOrganizationChanges_AddMultipleOrganizations_PartiallyUnauthorized() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setOrganizationsToAdd(Arrays.asList("Org1", "UnauthorizedOrg1", "UnauthorizedOrg2"));

        Rsu existingRsu = new Rsu();
        existingRsu.setIpv4Address(inetAddress);
        existingRsu.setRsuOrganizations(new HashSet<>());

        List<String> authorizedOrgs = Arrays.asList("Org1");

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(authorizedOrgs);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> rsuManagementService.modifyRsu(rsuIp, patch, authToken));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("UnauthorizedOrg1"));
        assertTrue(exception.getMessage().contains("UnauthorizedOrg2"));
        verify(rsuOrganizationRepository, never()).save(any(RsuOrganization.class));
    }

    @Test
    void testHandleOrganizationChanges_RemoveMultipleOrganizations_PartiallyUnauthorized() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setOrganizationsToRemove(Arrays.asList("Org1", "UnauthorizedOrg1", "UnauthorizedOrg2"));

        Rsu existingRsu = new Rsu();
        existingRsu.setIpv4Address(inetAddress);
        existingRsu.setRsuOrganizations(new HashSet<>());

        List<String> authorizedOrgs = Arrays.asList("Org1");

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(authorizedOrgs);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> rsuManagementService.modifyRsu(rsuIp, patch, authToken));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("UnauthorizedOrg1"));
        assertTrue(exception.getMessage().contains("UnauthorizedOrg2"));
        verify(rsuOrganizationRepository, never()).delete(any(RsuOrganization.class));
    }

    @Test
    void testHandleOrganizationChanges_NoOrganizationChanges() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        // No organization changes

        Rsu existingRsu = new Rsu();
        existingRsu.setIpv4Address(inetAddress);
        existingRsu.setRsuOrganizations(new HashSet<>());

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuRepository.save(existingRsu)).thenReturn(existingRsu);
        when(rsuMapper.toDto(existingRsu)).thenReturn(null);
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of());

        rsuManagementService.modifyRsu(rsuIp, patch, authToken);

        verify(rsuOrganizationRepository, never()).save(any(RsuOrganization.class));
        verify(rsuOrganizationRepository, never()).delete(any(RsuOrganization.class));
    }

    @Test
    void testHandleOrganizationChanges_EmptyAddList() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setOrganizationsToAdd(List.of());

        Rsu existingRsu = new Rsu();
        existingRsu.setIpv4Address(inetAddress);
        existingRsu.setRsuOrganizations(new HashSet<>());

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuRepository.save(existingRsu)).thenReturn(existingRsu);
        when(rsuMapper.toDto(existingRsu)).thenReturn(null);
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of());

        rsuManagementService.modifyRsu(rsuIp, patch, authToken);

        verify(rsuOrganizationRepository, never()).save(any(RsuOrganization.class));
    }

    @Test
    void testHandleOrganizationChanges_EmptyRemoveList() throws UnknownHostException {
        String rsuIp = "192.168.1.100";
        InetAddress inetAddress = InetAddress.getByName(rsuIp);

        RsuPatch patch = new RsuPatch();
        patch.setOrganizationsToRemove(List.of());

        Rsu existingRsu = new Rsu();
        existingRsu.setIpv4Address(inetAddress);
        existingRsu.setRsuOrganizations(new HashSet<>());

        when(rsuRepository.findByIpv4Address(inetAddress)).thenReturn(existingRsu);
        when(rsuRepository.save(existingRsu)).thenReturn(existingRsu);
        when(rsuMapper.toDto(existingRsu)).thenReturn(null);
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of());

        rsuManagementService.modifyRsu(rsuIp, patch, authToken);

        verify(rsuOrganizationRepository, never()).delete(any(RsuOrganization.class));
    }

    // ==================== HELPER METHODS ====================

    private RsuRepository.RsuModelProjection createRsuModelProjection(String manufacturer, String model) {
        return new RsuRepository.RsuModelProjection() {
            @Override
            public String getManufacturer() {
                return manufacturer;
            }

            @Override
            public String getModel() {
                return model;
            }
        };
    }
}