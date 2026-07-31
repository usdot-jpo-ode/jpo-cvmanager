package us.dot.its.jpo.ode.api.models.emails.contents;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;

@Schema(description = "Contents of intersection notification email, including the ConflictMonitor notification object that contains details about the intersection event")
@Data
public class IntersectionNotificationEmailContents {
    @Schema(description = "ConflictMonitor notification object")
    private Notification notification;
}
