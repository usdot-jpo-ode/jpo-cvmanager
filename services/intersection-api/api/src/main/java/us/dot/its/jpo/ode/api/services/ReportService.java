package us.dot.its.jpo.ode.api.services;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessmentGroup;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLinePassageEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.StopLineStopEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.MapMinimumDataEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.SpatMinimumDataEvent;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.accessors.assessments.LaneDirectionOfTravelAssessment.LaneDirectionOfTravelAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.events.ConnectionOfTravelEvent.ConnectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.IntersectionReferenceAlignmentEvent.IntersectionReferenceAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.LaneDirectionOfTravelEvent.LaneDirectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.MapBroadcastRateEvents.MapBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.MapMinimumDataEvent.MapMinimumDataEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateConflictEvent.SignalStateConflictEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateEvent.SignalStateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateStopEvent.SignalStateStopEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SpatBroadcastRateEvent.SpatBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SpatMinimumDataEvent.SpatMinimumDataEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.TimeChangeDetailsEvent.TimeChangeDetailsEventRepository;
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;

import us.dot.its.jpo.ode.api.accessors.reports.ReportRepository;
import us.dot.its.jpo.ode.api.models.ConnectionData;
import us.dot.its.jpo.ode.api.models.ConnectionOfTravelData;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.LaneConnectionCount;
import us.dot.its.jpo.ode.api.models.LaneDirectionOfTravelReportData;
import us.dot.its.jpo.ode.api.models.ReportDocument;
import us.dot.its.jpo.ode.api.models.StopLinePassageReportData;
import us.dot.its.jpo.ode.api.models.StopLineStopReportData;

@Service
public class ReportService {

    private final ProcessedMapRepository processedMapRepo;
    private final SignalStateEventRepository signalStateEventRepo;
    private final SignalStateStopEventRepository signalStateStopEventRepo;
    private final ConnectionOfTravelEventRepository connectionOfTravelEventRepo;
    private final IntersectionReferenceAlignmentEventRepository intersectionReferenceAlignmentEventRepo;
    private final LaneDirectionOfTravelEventRepository laneDirectionOfTravelEventRepo;
    private final SignalStateConflictEventRepository signalStateConflictEventRepo;
    private final TimeChangeDetailsEventRepository timeChangeDetailsEventRepo;
    private final LaneDirectionOfTravelAssessmentRepository laneDirectionOfTravelAssessmentRepo;
    private final SpatMinimumDataEventRepository spatMinimumDataEventRepo;
    private final MapMinimumDataEventRepository mapMinimumDataEventRepo;
    private final SpatBroadcastRateEventRepository spatBroadcastRateEventRepo;
    private final MapBroadcastRateEventRepository mapBroadcastRateEventRepo;
    private final ReportRepository reportRepo;

    int maximumResponseSize;

    @Autowired
    public ReportService(ProcessedMapRepository processedMapRepo,
            SignalStateEventRepository signalStateEventRepo,
            SignalStateStopEventRepository signalStateStopEventRepo,
            ConnectionOfTravelEventRepository connectionOfTravelEventRepo,
            IntersectionReferenceAlignmentEventRepository intersectionReferenceAlignmentEventRepo,
            LaneDirectionOfTravelEventRepository laneDirectionOfTravelEventRepo,
            SignalStateConflictEventRepository signalStateConflictEventRepo,
            TimeChangeDetailsEventRepository timeChangeDetailsEventRepo,
            LaneDirectionOfTravelAssessmentRepository laneDirectionOfTravelAssessmentRepo,
            SpatMinimumDataEventRepository spatMinimumDataEventRepo,
            MapMinimumDataEventRepository mapMinimumDataEventRepo,
            SpatBroadcastRateEventRepository spatBroadcastRateEventRepo,
            MapBroadcastRateEventRepository mapBroadcastRateEventRepo,
            ReportRepository reportRepo,
            @Value("${maximumResponseSize}") int maximumResponseSize) {
        this.processedMapRepo = processedMapRepo;
        this.signalStateEventRepo = signalStateEventRepo;
        this.signalStateStopEventRepo = signalStateStopEventRepo;
        this.connectionOfTravelEventRepo = connectionOfTravelEventRepo;
        this.intersectionReferenceAlignmentEventRepo = intersectionReferenceAlignmentEventRepo;
        this.laneDirectionOfTravelEventRepo = laneDirectionOfTravelEventRepo;
        this.signalStateConflictEventRepo = signalStateConflictEventRepo;
        this.timeChangeDetailsEventRepo = timeChangeDetailsEventRepo;
        this.laneDirectionOfTravelAssessmentRepo = laneDirectionOfTravelAssessmentRepo;
        this.spatMinimumDataEventRepo = spatMinimumDataEventRepo;
        this.mapMinimumDataEventRepo = mapMinimumDataEventRepo;
        this.spatBroadcastRateEventRepo = spatBroadcastRateEventRepo;
        this.mapBroadcastRateEventRepo = mapBroadcastRateEventRepo;
        this.reportRepo = reportRepo;
        this.maximumResponseSize = maximumResponseSize;

    }

