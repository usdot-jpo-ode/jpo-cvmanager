package us.dot.its.jpo.ode.api.controllers.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;
import us.dot.its.jpo.ode.api.TestcontainersConfiguration;
import us.dot.its.jpo.ode.api.models.UserRole;
import us.dot.its.jpo.ode.api.models.postgres.tables.FirmwareUploadStatus;
import us.dot.its.jpo.ode.api.models.storage.FirmwareUploadUrl;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.ManufacturerOption;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.UploadOptions;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.UploadUrlRequest;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.UploadVerification;
import us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadService.FirmwareUploadConfigurationException;
import us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadService.FirmwareUploadVerificationException;
import us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadService.FirmwareVersionAlreadyExistsException;
import us.dot.its.jpo.ode.api.services.firmware.ObuFirmwareUploadOptionsService;
import us.dot.its.jpo.ode.api.services.firmware.ObuFirmwareUploadService;
import us.dot.its.jpo.ode.api.services.PermissionService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "firmware-upload.cleanup.enabled=false")
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AdminObuFirmwareControllerTest {
    private static final String FILE_NAME = "ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig";
    private static final String REQUEST_BODY = """
            {
              "manufacturer_name": "Commsignia",
              "file_name": "%s",
              "content_length": 1024,
              "checksum_algorithm": "CRC32C",
              "checksum": "ImIEBA=="
            }
            """.formatted(FILE_NAME);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(name = "PermissionService")
    private PermissionService permissionService;

    @MockitoBean
    private ObuFirmwareUploadService uploadService;

    @MockitoBean
    private ObuFirmwareUploadOptionsService uploadOptionsService;

    @Test
    @WithMockUser
    void nonAdminsCannotUseTheObuFirmwareApi() throws Exception {
        mockMvc.perform(get("/admin/obu-firmware/upload-options")).andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/obu-firmware/signed-upload-url").contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST_BODY)).andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/obu-firmware/uploads/" + UUID.randomUUID() + "/complete"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(uploadService, uploadOptionsService);
    }

    @Test
    @WithMockUser
    void listsUploadOptionsInSnakeCase() throws Exception {
        when(permissionService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(uploadOptionsService.getOptions()).thenReturn(new UploadOptions(List.of(
                new ManufacturerOption(1, "Commsignia", ".tar.sig", "^pattern$", 128, FILE_NAME))));

        mockMvc.perform(get("/admin/obu-firmware/upload-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturers[0].manufacturer_id").value(1))
                .andExpect(jsonPath("$.manufacturers[0].file_extension").value(".tar.sig"))
                .andExpect(jsonPath("$.manufacturers[0].file_name_pattern").value("^pattern$"))
                .andExpect(jsonPath("$.manufacturers[0].max_file_name_length").value(128))
                .andExpect(jsonPath("$.manufacturers[0].example_file_name").value(FILE_NAME));
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    void createsSignedUploadUrlsWithADefaultContentType() throws Exception {
        when(permissionService.hasRole(UserRole.ADMIN)).thenReturn(true);
        UUID uploadId = UUID.randomUUID();
        when(uploadService.createSignedUploadUrl(any(), any())).thenReturn(new FirmwareUploadUrl(uploadId,
                "https://storage.googleapis.com/signed", "PUT", "ota/Commsignia/" + FILE_NAME,
                Instant.parse("2026-10-15T12:15:00Z"), Map.of("x-goog-hash", "crc32c=ImIEBA==")));

        mockMvc.perform(post("/admin/obu-firmware/signed-upload-url").contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upload_id").value(uploadId.toString()))
                .andExpect(jsonPath("$.method").value("PUT"))
                .andExpect(jsonPath("$.required_headers['x-goog-hash']").value("crc32c=ImIEBA=="));

        verify(uploadService).createSignedUploadUrl(new UploadUrlRequest("Commsignia", FILE_NAME, 1024L,
                "CRC32C", "ImIEBA==", "application/octet-stream"), "admin@example.com");
    }

    @Test
    @WithMockUser
    void rejectsInvalidUploadRequests() throws Exception {
        when(permissionService.hasRole(UserRole.ADMIN)).thenReturn(true);
        for (String body : List.of("{}",
                REQUEST_BODY.replace(FILE_NAME, "../" + FILE_NAME),
                REQUEST_BODY.replace("\"Commsignia\"", "\"bad/name\""),
                REQUEST_BODY.replace("1024", "0"),
                REQUEST_BODY.replace("\"ImIEBA==\"", "\"bad checksum\""))) {
            mockMvc.perform(post("/admin/obu-firmware/signed-upload-url").contentType(MediaType.APPLICATION_JSON)
                    .content(body)).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(uploadService);
    }

    @Test
    @WithMockUser
    void mapsUploadFailuresToActionableStatuses() throws Exception {
        when(permissionService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(uploadService.createSignedUploadUrl(any(), any()))
                .thenThrow(new IllegalArgumentException("file_name must follow the Commsignia convention"))
                .thenThrow(new EntityNotFoundException("Manufacturer 'Commsignia' was not found"))
                .thenThrow(new FirmwareVersionAlreadyExistsException("already exists"))
                .thenThrow(new FirmwareUploadConfigurationException("not supported"));

        for (var expected : List.of(status().isBadRequest(), status().isNotFound(), status().isConflict(),
                status().isServiceUnavailable())) {
            mockMvc.perform(post("/admin/obu-firmware/signed-upload-url").contentType(MediaType.APPLICATION_JSON)
                    .content(REQUEST_BODY)).andExpect(expected);
        }
    }

    @Test
    @WithMockUser
    void completesUploads() throws Exception {
        when(permissionService.hasRole(UserRole.ADMIN)).thenReturn(true);
        UUID uploadId = UUID.randomUUID();
        when(uploadService.completeUpload(uploadId)).thenReturn(new UploadVerification(uploadId,
                FirmwareUploadStatus.VERIFIED, "ota/Commsignia/" + FILE_NAME, 1024L, "CRC32C",
                "ImIEBA==", "17", Instant.parse("2026-10-15T12:00:00Z"), 42));

        mockMvc.perform(post("/admin/obu-firmware/uploads/" + uploadId + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"))
                .andExpect(jsonPath("$.obu_firmware_id").value(42))
                .andExpect(jsonPath("$.provider_object_version").value("17"))
                .andExpect(jsonPath("$.verified_at").value("2026-10-15T12:00:00Z"));
    }

    @Test
    @WithMockUser
    void reportsVerificationConflicts() throws Exception {
        when(permissionService.hasRole(UserRole.ADMIN)).thenReturn(true);
        UUID uploadId = UUID.randomUUID();
        when(uploadService.completeUpload(eq(uploadId)))
                .thenThrow(new FirmwareUploadVerificationException("checksum does not match"));

        mockMvc.perform(post("/admin/obu-firmware/uploads/" + uploadId + "/complete"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("checksum does not match"));
        mockMvc.perform(post("/admin/obu-firmware/uploads/not-a-uuid/complete"))
                .andExpect(status().isBadRequest());
    }
}
