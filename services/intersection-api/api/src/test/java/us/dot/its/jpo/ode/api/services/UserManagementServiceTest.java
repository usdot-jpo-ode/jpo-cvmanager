package us.dot.its.jpo.ode.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityNotFoundException;
import us.dot.its.jpo.ode.api.keycloak.config.KeycloakAdminConfig;
import us.dot.its.jpo.ode.api.mappers.UserMapper;
import us.dot.its.jpo.ode.api.mappers.UserPatchMapper;
import us.dot.its.jpo.ode.api.models.UserRole;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("integration-test")
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

    @Mock
    private KeycloakAdminConfig keycloakAdminConfig;

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
        testUserDto = new UserDto("test@example.com", "Test", "User", false, List.of());

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

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userManagementService.getUser("nonexistent@example.com"));

        assertEquals("User not found with email: nonexistent@example.com", exception.getMessage());
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
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(organizations);

        ModifyUserAllowedSelections result = userManagementService.getAllowedSelections(authToken);

        assertNotNull(result);
        assertEquals(roles, result.getRoles());
        assertEquals(organizations, result.getOrganizations());
        verify(roleRepository).findAllRoleNames();
        verify(authToken).getQualifiedOrgList(UserRole.ADMIN);
    }

    @Test
    void testGetAllowedSelections_EmptyLists() {
        when(roleRepository.findAllRoleNames()).thenReturn(List.of());
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of());

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
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of("TestOrg"));
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
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of("TestOrg"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userManagementService.modifyUser("nonexistent@example.com", patch, authToken));

        assertEquals("User not found with email: nonexistent@example.com", exception.getMessage());
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
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of("TestOrg"));
        when(userRepository.existsByEmailAndOrganizations("test@example.com", List.of("TestOrg"))).thenReturn(false);
        when(organizationRepository.findByName("TestOrg")).thenReturn(Optional.of(testOrganization));
        when(roleRepository.findByNameIgnoreCase("admin")).thenReturn(Optional.of(testRole));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userManagementService.modifyUser("test@example.com", patch, authToken);

        assertNotNull(result);
        verify(organizationRepository).findByName("TestOrg");
        verify(roleRepository).findByNameIgnoreCase("admin");
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
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of("TestOrg"));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userManagementService.modifyUser("test@example.com", patch, authToken));

        assertEquals("User does not have permission to add User to organization(s): UnauthorizedOrg",
                exception.getMessage());
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
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of("TestOrg"));
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
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of("NonexistentOrg"));
        when(userRepository.existsByEmailAndOrganizations("test@example.com", List.of("NonexistentOrg")))
                .thenReturn(false);
        when(organizationRepository.findByName("NonexistentOrg")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userManagementService.modifyUser("test@example.com", patch, authToken));

        assertEquals("Organization not found: NonexistentOrg", exception.getMessage());
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
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of("TestOrg"));
        when(userRepository.existsByEmailAndOrganizations("test@example.com", List.of("TestOrg"))).thenReturn(false);
        when(organizationRepository.findByName("TestOrg")).thenReturn(Optional.of(testOrganization));
        when(roleRepository.findByNameIgnoreCase("nonexistent_role")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userManagementService.modifyUser("test@example.com", patch, authToken));

        assertEquals("Role not found: nonexistent_role", exception.getMessage());
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
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of("TestOrg"));
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
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of("TestOrg"));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userManagementService.modifyUser("test@example.com", patch, authToken));

        assertEquals("User does not have permission to remove User from organization(s): UnauthorizedOrg",
                exception.getMessage());
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
        when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of("TestOrg"));
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

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userManagementService.deleteUserByEmail("nonexistent@example.com"));

        assertEquals("User not found with email: nonexistent@example.com", exception.getMessage());
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

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userManagementService.deleteMultipleUsersByEmail(emails));

        assertEquals("User(s) not found with email(s): nonexistent@example.com", exception.getMessage());
        verify(userRepository, never()).deleteAll(any());
        verify(userOrganizationRepository, never()).removeMultipleUserOrganizationsByEmail(any());
    }

    @Test
    void testDeleteMultipleUsersByEmail_EmptyList() {
        List<String> emails = List.of();

        when(userRepository.findByEmailIn(emails)).thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userManagementService.deleteMultipleUsersByEmail(emails));

        assertEquals("No valid user emails provided", exception.getMessage());
        verify(userRepository, never()).deleteAll(any());
    }

    @Test
    void testDeleteMultipleUsersByEmail_AllNotFound() {
        List<String> emails = List.of("nonexistent1@example.com", "nonexistent2@example.com");

        when(userRepository.findByEmailIn(emails)).thenReturn(List.of());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userManagementService.deleteMultipleUsersByEmail(emails));

        assertEquals("User(s) not found with email(s): nonexistent1@example.com, nonexistent2@example.com",
                exception.getMessage());
        verify(userRepository, never()).deleteAll(any());
    }

    // ==================== createUser Tests ====================

    @Test
    void testCreateUser_Success_SingleOrganization() {
        UserOrganizationDto orgDto = new UserOrganizationDto();
        orgDto.setOrganization("TestOrg");
        orgDto.setRole("USER");

        UserDto userDto = new UserDto(
                "newuser@example.com",
                "New",
                "User",
                false,
                List.of(orgDto));

        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setSuperUser(false);

        UserOrganization userOrg = new UserOrganization();
        userOrg.setUser(newUser);
        userOrg.setOrganization(testOrganization);
        userOrg.setRole(testRole);

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(201);

        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);
        String realm = "cvmanager";

        when(keycloakAdminConfig.getRealm()).thenReturn(realm);
        when(keycloakAdminConfig.keyCloakBuilder()).thenReturn(keycloak);
        when(keycloak.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(newUser));

        when(organizationRepository.findByName("TestOrg")).thenReturn(Optional.of(testOrganization));
        when(roleRepository.findByNameIgnoreCase("USER")).thenReturn(Optional.of(testRole));
        when(userOrganizationRepository.saveAll(anyList())).thenReturn(List.of(userOrg));

        User result = userManagementService.createUser(userDto);

        assertNotNull(result);
        assertEquals("newuser@example.com", result.getEmail());
        verify(usersResource).create(any(UserRepresentation.class));
        verify(organizationRepository).findByName("TestOrg");
        verify(roleRepository).findByNameIgnoreCase("USER");
        verify(userOrganizationRepository).saveAll(anyList());
    }

    @Test
    void testCreateUser_Success_MultipleOrganizations() {
        UserOrganizationDto org1Dto = new UserOrganizationDto();
        org1Dto.setOrganization("TestOrg");
        org1Dto.setRole("ADMIN");

        UserOrganizationDto org2Dto = new UserOrganizationDto();
        org2Dto.setOrganization("AnotherOrg");
        org2Dto.setRole("USER");

        UserDto userDto = new UserDto(
                "newuser@example.com",
                "New",
                "User",
                false,
                List.of(org1Dto, org2Dto));

        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setSuperUser(false);

        Organization anotherOrg = new Organization();
        anotherOrg.setId(2);
        anotherOrg.setName("AnotherOrg");

        Role adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName("ADMIN");

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(201);

        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);
        String realm = "cvmanager";

        when(keycloakAdminConfig.getRealm()).thenReturn(realm);
        when(keycloakAdminConfig.keyCloakBuilder()).thenReturn(keycloak);
        when(keycloak.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(newUser));

        when(organizationRepository.findByName("TestOrg")).thenReturn(Optional.of(testOrganization));
        when(organizationRepository.findByName("AnotherOrg")).thenReturn(Optional.of(anotherOrg));
        when(roleRepository.findByNameIgnoreCase("USER")).thenReturn(Optional.of(testRole));
        when(roleRepository.findByNameIgnoreCase("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userOrganizationRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        User result = userManagementService.createUser(userDto);

        assertNotNull(result);
        assertEquals("newuser@example.com", result.getEmail());
        verify(usersResource).create(any(UserRepresentation.class));
        verify(organizationRepository).findByName("TestOrg");
        verify(organizationRepository).findByName("AnotherOrg");
        verify(roleRepository).findByNameIgnoreCase("ADMIN");
        verify(roleRepository).findByNameIgnoreCase("USER");
        verify(userOrganizationRepository).saveAll(argThat(list -> {
            List<UserOrganization> listCopy = new ArrayList<>();
            list.forEach(listCopy::add);
            return listCopy.size() == 2;
        }));
    }

    @Test
    void testCreateUser_Success_NoOrganizations() {
        UserDto userDto = new UserDto(
                "newuser@example.com",
                "New",
                "User",
                false,
                List.of() // No organizations
        );

        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setSuperUser(false);

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(201);

        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);
        String realm = "cvmanager";

        when(keycloakAdminConfig.getRealm()).thenReturn(realm);
        when(keycloakAdminConfig.keyCloakBuilder()).thenReturn(keycloak);
        when(keycloak.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(newUser));

        when(userOrganizationRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        User result = userManagementService.createUser(userDto);

        assertNotNull(result);
        assertEquals("newuser@example.com", result.getEmail());
        verify(usersResource).create(any(UserRepresentation.class));
        verify(userOrganizationRepository).saveAll(argThat(list -> {
            List<UserOrganization> listCopy = new ArrayList<>();
            list.forEach(listCopy::add);
            return listCopy.size() == 0;
        }));
        verify(organizationRepository, never()).findByName(anyString());
        verify(roleRepository, never()).findByNameIgnoreCase(anyString());
    }

    @Test
    void testCreateUser_Success_SuperUser() {
        UserOrganizationDto orgDto = new UserOrganizationDto();
        orgDto.setOrganization("TestOrg");
        orgDto.setRole("ADMIN");

        UserDto userDto = new UserDto(
                "superuser@example.com",
                "Super",
                "User",
                true, // Super user
                List.of(orgDto));

        User newUser = new User();
        newUser.setEmail("superuser@example.com");
        newUser.setFirstName("Super");
        newUser.setLastName("User");
        newUser.setSuperUser(true);

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(201);

        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);
        String realm = "cvmanager";

        when(keycloakAdminConfig.getRealm()).thenReturn(realm);
        when(keycloakAdminConfig.keyCloakBuilder()).thenReturn(keycloak);
        when(keycloak.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);

        when(userRepository.findByEmail("superuser@example.com")).thenReturn(Optional.of(newUser));

        when(userRepository.save(newUser)).thenReturn(newUser);
        when(organizationRepository.findByName("TestOrg")).thenReturn(Optional.of(testOrganization));
        when(roleRepository.findByNameIgnoreCase("ADMIN")).thenReturn(Optional.of(testRole));
        when(userOrganizationRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        User result = userManagementService.createUser(userDto);

        assertNotNull(result);
        assertEquals("superuser@example.com", result.getEmail());
        assertTrue(result.getSuperUser());
        verify(usersResource).create(any(UserRepresentation.class));
        verify(userRepository).save(newUser);
    }

    @Test
    void testCreateUser_OrganizationNotFound() {
        UserOrganizationDto orgDto = new UserOrganizationDto();
        orgDto.setOrganization("NonexistentOrg");
        orgDto.setRole("USER");

        UserDto userDto = new UserDto(
                "newuser@example.com",
                "New",
                "User",
                false,
                List.of(orgDto));

        User newUser = new User();
        newUser.setEmail("newuser@example.com");

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(201);

        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);
        String realm = "cvmanager";

        when(keycloakAdminConfig.getRealm()).thenReturn(realm);
        when(keycloakAdminConfig.keyCloakBuilder()).thenReturn(keycloak);
        when(keycloak.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(newUser));

        when(organizationRepository.findByName("NonexistentOrg")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userManagementService.createUser(userDto));

        assertTrue(exception.getMessage().contains("Organization not found"));
        assertTrue(exception.getMessage().contains("NonexistentOrg"));
        verify(usersResource).create(any(UserRepresentation.class));
        verify(organizationRepository).findByName("NonexistentOrg");
        verify(userOrganizationRepository, never()).saveAll(anyList());
    }

    @Test
    void testCreateUser_RoleNotFound() {
        UserOrganizationDto orgDto = new UserOrganizationDto();
        orgDto.setOrganization("TestOrg");
        orgDto.setRole("NONEXISTENT_ROLE");

        UserDto userDto = new UserDto(
                "newuser@example.com",
                "New",
                "User",
                false,
                List.of(orgDto));

        User newUser = new User();
        newUser.setEmail("newuser@example.com");

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(201);

        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);
        String realm = "cvmanager";

        when(keycloakAdminConfig.getRealm()).thenReturn(realm);
        when(keycloakAdminConfig.keyCloakBuilder()).thenReturn(keycloak);
        when(keycloak.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(newUser));

        when(organizationRepository.findByName("TestOrg")).thenReturn(Optional.of(testOrganization));
        when(roleRepository.findByNameIgnoreCase("NONEXISTENT_ROLE")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userManagementService.createUser(userDto));

        assertTrue(exception.getMessage().contains("Role not found"));
        assertTrue(exception.getMessage().contains("NONEXISTENT_ROLE"));
        verify(usersResource).create(any(UserRepresentation.class));
        verify(organizationRepository).findByName("TestOrg");
        verify(roleRepository).findByNameIgnoreCase("NONEXISTENT_ROLE");
        verify(userOrganizationRepository, never()).saveAll(anyList());
    }

    @Test
    void testCreateUser_CaseInsensitiveOrganizationLookup() {
        UserOrganizationDto orgDto = new UserOrganizationDto();
        orgDto.setOrganization("testorg"); // lowercase
        orgDto.setRole("USER");

        UserDto userDto = new UserDto(
                "newuser@example.com",
                "New",
                "User",
                false,
                List.of(orgDto));

        User newUser = new User();
        newUser.setEmail("newuser@example.com");

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(201);

        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);
        String realm = "cvmanager";

        when(keycloakAdminConfig.getRealm()).thenReturn(realm);
        when(keycloakAdminConfig.keyCloakBuilder()).thenReturn(keycloak);
        when(keycloak.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(newUser));

        when(organizationRepository.findByName("testorg")).thenReturn(Optional.of(testOrganization));
        when(roleRepository.findByNameIgnoreCase("USER")).thenReturn(Optional.of(testRole));
        when(userOrganizationRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        User result = userManagementService.createUser(userDto);

        assertNotNull(result);
        verify(usersResource).create(any(UserRepresentation.class));
        verify(organizationRepository).findByName("testorg");
    }

    @Test
    void testCreateUser_CaseInsensitiveRoleLookup() {
        UserOrganizationDto orgDto = new UserOrganizationDto();
        orgDto.setOrganization("TestOrg");
        orgDto.setRole("admin"); // lowercase

        UserDto userDto = new UserDto(
                "newuser@example.com",
                "New",
                "User",
                false,
                List.of(orgDto));

        User newUser = new User();
        newUser.setEmail("newuser@example.com");

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(201);

        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realmResource = mock(RealmResource.class);
        UsersResource usersResource = mock(UsersResource.class);
        String realm = "cvmanager";

        when(keycloakAdminConfig.getRealm()).thenReturn(realm);
        when(keycloakAdminConfig.keyCloakBuilder()).thenReturn(keycloak);
        when(keycloak.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(newUser));

        when(organizationRepository.findByName("TestOrg")).thenReturn(Optional.of(testOrganization));
        when(roleRepository.findByNameIgnoreCase("admin")).thenReturn(Optional.of(testRole));
        when(userOrganizationRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        User result = userManagementService.createUser(userDto);

        assertNotNull(result);
        verify(usersResource).create(any(UserRepresentation.class));
        verify(roleRepository).findByNameIgnoreCase("admin");
    }

    // ==================== createUserOrgRelationship Tests ====================

    @Test
    void testCreateUserOrgRelationship_Success() {
        UserOrganizationDto orgDto = new UserOrganizationDto();
        orgDto.setOrganization("TestOrg");
        orgDto.setRole("USER");

        when(organizationRepository.findByName("TestOrg")).thenReturn(Optional.of(testOrganization));
        when(roleRepository.findByNameIgnoreCase("USER")).thenReturn(Optional.of(testRole));

        UserOrganization result = userManagementService.createUserOrgRelationship(orgDto, testUser);

        assertNotNull(result);
        assertEquals(testOrganization, result.getOrganization());
        assertEquals(testUser, result.getUser());
        assertEquals(testRole, result.getRole());
        verify(organizationRepository).findByName("TestOrg");
        verify(roleRepository).findByNameIgnoreCase("USER");
    }

    @Test
    void testCreateUserOrgRelationship_WithAdminRole() {
        UserOrganizationDto orgDto = new UserOrganizationDto();
        orgDto.setOrganization("TestOrg");
        orgDto.setRole("ADMIN");

        Role adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName("ADMIN");

        when(organizationRepository.findByName("TestOrg")).thenReturn(Optional.of(testOrganization));
        when(roleRepository.findByNameIgnoreCase("ADMIN")).thenReturn(Optional.of(adminRole));

        UserOrganization result = userManagementService.createUserOrgRelationship(orgDto, testUser);

        assertNotNull(result);
        assertEquals("ADMIN", result.getRole().getName());
        verify(roleRepository).findByNameIgnoreCase("ADMIN");
    }

    @Test
    void testCreateUserOrgRelationship_WithOperatorRole() {
        UserOrganizationDto orgDto = new UserOrganizationDto();
        orgDto.setOrganization("TestOrg");
        orgDto.setRole("OPERATOR");

        Role operatorRole = new Role();
        operatorRole.setId(3);
        operatorRole.setName("OPERATOR");

        when(organizationRepository.findByName("TestOrg")).thenReturn(Optional.of(testOrganization));
        when(roleRepository.findByNameIgnoreCase("OPERATOR")).thenReturn(Optional.of(operatorRole));

        UserOrganization result = userManagementService.createUserOrgRelationship(orgDto, testUser);

        assertNotNull(result);
        assertEquals("OPERATOR", result.getRole().getName());
        verify(roleRepository).findByNameIgnoreCase("OPERATOR");
    }

    @Test
    void testCreateUserOrgRelationship_OrganizationNotFound() {
        UserOrganizationDto orgDto = new UserOrganizationDto();
        orgDto.setOrganization("NonexistentOrg");
        orgDto.setRole("USER");

        when(organizationRepository.findByName("NonexistentOrg")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userManagementService.createUserOrgRelationship(orgDto, testUser));

        assertTrue(exception.getMessage().contains("Organization not found"));
        assertTrue(exception.getMessage().contains("NonexistentOrg"));
        verify(organizationRepository).findByName("NonexistentOrg");
        verify(roleRepository, never()).findByNameIgnoreCase(anyString());
    }

    @Test
    void testCreateUserOrgRelationship_RoleNotFound() {
        UserOrganizationDto orgDto = new UserOrganizationDto();
        orgDto.setOrganization("TestOrg");
        orgDto.setRole("INVALID_ROLE");

        when(organizationRepository.findByName("TestOrg")).thenReturn(Optional.of(testOrganization));
        when(roleRepository.findByNameIgnoreCase("INVALID_ROLE")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userManagementService.createUserOrgRelationship(orgDto, testUser));

        assertTrue(exception.getMessage().contains("Role not found"));
        assertTrue(exception.getMessage().contains("INVALID_ROLE"));
        verify(organizationRepository).findByName("TestOrg");
        verify(roleRepository).findByNameIgnoreCase("INVALID_ROLE");
    }

    @Test
    void testCreateUserOrgRelationship_CaseInsensitiveOrganization() {
        UserOrganizationDto orgDto = new UserOrganizationDto();
        orgDto.setOrganization("TESTORG"); // uppercase
        orgDto.setRole("USER");

        when(organizationRepository.findByName("TESTORG")).thenReturn(Optional.of(testOrganization));
        when(roleRepository.findByNameIgnoreCase("USER")).thenReturn(Optional.of(testRole));

        UserOrganization result = userManagementService.createUserOrgRelationship(orgDto, testUser);

        assertNotNull(result);
        assertEquals(testOrganization, result.getOrganization());
        verify(organizationRepository).findByName("TESTORG");
    }

    @Test
    void testCreateUserOrgRelationship_CaseInsensitiveRole() {
        UserOrganizationDto orgDto = new UserOrganizationDto();
        orgDto.setOrganization("TestOrg");
        orgDto.setRole("user"); // lowercase

        when(organizationRepository.findByName("TestOrg")).thenReturn(Optional.of(testOrganization));
        when(roleRepository.findByNameIgnoreCase("user")).thenReturn(Optional.of(testRole));

        UserOrganization result = userManagementService.createUserOrgRelationship(orgDto, testUser);

        assertNotNull(result);
        assertEquals(testRole, result.getRole());
        verify(roleRepository).findByNameIgnoreCase("user");
    }

    @Test
    void testCreateUserOrgRelationship_DifferentOrganization() {
        Organization anotherOrg = new Organization();
        anotherOrg.setId(2);
        anotherOrg.setName("AnotherOrg");

        UserOrganizationDto orgDto = new UserOrganizationDto();
        orgDto.setOrganization("AnotherOrg");
        orgDto.setRole("USER");

        when(organizationRepository.findByName("AnotherOrg")).thenReturn(Optional.of(anotherOrg));
        when(roleRepository.findByNameIgnoreCase("USER")).thenReturn(Optional.of(testRole));

        UserOrganization result = userManagementService.createUserOrgRelationship(orgDto, testUser);

        assertNotNull(result);
        assertEquals(anotherOrg, result.getOrganization());
        assertEquals("AnotherOrg", result.getOrganization().getName());
        verify(organizationRepository).findByName("AnotherOrg");
    }

    @Test
    void testCreateUserOrgRelationship_UserAssignment() {
        UserOrganizationDto orgDto = new UserOrganizationDto();
        orgDto.setOrganization("TestOrg");
        orgDto.setRole("USER");

        when(organizationRepository.findByName("TestOrg")).thenReturn(Optional.of(testOrganization));
        when(roleRepository.findByNameIgnoreCase("USER")).thenReturn(Optional.of(testRole));

        UserOrganization result = userManagementService.createUserOrgRelationship(orgDto, testUser);

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(testUser.getEmail(), result.getUser().getEmail());
    }
}