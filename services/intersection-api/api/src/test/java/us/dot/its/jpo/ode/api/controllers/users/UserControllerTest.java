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

import us.dot.its.jpo.ode.api.models.keycloak.CvManagerAuthToken;
import us.dot.its.jpo.ode.api.models.users.ModifyUserAllowedSelections;
import us.dot.its.jpo.ode.api.models.users.UserDto;
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
        testUserDto = new UserDto(1, "test@example.com", "Test", "User", false, List.of());

        // Set up mock request context
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", testToken);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    // ==================== getUsers Tests ====================

    @Test
    void testGetUsers_Success() {
        // Arrange
        String organization = "TestOrg";
        String search = "";
        Pageable pageable = PageRequest.of(0, 100);
        List<UserDto> users = List.of(testUserDto);
        Page<UserDto> userPage = new PageImpl<>(users, pageable, 1);

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        // Act
        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("test@example.com", result.getContent().get(0).getEmail());
        verify(userManagementService).getUsers(eq(organization), eq(search), any(Pageable.class));
    }

    @Test
    void testGetUsers_WithSearch() {
        // Arrange
        String organization = "TestOrg";
        String search = "test";
        Pageable pageable = PageRequest.of(0, 100);
        List<UserDto> users = List.of(testUserDto);
        Page<UserDto> userPage = new PageImpl<>(users, pageable, 1);

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        // Act
        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userManagementService).getUsers(eq(organization), eq(search), any(Pageable.class));
    }

    @Test
    void testGetUsers_WithSorting_FirstName() {
        // Arrange
        String organization = "TestOrg";
        String search = "";
        Pageable pageable = PageRequest.of(0, 100, Sort.by("first_name").ascending());
        List<UserDto> users = List.of(testUserDto);
        Page<UserDto> userPage = new PageImpl<>(users, pageable, 1);

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        // Act
        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        // Assert
        assertNotNull(result);
        verify(userManagementService).getUsers(eq(organization), eq(search), argThat(p -> {
            Sort.Order order = p.getSort().iterator().next();
            return "firstName".equals(order.getProperty()) && order.isAscending();
        }));
    }

    @Test
    void testGetUsers_WithSorting_LastName() {
        // Arrange
        String organization = "TestOrg";
        String search = "";
        Pageable pageable = PageRequest.of(0, 100, Sort.by("last_name").descending());
        List<UserDto> users = List.of(testUserDto);
        Page<UserDto> userPage = new PageImpl<>(users, pageable, 1);

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        // Act
        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        // Assert
        assertNotNull(result);
        verify(userManagementService).getUsers(eq(organization), eq(search), argThat(p -> {
            Sort.Order order = p.getSort().iterator().next();
            return "lastName".equals(order.getProperty()) && order.isDescending();
        }));
    }

    @Test
    void testGetUsers_WithSorting_SuperUser() {
        // Arrange
        String organization = "TestOrg";
        String search = "";
        Pageable pageable = PageRequest.of(0, 100, Sort.by("super_user").ascending());
        List<UserDto> users = List.of(testUserDto);
        Page<UserDto> userPage = new PageImpl<>(users, pageable, 1);

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        // Act
        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        // Assert
        assertNotNull(result);
        verify(userManagementService).getUsers(eq(organization), eq(search), argThat(p -> {
            Sort.Order order = p.getSort().iterator().next();
            return "superUser".equals(order.getProperty());
        }));
    }

    @Test
    void testGetUsers_WithSorting_UnmappedField() {
        // Arrange
        String organization = "TestOrg";
        String search = "";
        Pageable pageable = PageRequest.of(0, 100, Sort.by("email").ascending());
        List<UserDto> users = List.of(testUserDto);
        Page<UserDto> userPage = new PageImpl<>(users, pageable, 1);

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        // Act
        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        // Assert
        assertNotNull(result);
        // Should keep original field name if not in mapping
        verify(userManagementService).getUsers(eq(organization), eq(search), argThat(p -> {
            Sort.Order order = p.getSort().iterator().next();
            return "email".equals(order.getProperty());
        }));
    }

    @Test
    void testGetUsers_NoSorting() {
        // Arrange
        String organization = "TestOrg";
        String search = "";
        Pageable pageable = PageRequest.of(0, 100);
        List<UserDto> users = List.of(testUserDto);
        Page<UserDto> userPage = new PageImpl<>(users, pageable, 1);

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        // Act
        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        // Assert
        assertNotNull(result);
        verify(userManagementService).getUsers(eq(organization), eq(search), argThat(p -> !p.getSort().isSorted()));
    }

    @Test
    void testGetUsers_EmptyResults() {
        // Arrange
        String organization = "TestOrg";
        String search = "nonexistent";
        Pageable pageable = PageRequest.of(0, 100);
        Page<UserDto> userPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        // Act
        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void testGetUsers_Pagination() {
        // Arrange
        String organization = "TestOrg";
        String search = "";
        Pageable pageable = PageRequest.of(1, 25); // Page 2, size 25
        List<UserDto> users = List.of(testUserDto);
        Page<UserDto> userPage = new PageImpl<>(users, pageable, 100); // 100 total

        when(userManagementService.getUsers(eq(organization), eq(search), any(Pageable.class)))
                .thenReturn(userPage);

        // Act
        Page<UserDto> result = userController.getUsers(organization, search, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getNumber()); // Page number
        assertEquals(25, result.getSize()); // Page size
        assertEquals(100, result.getTotalElements()); // Total elements
        assertEquals(4, result.getTotalPages()); // Total pages (100/25)
    }

    // ==================== getSingleUser Tests ====================

    @Test
    void testGetSingleUser_Success() {
        // Arrange
        String email = "test@example.com";
        when(userManagementService.getUser(email)).thenReturn(testUserDto);

        // Act
        UserDto result = userController.getSingleUser(email);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userManagementService).getUser(email);
    }

    @Test
    void testGetSingleUser_DifferentEmail() {
        // Arrange
        String email = "another@example.com";
        UserDto anotherUser = new UserDto(2, email, "Another", "User", false, List.of());
        when(userManagementService.getUser(email)).thenReturn(anotherUser);

        // Act
        UserDto result = userController.getSingleUser(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userManagementService).getUser(email);
    }

    // ==================== getAllowedSelections Tests ====================

    @Test
    void testGetAllowedSelections_Success() {
        // Arrange
        ModifyUserAllowedSelections allowedSelections = new ModifyUserAllowedSelections();
        allowedSelections.setRoles(List.of("admin", "operator", "user"));
        allowedSelections.setOrganizations(List.of("TestOrg", "AnotherOrg"));

        when(userManagementService.getAllowedSelections(any(CvManagerAuthToken.class)))
                .thenReturn(allowedSelections);

        when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);

        // Act
        ModifyUserAllowedSelections result = userController.getAllowedSelections();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getRoles().size());
        assertEquals(2, result.getOrganizations().size());
        assertTrue(result.getRoles().contains("admin"));
        assertTrue(result.getOrganizations().contains("TestOrg"));
        verify(userManagementService).getAllowedSelections(authToken);
    }

    @Test
    void testGetAllowedSelections_EmptySelections() {
        // Arrange
        ModifyUserAllowedSelections allowedSelections = new ModifyUserAllowedSelections();
        allowedSelections.setRoles(new ArrayList<>());
        allowedSelections.setOrganizations(new ArrayList<>());

        when(userManagementService.getAllowedSelections(any(CvManagerAuthToken.class)))
                .thenReturn(allowedSelections);

        when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);

        // Act
        ModifyUserAllowedSelections result = userController.getAllowedSelections();

        // Assert
        assertNotNull(result);
        assertTrue(result.getRoles().isEmpty());
        assertTrue(result.getOrganizations().isEmpty());
    }

    // ==================== modifyUser Tests ====================

    @Test
    void testModifyUser_Success() {
        // Arrange
        String email = "test@example.com";
        UserPatch userPatch = new UserPatch();
        userPatch.setFirstName("Updated");
        userPatch.setLastName("Name");

        when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
        when(userManagementService.modifyUser(email, userPatch, authToken))
                .thenReturn(testUserDto);

        // Act
        ResponseEntity<Void> result = userController.modifyUser(email, userPatch);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userManagementService).modifyUser(email, userPatch, authToken);
    }

    @Test
    void testModifyUser_WithOrganizationChanges() {
        // Arrange
        String email = "test@example.com";
        UserPatch userPatch = new UserPatch();
        userPatch.setOrganizationsToAdd(List.of());
        userPatch.setOrganizationsToRemove(List.of());

        when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
        when(userManagementService.modifyUser(email, userPatch, authToken))
                .thenReturn(testUserDto);

        // Act
        ResponseEntity<Void> result = userController.modifyUser(email, userPatch);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userManagementService).modifyUser(email, userPatch, authToken);
    }

    // ==================== deleteUser Tests ====================

    @Test
    void testDeleteUser_Success() {
        // Arrange
        String email = "test@example.com";
        doNothing().when(userManagementService).deleteUserByEmail(email);

        // Act
        ResponseEntity<Void> result = userController.deleteUser(email);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userManagementService).deleteUserByEmail(email);
    }

    @Test
    void testDeleteUser_DifferentEmail() {
        // Arrange
        String email = "another@example.com";
        doNothing().when(userManagementService).deleteUserByEmail(email);

        // Act
        ResponseEntity<Void> result = userController.deleteUser(email);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userManagementService).deleteUserByEmail(email);
    }

    // ==================== deleteUsers Tests ====================

    @Test
    void testDeleteUsers_Success() {
        // Arrange
        List<String> emails = List.of("test1@example.com", "test2@example.com");
        doNothing().when(userManagementService).deleteMultipleUsersByEmail(emails);

        // Act
        ResponseEntity<Void> result = userController.deleteUsers(emails);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userManagementService).deleteMultipleUsersByEmail(emails);
    }

    @Test
    void testDeleteUsers_SingleUser() {
        // Arrange
        List<String> emails = List.of("test@example.com");
        doNothing().when(userManagementService).deleteMultipleUsersByEmail(emails);

        // Act
        ResponseEntity<Void> result = userController.deleteUsers(emails);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userManagementService).deleteMultipleUsersByEmail(emails);
    }

    @Test
    void testDeleteUsers_MultipleUsers() {
        // Arrange
        List<String> emails = List.of(
                "test1@example.com",
                "test2@example.com",
                "test3@example.com",
                "test4@example.com");
        doNothing().when(userManagementService).deleteMultipleUsersByEmail(emails);

        // Act
        ResponseEntity<Void> result = userController.deleteUsers(emails);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userManagementService).deleteMultipleUsersByEmail(emails);
    }

    @Test
    void testDeleteUsers_EmptyList() {
        // Arrange
        List<String> emails = new ArrayList<>();
        doNothing().when(userManagementService).deleteMultipleUsersByEmail(emails);

        // Act
        ResponseEntity<Void> result = userController.deleteUsers(emails);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(userManagementService).deleteMultipleUsersByEmail(emails);
    }
}