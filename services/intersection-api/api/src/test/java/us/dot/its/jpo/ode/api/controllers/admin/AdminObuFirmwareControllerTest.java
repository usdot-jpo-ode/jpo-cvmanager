package us.dot.its.jpo.ode.api.controllers.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import us.dot.its.jpo.ode.api.models.postgres.tables.ObuFirmwareDeviceType;
import us.dot.its.jpo.ode.api.models.storage.FirmwareUploadUrl;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.Item;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.ManufacturerOption;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.ObjectPage;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.Publication;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.UploadOptions;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.UploadUrlRequest;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.UploadVerification;
import us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadService.FirmwareUploadVerificationException;
import us.dot.its.jpo.ode.api.services.firmware.FirmwareUploadService.FirmwareVersionAlreadyExistsException;
import us.dot.its.jpo.ode.api.services.firmware.ObuFirmwareConflictException;
import us.dot.its.jpo.ode.api.services.firmware.ObuFirmwareDeletionService;
import us.dot.its.jpo.ode.api.services.firmware.ObuFirmwareObjectService;
import us.dot.its.jpo.ode.api.services.firmware.ObuFirmwarePublicationService;
import us.dot.its.jpo.ode.api.services.firmware.ObuFirmwareUploadOptionsService;
import us.dot.its.jpo.ode.api.services.firmware.ObuFirmwareUploadService;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.api.storage.ObjectStorageUnavailableException;

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

    @MockitoBean
    private ObuFirmwareObjectService objectService;

    @MockitoBean
    private ObuFirmwarePublicationService publicationService;

    @MockitoBean
    private ObuFirmwareDeletionService deletionService;

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
                .thenThrow(new ObjectStorageUnavailableException("Object storage provider is not configured"));

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

    @Test
    @WithMockUser
    void nonAdminsCannotManageObuFirmware() throws Exception {
        mockMvc.perform(get("/admin/obu-firmware/objects")).andExpect(status().isForbidden());
        mockMvc.perform(put("/admin/obu-firmware/images/42/publication").contentType(MediaType.APPLICATION_JSON)
                .content("{\"published\":true}")).andExpect(status().isForbidden());
        mockMvc.perform(delete("/admin/obu-firmware/objects/abc").param("provider_object_version", "17"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/admin/obu-firmware/objects/abc/records")).andExpect(status().isForbidden());
        verifyNoInteractions(objectService, publicationService, deletionService);
    }

    @Test
    @WithMockUser
    void listsObuFirmwareInSnakeCase() throws Exception {
        when(permissionService.hasRole(UserRole.ADMIN)).thenReturn(true);
        var item = new Item("b2JqZWN0", "ota/Commsignia/" + FILE_NAME, "gcp", "17", "Commsignia", FILE_NAME,
                ObuFirmwareDeviceType.OBU, "ob4-generic", "y20.48.2-b228647", false, true, 1024L,
                Instant.parse("2026-10-15T12:00:00Z"), Instant.parse("2026-10-15T12:01:00Z"), null, "VERIFIED", 42,
                true, Instant.parse("2026-10-15T12:02:00Z"), "admin", "VERIFIED");
        when(objectService.list(1, 25, "Commsignia", "y20", true, "file_name,desc"))
                .thenReturn(new ObjectPage("gcp", true, List.of(item), 26));

        mockMvc.perform(get("/admin/obu-firmware/objects").param("page", "1").param("size", "25")
                .param("manufacturer", "Commsignia").param("search", "y20").param("published", "true")
                .param("sort", "file_name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storage_available").value(true))
                .andExpect(jsonPath("$.total_elements").value(26))
                .andExpect(jsonPath("$.objects[0].object_id").value("b2JqZWN0"))
                .andExpect(jsonPath("$.objects[0].device_type").value("OBU"))
                .andExpect(jsonPath("$.objects[0].release_version").value("y20.48.2-b228647"))
                .andExpect(jsonPath("$.objects[0].secure_boot").value(true))
                .andExpect(jsonPath("$.objects[0].obu_firmware_id").value(42))
                .andExpect(jsonPath("$.objects[0].published_at").value("2026-10-15T12:02:00Z"))
                .andExpect(jsonPath("$.objects[0].verification_status").value("VERIFIED"));

        when(objectService.list(0, 100, null, null, null, "manufacturer,asc"))
                .thenReturn(new ObjectPage(null, false, List.of(), 0));
        mockMvc.perform(get("/admin/obu-firmware/objects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storage_available").value(false));
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    void publishesAndWithdrawsFirmware() throws Exception {
        when(permissionService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(publicationService.setPublished(42, true, "admin@example.com"))
                .thenReturn(new Publication(42, true, Instant.parse("2026-10-15T12:02:00Z"), "admin@example.com"));

        mockMvc.perform(put("/admin/obu-firmware/images/42/publication").contentType(MediaType.APPLICATION_JSON)
                .content("{\"published\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.obu_firmware_id").value(42))
                .andExpect(jsonPath("$.published").value(true))
                .andExpect(jsonPath("$.published_by").value("admin@example.com"));

        for (String body : List.of("{}", "{\"published\":null}", "not json")) {
            mockMvc.perform(put("/admin/obu-firmware/images/42/publication").contentType(MediaType.APPLICATION_JSON)
                    .content(body)).andExpect(status().isBadRequest());
        }
        mockMvc.perform(put("/admin/obu-firmware/images/0/publication").contentType(MediaType.APPLICATION_JSON)
                .content("{\"published\":false}")).andExpect(status().isBadRequest());

        when(publicationService.setPublished(eq(7), eq(true), any()))
                .thenThrow(new ObuFirmwareConflictException("The firmware file is missing or has changed"));
        mockMvc.perform(put("/admin/obu-firmware/images/7/publication").contentType(MediaType.APPLICATION_JSON)
                .content("{\"published\":true}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("The firmware file is missing or has changed"));
        when(publicationService.setPublished(eq(8), eq(true), any()))
                .thenThrow(new EntityNotFoundException("OBU firmware '8' was not found"));
        mockMvc.perform(put("/admin/obu-firmware/images/8/publication").contentType(MediaType.APPLICATION_JSON)
                .content("{\"published\":true}")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deletesFirmwareAndCleansUpRecords() throws Exception {
        when(permissionService.hasRole(UserRole.ADMIN)).thenReturn(true);

        mockMvc.perform(delete("/admin/obu-firmware/objects/abc").param("provider_object_version", "17"))
                .andExpect(status().isNoContent());
        verify(deletionService).delete("abc", "17");
        mockMvc.perform(delete("/admin/obu-firmware/objects/abc/records")).andExpect(status().isNoContent());
        verify(deletionService).cleanupMissingObject("abc");

        mockMvc.perform(delete("/admin/obu-firmware/objects/abc")).andExpect(status().isBadRequest());
        mockMvc.perform(delete("/admin/obu-firmware/objects/abc").param("provider_object_version", " "))
                .andExpect(status().isBadRequest());

        doThrow(new ObuFirmwareConflictException("Unpublish this firmware before deleting it."))
                .when(deletionService).delete("published", "17");
        mockMvc.perform(delete("/admin/obu-firmware/objects/published").param("provider_object_version", "17"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Unpublish this firmware before deleting it."));
        doThrow(new IllegalArgumentException("Invalid firmware object ID"))
                .when(deletionService).cleanupMissingObject("bad");
        mockMvc.perform(delete("/admin/obu-firmware/objects/bad/records")).andExpect(status().isBadRequest());
    }
}
