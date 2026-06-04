package us.dot.its.jpo.ode.api.controllers.users;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.ode.api.models.UserRole;
import us.dot.its.jpo.ode.api.models.emails.UserEmailNotificationDto;
import us.dot.its.jpo.ode.api.models.keycloak.CvManagerAuthToken;
import us.dot.its.jpo.ode.api.TestcontainersConfiguration;
import us.dot.its.jpo.ode.api.services.EmailService;
import us.dot.its.jpo.ode.api.services.PermissionService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private PermissionService permissionService;

    private CvManagerAuthToken authToken;

    private static final String email = "user@example.com";

    private static final List<UserEmailNotificationDto> validSubscriptionsList = Arrays.asList(
            new UserEmailNotificationDto("Support Requests", "Receive support requests from users", "admin", true,
                    false, false, false, false,
                    true, false, false, false, false),
            new UserEmailNotificationDto("Firmware Upgrade Failures",
                    "Receive automated firmware upgrade failure emails",
                    "operator", true, false, false, false, false,
                    true, false, false, false, false));

    private static final List<String> operatorOrgList = List.of("operator_org");
    private static final List<String> operatorAdminList = List.of("operator_org, admin_org");

    @BeforeEach
    void setUp() {
        authToken = Mockito.mock(CvManagerAuthToken.class);
    }

    @Nested
    @DisplayName("GET /users/subscriptions/email-subscriptions — list all subscriptions")
    class GetAllSubscriptions {

        @Test
        @DisplayName("returns 403 when no permissions are granted (unauthenticated)")
        void noPermissions_returns403() throws Exception {
            // Spring Security filter runs before argument binding; unauthenticated → 403
            mockMvc.perform(get("/users/subscriptions/email-subscriptions"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when authenticated but neither isSuperUser nor hasRole('USER')")
        void authenticated_insufficientPermissions_returns403() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.USER)).thenReturn(false);

            mockMvc.perform(get("/users/subscriptions/email-subscriptions"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 200 with subscriptions list when has only user role")
        void authenticated_returns200User() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.USER)).thenReturn(true);
            when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
            when(authToken.getEmail()).thenReturn(email);
            when(authToken.getQualifiedOrgList(UserRole.OPERATOR)).thenReturn(List.of());
            when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of());
            when(emailService.getAllEmailSubscriptionOptionsForUser(email, false, false))
                    .thenReturn(validSubscriptionsList);

            mockMvc.perform(get("/users/subscriptions/email-subscriptions"))
                    .andExpect(jsonPath("$.subscriptions").isArray())
                    .andExpect(jsonPath("$.subscriptions[0].category").value("Support Requests"))
                    .andExpect(jsonPath("$.subscriptions[1].category").value("Firmware Upgrade Failures"))
                    .andExpect(jsonPath("$.email").value(email));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 200 with subscriptions list when has operator role")
        void authenticated_returns200Operator() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.USER)).thenReturn(true);
            when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
            when(authToken.getEmail()).thenReturn(email);
            when(authToken.getQualifiedOrgList(UserRole.OPERATOR)).thenReturn(operatorOrgList);
            when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(List.of());
            when(emailService.getAllEmailSubscriptionOptionsForUser(email, true, false))
                    .thenReturn(validSubscriptionsList);

            mockMvc.perform(get("/users/subscriptions/email-subscriptions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subscriptions").isArray())
                    .andExpect(jsonPath("$.subscriptions[0].category").value("Support Requests"))
                    .andExpect(jsonPath("$.subscriptions[1].category").value("Firmware Upgrade Failures"))
                    .andExpect(jsonPath("$.email").value(email));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 200 with subscriptions list when has admin role")
        void authenticated_returns200Admin() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.USER)).thenReturn(true);
            when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
            when(authToken.getEmail()).thenReturn(email);
            when(authToken.getQualifiedOrgList(UserRole.OPERATOR)).thenReturn(operatorOrgList);
            when(authToken.getQualifiedOrgList(UserRole.ADMIN)).thenReturn(operatorAdminList);
            when(emailService.getAllEmailSubscriptionOptionsForUser(email, true, true))
                    .thenReturn(validSubscriptionsList);

            mockMvc.perform(get("/users/subscriptions/email-subscriptions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subscriptions").isArray())
                    .andExpect(jsonPath("$.subscriptions[0].category").value("Support Requests"))
                    .andExpect(jsonPath("$.subscriptions[1].category").value("Firmware Upgrade Failures"))
                    .andExpect(jsonPath("$.email").value(email));
        }
    }

    @Nested
    @DisplayName("POST /users/subscriptions/email-subscriptions — update all subscriptions")
    class UpdateAllSubscriptions {

        @Test
        @DisplayName("returns 403 when no permissions are granted (unauthenticated)")
        void noPermissions_returns403() throws Exception {
            // Spring Security filter runs before argument binding; unauthenticated → 403
            mockMvc.perform(post("/users/subscriptions/email-subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSubscriptionsList)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when authenticated but neither isSuperUser nor hasRole('USER')")
        void authenticated_insufficientPermissions_returns403() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.USER)).thenReturn(false);

            mockMvc.perform(post("/users/subscriptions/email-subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSubscriptionsList)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 200 with valid subscriptions list")
        void superUser_returns200() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);
            when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
            when(authToken.getEmail()).thenReturn(email);
            doNothing().when(emailService).updateEmailSubscriptions(email, true, true, validSubscriptionsList);

            mockMvc.perform(post("/users/subscriptions/email-subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSubscriptionsList)))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 with invalid subscriptions list")
        void superUser_returns400() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);
            when(permissionService.getCvManagerAuthToken()).thenReturn(authToken);
            when(authToken.getEmail()).thenReturn(email);
            doNothing().when(emailService).updateEmailSubscriptions(email, true, true, validSubscriptionsList);

            mockMvc.perform(post("/users/subscriptions/email-subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"invalid\": true}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
