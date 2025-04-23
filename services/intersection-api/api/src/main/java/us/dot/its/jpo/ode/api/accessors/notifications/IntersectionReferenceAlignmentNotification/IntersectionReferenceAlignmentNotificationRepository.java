package us.dot.its.jpo.ode.api.accessors.notifications.IntersectionReferenceAlignmentNotification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.IntersectionReferenceAlignmentNotification;
import us.dot.its.jpo.ode.api.models.DataLoader;

public interface IntersectionReferenceAlignmentNotificationRepository
        extends DataLoader<IntersectionReferenceAlignmentNotification> {
    long count(Integer intersectionID, Long startTime, Long endTime);

    Page<IntersectionReferenceAlignmentNotification> findLatest(Integer intersectionID, Long startTime, Long endTime);

    Page<IntersectionReferenceAlignmentNotification> find(Integer intersectionID, Long startTime, Long endTime,
            Pageable pageable);
}