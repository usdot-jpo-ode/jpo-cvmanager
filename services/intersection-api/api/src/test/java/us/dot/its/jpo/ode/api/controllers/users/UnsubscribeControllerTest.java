package us.dot.its.jpo.ode.api.controllers.users;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

import us.dot.its.jpo.ode.api.TestcontainersConfiguration;
import us.dot.its.jpo.ode.api.emails.UnsubscribeTokenGenerator;
import us.dot.its.jpo.ode.api.models.emails.UserEmailNotificationDto;
import us.dot.its.jpo.ode.api.models.postgres.tables.Role;
import us.dot.its.jpo.ode.api.models.postgres.tables.User;
import us.dot.its.jpo.ode.api.models.postgres.tables.UserOrganization;
import us.dot.its.jpo.ode.api.repositories.UserOrganizationRepository;
import us.dot.its.jpo.ode.api.repositories.UserRepository;
import us.dot.its.jpo.ode.api.services.EmailService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class UnsubscribeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UnsubscribeTokenGenerator unsubscribeTokenGenerator;

    @MockitoBean
    private UserOrganizationRepository userOrganizationRepository;

    private static final String validToken = "valid-token-123";
    private static final String invalidToken = "invalid-token-456";
    private static final String email = "test@example.com";

    private static final List<UserEmailNotificationDto> validSubscriptionList = Arrays.asList(
            new UserEmailNotificationDto("Support Requests", "Receive support requests from users", "admin", true,
                    false, false, false, false,
                    true, false, false, false, false),
            new UserEmailNotificationDto("Firmware Upgrade Failures",
                    "Receive automated firmware upgrade failure emails",
                    "operator", true, false, false, false, false,
                    true, false, false, false, false));

    @Nested
    @DisplayName("GET /users/unsubscribe/email-subscriptions — list all subscriptions")
    class GetAllSubscriptions {

        @Test
        @DisplayName("returns 400 when no token is provided")
        void noToken_returns400() throws Exception {
            mockMvc.perform(get("/users/unsubscribe/email-subscriptions"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when token is invalid")
        void authenticated_invalidToken_returns403() throws Exception {
            when(unsubscribeTokenGenerator.parseAndValidateToken(invalidToken)).thenReturn(null);

            mockMvc.perform(get("/users/unsubscribe/email-subscriptions")
                    .header("Authorization", invalidToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 200 with valid token and permissions")
        void authenticated_validToken_200() throws Exception {
            User mockUser = mock(User.class);
            when(mockUser.getSuperUser()).thenReturn(true);
            when(userRepository.findByEmail(email)).thenReturn(mockUser);

            Role roleOperator = mock(Role.class);
            when(roleOperator.getName()).thenReturn("operator");
            UserOrganization orgOperator = mock(UserOrganization.class);
            when(orgOperator.getRole()).thenReturn(roleOperator);

            Role roleAdmin = mock(Role.class);
            when(roleAdmin.getName()).thenReturn("admin");
            UserOrganization orgAdmin = mock(UserOrganization.class);
            when(orgAdmin.getRole()).thenReturn(roleAdmin);

            List<UserOrganization> authToken = Arrays.asList(orgAdmin, orgOperator);
            when(userOrganizationRepository.findAllByEmail(email)).thenReturn(authToken);

            when(unsubscribeTokenGenerator.parseAndValidateToken(validToken)).thenReturn(email);
            when(emailService.getAllEmailSubscriptionOptionsForUser(email, true, true))
                    .thenReturn(validSubscriptionList);

            mockMvc.perform(get("/users/unsubscribe/email-subscriptions")
                    .header("Authorization", validToken))
                    .andExpect(status().isOk());

            verify(emailService).getAllEmailSubscriptionOptionsForUser(email, true, true);
        }
    }

    @Nested
    @DisplayName("POST /users/unsubscribe/email-subscriptions — update all subscriptions")
    class UpdateAllSubscriptions {

        @Test
        @DisplayName("returns 400 when no token is provided")
        void noToken_returns400() throws Exception {
            mockMvc.perform(post("/users/unsubscribe/email-subscriptions"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when token is invalid")
        void authenticated_invalidToken_returns403() throws Exception {
            when(unsubscribeTokenGenerator.parseAndValidateToken(validToken)).thenReturn(null);

            mockMvc.perform(post("/users/unsubscribe/email-subscriptions")
                    .header("Authorization", invalidToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSubscriptionList)))
                    .andExpect(status().isForbidden());

            verify(emailService, never()).updateEmailSubscriptions(email, true, true, validSubscriptionList);
        }

        @Test
        @WithMockUser
        @DisplayName("returns 200 with valid subscriptions list")
        void superUser_returns200() throws Exception {
            User mockUser = mock(User.class);
            when(mockUser.getSuperUser()).thenReturn(true);
            when(userRepository.findByEmail(email)).thenReturn(mockUser);

            when(unsubscribeTokenGenerator.parseAndValidateToken(validToken)).thenReturn(email);
            doNothing().when(emailService).updateEmailSubscriptions(email, true, true, validSubscriptionList);

            mockMvc.perform(post("/users/unsubscribe/email-subscriptions")
                    .header("Authorization", validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSubscriptionList)))
                    .andExpect(status().isOk());

            verify(emailService).updateEmailSubscriptions(email, true, true, validSubscriptionList);
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 with invalid subscriptions list")
        void superUser_returns400() throws Exception {
            when(unsubscribeTokenGenerator.parseAndValidateToken(validToken)).thenReturn(email);
            doNothing().when(emailService).updateEmailSubscriptions(email, true, true, validSubscriptionList);

            mockMvc.perform(post("/users/unsubscribe/email-subscriptions")
                    .header("Authorization", validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"invalid\": true}"))
                    .andExpect(status().isBadRequest());

            verify(emailService, never()).updateEmailSubscriptions(email, true, true, validSubscriptionList);
        }
    }
}
