package us.dot.its.jpo.ode.api.controllers.intersections;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.ode.api.accessors.notifications.ActiveNotification.ActiveNotificationRepository;
import us.dot.its.jpo.ode.mockdata.MockNotificationGenerator;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
@RequestMapping("/data/cm-notifications")
public class ActiveNotificationController {

    private final ActiveNotificationRepository activeNotificationRepo;

    @Autowired
    public ActiveNotificationController(
            ActiveNotificationRepository activeNotificationRepo) {

        this.activeNotificationRepo = activeNotificationRepo;

    }

    @Operation(summary = "Find Active Notifications", description = "Returns a list of Active Notifications, filtered by intersection ID, start time, end time, and key")
    @RequestMapping(value = "/active", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Page<Notification>> findActiveNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "notification_type", required = false) String notificationType,
            @RequestParam(name = "key", required = false) String key,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {
        if (testData) {
            List<Notification> list = new ArrayList<>();
            list.add(MockNotificationGenerator.getConnectionOfTravelNotification());
            return ResponseEntity.ok(new PageImpl<>(list, PageRequest.of(page, size), list.size()));
        }

        // Retrieve a paginated result from the repository
        PageRequest pageable = PageRequest.of(page, size);
        Page<Notification> response = activeNotificationRepo.find(intersectionID, notificationType, key, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Count Active Notifications", description = "Returns the count of Active Notifications, filtered by intersection ID, start time, end time, and key")
    @RequestMapping(value = "/active/count", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested")
    })
    public ResponseEntity<Long> countActiveNotifications(
            @RequestParam(name = "intersection_id") Integer intersectionID,
            @RequestParam(name = "notification_type", required = false) String notificationType,
            @RequestParam(name = "key", required = false) String key,
            @RequestParam(name = "test", required = false, defaultValue = "false") boolean testData) {

        if (testData) {
            return ResponseEntity.ok(1L);
        } else {
            long count = activeNotificationRepo.count(intersectionID, notificationType, key);

            return ResponseEntity.ok(count);
        }
    }

    @Operation(summary = "Delete Active Notification", description = "Deletes a specific Active Notification by key")
    @DeleteMapping(value = "/active", produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('OPERATOR')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or OPERATOR role"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public @ResponseBody ResponseEntity<String> deleteActiveNotification(@RequestBody String key) {
        try {
            long count = activeNotificationRepo.delete(key.replace("\"", ""));
            if (count == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Active Notification with key " + key + " not found");
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to delete Active Notification: " + e.getMessage(), e);
        }
    }
}