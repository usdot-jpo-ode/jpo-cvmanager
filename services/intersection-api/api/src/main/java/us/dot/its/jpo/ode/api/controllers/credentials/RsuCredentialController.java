package us.dot.its.jpo.ode.api.controllers.credentials;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import us.dot.its.jpo.ode.api.mappers.RsuCredentialMapper;
import us.dot.its.jpo.ode.api.models.credentials.RsuCredentialDTO;
import us.dot.its.jpo.ode.api.services.RsuCredentialManagementService;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true")
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
}) 
@RequestMapping("/credentials/rsu")
@RequiredArgsConstructor
public class RsuCredentialController {
    private final RsuCredentialManagementService rsuCredentialManagementService;
    private final RsuCredentialMapper rsuCredentialMapper;

    @PostMapping("/create")
    @PreAuthorize("@PermissionService.hasRoleInOrg(#rsuCredentialCreateRequest.getOrganization(), 'ADMIN')")
    public RsuCredentialDTO createRsuCredential(
            @RequestBody RsuCredentialCreateRequest rsuCredentialCreateRequest) throws EntityNotFoundException, RsuCredentialManagementService.RsuCredentialAlreadyExistsException {
        return rsuCredentialMapper.toDto(rsuCredentialManagementService.create(rsuCredentialCreateRequest));
    }

    @GetMapping("/get-by-nickname")
    @PreAuthorize("@PermissionService.hasRsuCredential(#rsuCredentialGetRequest.getNickname(), 'ADMIN')")
    public RsuCredentialDTO getByNickname(
            RsuCredentialGetRequest rsuCredentialGetRequest) throws EntityNotFoundException {
        return rsuCredentialMapper.toDto(rsuCredentialManagementService.getByNickname(rsuCredentialGetRequest.getNickname()));
    }

    @PostMapping("/update")
    @PreAuthorize("@PermissionService.hasRsuCredential(#rsuCredentialPatch.getNickname(), 'ADMIN') && " +
            "(#rsuCredentialPatch.getOrganization() == null || @PermissionService.hasRoleInOrg(#rsuCredentialPatch.getOrganization(), 'ADMIN'))")
    public RsuCredentialDTO update(
            @RequestBody RsuCredentialPatch rsuCredentialPatch) throws EntityNotFoundException {
        return rsuCredentialMapper.toDto(rsuCredentialManagementService.update(rsuCredentialPatch));
    }

    @PostMapping("/delete")
    @PreAuthorize("@PermissionService.hasRsuCredential(#rsuCredentialDeleteRequest.getNickname(), 'ADMIN')")
    public void deleteByNickname(
            @RequestBody RsuCredentialDeleteRequest rsuCredentialDeleteRequest) {
        rsuCredentialManagementService.deleteByNickname(rsuCredentialDeleteRequest.getNickname());
    }

    // requests
    @Data
    public static class RsuCredentialCreateRequest {
        private final String nickname;
        private final String username;
        private final String password;
        private final String organization;
    }

    @Data
    public static class RsuCredentialGetRequest {
        private final String nickname;
    }

    @Data
    public static class RsuCredentialPatch {
        private final String nickname;
        private String username;
        private String password;
        private String organization;
    }

    @Data
    public static class RsuCredentialDeleteRequest {
        private final String nickname;
    }
}
