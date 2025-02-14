package us.dot.its.jpo.ode.api.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.api.models.IntersectionReferenceData;
import us.dot.its.jpo.ode.api.services.PermissionService;
import us.dot.its.jpo.ode.api.services.PostgresService;

@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
public class IntersectionController {

    private final ProcessedMapRepository processedMapRepo;
    private final PostgresService postgresService;

    @Autowired
    public IntersectionController(
            ProcessedMapRepository processedMapRepo,
            PostgresService postgresService) {
        this.processedMapRepo = processedMapRepo;
        this.postgresService = postgresService;
    }

    @Operation(summary = "List Intersections", description = "Returns a list of intersections")
    @RequestMapping(value = "/intersection/list", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role"),
    })
    public ResponseEntity<List<IntersectionReferenceData>> getIntersections(
            @RequestHeader(name = "Organization", required = false) String organization,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            IntersectionReferenceData ref = new IntersectionReferenceData();
            ref.setRsuIP("1.1.1.1");
            ref.setIntersectionID(12109);
            ref.setRoadRegulatorID("0");

            List<IntersectionReferenceData> refList = new ArrayList<>();
            refList.add(ref);

            return ResponseEntity.ok(refList);
        } else {
            List<IntersectionReferenceData> allIntersections = processedMapRepo.getIntersectionIDs();
            if (organization == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = PermissionService.getUsername(auth);
                List<Integer> allowedIntersectionIds = postgresService.getAllowedIntersectionIdsByEmail(username);
                return ResponseEntity.ok(allIntersections.stream()
                        .filter(intersection -> allowedIntersectionIds.contains(intersection.getIntersectionID()))
                        .collect(Collectors.toList()));
            } else {
                List<Integer> allowedIntersectionIds = postgresService
                        .getAllowedIntersectionIdsByOrganization(organization);
                return ResponseEntity.ok(allIntersections.stream()
                        .filter(intersection -> allowedIntersectionIds.contains(intersection.getIntersectionID()))
                        .collect(Collectors.toList()));
            }
        }
    }

    @Operation(summary = "List Intersections by Location", description = "Returns a list of intersections whose bounding box contains the request point, in latitude and longitude")
    @RequestMapping(value = "/intersection/list/location", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role"),
    })
    public ResponseEntity<List<IntersectionReferenceData>> getIntersectionsByLocation(
            @RequestHeader(name = "Organization", required = false) String organization,
            @RequestParam(name = "longitude", defaultValue = "false") Double longitude,
            @RequestParam(name = "latitude", defaultValue = "false") Double latitude,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            IntersectionReferenceData ref = new IntersectionReferenceData();
            ref.setRsuIP("1.1.1.1");
            ref.setIntersectionID(12109);
            ref.setRoadRegulatorID("0");

            List<IntersectionReferenceData> refList = new ArrayList<>();
            refList.add(ref);

            return ResponseEntity.ok(refList);
        } else {

            List<IntersectionReferenceData> allIntersections = processedMapRepo
                    .getIntersectionsContainingPoint(longitude, latitude);
            if (organization == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = PermissionService.getUsername(auth);
                List<Integer> allowedIntersectionIds = postgresService.getAllowedIntersectionIdsByEmail(username);
                return ResponseEntity.ok(allIntersections.stream()
                        .filter(intersection -> allowedIntersectionIds.contains(intersection.getIntersectionID()))
                        .collect(Collectors.toList()));
            } else {
                List<Integer> allowedIntersectionIds = postgresService
                        .getAllowedIntersectionIdsByOrganization(organization);
                return ResponseEntity.ok(allIntersections.stream()
                        .filter(intersection -> allowedIntersectionIds.contains(intersection.getIntersectionID()))
                        .collect(Collectors.toList()));
            }
        }
    }
}
