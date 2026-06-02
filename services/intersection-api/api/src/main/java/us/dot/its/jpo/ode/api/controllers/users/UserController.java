package us.dot.its.jpo.ode.api.controllers.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Parameter;
import us.dot.its.jpo.ode.api.models.UserRole;
import us.dot.its.jpo.ode.api.models.users.ModifyUserAllowedSelections;
import us.dot.its.jpo.ode.api.models.users.UserDto;
import us.dot.its.jpo.ode.api.models.users.UserPatch;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.api.services.UserManagementService;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserManagementService userManagementService;
    private final PermissionService permissionService;

    private static final Map<String, String> SORT_FIELD_MAPPING = Map.of(
            "first_name", "firstName",
            "last_name", "lastName",
            "super_user", "superUser");

    @Operation(summary = "Get All Users for Organization", description = "Get summary data for all Users the user has access to in the specified organization.")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role"),
    })
    public Page<UserDto> getUsers(
            @RequestHeader(name = "Organization", required = true) String organization,
            @RequestParam(name = "search", required = false) String search,
            @PageableDefault(size = 100) Pageable pageable) {
        Pageable mappedPageable = mapSortFields(SORT_FIELD_MAPPING, pageable);

        return userManagementService.getUsers(organization, search, mappedPageable);
    }

    @Operation(summary = "Get Single User Management Data", description = "Get User data required for User modification page. "
            + "Returns detailed data for the specified User along with allowed selections for modification.")
    @RequestMapping(method = RequestMethod.GET, path = "{email}", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasUser(#email, 'USER') and @PermissionService.hasRole('USER'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the User requested"),
    })
    public UserDto getSingleUser(
            @Parameter(description = "User email address", example = "user@example.com", required = true) @PathVariable(name = "email") String email) {
        return userManagementService.getUser(email);
    }

    @Operation(summary = "Get Allowed Selections for User Management", description = "Get User data required for User modification page. "
            + "Returns detailed data for the specified User along with allowed selections for modification.")
    @RequestMapping(method = RequestMethod.GET, path = "/allowed-selections", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or ADMIN role with access to the User requested"),
    })
    public ModifyUserAllowedSelections getAllowedSelections() {
        ModifyUserAllowedSelections allowedSelections = userManagementService
                .getAllowedSelections(permissionService.getCvManagerAuthToken());

        return allowedSelections;
    }

    @Operation(summary = "Create User", description = "Create a new User")
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or ADMIN role"),
    })
    @ResponseStatus(HttpStatus.CREATED)
    public void createUser(@Validated @RequestBody UserDto body) {
        if (!permissionService.hasRoleInOrgs(UserRole.ADMIN,
                body.getOrganizations().stream().map(org -> org.getOrganization()).toList())) {
            // This catches unqualified orgs or nonexistent orgs
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "User not qualified to modify all specified organizations");
        } else if (!permissionService.isSuperUser() && body.getSuperUser()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Non-super user not qualified to create super user");
        }

        userManagementService.createUser(body);
        return;
    }

    @Operation(summary = "Modify User", description = "Modify User information")
    @RequestMapping(method = RequestMethod.PATCH, path = "{email}", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasUser(#email, 'ADMIN') and @PermissionService.hasRole('ADMIN'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or ADMIN role with access to the User requested"),
    })
    public ResponseEntity<Void> modifyUser(
            @Parameter(description = "User email address", example = "user@example.com", required = true) @PathVariable(name = "email") String email,
            @Validated @RequestBody UserPatch body) {
        userManagementService.modifyUser(email, body, permissionService.getCvManagerAuthToken());

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete User", description = "Delete User from management system")
    @RequestMapping(method = RequestMethod.DELETE, path = "{email}", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasUser(#email, 'ADMIN') and @PermissionService.hasRole('ADMIN'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or ADMIN role with access to the User requested"),
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User email address", example = "user@example.com", required = true) @PathVariable(name = "email") String email) {
        userManagementService.deleteUserByEmail(email);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete Multiple Users", description = "Delete Multiple Users from management system")
    @RequestMapping(method = RequestMethod.DELETE, path = "/batch", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasUsers(#emails, 'ADMIN') && @PermissionService.hasRole('ADMIN'))")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or ADMIN role with access to the Users requested"),
    })
    public ResponseEntity<Void> deleteUsers(@RequestBody List<String> emails) {
        userManagementService.deleteMultipleUsersByEmail(emails);

        return ResponseEntity.noContent().build();
    }

    /*
     * Maps API sort fields to database fields. If no mapping is found, the original
     * field is used. This allows the API to use more user-friendly field names
     * while still supporting sorting on database fields.
     */
    private Pageable mapSortFields(Map<String, String> SORT_FIELD_MAPPING, Pageable pageable) {
        if (!pageable.getSort().isSorted()) {
            return pageable;
        }

        Sort mappedSort = Sort.unsorted();

        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            String mappedProperty = SORT_FIELD_MAPPING.getOrDefault(property, property);
            mappedSort = mappedSort.and(Sort.by(order.getDirection(), mappedProperty));
        }

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                mappedSort);
    }
}