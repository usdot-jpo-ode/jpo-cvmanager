package us.dot.its.jpo.ode.api.services;

import org.junit.jupiter.api.BeforeEach;
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

import us.dot.its.jpo.ode.api.mappers.UserMapper;
import us.dot.its.jpo.ode.api.mappers.UserPatchMapper;
import us.dot.its.jpo.ode.api.models.keycloak.CvManagerAuthToken;
import us.dot.its.jpo.ode.api.models.postgres.tables.Organization;
import us.dot.its.jpo.ode.api.models.postgres.tables.Role;
import us.dot.its.jpo.ode.api.models.postgres.tables.User;
import us.dot.its.jpo.ode.api.models.postgres.tables.UserOrganization;
import us.dot.its.jpo.ode.api.models.users.ModifyUserAllowedSelections;
import us.dot.its.jpo.ode.api.models.users.UserDto;
import us.dot.its.jpo.ode.api.models.users.UserOrganizationDto;
import us.dot.its.jpo.ode.api.models.users.UserPatch;
import us.dot.its.jpo.ode.api.repositories.OrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.RoleRepository;
import us.dot.its.jpo.ode.api.repositories.UserOrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserOrganizationRepository userOrganizationRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserPatchMapper userPatchMapper;

    @Mock
    private CvManagerAuthToken authToken;

    @InjectMocks
    private UserManagementService userManagementService;

    private User testUser;
    private UserDto testUserDto;
    private Organization testOrganization;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setKeycloakId(UUID.randomUUID());
        testUser.setCreatedTimestamp(System.currentTimeMillis());
        testUser.setSuperUser(false);

        // Set up test user DTO
        testUserDto = new UserDto(1, "test@example.com", "Test", "User", false, List.of());

        // Set up test organization
        testOrganization = new Organization();
        testOrganization.setId(1);
        testOrganization.setName("TestOrg");

        // Set up test role
        testRole = new Role();
        testRole.setId(1);
        testRole.setName("admin");
    }

    // ==================== getUser Tests ====================

    @Test
    void testGetUser_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userManagementService.getUser("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findByEmail("test@example.com");
        verify(userMapper).toDto(testUser);
    }

    @Test
    void testGetUser_NotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userManagementService.getUser("nonexistent@example.com"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User not found"));
        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(userMapper, never()).toDto(any());
    }

    // ==================== getUsers Tests ====================

    @Test
    void testGetUsers_WithOrganization() {
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);

        when(userRepository.findAllByOrganization("TestOrg", "", pageable)).thenReturn(userPage);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        Page<UserDto> result = userManagementService.getUsers("TestOrg", "", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("test@example.com", result.getContent().get(0).getEmail());
        verify(userRepository).findAllByOrganization("TestOrg", "", pageable);
    }

    @Test
    void testGetUsers_WithSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(testUser);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);

        when(userRepository.findAllByOrganization("TestOrg", "test", pageable)).thenReturn(userPage);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        Page<UserDto> result = userManagementService.getUsers("TestOrg", "test", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAllByOrganization("TestOrg", "test", pageable);
    }

    @Test
    void testGetUsers_EmptyResult() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(), pageable, 0);

        when(userRepository.findAllByOrganization("TestOrg", "", pageable)).thenReturn(userPage);

        Page<UserDto> result = userManagementService.getUsers("TestOrg", "", pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(userRepository).findAllByOrganization("TestOrg", "", pageable);
    }

    // ==================== getAllowedSelections Tests ====================

    @Test
    void testGetAllowedSelections_Success() {
        List<String> roles = List.of("admin", "operator", "user");
        List<String> organizations = List.of("TestOrg", "AnotherOrg");

        when(roleRepository.findAllRoleNames()).thenReturn(roles);
        when(authToken.getQualifiedOrgList("ADMIN")).thenReturn(organizations);

        ModifyUserAllowedSelections result = userManagementService.getAllowedSelections(authToken);

        assertNotNull(result);
        assertEquals(roles, result.getRoles());
        assertEquals(organizations, result.getOrganizations());
        verify(roleRepository).findAllRoleNames();
        verify(authToken).getQualifiedOrgList("ADMIN");
    }

    @Test
    void testGetAllowedSelections_EmptyLists() {
        when(roleRepository.findAllRoleNames()).thenReturn(List.of());
        when(authToken.getQualifiedOrgList("ADMIN")).thenReturn(List.of());

        ModifyUserAllowedSelections result = userManagementService.getAllowedSelections(authToken);

        assertNotNull(result);
        assertTrue(result.getRoles().isEmpty());
        assertTrue(result.getOrganizations().isEmpty());
    }

    // ==================== modifyUser Tests ====================

    @Test
    void testModifyUser_UpdateBasicFields() {
        UserPatch patch = new UserPatch();
        patch.setFirstName("Updated");
        patch.setLastName("Name");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(authToken.getQualifiedOrgList("ADMIN")).thenReturn(List.of("TestOrg"));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userManagementService.modifyUser("test@example.com", patch, authToken);

        assertNotNull(result);
        verify(userRepository).findByEmail("test@example.com");
        verify(userPatchMapper).updateUserFromPatch(patch, testUser);
        verify(userRepository).save(testUser);
        verify(userMapper).toDto(testUser);
    }

    @Test
    void testModifyUser_UserNotFound() {
        UserPatch patch = new UserPatch();

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        when(authToken.getQualifiedOrgList("ADMIN")).thenReturn(List.of("TestOrg"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userManagementService.modifyUser("nonexistent@example.com", patch, authToken));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testModifyUser_AddOrganization_Success() {
        UserPatch patch = new UserPatch();
        UserOrganizationDto orgToAdd = new UserOrganizationDto();
        orgToAdd.setOrganization("TestOrg");
        orgToAdd.setRole("admin");
        patch.setOrganizationsToAdd(List.of(orgToAdd));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(authToken.getQualifiedOrgList("ADMIN")).thenReturn(List.of("TestOrg"));
        when(userRepository.existsByEmailAndOrganizations("test@example.com", List.of("TestOrg"))).thenReturn(false);
        when(organizationRepository.findByName("TestOrg")).thenReturn(Optional.of(testOrganization));
        when(roleRepository.findByName("admin")).thenReturn(Optional.of(testRole));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userManagementService.modifyUser("test@example.com", patch, authToken);

        assertNotNull(result);
        verify(organizationRepository).findByName("TestOrg");
        verify(roleRepository).findByName("admin");
        verify(userOrganizationRepository).save(any(UserOrganization.class));
    }

    @Test
    void testModifyUser_AddOrganization_Unauthorized() {
        UserPatch patch = new UserPatch();
        UserOrganizationDto orgToAdd = new UserOrganizationDto();
        orgToAdd.setOrganization("UnauthorizedOrg");
        orgToAdd.setRole("admin");
        patch.setOrganizationsToAdd(List.of(orgToAdd));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(authToken.getQualifiedOrgList("ADMIN")).thenReturn(List.of("TestOrg"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userManagementService.modifyUser("test@example.com", patch, authToken));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getReason().contains("does not have permission"));
        verify(userOrganizationRepository, never()).save(any());
    }

    @Test
    void testModifyUser_AddOrganization_AlreadyExists() {
        UserPatch patch = new UserPatch();
        UserOrganizationDto orgToAdd = new UserOrganizationDto();
        orgToAdd.setOrganization("TestOrg");
        orgToAdd.setRole("admin");
        patch.setOrganizationsToAdd(List.of(orgToAdd));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(authToken.getQualifiedOrgList("ADMIN")).thenReturn(List.of("TestOrg"));
        when(userRepository.existsByEmailAndOrganizations("test@example.com", List.of("TestOrg"))).thenReturn(true);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userManagementService.modifyUser("test@example.com", patch, authToken);

        assertNotNull(result);
        // Should not create a new association since it already exists
        verify(userOrganizationRepository, never()).save(any());
    }

    @Test
    void testModifyUser_AddOrganization_OrganizationNotFound() {
        UserPatch patch = new UserPatch();
        UserOrganizationDto orgToAdd = new UserOrganizationDto();
        orgToAdd.setOrganization("NonexistentOrg");
        orgToAdd.setRole("admin");
        patch.setOrganizationsToAdd(List.of(orgToAdd));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(authToken.getQualifiedOrgList("ADMIN")).thenReturn(List.of("NonexistentOrg"));
        when(userRepository.existsByEmailAndOrganizations("test@example.com", List.of("NonexistentOrg")))
                .thenReturn(false);
        when(organizationRepository.findByName("NonexistentOrg")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userManagementService.modifyUser("test@example.com", patch, authToken));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Organization not found"));
        verify(userOrganizationRepository, never()).save(any());
    }

    @Test
    void testModifyUser_AddOrganization_RoleNotFound() {
        UserPatch patch = new UserPatch();
        UserOrganizationDto orgToAdd = new UserOrganizationDto();
        orgToAdd.setOrganization("TestOrg");
        orgToAdd.setRole("nonexistent_role");
        patch.setOrganizationsToAdd(List.of(orgToAdd));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(authToken.getQualifiedOrgList("ADMIN")).thenReturn(List.of("TestOrg"));
        when(userRepository.existsByEmailAndOrganizations("test@example.com", List.of("TestOrg"))).thenReturn(false);
        when(organizationRepository.findByName("TestOrg")).thenReturn(Optional.of(testOrganization));
        when(roleRepository.findByName("nonexistent_role")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userManagementService.modifyUser("test@example.com", patch, authToken));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Role not found"));
        verify(userOrganizationRepository, never()).save(any());
    }

    @Test
    void testModifyUser_RemoveOrganization_Success() {
        UserPatch patch = new UserPatch();
        UserOrganizationDto orgToRemove = new UserOrganizationDto();
        orgToRemove.setOrganization("TestOrg");
        orgToRemove.setRole("admin");
        patch.setOrganizationsToRemove(List.of(orgToRemove));

        UserOrganization userOrg = new UserOrganization();
        userOrg.setUser(testUser);
        userOrg.setOrganization(testOrganization);
        userOrg.setRole(testRole);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(authToken.getQualifiedOrgList("ADMIN")).thenReturn(List.of("TestOrg"));
        when(userOrganizationRepository.findByUserAndOrganization_Name(testUser, "TestOrg"))
                .thenReturn(Optional.of(userOrg));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userManagementService.modifyUser("test@example.com", patch, authToken);

        assertNotNull(result);
        verify(userOrganizationRepository).delete(userOrg);
    }

    @Test
    void testModifyUser_RemoveOrganization_Unauthorized() {
        UserPatch patch = new UserPatch();
        UserOrganizationDto orgToRemove = new UserOrganizationDto();
        orgToRemove.setOrganization("UnauthorizedOrg");
        orgToRemove.setRole("admin");
        patch.setOrganizationsToRemove(List.of(orgToRemove));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(authToken.getQualifiedOrgList("ADMIN")).thenReturn(List.of("TestOrg"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userManagementService.modifyUser("test@example.com", patch, authToken));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getReason().contains("does not have permission to remove"));
        verify(userOrganizationRepository, never()).delete(any());
    }

    @Test
    void testModifyUser_RemoveOrganization_NotFound() {
        UserPatch patch = new UserPatch();
        UserOrganizationDto orgToRemove = new UserOrganizationDto();
        orgToRemove.setOrganization("TestOrg");
        orgToRemove.setRole("admin");
        patch.setOrganizationsToRemove(List.of(orgToRemove));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(authToken.getQualifiedOrgList("ADMIN")).thenReturn(List.of("TestOrg"));
        when(userOrganizationRepository.findByUserAndOrganization_Name(testUser, "TestOrg"))
                .thenReturn(Optional.empty());
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userManagementService.modifyUser("test@example.com", patch, authToken);

        assertNotNull(result);
        // Should not throw exception, just skip deletion
        verify(userOrganizationRepository, never()).delete(any());
    }

    // ==================== deleteUserByEmail Tests ====================

    @Test
    void testDeleteUserByEmail_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        userManagementService.deleteUserByEmail("test@example.com");

        verify(userRepository).findByEmail("test@example.com");
        verify(userOrganizationRepository).removeUserOrganizationByEmail("test@example.com");
        verify(userRepository).delete(testUser);
    }

    @Test
    void testDeleteUserByEmail_UserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userManagementService.deleteUserByEmail("nonexistent@example.com"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User not found"));
        verify(userRepository, never()).delete(any());
        verify(userOrganizationRepository, never()).removeUserOrganizationByEmail(any());
    }

    // ==================== deleteMultipleUsersByEmail Tests ====================

    @Test
    void testDeleteMultipleUsersByEmail_Success() {
        List<String> emails = List.of("test1@example.com", "test2@example.com");
        User user1 = new User();
        user1.setEmail("test1@example.com");
        User user2 = new User();
        user2.setEmail("test2@example.com");
        List<User> users = List.of(user1, user2);

        when(userRepository.findByEmailIn(emails)).thenReturn(users);

        userManagementService.deleteMultipleUsersByEmail(emails);

        verify(userRepository).findByEmailIn(emails);
        verify(userOrganizationRepository).removeMultipleUserOrganizationsByEmail(emails);
        verify(userRepository).deleteAll(users);
    }

    @Test
    void testDeleteMultipleUsersByEmail_SomeNotFound() {
        List<String> emails = List.of("test1@example.com", "nonexistent@example.com");
        User user1 = new User();
        user1.setEmail("test1@example.com");
        List<User> users = List.of(user1);

        when(userRepository.findByEmailIn(emails)).thenReturn(users);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userManagementService.deleteMultipleUsersByEmail(emails));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User(s) not found"));
        assertTrue(exception.getReason().contains("nonexistent@example.com"));
        verify(userRepository, never()).deleteAll(any());
        verify(userOrganizationRepository, never()).removeMultipleUserOrganizationsByEmail(any());
    }

    @Test
    void testDeleteMultipleUsersByEmail_EmptyList() {
        List<String> emails = List.of();

        when(userRepository.findByEmailIn(emails)).thenReturn(List.of());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userManagementService.deleteMultipleUsersByEmail(emails));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("No valid user emails provided"));
        verify(userRepository, never()).deleteAll(any());
    }

    @Test
    void testDeleteMultipleUsersByEmail_AllNotFound() {
        List<String> emails = List.of("nonexistent1@example.com", "nonexistent2@example.com");

        when(userRepository.findByEmailIn(emails)).thenReturn(List.of());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userManagementService.deleteMultipleUsersByEmail(emails));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(userRepository, never()).deleteAll(any());
    }
}