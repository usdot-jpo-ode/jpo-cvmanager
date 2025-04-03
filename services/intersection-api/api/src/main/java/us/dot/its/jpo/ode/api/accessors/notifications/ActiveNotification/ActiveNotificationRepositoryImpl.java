package us.dot.its.jpo.ode.api.accessors.notifications.ActiveNotification;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;

import lombok.extern.slf4j.Slf4j;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.mongodb.client.result.DeleteResult;

import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.IntersectionReferenceAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.LaneDirectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalGroupAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalStateConflictNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.TimeChangeDetailsNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.app_health.KafkaStreamsAnomalyNotification;

@Slf4j
@Component
public class ActiveNotificationRepositoryImpl
        implements ActiveNotificationRepository, PageableQuery {

    private final MongoTemplate mongoTemplate;

    private final String collectionName = "CmNotification";
    private final String INTERSECTION_ID_FIELD = "intersectionID";
    private final String NOTIFICATION_TYPE_FIELD = "notificationType";
    private final String KEY_FIELD = "key";

    @Autowired
    public ActiveNotificationRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Get a page representing the count of data for a given intersectionID,
     * startTime, and endTime
     *
     * @param intersectionID the intersection ID to query by, if null will not be
     *                       applied
     * @param startTime      the start time to query by, if null will not be applied
     * @param endTime        the end time to query by, if null will not be applied
     * @param pageable       the pageable object to use for pagination
     * @return the paginated data that matches the given criteria
     */
    public long count(
            Integer intersectionID,
            String notificationType,
            String key,
            @Nullable Pageable pageable) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .whereOptional(NOTIFICATION_TYPE_FIELD, notificationType)
                .whereOptional(KEY_FIELD, key);
        Query query = Query.query(criteria);
        if (pageable != null) {
            query = query.with(pageable);
        }
        return mongoTemplate.count(query, collectionName);
    }

    /**
     * Get a page containing the single most recent record for a given
     * intersectionID, startTime, and endTime
     *
     * @param intersectionID the intersection ID to query by, if null will not be
     *                       applied
     * @param startTime      the start time to query by, if null will not be applied
     * @param endTime        the end time to query by, if null will not be applied
     * @return the paginated data that matches the given criteria
     */
    public Page<Notification> findLatest(
            Integer intersectionID,
            String notificationType,
            String key) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .whereOptional(NOTIFICATION_TYPE_FIELD, notificationType)
                .whereOptional(KEY_FIELD, key);
        Query query = Query.query(criteria);
        Sort sort = Sort.by(Sort.Direction.DESC, INTERSECTION_ID_FIELD)
                .and(Sort.by(Sort.Direction.DESC, NOTIFICATION_TYPE_FIELD))
                .and(Sort.by(Sort.Direction.DESC, KEY_FIELD));
        return wrapSingleResultWithPage(
                mongoTemplate.findOne(
                        query.with(sort),
                        Notification.class,
                        collectionName));
    }

    /**
     * Get paginated data from a given intersectionID, startTime, and endTime
     *
     * @param intersectionID the intersection ID to query by, if null will not be
     *                       applied
     * @param startTime      the start time to query by, if null will not be applied
     * @param endTime        the end time to query by, if null will not be applied
     * @param pageable       the pageable object to use for pagination
     * @return the paginated data that matches the given criteria
     */
    public Page<Notification> find(
            Integer intersectionID,
            String notificationType,
            String key,
            Pageable pageable) {
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .whereOptional(NOTIFICATION_TYPE_FIELD, notificationType)
                .whereOptional(KEY_FIELD, key);
        Sort sort = Sort.by(Sort.Direction.DESC, NOTIFICATION_TYPE_FIELD);

        Page<Document> dbObjects = findPageAsBson(mongoTemplate, collectionName, pageable, criteria, sort);

        List<Notification> notifications = new ArrayList<>();
        for (Bson dbObject : dbObjects.getContent()) {
            String type = dbObject.toBsonDocument().getString("notificationType").getValue();
            switch (type) {
                case "ConnectionOfTravelNotification" ->
                    notifications
                            .add(mongoTemplate.getConverter().read(ConnectionOfTravelNotification.class, dbObject));
                case "IntersectionReferenceAlignmentNotification" -> notifications.add(
                        mongoTemplate.getConverter().read(IntersectionReferenceAlignmentNotification.class, dbObject));
                case "LaneDirectionOfTravelAssessmentNotification" ->
                    notifications
                            .add(mongoTemplate.getConverter().read(LaneDirectionOfTravelNotification.class, dbObject));
                case "SignalGroupAlignmentNotification" ->
                    notifications
                            .add(mongoTemplate.getConverter().read(SignalGroupAlignmentNotification.class, dbObject));
                case "SignalStateConflictNotification" ->
                    notifications
                            .add(mongoTemplate.getConverter().read(SignalStateConflictNotification.class, dbObject));
                case "TimeChangeDetailsNotification" ->
                    notifications.add(mongoTemplate.getConverter().read(TimeChangeDetailsNotification.class, dbObject));
                case "AppHealthNotification" ->
                    notifications
                            .add(mongoTemplate.getConverter().read(KafkaStreamsAnomalyNotification.class, dbObject));
                default ->
                    log.warn("Attempted to find unknown notificationType: {}", type);
            }
        }
        return new PageImpl<>(notifications, pageable, dbObjects.getTotalElements());
    }

    public long delete(Integer intersectionID,
            String notificationType,
            String key) {
        if (key == null) {
            log.error("No key provided for delete");
            return 0;
        }
        Criteria criteria = new IntersectionCriteria()
                .whereOptional(INTERSECTION_ID_FIELD, intersectionID)
                .whereOptional(NOTIFICATION_TYPE_FIELD, notificationType)
                .whereOptional(KEY_FIELD, key);
        Query query = Query.query(criteria);

        DeleteResult result = mongoTemplate.remove(query, collectionName);
        return result.getDeletedCount();
    }

    @Override
    public void add(Notification item) {
        item.setId(item.getNotificationType()
                + "_" + item.getIntersectionID() + "_" + item.getRoadRegulatorID());
        mongoTemplate.insert(item, collectionName);
    }

}