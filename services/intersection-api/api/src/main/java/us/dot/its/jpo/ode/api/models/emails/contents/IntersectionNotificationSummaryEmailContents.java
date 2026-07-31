package us.dot.its.jpo.ode.api.models.emails.contents;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;

@Schema(description = "Contents of intersection notification summary email, including a list of ConflictMonitor notification objects that contain details about recent intersection events")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class IntersectionNotificationSummaryEmailContents {
    @Schema(description = "List of ConflictMonitor notification objects")
    @NotEmpty(message = "Notifications list cannot be empty") // Ensure that the list contains at least one notification
    @NotNull(message = "Notifications list cannot be null")
    private List<Notification> notifications;
}
