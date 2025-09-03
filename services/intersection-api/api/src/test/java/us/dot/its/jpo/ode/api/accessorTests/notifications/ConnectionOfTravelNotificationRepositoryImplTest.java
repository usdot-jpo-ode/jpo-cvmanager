package us.dot.its.jpo.ode.api.accessorTests.notifications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.bson.Document;

import us.dot.its.jpo.ode.api.accessors.notifications.connection_of_travel_notification.ConnectionOfTravelNotificationRepositoryImpl;
import us.dot.its.jpo.ode.api.models.AggregationResult;
import us.dot.its.jpo.ode.api.models.AggregationResultCount;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase
public class ConnectionOfTravelNotificationRepositoryImplTest {

        @SpyBean
        private MongoTemplate mongoTemplate;

        @Mock
        private AggregationResults<AggregationResult> mockAggregationResult;

        @Mock
        private Page<Document> mockDocumentPage;

        @Mock
        private Page<ConnectionOfTravelNotification> mockPage;

        @InjectMocks
        private ConnectionOfTravelNotificationRepositoryImpl repository;

        Integer intersectionID = 123;
        Long startTime = 1724170658205L;
        String startTimeString = "2024-08-20T16:17:38.205Z";
        Long endTime = 1724170778205L;
        String endTimeString = "2024-08-20T16:19:38.205Z";
        boolean latest = true;

        @BeforeEach
        void setUp() throws Exception {
                MockitoAnnotations.openMocks(this);
                repository = new ConnectionOfTravelNotificationRepositoryImpl(mongoTemplate);
        }

        @Test
        public void testCount() {
                long expectedCount = 10;

                doReturn(expectedCount).when(mongoTemplate).count(any(),
                                Mockito.<String>any());

                long resultCount = repository.count(1, null, null);

                assertThat(resultCount).isEqualTo(expectedCount);
                verify(mongoTemplate).count(any(Query.class), anyString());
        }

        @Test
        public void testFind() {
                ConnectionOfTravelNotificationRepositoryImpl repo = mock(
                                ConnectionOfTravelNotificationRepositoryImpl.class);

                when(repo.findPage(
                                any(),
                                any(),
                                any(PageRequest.class),
                                any(Criteria.class),
                                any(Sort.class),
                                any(),
                                eq(ConnectionOfTravelNotification.class))).thenReturn(mockPage);
                PageRequest pageRequest = PageRequest.of(0, 1);
                doCallRealMethod().when(repo).find(1, null, null, pageRequest);

                Page<ConnectionOfTravelNotification> results = repo.find(1, null, null, pageRequest);

                assertThat(results).isEqualTo(mockPage);
        }

        @Test
        public void testFindWithData() throws IOException {
                // Load sample JSON data
                String json = new String(
                                Files.readAllBytes(
                                                Paths.get("src/test/resources/json/ConflictMonitor.CmConnectionOfTravelNotification.json")));
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()); // Register
                                                                                                         // JavaTimeModule

                List<Document> sampleDocuments = List.of(Document.parse(json));

                // Mock dependencies
                when(mockDocumentPage.getContent()).thenReturn(sampleDocuments);
                when(mockDocumentPage.getTotalElements()).thenReturn(1L);

                AggregationResult aggregationResult = new AggregationResult();
                aggregationResult.setResults(sampleDocuments);
                AggregationResultCount aggregationResultCount = new AggregationResultCount();
                aggregationResultCount.setCount(1L);
                aggregationResult.setMetadata(List.of(aggregationResultCount));

                when(mockAggregationResult.getUniqueMappedResult()).thenReturn(aggregationResult);

                ArgumentCaptor<Aggregation> aggregationCaptor = ArgumentCaptor.forClass(Aggregation.class);
                doReturn(mockAggregationResult).when(mongoTemplate).aggregate(aggregationCaptor.capture(),
                                anyString(),
                                eq(AggregationResult.class));

                // Call the repository find method
                PageRequest pageRequest = PageRequest.of(0, 1);
                Page<ConnectionOfTravelNotification> findResponse = repository.find(intersectionID, startTime, endTime,
                                pageRequest);

                // Extract the captured Aggregation
                Aggregation capturedAggregation = aggregationCaptor.getValue();

                // Extract the MatchOperation from the Aggregation pipeline
                Document pipeline = capturedAggregation.toPipeline(Aggregation.DEFAULT_CONTEXT).get(0);

                // Assert the Match operation Criteria
                assertThat(pipeline.toJson())
                                .isEqualTo(String.format(
                                                "{\"$match\": {\"intersectionID\": %s, \"notificationGeneratedAt\": {\"$gte\": {\"$date\": \"%s\"}, \"$lte\": {\"$date\": \"%s\"}}}}",
                                                intersectionID, startTimeString, endTimeString));

                // Serialize results to JSON and compare with the original JSON
                String resultJson = objectMapper.writeValueAsString(findResponse.getContent().get(0));

                // Remove unused fields from each entry
                List<Document> expectedResult = sampleDocuments.stream().map(doc -> {
                        doc.remove("_id");
                        doc.remove("recordGeneratedAt");
                        return doc;
                }).toList();
                String expectedJson = objectMapper.writeValueAsString(expectedResult.get(0));

                // Compare JSON with ignored fields
                JSONAssert.assertEquals(expectedJson, resultJson, new CustomComparator(
                                JSONCompareMode.LENIENT, // Allows different key orders
                                new Customization("properties.timeStamp", (o1, o2) -> true),
                                new Customization("properties.odeReceivedAt", (o1, o2) -> true)));
        }

        @Test
        void testFindLatest() {
                ConnectionOfTravelNotification event = new ConnectionOfTravelNotification();
                event.setIntersectionID(intersectionID);

                doReturn(event).when(mongoTemplate).findOne(any(Query.class), eq(ConnectionOfTravelNotification.class),
                                anyString());

                Page<ConnectionOfTravelNotification> page = repository.findLatest(intersectionID, startTime, endTime);

                assertThat(page.getContent()).hasSize(1);
                assertThat(page.getContent().get(0).getIntersectionID()).isEqualTo(intersectionID);
                verify(mongoTemplate).findOne(any(Query.class), eq(ConnectionOfTravelNotification.class),
                                eq("CmConnectionOfTravelNotification"));
        }

        @Test
        void testAdd() {
                ConnectionOfTravelNotification event = new ConnectionOfTravelNotification();
                event.setIntersectionID(intersectionID);

                doReturn(null).when(mongoTemplate).insert(any(ConnectionOfTravelNotification.class), anyString());

                repository.add(event);

                verify(mongoTemplate).insert(event, "CmConnectionOfTravelNotification");
        }
}