package us.dot.its.jpo.ode.api.accessors.notifications.intersection_reference_alignment_notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.IntersectionReferenceAlignmentNotification;

public interface IntersectionReferenceAlignmentNotificationRepository {
        long count(Integer intersectionID, Long startTime, Long endTime);

        Page<IntersectionReferenceAlignmentNotification> findLatest(Integer intersectionID, Long startTime,
                        Long endTime);

        Page<IntersectionReferenceAlignmentNotification> find(Integer intersectionID, Long startTime, Long endTime,
                        Pageable pageable);
}