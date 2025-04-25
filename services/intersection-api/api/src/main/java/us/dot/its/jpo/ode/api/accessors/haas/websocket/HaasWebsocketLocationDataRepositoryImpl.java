package us.dot.its.jpo.ode.api.accessors.haas.websocket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import us.dot.its.jpo.ode.api.accessors.IntersectionCriteria;
import us.dot.its.jpo.ode.api.accessors.PageableQuery;
import us.dot.its.jpo.ode.api.models.haas.websocket.HaasWebsocketLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class HaasWebsocketLocationDataRepositoryImpl
                implements HaasWebsocketLocationDataRepository, PageableQuery {

        private final MongoTemplate mongoTemplate;

        private final String collectionName = "HaasAlertLocation";
        private final String DATE_FIELD = "start_time";
        private final String IS_ACTIVE_FIELD = "is_active";
        private final String ID_FIELD = "id";

        @Autowired
        public HaasWebsocketLocationDataRepositoryImpl(MongoTemplate mongoTemplate) {
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
                        boolean activeOnly,
                        Long startTime,
                        Long endTime,
                        Pageable pageable) {
                Criteria criteria = new IntersectionCriteria()
                                .whereOptional(IS_ACTIVE_FIELD, activeOnly)
                                .withinTimeWindow(DATE_FIELD, startTime, endTime, true);
                Query query = Query.query(criteria);
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
        public Page<HaasWebsocketLocation> findLatest(
                        boolean activeOnly,
                        Long startTime,
                        Long endTime,
                        Pageable pageable) {
                Criteria criteria = new IntersectionCriteria()
                                .whereOptional(IS_ACTIVE_FIELD, activeOnly)
                                .withinTimeWindow(DATE_FIELD, startTime, endTime, true);
                Sort sort = Sort.by(Sort.Direction.DESC, DATE_FIELD);
                List<String> excludedFields = new ArrayList<>();
                excludedFields.add("recordGeneratedAt");
                excludedFields.add("_id");

                return findPage(mongoTemplate, collectionName, pageable, criteria,
                                sort,
                                excludedFields, HaasWebsocketLocation.class);
        }

        @Override
        public void add(HaasWebsocketLocation item) {
                mongoTemplate.insert(item, collectionName);
        }

}
