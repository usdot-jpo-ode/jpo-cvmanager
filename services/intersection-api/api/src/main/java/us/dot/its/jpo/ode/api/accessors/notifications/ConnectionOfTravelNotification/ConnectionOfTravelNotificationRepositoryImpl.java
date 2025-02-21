package us.dot.its.jpo.ode.api.accessors.notifications.ConnectionOfTravelNotification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import org.springframework.data.domain.Pageable;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.ode.api.accessors.PaginatedQueryUtils;

@Component
public class ConnectionOfTravelNotificationRepositoryImpl implements ConnectionOfTravelNotificationRepository {

    private final MongoTemplate mongoTemplate;

    private final String collectionName = "CmConnectionOfTravelNotification";
    private final String DATE_FIELD = "notificationGeneratedAt";

    @Autowired
    public ConnectionOfTravelNotificationRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public long getQueryResultCount(
            Integer intersectionID,
            Long startTime,
            Long endTime,
            Pageable pageable) {
        return PaginatedQueryUtils.countPagedDataFromArgs(
                mongoTemplate,
                collectionName,
                DATE_FIELD,
                pageable,
                intersectionID,
                startTime,
                endTime);
    }

    public ConnectionOfTravelNotification findLatest(
            Integer intersectionID,
            Long startTime,
            Long endTime) {
        return PaginatedQueryUtils.getLatestDataFromArgs(
                mongoTemplate,
                collectionName,
                DATE_FIELD,
                ConnectionOfTravelNotification.class,
                intersectionID,
                startTime,
                endTime);
    }

    public Page<ConnectionOfTravelNotification> find(
            Integer intersectionID,
            Long startTime,
            Long endTime,
            Pageable pageable) {

        return PaginatedQueryUtils.getDataFromArgs(
                mongoTemplate,
                collectionName,
                DATE_FIELD,
                pageable,
                intersectionID,
                startTime,
                endTime);
    }

    public void add(ConnectionOfTravelNotification item) {
        mongoTemplate.insert(item, collectionName);
    }
}