    private List<String> cleanMissingElements(List<String> elements, boolean isMap) {
        return elements.stream()
                .filter(element -> !(isMap && element.contains("connectsTo")))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public ReportDocument buildReport(int intersectionID, String roadRegulatorID, long startTime, long endTime) {
        // ####### 1. Name Report #######
        String reportName = "CmReport_" + intersectionID + "_" + roadRegulatorID + "_" + startTime + "_" + endTime;

        // ####### 2. Collect Report Data By Category #######
        // Lane Direction of Travel Info
        List<IDCount> laneDirectionOfTravelEventCounts = laneDirectionOfTravelEventRepo
                .getAggregatedDailyLaneDirectionOfTravelEventCounts(intersectionID, startTime, endTime);
        List<IDCount> laneDirectionOfTravelMedianDistanceDistribution = laneDirectionOfTravelEventRepo
                .countEventsByCenterlineDistance(intersectionID, startTime, endTime);
        List<IDCount> laneDirectionOfTravelMedianHeadingDistribution = laneDirectionOfTravelEventRepo
                .getMedianDistanceByDegree(intersectionID, startTime, endTime);
        List<LaneDirectionOfTravelAssessment> laneDirectionOfTravelAssessmentCount = laneDirectionOfTravelAssessmentRepo
                .getLaneDirectionOfTravelOverTime(intersectionID, startTime, endTime);

        // Connection of Travel Info
        List<IDCount> connectionOfTravelEventCounts = connectionOfTravelEventRepo
                .getAggregatedDailyConnectionOfTravelEventCounts(intersectionID, startTime, endTime);
        List<LaneConnectionCount> laneConnectionCounts = connectionOfTravelEventRepo
                .getConnectionOfTravelEventsByConnection(intersectionID, startTime, endTime);

        // Retrieve the most recent ProcessedMap
        List<ProcessedMap<LineString>> processedMaps = processedMapRepo.findLatest(intersectionID, null, null, true)
                .getContent();
        ProcessedMap<LineString> mostRecentProcessedMap = processedMaps.isEmpty() ? null : processedMaps.getFirst();

        // Process connection of travel data
        List<ConnectionData> validConnectionOfTravelData = new ArrayList<>();
        List<ConnectionData> invalidConnectionOfTravelData = new ArrayList<>();
        if (mostRecentProcessedMap != null && !laneConnectionCounts.isEmpty()) {
            ConnectionOfTravelData connectionData = ConnectionOfTravelData
                    .processConnectionOfTravelData(laneConnectionCounts, mostRecentProcessedMap);
            validConnectionOfTravelData = connectionData.getValidConnections();
            invalidConnectionOfTravelData = connectionData.getInvalidConnections();
        }

        // Signal State Event Counts
        List<IDCount> signalStateEventCounts = signalStateEventRepo.getAggregatedDailySignalStateEventCounts(
                intersectionID, startTime,
                endTime);

        // Signal state Stop Events
        List<IDCount> signalStateStopEventCounts = signalStateStopEventRepo
                .getAggregatedDailySignalStateStopEventCounts(intersectionID, startTime, endTime);

        // Signal state Conflict Events
        List<IDCount> signalStateConflictEventCounts = signalStateConflictEventRepo
                .getAggregatedDailySignalStateConflictEventCounts(intersectionID, startTime, endTime);

        // Time Change Details Events
        List<IDCount> timeChangeDetailsEventCounts = timeChangeDetailsEventRepo
                .getAggregatedDailyTimeChangeDetailsEventCounts(intersectionID, startTime, endTime);

        // Intersection Reference Alignment Event Counts
        List<IDCount> intersectionReferenceAlignmentEventCounts = intersectionReferenceAlignmentEventRepo
                .getAggregatedDailyIntersectionReferenceAlignmentEventCounts(intersectionID, startTime, endTime);

        // Map / Spat counts
        List<IDCount> mapMinimumDataEventCount = mapMinimumDataEventRepo.getAggregatedDailyMapMinimumDataEventCounts(
                intersectionID,
                startTime, endTime);
        List<IDCount> spatMinimumDataEventCount = spatMinimumDataEventRepo.getAggregatedDailySpatMinimumDataEventCounts(
                intersectionID,
                startTime, endTime);

        List<IDCount> mapBroadcastRateEventCount = mapBroadcastRateEventRepo
                .getAggregatedDailyMapBroadcastRateEventCounts(intersectionID, startTime, endTime);
        List<IDCount> spatBroadcastRateEventCount = spatBroadcastRateEventRepo
                .getAggregatedDailySpatBroadcastRateEventCounts(intersectionID, startTime, endTime);

        List<SpatMinimumDataEvent> spatMinimumDataEvents = spatMinimumDataEventRepo
                .findLatest(intersectionID, startTime, endTime).getContent();
        List<MapMinimumDataEvent> mapMinimumDataEvents = mapMinimumDataEventRepo
                .findLatest(intersectionID, startTime, endTime).getContent();

        // Parse missing elements from minimum data events
        List<String> latestMapMinimumDataEventMissingElements = mapMinimumDataEvents.isEmpty()
                ? Collections.emptyList()
                : cleanMissingElements(mapMinimumDataEvents.getFirst().getMissingDataElements(), true);

        List<String> latestSpatMinimumDataEventMissingElements = spatMinimumDataEvents.isEmpty()
                ? Collections.emptyList()
                : cleanMissingElements(spatMinimumDataEvents.getFirst().getMissingDataElements(), false);

        // Process lane direction of travel data
        List<LaneDirectionOfTravelReportData> laneDirectionOfTravelReportData = LaneDirectionOfTravelReportData
                .processLaneDirectionOfTravelData(laneDirectionOfTravelAssessmentCount);

        // Extract HeadingTolerance and DistanceTolerance from the most recent
        // assessment
        double headingTolerance = 0.0;
        double distanceTolerance = 0.0;
        if (!laneDirectionOfTravelAssessmentCount.isEmpty()) {
            LaneDirectionOfTravelAssessment mostRecentAssessment = laneDirectionOfTravelAssessmentCount.getFirst();
            if (!mostRecentAssessment.getLaneDirectionOfTravelAssessmentGroup().isEmpty()) {
                LaneDirectionOfTravelAssessmentGroup group = mostRecentAssessment
                        .getLaneDirectionOfTravelAssessmentGroup().getFirst();
                headingTolerance = group.getTolerance();
                distanceTolerance = group.getDistanceFromCenterlineTolerance();
            }
        }

        // Retrieve StopLineStopEvents
        List<StopLineStopEvent> stopLineStopEvents = signalStateStopEventRepo
                .find(intersectionID, startTime, endTime, PageRequest.of(0, maximumResponseSize)).getContent();
        List<StopLineStopReportData> stopLineStopReportData = StopLineStopReportData
                .aggregateStopLineStopEvents(stopLineStopEvents);

        // Retrieve SignalStateEvents
        List<StopLinePassageEvent> signalStateEvents = signalStateEventRepo
                .find(intersectionID, startTime, endTime,
                        PageRequest.of(0, maximumResponseSize))
                .getContent();
        List<StopLinePassageReportData> stopLinePassageReportData = StopLinePassageReportData
                .aggregateSignalStateEvents(signalStateEvents);

        // ####### 3. Create Report Document #######
        ReportDocument doc = new ReportDocument();
        doc.setIntersectionID(intersectionID);
        doc.setRoadRegulatorID(roadRegulatorID);
        doc.setReportGeneratedAt(Instant.now().toEpochMilli());
        doc.setReportStartTime(startTime);
        doc.setReportStopTime(endTime);
        doc.setReportContents(new byte[] {});
        doc.setReportName(reportName);
        doc.setLaneDirectionOfTravelEventCounts(laneDirectionOfTravelEventCounts);
        doc.setLaneDirectionOfTravelMedianDistanceDistribution(laneDirectionOfTravelMedianDistanceDistribution);
        doc.setLaneDirectionOfTravelMedianHeadingDistribution(laneDirectionOfTravelMedianHeadingDistribution);
        doc.setLaneDirectionOfTravelReportData(laneDirectionOfTravelReportData);
        doc.setHeadingTolerance(headingTolerance);
        doc.setDistanceTolerance(distanceTolerance);
        doc.setConnectionOfTravelEventCounts(connectionOfTravelEventCounts);
        doc.setValidConnectionOfTravelData(validConnectionOfTravelData);
        doc.setInvalidConnectionOfTravelData(invalidConnectionOfTravelData);
        doc.setSignalStateConflictEventCount(signalStateConflictEventCounts);
        doc.setSignalStateEventCounts(signalStateEventCounts);
        doc.setSignalStateStopEventCounts(signalStateStopEventCounts);
        doc.setTimeChangeDetailsEventCount(timeChangeDetailsEventCounts);
        doc.setIntersectionReferenceAlignmentEventCounts(intersectionReferenceAlignmentEventCounts);
        doc.setMapBroadcastRateEventCount(mapBroadcastRateEventCount);
        doc.setMapMinimumDataEventCount(mapMinimumDataEventCount);
        doc.setSpatBroadcastRateEventCount(spatBroadcastRateEventCount);
        doc.setSpatMinimumDataEventCount(spatMinimumDataEventCount);
        doc.setLatestMapMinimumDataEventMissingElements(latestMapMinimumDataEventMissingElements);
        doc.setLatestSpatMinimumDataEventMissingElements(latestSpatMinimumDataEventMissingElements);
        doc.setStopLineStopReportData(stopLineStopReportData);
        doc.setStopLinePassageReportData(stopLinePassageReportData);

        // ####### 4. Save Report Document to Database #######
        reportRepo.add(doc);
        return doc;
    }
}
