package us.dot.its.jpo.ode.api.models.emails.contents;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IntersectionNotificationSummaryEmailContents {
    private List<Notification> notifications;
}
