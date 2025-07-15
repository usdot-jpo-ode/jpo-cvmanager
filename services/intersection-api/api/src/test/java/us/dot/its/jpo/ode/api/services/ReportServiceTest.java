package us.dot.its.jpo.ode.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessmentGroup;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLinePassageEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLineStopEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.MapMinimumDataEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.SpatMinimumDataEvent;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.accessors.assessments.lane_direction_of_travel_assessment.LaneDirectionOfTravelAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.events.connection_of_travel_event.ConnectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.intersection_reference_alignment_event.IntersectionReferenceAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.lane_direction_of_travel_event.LaneDirectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.map_broadcast_rate_event.MapBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.map_minimum_data_event.MapMinimumDataEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.signal_state_conflict_event.SignalStateConflictEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.spat_broadcast_rate_event.SpatBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.spat_minimum_data_event.SpatMinimumDataEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.stop_line_passage_event.StopLinePassageEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.stop_line_stop_event.StopLineStopEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.time_change_details_event.TimeChangeDetailsEventRepository;
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.api.accessors.reports.ReportRepository;
import us.dot.its.jpo.ode.api.models.ConnectionOfTravelData;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.LaneConnectionCount;
import us.dot.its.jpo.ode.api.models.ReportDocument;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    @Mock
    private ProcessedMapRepository processedMapRepo;

    @Mock
    private StopLinePassageEventRepository stopLinePassageEventRepo;

    @Mock
    private StopLineStopEventRepository stopLineStopEventRepo;

    @Mock
    private ConnectionOfTravelEventRepository connectionOfTravelEventRepo;

    @Mock
    private IntersectionReferenceAlignmentEventRepository intersectionReferenceAlignmentEventRepo;

    @Mock
    private LaneDirectionOfTravelEventRepository laneDirectionOfTravelEventRepo;

    @Mock
    private SignalStateConflictEventRepository signalStateConflictEventRepo;

    @Mock
    private TimeChangeDetailsEventRepository timeChangeDetailsEventRepo;

    @Mock
    private LaneDirectionOfTravelAssessmentRepository laneDirectionOfTravelAssessmentRepo;

    @Mock
    private SpatMinimumDataEventRepository spatMinimumDataEventRepo;

    @Mock
    private MapMinimumDataEventRepository mapMinimumDataEventRepo;

    @Mock
    private SpatBroadcastRateEventRepository spatBroadcastRateEventRepo;

    @Mock
    private MapBroadcastRateEventRepository mapBroadcastRateEventRepo;

    @Mock
    private ReportRepository reportRepo;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Manually instantiate ReportService with mocked dependencies
        reportService = new ReportService(
                processedMapRepo,
                stopLinePassageEventRepo,
                stopLineStopEventRepo,
                connectionOfTravelEventRepo,
                intersectionReferenceAlignmentEventRepo,
                laneDirectionOfTravelEventRepo,
                signalStateConflictEventRepo,
                timeChangeDetailsEventRepo,
                laneDirectionOfTravelAssessmentRepo,
                spatMinimumDataEventRepo,
                mapMinimumDataEventRepo,
                spatBroadcastRateEventRepo,
                mapBroadcastRateEventRepo,
                reportRepo,
                1000 // Example value for maximumResponseSize
        );
    }

    @Test
    void testBuildReport() {
        // Arrange
        int intersectionID = 1;
        long startTime = Instant.now().minusSeconds(3600).toEpochMilli();
        long endTime = Instant.now().toEpochMilli();

        // mock LaneConnectionCounts
        LaneConnectionCount laneConnectionCount = new LaneConnectionCount();
        List<LaneConnectionCount> laneConnectionCounts = List.of(laneConnectionCount);
        when(connectionOfTravelEventRepo.getConnectionOfTravelEventsByConnection(intersectionID, startTime, endTime))
                .thenReturn(laneConnectionCounts);

        // Mock ProcessedMapRepository
        ProcessedMap<LineString> processedMap = new ProcessedMap<>();
        Page<ProcessedMap<LineString>> processedMapPage = new PageImpl<>(Collections.singletonList(processedMap));
        when(processedMapRepo.findLatest(eq(intersectionID), any(), any(), eq(true))).thenReturn(processedMapPage);

        // Mock StopLinePassageEventRepository
        List<IDCount> stopLinePassageEventCounts = List.of(new IDCount("1", 10.0), new IDCount("2", 20.0));
        when(stopLinePassageEventRepo.getAggregatedDailyStopLinePassageEventCounts(intersectionID, startTime, endTime))
                .thenReturn(stopLinePassageEventCounts);

        // Mock StopLineStopEventRepository
        List<IDCount> stopLineStopEventCounts = Arrays.asList(new IDCount("1", 5), new IDCount("2", 15.0));
        when(stopLineStopEventRepo.getAggregatedDailyStopLineStopEventCounts(intersectionID, startTime, endTime))
                .thenReturn(stopLineStopEventCounts);

        // Mock LaneDirectionOfTravelAssessmentRepository
        LaneDirectionOfTravelAssessmentGroup assessmentGroup = new LaneDirectionOfTravelAssessmentGroup();
        assessmentGroup.setTolerance(0.0);
        assessmentGroup.setDistanceFromCenterlineTolerance(0.0);
        LaneDirectionOfTravelAssessment assessment = new LaneDirectionOfTravelAssessment();
        assessment.setLaneDirectionOfTravelAssessmentGroup(List.of(assessmentGroup));
        List<LaneDirectionOfTravelAssessment> laneDirectionOfTravelAssessments = List.of(assessment);
        when(laneDirectionOfTravelAssessmentRepo.getLaneDirectionOfTravelOverTime(intersectionID, startTime, endTime))
                .thenReturn(laneDirectionOfTravelAssessments);

        // Mock SpatMinimumDataEventRepository
        Page<SpatMinimumDataEvent> spatMinimumDataEventPage = new PageImpl<>(Collections.emptyList());
        when(spatMinimumDataEventRepo.findLatest(intersectionID, startTime, endTime))
                .thenReturn(spatMinimumDataEventPage);

        // Mock MapMinimumDataEventRepository
        Page<MapMinimumDataEvent> mapMinimumDataEventPage = new PageImpl<>(Collections.emptyList());
        when(mapMinimumDataEventRepo.findLatest(intersectionID, startTime, endTime))
                .thenReturn(mapMinimumDataEventPage);

        // Mock StopLineStopEventRepository
        Page<StopLineStopEvent> stopLineStopEventsPage = new PageImpl<>(Collections.emptyList());
        when(stopLineStopEventRepo.find(eq(intersectionID), eq(startTime), eq(endTime), any(PageRequest.class)))
                .thenReturn(stopLineStopEventsPage);

        // Mock StopLinePassageEventRepository
        Page<StopLinePassageEvent> stopLinePassageEventsPage = new PageImpl<>(Collections.emptyList());
        when(stopLinePassageEventRepo.find(eq(intersectionID), eq(startTime), eq(endTime), any(PageRequest.class)))
                .thenReturn(stopLinePassageEventsPage);

        // Mock ReportRepository
        doNothing().when(reportRepo).add(any(ReportDocument.class));

        ReportDocument result;
        try (MockedStatic<ConnectionOfTravelData> mockedStatic = mockStatic(ConnectionOfTravelData.class)) {
            ConnectionOfTravelData connectionOfTravelData = new ConnectionOfTravelData(
                    Collections.emptyList(),
                    Collections.emptyList());

            mockedStatic.when(
                    () -> ConnectionOfTravelData.processConnectionOfTravelData(laneConnectionCounts, processedMap))
                    .thenReturn(connectionOfTravelData);
            // Act
            result = reportService.buildReport(intersectionID, startTime, endTime);
        }

        // Assert
        assertNotNull(result);
        assertEquals(intersectionID, result.getIntersectionID());
        assertEquals(startTime, result.getReportStartTime());
        assertEquals(endTime, result.getReportStopTime());
        assertNotNull(result.getReportGeneratedAt());
        assertEquals(stopLinePassageEventCounts, result.getStopLinePassageEventCounts());
        assertEquals(stopLineStopEventCounts, result.getStopLineStopEventCounts());

        // Verify interactions
        verify(processedMapRepo, times(1)).findLatest(eq(intersectionID), any(), any(), eq(true));
        verify(stopLinePassageEventRepo, times(1))
                .getAggregatedDailyStopLinePassageEventCounts(intersectionID, startTime, endTime);
        verify(stopLineStopEventRepo, times(1))
                .getAggregatedDailyStopLineStopEventCounts(intersectionID, startTime, endTime);
        verify(reportRepo, times(1)).add(any(ReportDocument.class));
    }

    @Test
    void testBuildReportWithNoProcessedMap() {
        // Arrange
        int intersectionID = 1;
        long startTime = Instant.now().minusSeconds(3600).toEpochMilli();
        long endTime = Instant.now().toEpochMilli();

        // Mock ProcessedMapRepository to return an empty page
        Page<ProcessedMap<LineString>> emptyPage = new PageImpl<>(Collections.emptyList());
        when(processedMapRepo.findLatest(eq(intersectionID), any(), any(), eq(true))).thenReturn(emptyPage);

        // Mock other repositories
        when(stopLinePassageEventRepo.getAggregatedDailyStopLinePassageEventCounts(intersectionID, startTime, endTime))
                .thenReturn(Collections.emptyList());
        when(stopLineStopEventRepo.getAggregatedDailyStopLineStopEventCounts(intersectionID, startTime, endTime))
                .thenReturn(Collections.emptyList());
        when(laneDirectionOfTravelAssessmentRepo.getLaneDirectionOfTravelOverTime(intersectionID, startTime, endTime))
                .thenReturn(Collections.emptyList());
        doNothing().when(reportRepo).add(any(ReportDocument.class));

        // Mock SpatMinimumDataEventRepository
        Page<SpatMinimumDataEvent> spatMinimumDataEventPage = new PageImpl<>(Collections.emptyList());
        when(spatMinimumDataEventRepo.findLatest(intersectionID, startTime, endTime))
                .thenReturn(spatMinimumDataEventPage);

        // Mock MapMinimumDataEventRepository
        Page<MapMinimumDataEvent> mapMinimumDataEventPage = new PageImpl<>(Collections.emptyList());
        when(mapMinimumDataEventRepo.findLatest(intersectionID, startTime, endTime))
                .thenReturn(mapMinimumDataEventPage);

        // Mock StopLineStopEventRepository
        Page<StopLineStopEvent> stopLineStopEventsPage = new PageImpl<>(Collections.emptyList());
        when(stopLineStopEventRepo.find(eq(intersectionID), eq(startTime), eq(endTime), any(PageRequest.class)))
                .thenReturn(stopLineStopEventsPage);

        // Mock StopLinePassageEventRepository
        Page<StopLinePassageEvent> stopLinePassageEventsPage = new PageImpl<>(Collections.emptyList());
        when(stopLinePassageEventRepo.find(eq(intersectionID), eq(startTime), eq(endTime), any(PageRequest.class)))
                .thenReturn(stopLinePassageEventsPage);

        // Act
        ReportDocument result = reportService.buildReport(intersectionID, startTime, endTime);

        // Assert
        assertNotNull(result);
        assertEquals(intersectionID, result.getIntersectionID());
        assertEquals(startTime, result.getReportStartTime());
        assertEquals(endTime, result.getReportStopTime());
        assertEquals(result.getValidConnectionOfTravelData(), Collections.emptyList());
        assertEquals(result.getInvalidConnectionOfTravelData(), Collections.emptyList());

        // Verify interactions
        verify(processedMapRepo, times(1)).findLatest(eq(intersectionID), any(), any(), eq(true));
        verify(stopLinePassageEventRepo, times(1))
                .getAggregatedDailyStopLinePassageEventCounts(intersectionID, startTime, endTime);
        verify(stopLineStopEventRepo, times(1))
                .getAggregatedDailyStopLineStopEventCounts(intersectionID, startTime, endTime);
        verify(reportRepo, times(1)).add(any(ReportDocument.class));
    }
}