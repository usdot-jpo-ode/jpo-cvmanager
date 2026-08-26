package us.dot.its.jpo.ode.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
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

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.ode.api.TestcontainersConfiguration;
import us.dot.its.jpo.ode.api.models.UserRole;
import us.dot.its.jpo.ode.api.models.emails.EmailSendResponse;
import us.dot.its.jpo.ode.api.models.emails.contents.ApiErrorEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.IntersectionNotificationSummaryEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.RsuErrorSummaryEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.SupportRequestEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.message_counts.MessageCountCountsItem;
import us.dot.its.jpo.ode.api.models.emails.contents.message_counts.MessageCountEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.message_counts.MessageCountRsuItem;
import us.dot.its.jpo.ode.api.services.EmailService;
import us.dot.its.jpo.ode.api.services.PermissionService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = { "enable.api=true",
        "enable.email=true" })
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private PermissionService permissionService;

    private static final List<EmailSendResponse> SUCCESS = List.of(new EmailSendResponse(0, "OK"));

    @BeforeEach
    void setUp() {
        when(emailService.sendMessageCounts(any())).thenReturn(SUCCESS);
        when(emailService.sendFirmwareUpgradeFailure(any())).thenReturn(SUCCESS);
        when(emailService.sendApiError(any())).thenReturn(SUCCESS);
        when(emailService.sendRsuErrorSummary(any())).thenReturn(SUCCESS);
        when(emailService.sendSupportRequest(any())).thenReturn(SUCCESS);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /emails/message-counts
    // @PreAuthorize: @PermissionService.isSuperUser() ||
    // hasRole('ROLE_SEND_MESSAGE_COUNTS_EMAILS')
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /emails/message-counts")
    class MessageCounts {

        @Test
        @DisplayName("returns 403 when unauthenticated")
        void unauthenticated_returns403() throws Exception {
            mockMvc.perform(post("/emails/message-counts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validMessageCountBody())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when authenticated but does not have the required role")
        void insufficientPermissions_returns403() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);

            mockMvc.perform(post("/emails/message-counts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validMessageCountBody())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 200 when isSuperUser returns true")
        void superUser_returns200() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);

            mockMvc.perform(post("/emails/message-counts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validMessageCountBody())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCount").value(1))
                    .andExpect(jsonPath("$.failureCount").value(0));
        }

        @Test
        @WithMockUser(roles = "SEND_MESSAGE_COUNTS_EMAILS")
        @DisplayName("returns 200 when user has ROLE_SEND_MESSAGE_COUNTS_EMAILS")
        void specificRole_returns200() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);

            mockMvc.perform(post("/emails/message-counts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validMessageCountBody())))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when required fields are missing")
        void invalidBody_returns400() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);

            mockMvc.perform(post("/emails/message-counts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /emails/firmware-upgrade-failures
    // @PreAuthorize: @PermissionService.isSuperUser() ||
    // hasRole('ROLE_SEND_FIRMWARE_UPGRADE_EMAILS')
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /emails/firmware-upgrade-failures")
    class FirmwareUpgradeFailures {

        @Test
        @DisplayName("returns 403 when unauthenticated")
        void unauthenticated_returns403() throws Exception {
            mockMvc.perform(post("/emails/firmware-upgrade-failures")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validFirmwareUpgradeJson()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when authenticated but does not have the required role")
        void insufficientPermissions_returns403() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);

            mockMvc.perform(post("/emails/firmware-upgrade-failures")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validFirmwareUpgradeJson()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 200 when isSuperUser returns true")
        void superUser_returns200() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);

            mockMvc.perform(post("/emails/firmware-upgrade-failures")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validFirmwareUpgradeJson()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCount").value(1))
                    .andExpect(jsonPath("$.failureCount").value(0));
        }

        @Test
        @WithMockUser(roles = "SEND_FIRMWARE_UPGRADE_EMAILS")
        @DisplayName("returns 200 when user has ROLE_SEND_FIRMWARE_UPGRADE_EMAILS")
        void specificRole_returns200() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);

            mockMvc.perform(post("/emails/firmware-upgrade-failures")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validFirmwareUpgradeJson()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when required fields are missing")
        void invalidBody_returns400() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);

            mockMvc.perform(post("/emails/firmware-upgrade-failures")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /emails/api-errors
    // @PreAuthorize: @PermissionService.isSuperUser() ||
    // hasRole('ROLE_SEND_CRITICAL_ERROR_MESSAGE_EMAILS')
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /emails/api-errors")
    class ApiErrors {

        @Test
        @DisplayName("returns 403 when unauthenticated")
        void unauthenticated_returns403() throws Exception {
            mockMvc.perform(post("/emails/api-errors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validApiErrorBody())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when authenticated but does not have the required role")
        void insufficientPermissions_returns403() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);

            mockMvc.perform(post("/emails/api-errors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validApiErrorBody())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 200 when isSuperUser returns true")
        void superUser_returns200() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);

            mockMvc.perform(post("/emails/api-errors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validApiErrorBody())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCount").value(1))
                    .andExpect(jsonPath("$.failureCount").value(0));
        }

        @Test
        @WithMockUser(roles = "SEND_CRITICAL_ERROR_MESSAGE_EMAILS")
        @DisplayName("returns 200 when user has ROLE_SEND_CRITICAL_ERROR_MESSAGE_EMAILS")
        void specificRole_returns200() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);

            mockMvc.perform(post("/emails/api-errors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validApiErrorBody())))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when required fields are missing")
        void invalidBody_returns400() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);

            mockMvc.perform(post("/emails/api-errors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /emails/rsu-errors
    // @PreAuthorize: @PermissionService.isSuperUser() ||
    // @PermissionService.hasRole('USER')
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /emails/rsu-errors")
    class RsuErrors {

        @Test
        @DisplayName("returns 403 when unauthenticated")
        void unauthenticated_returns403() throws Exception {
            mockMvc.perform(post("/emails/rsu-errors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRsuErrorBody())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 403 when authenticated but neither isSuperUser nor hasRole('USER')")
        void insufficientPermissions_returns403() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.USER)).thenReturn(false);

            mockMvc.perform(post("/emails/rsu-errors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRsuErrorBody())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 200 when isSuperUser returns true")
        void superUser_returns200() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);

            mockMvc.perform(post("/emails/rsu-errors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRsuErrorBody())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCount").value(1))
                    .andExpect(jsonPath("$.failureCount").value(0));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 200 when hasRole('USER') returns true")
        void userRole_returns200() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(false);
            when(permissionService.hasRole(UserRole.USER)).thenReturn(true);

            mockMvc.perform(post("/emails/rsu-errors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRsuErrorBody())))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when required fields are missing")
        void invalidBody_returns400() throws Exception {
            when(permissionService.isSuperUser()).thenReturn(true);

            mockMvc.perform(post("/emails/rsu-errors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /emails/support-requests (no @PreAuthorize — open to any caller)
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /emails/support-requests")
    class SupportRequests {

        @Test
        @WithMockUser
        @DisplayName("returns 200 for any authenticated caller")
        void authenticated_returns200() throws Exception {
            mockMvc.perform(post("/emails/support-requests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSupportRequestBody())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCount").value(1))
                    .andExpect(jsonPath("$.failureCount").value(0));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when required fields are missing")
        void missingBody_returns400() throws Exception {
            mockMvc.perform(post("/emails/support-requests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when email field is blank")
        void blankEmail_returns400() throws Exception {
            SupportRequestEmailContents body = new SupportRequestEmailContents("", "Subject", "Message body");

            mockMvc.perform(post("/emails/support-requests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── Request body helpers ──────────────────────────────────────────────────

    private IntersectionNotificationSummaryEmailContents validIntersectionNotificationBody() {
        // ConnectionOfTravelNotification is a concrete Notification subtype safe to
        // instantiate
        return new IntersectionNotificationSummaryEmailContents(
                List.of(new ConnectionOfTravelNotification()));
    }

    private MessageCountEmailContents validMessageCountBody() {
        MessageCountCountsItem counts = new MessageCountCountsItem(); // int/double fields default to 0

        MessageCountRsuItem rsuItem = new MessageCountRsuItem();
        rsuItem.setRsuIp("192.168.1.1");
        rsuItem.setPrimaryRoute("C470");
        rsuItem.setMessageCountsByType(Map.of("BSM", counts));

        MessageCountEmailContents body = new MessageCountEmailContents();
        body.setOrganizationName("TestOrg");
        body.setDeploymentTitle("TestDeployment");
        body.setStartDate(Instant.parse("2024-01-01T00:00:00Z"));
        body.setEndDate(Instant.parse("2024-01-02T00:00:00Z"));
        body.setMessageTypeList(List.of("BSM"));
        body.setRsuCounts(List.of(rsuItem));
        return body;
    }

    /**
     * FirmwareUpgradeFailureEmailContents uses @Data only (no @AllArgsConstructor),
     * so raw JSON is the most concise way to build a valid instance.
     */
    private String validFirmwareUpgradeJson() {
        return """
                {
                  "rsu_ip": "192.168.1.1",
                  "message": "Firmware upgrade failed",
                  "failure_type": "Yunex Firmware Upgrade Error",
                  "stack_trace": "java.lang.RuntimeException: upgrade failed\\n\\tat com.example.Foo.bar(Foo.java:42)"
                }
                """;
    }

    private ApiErrorEmailContents validApiErrorBody() {
        return new ApiErrorEmailContents(
                "NullPointerException",
                "java.lang.NullPointerException\\n\\tat com.example.Foo.bar(Foo.java:10)",
                Instant.parse("2024-01-01T12:00:00Z"),
                "https://logs.example.com/errors/123");
    }

    private RsuErrorSummaryEmailContents validRsuErrorBody() {
        return new RsuErrorSummaryEmailContents("RSU Error Summary", "Several RSUs reported errors.");
    }

    private SupportRequestEmailContents validSupportRequestBody() {
        return new SupportRequestEmailContents("user@example.com", "Help needed", "I need help with X.");
    }
}