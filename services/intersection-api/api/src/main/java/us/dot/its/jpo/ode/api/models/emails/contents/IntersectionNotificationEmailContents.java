package us.dot.its.jpo.ode.api.models.emails.contents;

import lombok.Data;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;

@Data
public class IntersectionNotificationEmailContents {
    private Notification notification;
}
