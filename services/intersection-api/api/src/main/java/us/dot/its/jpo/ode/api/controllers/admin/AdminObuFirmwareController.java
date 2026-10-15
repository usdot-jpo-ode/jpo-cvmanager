package us.dot.its.jpo.ode.api.controllers.admin;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.ode.api.models.storage.FirmwareUploadUrl;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.UploadOptions;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.UploadUrlRequest;
import us.dot.its.jpo.ode.api.models.storage.ObuFirmwareModels.UploadVerification;
import us.dot.its.jpo.ode.api.services.firmware.ObuFirmwareUploadOptionsService;
import us.dot.its.jpo.ode.api.services.firmware.ObuFirmwareUploadService;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true")
@RequestMapping("/admin/obu-firmware")
@RequiredArgsConstructor
@Validated
@Tag(name = "Admin OBU Firmware", description = "Manage OBU firmware served by the OBU OTA server")
public class AdminObuFirmwareController {
    private final ObuFirmwareUploadService uploadService;
    private final ObuFirmwareUploadOptionsService uploadOptionsService;

    @Operation(summary = "List manufacturers and file naming conventions accepted for OBU firmware uploads")
    @GetMapping(value = "/upload-options", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('ADMIN')")
    public UploadOptions getUploadOptions() {
        return uploadOptionsService.getOptions();
    }

    @Operation(summary = "Create a signed OBU firmware upload URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signed upload URL created"),
            @ApiResponse(responseCode = "400", description = "Invalid upload request or file name, or the manufacturer does not support OBU firmware uploads"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Manufacturer was not found"),
            @ApiResponse(responseCode = "409", description = "Firmware with this file name already exists"),
            @ApiResponse(responseCode = "502", description = "Object storage signing failed"),
            @ApiResponse(responseCode = "503", description = "Object storage is not configured")
    })
    @PostMapping(value = "/signed-upload-url", consumes = "application/json", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('ADMIN')")
    public FirmwareUploadUrl createSignedUploadUrl(@Validated @RequestBody UploadUrlRequest request,
            Authentication authentication) {
        log.info("POST /admin/obu-firmware/signed-upload-url. manufacturer={}, file={}",
                request.manufacturerName(), request.fileName());
        return uploadService.createSignedUploadUrl(request, authentication.getName());
    }

    @Operation(summary = "Verify a completed OBU firmware upload and add it to the catalog unpublished")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload verified and registered"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Upload was not found"),
            @ApiResponse(responseCode = "409", description = "Object is missing or does not match the upload intent"),
            @ApiResponse(responseCode = "502", description = "Object storage metadata lookup failed"),
            @ApiResponse(responseCode = "503", description = "Object storage is not configured")
    })
    @PostMapping(value = "/uploads/{uploadId}/complete", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('ADMIN')")
    public UploadVerification completeUpload(@PathVariable UUID uploadId) {
        log.info("POST /admin/obu-firmware/uploads/{}/complete", uploadId);
        return uploadService.completeUpload(uploadId);
    }
}
