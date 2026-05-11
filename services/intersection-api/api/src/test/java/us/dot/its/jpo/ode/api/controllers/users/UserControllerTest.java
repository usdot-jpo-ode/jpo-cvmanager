package us.dot.its.jpo.ode.api.controllers.users;

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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import us.dot.its.jpo.ode.api.models.UserRole;
import us.dot.its.jpo.ode.api.models.keycloak.CvManagerAuthToken;
import us.dot.its.jpo.ode.api.models.postgres.tables.User;
import us.dot.its.jpo.ode.api.models.users.ModifyUserAllowedSelections;
import us.dot.its.jpo.ode.api.models.users.UserDto;
import us.dot.its.jpo.ode.api.models.users.UserOrganizationDto;
import us.dot.its.jpo.ode.api.models.users.UserPatch;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.api.services.UserManagementService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserManagementService userManagementService;

    @Mock
    private PermissionService permissionService;

    @Mock
    private CvManagerAuthToken authToken;

    @InjectMocks
    private UserController userController;

    private UserDto testUserDto;
    private String testToken = "Bearer mock-jwt-token";

    @BeforeEach
    void setUp() {
        // Set up test user DTO
        testUserDto = new UserDto("test@example.com", "Test", "User", false, List.of());

        // Set up mock request context
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", testToken);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    // ==================== getUsers Tests ====================

    @Test
    void testGetUsers_Success() {
        String organization = "TestOrg";
        String search = "";
        Pageable pageable = PageRequest.of(0, 100);
        List<UserDto> users = List.of(testUserDto);
        Page<UserDto> userPage = new PageImpl<>(users, pageable, 1);

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("test@example.com", result.getContent().get(0).getEmail());
        verify(userManagementService).getUsers(eq(organization), eq(search), any(Pageable.class));
    }

    @Test
    void testGetUsers_WithSearch() {
        String organization = "TestOrg";
        String search = "test";
        Pageable pageable = PageRequest.of(0, 100);
        List<UserDto> users = List.of(testUserDto);
        Page<UserDto> userPage = new PageImpl<>(users, pageable, 1);

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userManagementService).getUsers(eq(organization), eq(search), any(Pageable.class));
    }

    @Test
    void testGetUsers_WithSorting_FirstName() {
        String organization = "TestOrg";
        String search = "";
        Pageable pageable = PageRequest.of(0, 100, Sort.by("first_name").ascending());
        List<UserDto> users = List.of(testUserDto);
        Page<UserDto> userPage = new PageImpl<>(users, pageable, 1);

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        assertNotNull(result);
        verify(userManagementService).getUsers(eq(organization), eq(search), argThat(p -> {
            Sort.Order order = p.getSort().iterator().next();
            return "firstName".equals(order.getProperty()) && order.isAscending();
        }));
    }

    @Test
    void testGetUsers_WithSorting_LastName() {
        String organization = "TestOrg";
        String search = "";
        Pageable pageable = PageRequest.of(0, 100, Sort.by("last_name").descending());
        List<UserDto> users = List.of(testUserDto);
        Page<UserDto> userPage = new PageImpl<>(users, pageable, 1);

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        assertNotNull(result);
        verify(userManagementService).getUsers(eq(organization), eq(search), argThat(p -> {
            Sort.Order order = p.getSort().iterator().next();
            return "lastName".equals(order.getProperty()) && order.isDescending();
        }));
    }

    @Test
    void testGetUsers_WithSorting_SuperUser() {
        String organization = "TestOrg";
        String search = "";
        Pageable pageable = PageRequest.of(0, 100, Sort.by("super_user").ascending());
        List<UserDto> users = List.of(testUserDto);
        Page<UserDto> userPage = new PageImpl<>(users, pageable, 1);

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        assertNotNull(result);
        verify(userManagementService).getUsers(eq(organization), eq(search), argThat(p -> {
            Sort.Order order = p.getSort().iterator().next();
            return "superUser".equals(order.getProperty());
        }));
    }

    @Test
    void testGetUsers_WithSorting_UnmappedField() {
        String organization = "TestOrg";
        String search = "";
        Pageable pageable = PageRequest.of(0, 100, Sort.by("email").ascending());
        List<UserDto> users = List.of(testUserDto);
        Page<UserDto> userPage = new PageImpl<>(users, pageable, 1);

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        assertNotNull(result);
        // Should keep original field name if not in mapping
        verify(userManagementService).getUsers(eq(organization), eq(search), argThat(p -> {
            Sort.Order order = p.getSort().iterator().next();
            return "email".equals(order.getProperty());
        }));
    }

    @Test
    void testGetUsers_NoSorting() {
        String organization = "TestOrg";
        String search = "";
        Pageable pageable = PageRequest.of(0, 100);
        List<UserDto> users = List.of(testUserDto);
        Page<UserDto> userPage = new PageImpl<>(users, pageable, 1);

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        assertNotNull(result);
        verify(userManagementService).getUsers(eq(organization), eq(search), argThat(p -> !p.getSort().isSorted()));
    }

    @Test
    void testGetUsers_EmptyResults() {
        String organization = "TestOrg";
        String search = "nonexistent";
        Pageable pageable = PageRequest.of(0, 100);
        Page<UserDto> userPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void testGetUsers_Pagination() {
        String organization = "TestOrg";
        String search = "";
        Pageable pageable = PageRequest.of(1, 25); // Page 2, size 25
        List<UserDto> users = List.of(testUserDto);
        Page<UserDto> userPage = new PageImpl<>(users, pageable, 100); // 100 total

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        assertNotNull(result);
        assertEquals(1, result.getNumber()); // Page number
        assertEquals(25, result.getSize()); // Page size
        assertEquals(100, result.getTotalElements()); // Total elements
        assertEquals(4, result.getTotalPages()); // Total pages (100/25)
    }

    // ==================== getSingleUser Tests ====================

    @Test
    void testGetSingleUser_Success() {
        String email = "test@example.com";
        when(userManagementService.getUser(email)).thenReturn(testUserDto);

        UserDto result = userController.getSingleUser(email);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userManagementService).getUser(email);
    }

    @Test
    void testGetSingleUser_DifferentEmail() {
        String email = "another@example.com";
        UserDto anotherUser = new UserDto(email, "Another", "User", false, List.of());
        when(userManagementService.getUser(email)).thenReturn(anotherUser);

        UserDto result = userController.getSingleUser(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userManagementService).getUser(email);
    }

    // ==================== getAllowedSelections Tests ====================

    @Test
    void testGetAllowedSelections_Success() {
        ModifyUserAllowedSelections allowedSelections = new ModifyUserAllowedSelections();
        allowedSelections.setRoles(List.of("admin", "operator", "user"));
        allowedSelections.setOrganizations(List.of("TestOrg", "AnotherOrg"));

        when(userManagementService.getAllowedSelections(any(CvManagerAuthToken.class)))
                .thenReturn(allowedSelections);

        when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);

        ModifyUserAllowedSelections result = userController.getAllowedSelections();

        assertNotNull(result);
        assertEquals(3, result.getRoles().size());
        assertEquals(2, result.getOrganizations().size());
        assertTrue(result.getRoles().contains("admin"));
        assertTrue(result.getOrganizations().contains("TestOrg"));
        verify(userManagementService).getAllowedSelections(authToken);
    }

    @Test
    void testGetAllowedSelections_EmptySelections() {
        ModifyUserAllowedSelections allowedSelections = new ModifyUserAllowedSelections();
        allowedSelections.setRoles(new ArrayList<>());
        allowedSelections.setOrganizations(new ArrayList<>());

        when(userManagementService.getAllowedSelections(any(CvManagerAuthToken.class)))
                .thenReturn(allowedSelections);

        when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);

        ModifyUserAllowedSelections result = userController.getAllowedSelections();

        assertNotNull(result);
        assertTrue(result.getRoles().isEmpty());
        assertTrue(result.getOrganizations().isEmpty());
    }

    // ==================== createUser Tests ====================

    @Test
    void testCreateUser_Success() {
        UserOrganizationDto org1 = new UserOrganizationDto();
        org1.setOrganization("TestOrg");
        org1.setRole("USER");

        UserDto newUser = new UserDto(
                "newuser@example.com",
                "New",
                "User",
                false,
                List.of(org1));

        when(permissionService.hasRoleInOrgs(UserRole.ADMIN, List.of("TestOrg")))
                .thenReturn(true);
        when(userManagementService.createUser(newUser)).thenReturn(new User());

        userController.createUser(newUser);

        verify(permissionService).hasRoleInOrgs(UserRole.ADMIN, List.of("TestOrg"));
        verify(userManagementService).createUser(newUser);
    }

    @Test
    void testCreateUser_MultipleOrganizations() {
        UserOrganizationDto org1 = new UserOrganizationDto();
        org1.setOrganization("TestOrg");
        org1.setRole("USER");

        UserOrganizationDto org2 = new UserOrganizationDto();
        org2.setOrganization("AnotherOrg");
        org2.setRole("OPERATOR");

        UserDto newUser = new UserDto(
                "newuser@example.com",
                "New",
                "User",
                false,
                List.of(org1, org2));

        when(permissionService.hasRoleInOrgs(UserRole.ADMIN, List.of("TestOrg", "AnotherOrg")))
                .thenReturn(true);
        when(userManagementService.createUser(newUser)).thenReturn(new User());

        userController.createUser(newUser);

        verify(permissionService).hasRoleInOrgs(UserRole.ADMIN, List.of("TestOrg", "AnotherOrg"));
        verify(userManagementService).createUser(newUser);
    }

    @Test
    void testCreateUser_WithSuperUserFlag() {
        UserOrganizationDto org = new UserOrganizationDto();
        org.setOrganization("TestOrg");
        org.setRole("ADMIN");

        UserDto newUser = new UserDto(
                "admin@example.com",
                "Admin",
                "User",
                true, // super_user = true
                List.of(org));

        when(permissionService.hasRoleInOrgs(UserRole.ADMIN, List.of("TestOrg")))
                .thenReturn(true);
        when(permissionService.isSuperUser())
                .thenReturn(true);
        when(userManagementService.createUser(newUser)).thenReturn(new User());

        userController.createUser(newUser);

        assertTrue(newUser.getSuperUser());
        verify(userManagementService).createUser(newUser);
    }

    @Test
    void testCreateUser_Forbidden_NoAdminRoleInOrganization() {
        UserOrganizationDto org = new UserOrganizationDto();
        org.setOrganization("TestOrg");
        org.setRole("USER");

        UserDto newUser = new UserDto(
                "newuser@example.com",
                "New",
                "User",
                false,
                List.of(org));

        when(permissionService.hasRoleInOrgs(UserRole.ADMIN, List.of("TestOrg")))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userController.createUser(newUser));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("User not qualified to modify all specified organizations", exception.getReason());
        verify(permissionService).hasRoleInOrgs(UserRole.ADMIN, List.of("TestOrg"));
        verify(userManagementService, never()).createUser(any());
    }

    @Test
    void testCreateUser_Forbidden_NoAdminRoleInOneOfMultipleOrganizations() {
        UserOrganizationDto org1 = new UserOrganizationDto();
        org1.setOrganization("TestOrg");
        org1.setRole("USER");

        UserOrganizationDto org2 = new UserOrganizationDto();
        org2.setOrganization("UnauthorizedOrg");
        org2.setRole("USER");

        UserDto newUser = new UserDto(
                "newuser@example.com",
                "New",
                "User",
                false,
                List.of(org1, org2));

        when(permissionService.hasRoleInOrgs(UserRole.ADMIN, List.of("TestOrg", "UnauthorizedOrg")))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userController.createUser(newUser));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("User not qualified to modify all specified organizations", exception.getReason());
        verify(permissionService).hasRoleInOrgs(UserRole.ADMIN, List.of("TestOrg", "UnauthorizedOrg"));
        verify(userManagementService, never()).createUser(any());
    }

    @Test
    void testCreateUser_NonexistentOrganization() {
        UserOrganizationDto org = new UserOrganizationDto();
        org.setOrganization("NonexistentOrg");
        org.setRole("USER");

        UserDto newUser = new UserDto(
                "newuser@example.com",
                "New",
                "User",
                false,
                List.of(org));

        // hasRoleInOrgs returns false for nonexistent organizations
        when(permissionService.hasRoleInOrgs(UserRole.ADMIN, List.of("NonexistentOrg")))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userController.createUser(newUser));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("User not qualified to modify all specified organizations", exception.getReason());
        verify(userManagementService, never()).createUser(any());
    }

    @Test
    void testCreateUser_EmptyOrganizationsList() {
        UserDto newUser = new UserDto(
                "newuser@example.com",
                "New",
                "User",
                false,
                List.of() // Empty organizations list
        );

        when(permissionService.hasRoleInOrgs(UserRole.ADMIN, List.of()))
                .thenReturn(true);
        when(userManagementService.createUser(newUser)).thenReturn(new User());

        userController.createUser(newUser);

        verify(permissionService).hasRoleInOrgs(UserRole.ADMIN, List.of());
        verify(userManagementService).createUser(newUser);
    }

    @Test
    void testCreateUser_WithDifferentRoles() {
        UserOrganizationDto orgAdmin = new UserOrganizationDto();
        orgAdmin.setOrganization("TestOrg1");
        orgAdmin.setRole("ADMIN");

        UserOrganizationDto orgOperator = new UserOrganizationDto();
        orgOperator.setOrganization("TestOrg2");
        orgOperator.setRole("OPERATOR");

        UserOrganizationDto orgUser = new UserOrganizationDto();
        orgUser.setOrganization("TestOrg3");
        orgUser.setRole("USER");

        UserDto newUser = new UserDto(
                "newuser@example.com",
                "New",
                "User",
                false,
                List.of(orgAdmin, orgOperator, orgUser));

        when(permissionService.hasRoleInOrgs(UserRole.ADMIN, List.of("TestOrg1", "TestOrg2", "TestOrg3")))
                .thenReturn(true);
        when(userManagementService.createUser(newUser)).thenReturn(new User());

        userController.createUser(newUser);

        verify(permissionService).hasRoleInOrgs(UserRole.ADMIN, List.of("TestOrg1", "TestOrg2", "TestOrg3"));
        verify(userManagementService).createUser(newUser);
    }

    @Test
    void testCreateUser_WithSpecialCharactersInEmail() {
        UserOrganizationDto org = new UserOrganizationDto();
        org.setOrganization("TestOrg");
        org.setRole("USER");

        UserDto newUser = new UserDto(
                "new.user+tag@example.co.uk",
                "New",
                "User",
                false,
                List.of(org));

        when(permissionService.hasRoleInOrgs(UserRole.ADMIN, List.of("TestOrg")))
                .thenReturn(true);
        when(userManagementService.createUser(newUser)).thenReturn(new User());

        userController.createUser(newUser);

        assertEquals("new.user+tag@example.co.uk", newUser.getEmail());
        verify(userManagementService).createUser(newUser);
    }

    @Test
    void testCreateUser_ServiceLayerValidationHandled() {
        UserOrganizationDto org = new UserOrganizationDto();
        org.setOrganization("TestOrg");
        org.setRole("USER");

        UserDto newUser = new UserDto(
                "duplicate@example.com",
                "New",
                "User",
                false,
                List.of(org));

        when(permissionService.hasRoleInOrgs(UserRole.ADMIN, List.of("TestOrg")))
                .thenReturn(true);
        doThrow(new IllegalArgumentException("User with email already exists"))
                .when(userManagementService).createUser(newUser);

        assertThrows(IllegalArgumentException.class, () -> userController.createUser(newUser));
        verify(permissionService).hasRoleInOrgs(UserRole.ADMIN, List.of("TestOrg"));
        verify(userManagementService).createUser(newUser);
    }

    // ==================== modifyUser Tests ====================

    @Test
    void testModifyUser_Success() {
        String email = "test@example.com";
        UserPatch userPatch = new UserPatch();
        userPatch.setFirstName("Updated");
        userPatch.setLastName("Name");

        when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
        when(userManagementService.modifyUser(email, userPatch, authToken))
                .thenReturn(testUserDto);

        ResponseEntity<Void> result = userController.modifyUser(email, userPatch);

        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userManagementService).modifyUser(email, userPatch, authToken);
    }

    @Test
    void testModifyUser_WithOrganizationChanges() {
        String email = "test@example.com";
        UserPatch userPatch = new UserPatch();
        userPatch.setOrganizationsToAdd(List.of());
        userPatch.setOrganizationsToRemove(List.of());

        when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
        when(userManagementService.modifyUser(email, userPatch, authToken))
                .thenReturn(testUserDto);

        ResponseEntity<Void> result = userController.modifyUser(email, userPatch);

        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userManagementService).modifyUser(email, userPatch, authToken);
    }

    // ==================== deleteUser Tests ====================

    @Test
    void testDeleteUser_Success() {
        String email = "test@example.com";
        doNothing().when(userManagementService).deleteUserByEmail(email);

        ResponseEntity<Void> result = userController.deleteUser(email);

        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userManagementService).deleteUserByEmail(email);
    }

    @Test
    void testDeleteUser_DifferentEmail() {
        String email = "another@example.com";
        doNothing().when(userManagementService).deleteUserByEmail(email);

        ResponseEntity<Void> result = userController.deleteUser(email);

        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userManagementService).deleteUserByEmail(email);
    }

    // ==================== deleteUsers Tests ====================

    @Test
    void testDeleteUsers_Success() {
        List<String> emails = List.of("test1@example.com", "test2@example.com");
        doNothing().when(userManagementService).deleteMultipleUsersByEmail(emails);

        ResponseEntity<Void> result = userController.deleteUsers(emails);

        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userManagementService).deleteMultipleUsersByEmail(emails);
    }

    @Test
    void testDeleteUsers_SingleUser() {
        List<String> emails = List.of("test@example.com");
        doNothing().when(userManagementService).deleteMultipleUsersByEmail(emails);

        ResponseEntity<Void> result = userController.deleteUsers(emails);

        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userManagementService).deleteMultipleUsersByEmail(emails);
    }

    @Test
    void testDeleteUsers_MultipleUsers() {
        List<String> emails = List.of(
                "test1@example.com",
                "test2@example.com",
                "test3@example.com",
                "test4@example.com");
        doNothing().when(userManagementService).deleteMultipleUsersByEmail(emails);

        ResponseEntity<Void> result = userController.deleteUsers(emails);

        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userManagementService).deleteMultipleUsersByEmail(emails);
    }

    @Test
    void testDeleteUsers_EmptyList() {
        List<String> emails = new ArrayList<>();
        doNothing().when(userManagementService).deleteMultipleUsersByEmail(emails);

        ResponseEntity<Void> result = userController.deleteUsers(emails);

        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userManagementService).deleteMultipleUsersByEmail(emails);
    }
}