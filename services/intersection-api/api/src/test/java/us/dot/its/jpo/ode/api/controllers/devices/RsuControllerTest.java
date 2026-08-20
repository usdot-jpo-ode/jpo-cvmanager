package us.dot.its.jpo.ode.api.controllers.devices;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ResponseStatusException;

import us.dot.its.jpo.ode.api.models.devices.RsuInfoDto;
import us.dot.its.jpo.ode.api.models.devices.management.ModifyRsuAllowedSelections;
import us.dot.its.jpo.ode.api.models.devices.management.RsuPatch;
import us.dot.its.jpo.ode.api.models.keycloak.CvManagerAuthToken;
import us.dot.its.jpo.ode.api.models.postgres.tables.Rsu;
import us.dot.its.jpo.ode.api.models.SimplePosition;
import us.dot.its.jpo.ode.api.models.UserRole;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.api.services.RsuManagementService;
import us.dot.its.jpo.ode.api.services.RsuOptionManagementService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RsuControllerTest {

    @Mock
    private Authentication authentication;

    @Mock
    private RsuManagementService rsuManagementService;

    @Mock
    private RsuOptionManagementService rsuOptionManagementService;

    @Mock
    private PermissionService permissionService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private CvManagerAuthToken authToken;

    @InjectMocks
    private RsuController rsuController;

    @Nested
    @DisplayName("Tests for getAllRsus endpoint")
    class GetAllRsusTests {
        @Test
        void testGetAllRsus_Success() {
            String organization = "TestOrg";
            String search = "Search Term";
            Pageable pageable = PageRequest.of(0, 100);

            RsuInfoDto rsu1 = new RsuInfoDto(
                    "192.168.1.100",
                    new SimplePosition(39.7392, -105.0844),
                    123.4,
                    "I-25",
                    "RSU1",
                    "SCMS1",
                    "Commsignia ITS-RS4-M",
                    "ssh-group-1",
                    "snmp-group-1",
                    "v3",
                    Arrays.asList("TestOrg"),
                    Boolean.TRUE,
                    Boolean.TRUE);

            RsuInfoDto rsu2 = new RsuInfoDto(
                    "192.168.1.101",
                    new SimplePosition(39.7400, -105.0850),
                    124.5,
                    "I-70",
                    "RSU2",
                    "SCMS2",
                    "Yunex RSU-2X",
                    "ssh-group-2",
                    "snmp-group-2",
                    "v2c",
                    Arrays.asList("TestOrg"),
                    Boolean.TRUE,
                    Boolean.TRUE);

            List<RsuInfoDto> rsuList = Arrays.asList(rsu1, rsu2);
            Page<RsuInfoDto> rsuPage = new PageImpl<>(rsuList, pageable, 2);

            when(rsuManagementService.getAllRsuInfo(organization, search, pageable)).thenReturn(rsuPage);

            Page<RsuInfoDto> result = rsuController.getAllRsus(organization, search, pageable);

            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals(2, result.getContent().size());
            assertEquals("192.168.1.100", result.getContent().get(0).getIpv4Address());
            assertEquals("192.168.1.101", result.getContent().get(1).getIpv4Address());

            verify(rsuManagementService).getAllRsuInfo(organization, search, pageable);
        }

        @Test
        void testGetAllRsus_Sorting_TimDeposit() {
            String organization = "TestOrg";
            String search = "";
            Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "tim_deposit"));
            Pageable expectedMappedPageable = PageRequest.of(0, 100,
                    Sort.by(Sort.Direction.ASC, "rsuOption.timDeposit"));

            Page<RsuInfoDto> emptyPage = new PageImpl<>(List.of(), expectedMappedPageable, 0);

            when(rsuManagementService.getAllRsuInfo(eq(organization), eq(search), eq(expectedMappedPageable)))
                    .thenReturn(emptyPage);

            Page<RsuInfoDto> result = rsuController.getAllRsus(organization, search, pageable);

            assertNotNull(result);
            verify(rsuManagementService).getAllRsuInfo(eq(organization), eq(search), eq(expectedMappedPageable));
        }

        @Test
        void testGetAllRsus_Sorting_SnmpMonitoring() {
            String organization = "TestOrg";
            String search = "";
            Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "snmp_monitoring"));
            Pageable expectedMappedPageable = PageRequest.of(0, 100,
                    Sort.by(Sort.Direction.ASC, "rsuOption.snmpMonitoring"));

            Page<RsuInfoDto> emptyPage = new PageImpl<>(List.of(), expectedMappedPageable, 0);

            when(rsuManagementService.getAllRsuInfo(eq(organization), eq(search), eq(expectedMappedPageable)))
                    .thenReturn(emptyPage);

            Page<RsuInfoDto> result = rsuController.getAllRsus(organization, search, pageable);

            assertNotNull(result);
            verify(rsuManagementService).getAllRsuInfo(eq(organization), eq(search), eq(expectedMappedPageable));
        }

        @Test
        void testGetAllRsus_EmptyResult() {
            String organization = "EmptyOrg";
            String search = "Search Term";
            Pageable pageable = PageRequest.of(0, 100);
            Page<RsuInfoDto> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(rsuManagementService.getAllRsuInfo(organization, search, pageable)).thenReturn(emptyPage);

            Page<RsuInfoDto> result = rsuController.getAllRsus(organization, search, pageable);

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());

            verify(rsuManagementService).getAllRsuInfo(organization, search, pageable);
        }

        @Test
        void testGetAllRsus_WithCustomPageSize() {
            String organization = "TestOrg";
            String search = "Search Term";
            Pageable pageable = PageRequest.of(0, 50);

            RsuInfoDto rsu1 = new RsuInfoDto(
                    "192.168.1.100",
                    new SimplePosition(39.7392, -105.0844),
                    123.4,
                    "I-25",
                    "RSU1",
                    "SCMS1",
                    "Model X",
                    "ssh-group",
                    "snmp-group",
                    "v3",
                    Arrays.asList("TestOrg"),
                    Boolean.TRUE,
                    Boolean.TRUE);

            Page<RsuInfoDto> rsuPage = new PageImpl<>(List.of(rsu1), pageable, 1);

            when(rsuManagementService.getAllRsuInfo(organization, search, pageable)).thenReturn(rsuPage);

            Page<RsuInfoDto> result = rsuController.getAllRsus(organization, search, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(50, result.getPageable().getPageSize());

            verify(rsuManagementService).getAllRsuInfo(organization, search, pageable);
        }
    }

    @Nested
    @DisplayName("Tests for getSingleRsuData endpoint")
    class GetSingleRsuDataTests {
        @Test
        void testGetSingleRsuData_Success() {
            String rsuIp = "192.168.1.100";

            RsuInfoDto rsuInfo = new RsuInfoDto(
                    rsuIp,
                    new SimplePosition(39.7392, -105.0844),
                    123.4,
                    "I-25",
                    "RSU123",
                    "SCMS123",
                    "Commsignia ITS-RS4-M",
                    "ssh-group-1",
                    "snmp-group-1",
                    "v3",
                    Arrays.asList("TestOrg"),
                    Boolean.TRUE,
                    Boolean.TRUE);

            when(rsuManagementService.getRsuInfo(rsuIp)).thenReturn(rsuInfo);

            RsuInfoDto result = rsuController.getSingleRsuData(rsuIp);

            assertNotNull(result);

            assertEquals(rsuIp, result.getIpv4Address());
            assertEquals("I-25", result.getPrimaryRoute());

            verify(rsuManagementService).getRsuInfo(rsuIp);
        }

        @Test
        void testGetSingleRsuData_RsuNotFound() {
            String rsuIp = "192.168.1.999";

            when(rsuManagementService.getRsuInfo(rsuIp)).thenReturn(null);

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> rsuController.getSingleRsuData(rsuIp));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("RSU not found", exception.getReason());

            verify(rsuManagementService).getRsuInfo(rsuIp);
        }

        @Test
        void testGetSingleRsuData_InvalidIpAddress() {
            String invalidRsuIp = "invalid-ip";

            when(rsuManagementService.getRsuInfo(invalidRsuIp))
                    .thenThrow(
                            new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IP address: " + invalidRsuIp));

            assertThrows(
                    ResponseStatusException.class,
                    () -> rsuController.getSingleRsuData(invalidRsuIp));

            verify(rsuManagementService).getRsuInfo(invalidRsuIp);
        }
    }

    @Nested
    @DisplayName("Tests for getAllowedSelections endpoint")
    class GetAllowedSelectionsTests {
        @Test
        void testGetAllowedSelections_Success() {

            ModifyRsuAllowedSelections allowedSelections = new ModifyRsuAllowedSelections(
                    Arrays.asList("I-25", "I-70"),
                    Arrays.asList("Commsignia ITS-RS4-M", "Yunex RSU-2X"),
                    Arrays.asList("ssh-group-1", "ssh-group-2"),
                    Arrays.asList("snmp-group-1", "snmp-group-2"),
                    Arrays.asList("v2c", "v3"),
                    Arrays.asList("TestOrg", "OtherOrg"));

            when(rsuManagementService.getAllowedSelections(any(CvManagerAuthToken.class)))
                    .thenReturn(allowedSelections);

            when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);

            ModifyRsuAllowedSelections result = rsuController.getAllowedSelections();

            assertNotNull(result);

            assertEquals(2, result.getPrimaryRoutes().size());
            assertEquals(2, result.getRsuModels().size());
            assertEquals(2, result.getSshCredentialGroups().size());
            assertEquals(2, result.getSnmpCredentialGroups().size());
            assertEquals(2, result.getSnmpVersionGroups().size());
            assertEquals(2, result.getOrganizations().size());

            verify(rsuManagementService).getAllowedSelections(any(CvManagerAuthToken.class));
        }

        @Nested
        @DisplayName("Tests for modifyRsu endpoint")
        class ModifyRsuTests {
            @Test
            void testModifyRsu_Success() {
                String rsuIp = "192.168.1.100";
                RsuPatch patch = new RsuPatch();
                patch.setIpv4Address("192.168.1.101");

                doReturn(null).when(rsuManagementService).modifyRsu(rsuIp, patch, authToken);
                doNothing().when(rsuOptionManagementService).modifyRsuOption(rsuIp, patch);

                when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);

                ResponseEntity<Void> result = rsuController.modifyRsu(rsuIp, patch);

                assertNotNull(result);
                assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
                assertNull(result.getBody());

                verify(rsuManagementService).modifyRsu(rsuIp, patch, authToken);
                verify(rsuOptionManagementService).modifyRsuOption(rsuIp, patch);
            }

            @Test
            void testModifyRsu_RsuNotFound() {
                String rsuIp = "192.168.1.999";
                RsuPatch patch = new RsuPatch();

                doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "RSU not found"))
                        .when(rsuManagementService).modifyRsu(rsuIp, patch, authToken);

                when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
                assertThrows(
                        ResponseStatusException.class,
                        () -> rsuController.modifyRsu(rsuIp, patch));

                verify(rsuManagementService).modifyRsu(rsuIp, patch, authToken);
                verify(rsuOptionManagementService, never()).modifyRsuOption(any(), any());
            }

            @Test
            void testModifyRsu_InvalidPatch() {
                String rsuIp = "192.168.1.100";
                RsuPatch invalidPatch = new RsuPatch();
                invalidPatch.setIpv4Address("invalid-ip");

                doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IP address"))
                        .when(rsuManagementService).modifyRsu(rsuIp, invalidPatch, authToken);

                when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);

                assertThrows(
                        ResponseStatusException.class,
                        () -> rsuController.modifyRsu(rsuIp, invalidPatch));

                verify(rsuManagementService).modifyRsu(rsuIp, invalidPatch, authToken);
                verify(rsuOptionManagementService, never()).modifyRsuOption(any(), any());
            }

            @Test
            void testModifyRsu_ServiceException() {
                String rsuIp = "192.168.1.100";
                RsuPatch patch = new RsuPatch();

                doThrow(new RuntimeException("Database error"))
                        .when(rsuManagementService).modifyRsu(rsuIp, patch, authToken);

                when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
                assertThrows(
                        RuntimeException.class,
                        () -> rsuController.modifyRsu(rsuIp, patch));

                verify(rsuManagementService).modifyRsu(rsuIp, patch, authToken);
                verify(rsuOptionManagementService, never()).modifyRsuOption(any(), any());
            }
        }

        @Nested
        @DisplayName("Tests for deleteRsu endpoint")
        class DeleteRsuTests {
            @Test
            void testDeleteRsu_Success() {
                String rsuIp = "192.168.1.100";

                doNothing().when(rsuManagementService).deleteRsuByIpv4Address(rsuIp);

                ResponseEntity<Void> result = rsuController.deleteRsu(rsuIp);

                assertNotNull(result);
                assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
                assertNull(result.getBody());

                verify(rsuManagementService).deleteRsuByIpv4Address(rsuIp);
            }

            @Test
            void testDeleteRsu_RsuNotFound() {
                String rsuIp = "192.168.1.999";

                doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "RSU not found"))
                        .when(rsuManagementService).deleteRsuByIpv4Address(rsuIp);

                assertThrows(
                        ResponseStatusException.class,
                        () -> rsuController.deleteRsu(rsuIp));

                verify(rsuManagementService).deleteRsuByIpv4Address(rsuIp);
            }

            @Test
            void testDeleteRsu_InvalidIpAddress() {
                String invalidRsuIp = "invalid-ip";

                doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IP address: " + invalidRsuIp))
                        .when(rsuManagementService).deleteRsuByIpv4Address(invalidRsuIp);

                assertThrows(
                        ResponseStatusException.class,
                        () -> rsuController.deleteRsu(invalidRsuIp));

                verify(rsuManagementService).deleteRsuByIpv4Address(invalidRsuIp);
            }

            @Test
            void testDeleteRsu_ServiceException() {
                String rsuIp = "192.168.1.100";

                doThrow(new RuntimeException("Database connection failed"))
                        .when(rsuManagementService).deleteRsuByIpv4Address(rsuIp);

                assertThrows(
                        RuntimeException.class,
                        () -> rsuController.deleteRsu(rsuIp));

                verify(rsuManagementService).deleteRsuByIpv4Address(rsuIp);
            }
        }

        @Nested
        @DisplayName("Tests for deleteRsus (multiple) endpoint")
        class DeleteMultipleRsusTests {
            @Test
            void testDeleteRsus_Success() {
                List<String> rsuIps = Arrays.asList("192.168.1.100", "192.168.1.101", "192.168.1.102");

                doNothing().when(rsuManagementService).deleteMultipleRsusByIpv4Address(rsuIps);

                ResponseEntity<Void> result = rsuController.deleteRsus(rsuIps);

                assertNotNull(result);
                assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
                assertNull(result.getBody());

                verify(rsuManagementService).deleteMultipleRsusByIpv4Address(rsuIps);
            }

            @Test
            void testDeleteRsus_SingleRsu() {
                List<String> rsuIps = Arrays.asList("192.168.1.100");

                doNothing().when(rsuManagementService).deleteMultipleRsusByIpv4Address(rsuIps);

                ResponseEntity<Void> result = rsuController.deleteRsus(rsuIps);

                assertNotNull(result);
                assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

                verify(rsuManagementService).deleteMultipleRsusByIpv4Address(rsuIps);
            }

            @Test
            void testDeleteRsus_EmptyList() {
                List<String> emptyList = Arrays.asList();

                doNothing().when(rsuManagementService).deleteMultipleRsusByIpv4Address(emptyList);

                ResponseEntity<Void> result = rsuController.deleteRsus(emptyList);

                assertNotNull(result);
                assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

                verify(rsuManagementService).deleteMultipleRsusByIpv4Address(emptyList);
            }

            @Test
            void testDeleteRsus_SomeNotFound() {
                List<String> rsuIps = Arrays.asList("192.168.1.100", "192.168.1.999", "192.168.1.101");

                doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Some RSUs not found"))
                        .when(rsuManagementService).deleteMultipleRsusByIpv4Address(rsuIps);

                assertThrows(
                        ResponseStatusException.class,
                        () -> rsuController.deleteRsus(rsuIps));

                verify(rsuManagementService).deleteMultipleRsusByIpv4Address(rsuIps);
            }

            @Test
            void testDeleteRsus_InvalidIpInList() {
                List<String> rsuIps = Arrays.asList("192.168.1.100", "invalid-ip", "192.168.1.101");

                doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IP address: invalid-ip"))
                        .when(rsuManagementService).deleteMultipleRsusByIpv4Address(rsuIps);

                assertThrows(
                        ResponseStatusException.class,
                        () -> rsuController.deleteRsus(rsuIps));

                verify(rsuManagementService).deleteMultipleRsusByIpv4Address(rsuIps);
            }

            @Test
            void testDeleteRsus_LargeList() {
                List<String> largeList = Arrays.asList(
                        "192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4", "192.168.1.5",
                        "192.168.1.6", "192.168.1.7", "192.168.1.8", "192.168.1.9", "192.168.1.10");

                doNothing().when(rsuManagementService).deleteMultipleRsusByIpv4Address(largeList);

                ResponseEntity<Void> result = rsuController.deleteRsus(largeList);

                assertNotNull(result);
                assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

                verify(rsuManagementService).deleteMultipleRsusByIpv4Address(largeList);
            }

            @Test
            void testDeleteRsus_ServiceException() {
                List<String> rsuIps = Arrays.asList("192.168.1.100", "192.168.1.101");

                doThrow(new RuntimeException("Database transaction failed"))
                        .when(rsuManagementService).deleteMultipleRsusByIpv4Address(rsuIps);

                assertThrows(
                        RuntimeException.class,
                        () -> rsuController.deleteRsus(rsuIps));

                verify(rsuManagementService).deleteMultipleRsusByIpv4Address(rsuIps);
            }
        }

        @Nested
        @DisplayName("Tests for createRsu endpoint")
        class CreateRsuTests {
            @Test
            void testCreateRsu_Success() {
                List<String> orgsToAdd = Arrays.asList("TestOrg");
                UserRole role = UserRole.OPERATOR;

                RsuInfoDto rsuInfoDto = new RsuInfoDto(
                        "192.168.1.100",
                        new SimplePosition(39.7392, -105.0844),
                        123.4,
                        "I-25",
                        "RSU123",
                        "SCMS123",
                        "Commsignia ITS-RS4-M",
                        "ssh-group-1",
                        "snmp-group-1",
                        "v3",
                        orgsToAdd,
                        true,
                        true);

                Rsu mockRsu = new Rsu();

                when(permissionService.hasRoleInOrgs(role, orgsToAdd)).thenReturn(true);
                when(rsuManagementService.createRsu(rsuInfoDto, orgsToAdd)).thenReturn(mockRsu);

                ResponseEntity<Void> result = rsuController.createRsu(rsuInfoDto);

                assertNotNull(result);
                assertEquals(HttpStatus.CREATED, result.getStatusCode());
                assertNull(result.getBody());

                verify(permissionService).hasRoleInOrgs(role, orgsToAdd);
                verify(rsuManagementService).createRsu(rsuInfoDto, orgsToAdd);
            }

            @Test
            void testCreateRsu_UnqualifiedOrganization() {
                List<String> orgsToAdd = Arrays.asList("TestOrg", "UnqualifiedOrg");
                UserRole role = UserRole.OPERATOR;

                RsuInfoDto rsuInfoDto = new RsuInfoDto(
                        "192.168.1.100",
                        new SimplePosition(39.7392, -105.0844),
                        123.4,
                        "I-25",
                        "RSU123",
                        "SCMS123",
                        "Commsignia ITS-RS4-M",
                        "ssh-group-1",
                        "snmp-group-1",
                        "v3",
                        orgsToAdd,
                        true,
                        true);

                when(permissionService.hasRoleInOrgs(role, orgsToAdd)).thenReturn(false);

                ResponseStatusException exception = assertThrows(
                        ResponseStatusException.class,
                        () -> rsuController.createRsu(rsuInfoDto));

                assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
                assertTrue(exception.getReason().contains("User not qualified to modify all specified organizations"));

                verify(rsuManagementService, never()).createRsu(any(), anyList());
            }

            @Test
            void testCreateRsu_DuplicateIpAddress() {
                List<String> orgsToAdd = Arrays.asList("TestOrg");
                UserRole role = UserRole.OPERATOR;

                RsuInfoDto rsuInfoDto = new RsuInfoDto(
                        "192.168.1.100",
                        new SimplePosition(39.7392, -105.0844),
                        123.4,
                        "I-25",
                        "RSU123",
                        "SCMS123",
                        "Commsignia ITS-RS4-M",
                        "ssh-group-1",
                        "snmp-group-1",
                        "v3",
                        orgsToAdd,
                        true,
                        true);

                when(permissionService.hasRoleInOrgs(role, orgsToAdd)).thenReturn(true);

                when(rsuManagementService.createRsu(rsuInfoDto, orgsToAdd))
                        .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT,
                                "RSU with IP 192.168.1.100 already exists"));

                assertThrows(
                        ResponseStatusException.class,
                        () -> rsuController.createRsu(rsuInfoDto));

                verify(rsuManagementService).createRsu(rsuInfoDto, orgsToAdd);
            }
        }

        @Test
        void testCreateRsu_ServiceException() {
            List<String> orgsToAdd = Arrays.asList("TestOrg");

            RsuInfoDto rsuInfoDto = new RsuInfoDto(
                    "192.168.1.100",
                    new SimplePosition(39.7392, -105.0844),
                    123.4,
                    "I-25",
                    "RSU123",
                    "SCMS123",
                    "Commsignia ITS-RS4-M",
                    "ssh-group-1",
                    "snmp-group-1",
                    "v3",
                    orgsToAdd,
                    true,
                    true);

            assertThrows(
                    RuntimeException.class,
                    () -> rsuController.createRsu(rsuInfoDto));
        }

        @Test
        void testCreateRsu_OrgRelationshipCreationFails() {
            List<String> orgsToAdd = Arrays.asList("TestOrg");

            RsuInfoDto rsuInfoDto = new RsuInfoDto(
                    "192.168.1.100",
                    new SimplePosition(39.7392, -105.0844),
                    123.4,
                    "I-25",
                    "RSU123",
                    "SCMS123",
                    "Commsignia ITS-RS4-M",
                    "ssh-group-1",
                    "snmp-group-1",
                    "v3",
                    orgsToAdd,
                    true,
                    true);

            assertThrows(
                    ResponseStatusException.class,
                    () -> rsuController.createRsu(rsuInfoDto));
        }

        @Test
        void testCreateRsu_NullOrganizationsList() {
            RsuInfoDto rsuInfoDto = new RsuInfoDto(
                    "192.168.1.100",
                    new SimplePosition(39.7392, -105.0844),
                    123.4,
                    "I-25",
                    "RSU123",
                    "SCMS123",
                    "Commsignia ITS-RS4-M",
                    "ssh-group-1",
                    "snmp-group-1",
                    "v3",
                    null,
                    true,
                    true);

            assertThrows(
                    ResponseStatusException.class,
                    () -> rsuController.createRsu(rsuInfoDto));

            verify(rsuManagementService, never()).createRsu(any(), anyList());
        }
    }
}
