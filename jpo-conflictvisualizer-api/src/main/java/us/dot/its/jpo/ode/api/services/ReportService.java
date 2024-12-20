package us.dot.its.jpo.ode.api.services;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessmentGroup;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.MapMinimumDataEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.minimum_data.SpatMinimumDataEvent;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.connectinglanes.ConnectingLanesFeatureCollection;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.ode.api.ReportBuilder;
import us.dot.its.jpo.ode.api.accessors.assessments.ConnectionOfTravelAssessment.ConnectionOfTravelAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.LaneDirectionOfTravelAssessment.LaneDirectionOfTravelAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.SignalStateAssessment.StopLineStopAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.SignalStateEventAssessment.SignalStateEventAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.events.ConnectionOfTravelEvent.ConnectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.IntersectionReferenceAlignmentEvent.IntersectionReferenceAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.LaneDirectionOfTravelEvent.LaneDirectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.MapBroadcastRateEvents.MapBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.MapMinimumDataEvent.MapMinimumDataEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalGroupAlignmentEvent.SignalGroupAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateConflictEvent.SignalStateConflictEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateEvent.SignalStateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateStopEvent.SignalStateStopEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SpatBroadcastRateEvent.SpatBroadcastRateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SpatMinimumDataEvent.SpatMinimumDataEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.TimeChangeDetailsEvent.TimeChangeDetailsEventRepository;
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.api.accessors.reports.ReportRepository;
import us.dot.its.jpo.ode.api.accessors.spat.ProcessedSpatRepository;
import us.dot.its.jpo.ode.api.models.ChartData;
import us.dot.its.jpo.ode.api.models.DailyData;
import us.dot.its.jpo.ode.api.models.IDCount;
import us.dot.its.jpo.ode.api.models.LaneConnectionCount;
import us.dot.its.jpo.ode.api.models.LaneDirectionOfTravelReportData;
import us.dot.its.jpo.ode.api.models.ReportDocument;
import java.util.Arrays;

@Service
public class ReportService {

    @Autowired
    ProcessedMapRepository processedMapRepo;

    @Autowired
    ProcessedSpatRepository processedSpatRepo;

    @Autowired
    SignalStateEventRepository signalStateEventRepo;

    @Autowired
    SignalStateStopEventRepository signalStateStopEventRepo;

    @Autowired
    ConnectionOfTravelEventRepository connectionOfTravelEventRepo;

    @Autowired
    IntersectionReferenceAlignmentEventRepository intersectionReferenceAlignmentEventRepo;

    @Autowired
    LaneDirectionOfTravelEventRepository laneDirectionOfTravelEventRepo;

    @Autowired
    SignalGroupAlignmentEventRepository signalGroupAlignmentEventRepo;

    @Autowired
    SignalStateConflictEventRepository signalStateConflictEventRepo;

    @Autowired
    TimeChangeDetailsEventRepository timeChangeDetailsEventRepo;

    @Autowired
    LaneDirectionOfTravelAssessmentRepository laneDirectionOfTravelAssessmentRepo;

    @Autowired
    ConnectionOfTravelAssessmentRepository connectionOfTravelAssessmentRepo;

    @Autowired
    StopLineStopAssessmentRepository signalStateAssessmentRepo;

    @Autowired
    SignalStateEventAssessmentRepository signalStateEventAssessmentRepo;

    @Autowired
    SpatMinimumDataEventRepository spatMinimumDataEventRepo;

    @Autowired
    MapMinimumDataEventRepository mapMinimumDataEventRepo;

    @Autowired
    SpatBroadcastRateEventRepository spatBroadcastRateEventRepo;

    @Autowired
    MapBroadcastRateEventRepository mapBroadcastRateEventRepo;

    @Autowired
    ReportRepository reportRepo;
    
public class ConnectionOfTravelData {
    private List<Map<String, Object>> validConnections;
    private List<Map<String, Object>> invalidConnections;

    public ConnectionOfTravelData(List<Map<String, Object>> validConnections, List<Map<String, Object>> invalidConnections) {
        this.validConnections = validConnections;
        this.invalidConnections = invalidConnections;
    }

    public List<Map<String, Object>> getValidConnections() {
        return validConnections;
    }

    public List<Map<String, Object>> getInvalidConnections() {
        return invalidConnections;
    }
}

    private List<String> cleanMissingElements(List<String> elements, boolean isMap) {
        return elements.stream()
            .filter(element -> !(isMap && element.contains("connectsTo")))
            .map(element -> element.trim())
            .collect(Collectors.toList());
    }

    private ConnectionOfTravelData processConnectionOfTravelData(List<LaneConnectionCount> connectionCounts, ProcessedMap<LineString> mostRecentProcessedMap) {
        // Get the valid connections from the most recent processed map
        ConnectingLanesFeatureCollection<LineString> connectingLanesFeatureCollection = mostRecentProcessedMap.getConnectingLanesFeatureCollection();
        Set<String> validConnectionIDs = Arrays.stream(connectingLanesFeatureCollection.getFeatures())
            .map(feature -> feature.getId())
            .collect(Collectors.toSet());
    

        System.out.println("Valid Connection IDs: " + validConnectionIDs);

        List<Map<String, Object>> validConnections = new ArrayList<>();
        List<Map<String, Object>> invalidConnections = new ArrayList<>();
    
        for (LaneConnectionCount count : connectionCounts) {
            String key = count.getIngressLaneID() + "-" + count.getEgressLaneID();
            Map<String, Object> map = new HashMap<>();
            map.put("connectionID", key);
            map.put("ingressLaneID", count.getIngressLaneID());
            map.put("egressLaneID", count.getEgressLaneID());
            map.put("eventCount", count.getCount());
    
            if (validConnectionIDs.contains(key)) {
                validConnections.add(map);
            } else {
                invalidConnections.add(map);
            }
        }

        System.out.println("Valid Connections: " + validConnections);
        System.out.println("Invalid Connections: " + invalidConnections);
    
        return new ConnectionOfTravelData(validConnections, invalidConnections);
    }
    
private List<LaneDirectionOfTravelReportData> processLaneDirectionOfTravelData(List<LaneDirectionOfTravelAssessment> assessments) {
    List<LaneDirectionOfTravelReportData> reportDataList = new ArrayList<>();

    // Sort data by timestamp
    List<LaneDirectionOfTravelAssessment> sortedData = assessments.stream()
        .sorted(Comparator.comparing(LaneDirectionOfTravelAssessment::getTimestamp))
        .collect(Collectors.toList());

    // Group data by lane ID and segment ID
    Map<Integer, Map<Integer, List<LaneDirectionOfTravelReportData>>> groupedData = new HashMap<>();
    for (LaneDirectionOfTravelAssessment assessment : sortedData) {
        long minute = Instant.ofEpochMilli(assessment.getTimestamp()).truncatedTo(ChronoUnit.MINUTES).toEpochMilli();
        for (LaneDirectionOfTravelAssessmentGroup group : assessment.getLaneDirectionOfTravelAssessmentGroup()) {

            LaneDirectionOfTravelReportData reportData = new LaneDirectionOfTravelReportData();
            reportData.setTimestamp(minute);
            reportData.setLaneID(group.getLaneID());
            reportData.setSegmentID(group.getSegmentID());
            reportData.setHeadingDelta(group.getMedianHeading() - group.getExpectedHeading());
            reportData.setMedianCenterlineDistance(group.getMedianCenterlineDistance());

            groupedData
                .computeIfAbsent(group.getLaneID(), k -> new HashMap<>())
                .computeIfAbsent(group.getSegmentID(), k -> new ArrayList<>())
                .add(reportData);
        }
    }

    // Aggregate data by minute for each lane ID and segment ID
    for (Map.Entry<Integer, Map<Integer, List<LaneDirectionOfTravelReportData>>> laneEntry : groupedData.entrySet()) {
        int laneID = laneEntry.getKey();
        for (Map.Entry<Integer, List<LaneDirectionOfTravelReportData>> segmentEntry : laneEntry.getValue().entrySet()) {
            int segmentID = segmentEntry.getKey();
            Map<Long, List<LaneDirectionOfTravelReportData>> aggregatedData = segmentEntry.getValue().stream()
                .collect(Collectors.groupingBy(LaneDirectionOfTravelReportData::getTimestamp));

            for (Map.Entry<Long, List<LaneDirectionOfTravelReportData>> minuteEntry : aggregatedData.entrySet()) {
                long minute = minuteEntry.getKey();
                List<LaneDirectionOfTravelReportData> minuteData = minuteEntry.getValue();

                double averageHeadingDelta = minuteData.stream()
                    .mapToDouble(LaneDirectionOfTravelReportData::getHeadingDelta)
                    .average()
                    .orElse(0.0);

                double averageCenterlineDistance = minuteData.stream()
                    .mapToDouble(LaneDirectionOfTravelReportData::getMedianCenterlineDistance)
                    .average()
                    .orElse(0.0);

                LaneDirectionOfTravelReportData aggregatedReportData = new LaneDirectionOfTravelReportData();
                aggregatedReportData.setTimestamp(minute);
                aggregatedReportData.setLaneID(laneID);
                aggregatedReportData.setSegmentID(segmentID);
                aggregatedReportData.setHeadingDelta(averageHeadingDelta);
                aggregatedReportData.setMedianCenterlineDistance(averageCenterlineDistance);

                reportDataList.add(aggregatedReportData);
            }
        }
    }

    return reportDataList;
}

    public ReportDocument buildReport(int intersectionID, String roadRegulatorID, long startTime, long endTime) {
    
        String reportName = "CmReport_" + intersectionID + "_" + roadRegulatorID + "_" + startTime + "_" + endTime;
    
        // Lane Direction of Travel Info
        List<IDCount> laneDirectionOfTravelEventCounts = laneDirectionOfTravelEventRepo.getLaneDirectionOfTravelEventsByDay(intersectionID, startTime, endTime);
        List<IDCount> laneDirectionOfTravelMedianDistanceDistribution = laneDirectionOfTravelEventRepo.getMedianDistanceByFoot(intersectionID, startTime, endTime);
        List<IDCount> laneDirectionOfTravelMedianHeadingDistribution = laneDirectionOfTravelEventRepo.getMedianDistanceByDegree(intersectionID, startTime, endTime);
        List<LaneDirectionOfTravelAssessment> laneDirectionOfTravelAssessmentCount = laneDirectionOfTravelAssessmentRepo.getLaneDirectionOfTravelOverTime(intersectionID, startTime, endTime);
    
        // Connection of Travel Info
        List<IDCount> connectionOfTravelEventCounts = connectionOfTravelEventRepo.getConnectionOfTravelEventsByDay(intersectionID, startTime, endTime);
        List<LaneConnectionCount> laneConnectionCounts = connectionOfTravelEventRepo.getConnectionOfTravelEventsByConnection(intersectionID, startTime, endTime);
            
        // Retrieve the most recent ProcessedMap
        List<ProcessedMap<LineString>> processedMaps = processedMapRepo.findProcessedMaps(processedMapRepo.getQuery(intersectionID, startTime, endTime, true, true));
        ProcessedMap<LineString> mostRecentProcessedMap = processedMaps.isEmpty() ? null : processedMaps.get(0);
    
        // Process connection of travel data
        List<Map<String, Object>> validConnectionOfTravelData = new ArrayList<>();
        List<Map<String, Object>> invalidConnectionOfTravelData = new ArrayList<>();
        if (mostRecentProcessedMap != null && !laneConnectionCounts.isEmpty()) {
            ConnectionOfTravelData connectionData = processConnectionOfTravelData(laneConnectionCounts, mostRecentProcessedMap);
            validConnectionOfTravelData = connectionData.getValidConnections();
            invalidConnectionOfTravelData = connectionData.getInvalidConnections();
        }
    
        // Signal State Event Counts
        List<IDCount> signalstateEventCounts = signalStateEventRepo.getSignalStateEventsByDay(intersectionID, startTime, endTime);
    
        // Signal state Stop Events
        List<IDCount> signalStateStopEventCounts = signalStateStopEventRepo.getSignalStateStopEventsByDay(intersectionID, startTime, endTime);
    
        // Signal state Conflict Events
        List<IDCount> signalStateConflictEventCounts = signalStateConflictEventRepo.getSignalStateConflictEventsByDay(intersectionID, startTime, endTime);
        
        // Time Change Details Events
        List<IDCount> timeChangeDetailsEventCounts = timeChangeDetailsEventRepo.getTimeChangeDetailsEventsByDay(intersectionID, startTime, endTime);
    
        // Intersection Reference Alignment Event Counts
        List<IDCount> intersectionReferenceAlignmentEventCounts = intersectionReferenceAlignmentEventRepo.getIntersectionReferenceAlignmentEventsByDay(intersectionID, startTime, endTime);
    
        // Map / Spat counts
        List<IDCount> mapMinimumDataEventCount = mapMinimumDataEventRepo.getMapMinimumDataEventsByDay(intersectionID, startTime, endTime);
        List<IDCount> spatMinimumDataEventCount = spatMinimumDataEventRepo.getSpatMinimumDataEventsByDay(intersectionID, startTime, endTime);
    
        List<IDCount> mapBroadcastRateEventCount = mapBroadcastRateEventRepo.getMapBroadcastRateEventsByDay(intersectionID, startTime, endTime);
        List<IDCount> spatBroadcastRateEventCount = spatBroadcastRateEventRepo.getSpatBroadcastRateEventsByDay(intersectionID, startTime, endTime);
    
        List<SpatMinimumDataEvent> latestSpatMinimumdataEvent = spatMinimumDataEventRepo.find(spatMinimumDataEventRepo.getQuery(intersectionID, startTime, endTime, true));
        List<MapMinimumDataEvent> latestMapMinimumdataEvent = mapMinimumDataEventRepo.find(mapMinimumDataEventRepo.getQuery(intersectionID, startTime, endTime, true));
    
        // Parse missing elements from minimum data events
        List<String> latestMapMinimumDataEventMissingElements = latestMapMinimumdataEvent.isEmpty() ? 
            Collections.emptyList() : cleanMissingElements(latestMapMinimumdataEvent.get(0).getMissingDataElements(), true);
    
        List<String> latestSpatMinimumDataEventMissingElements = latestSpatMinimumdataEvent.isEmpty() ? 
            Collections.emptyList() : cleanMissingElements(latestSpatMinimumdataEvent.get(0).getMissingDataElements(), false);
    
        // Process lane direction of travel data
        List<LaneDirectionOfTravelReportData> laneDirectionOfTravelReportData = processLaneDirectionOfTravelData(laneDirectionOfTravelAssessmentCount);
    
        // Extract HeadingTolerance and DistanceTolerance from the most recent assessment
        double headingTolerance = 0.0;
        double distanceTolerance = 0.0;
        if (!laneDirectionOfTravelAssessmentCount.isEmpty()) {
            LaneDirectionOfTravelAssessment mostRecentAssessment = laneDirectionOfTravelAssessmentCount.get(0);
            if (!mostRecentAssessment.getLaneDirectionOfTravelAssessmentGroup().isEmpty()) {
                LaneDirectionOfTravelAssessmentGroup group = mostRecentAssessment.getLaneDirectionOfTravelAssessmentGroup().get(0);
                headingTolerance = group.getTolerance();
                distanceTolerance = group.getDistanceFromCenterlineTolerance();
            }
        }
    
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
    
        ReportBuilder builder = new ReportBuilder(stream);
        List<String> dateStrings = builder.getDayStringsInRange(startTime, endTime);
        builder.addTitlePage("Conflict Monitor Report", startTime, endTime);
    
        // Add Lane Direction of Travel Information
        builder.addTitle("Lane Direction of Travel");
        builder.addLaneDirectionOfTravelEvent(DailyData.fromIDCountDays(laneDirectionOfTravelEventCounts, dateStrings));
        builder.addLaneDirectionOfTravelMedianDistanceDistribution(ChartData.fromIDCountList(laneDirectionOfTravelMedianDistanceDistribution));
        builder.addLaneDirectionOfTravelMedianHeadingDistribution(ChartData.fromIDCountList(laneDirectionOfTravelMedianHeadingDistribution));
        builder.addDistanceFromCenterlineOverTime(laneDirectionOfTravelAssessmentCount);
        builder.addHeadingOverTime(laneDirectionOfTravelAssessmentCount);
        builder.addPageBreak();
    
        // Add Lane Connection of Travel Information
        builder.addTitle("Connection of Travel");
        builder.addConnectionOfTravelEvent(DailyData.fromIDCountDays(connectionOfTravelEventCounts, dateStrings));
        builder.addLaneConnectionOfTravelMap(laneConnectionCounts);
        builder.addPageBreak();
    
        // Add Signal State Events
        builder.addTitle("Signal State Events");
        builder.addSignalStateEvents(DailyData.fromIDCountDays(signalstateEventCounts, dateStrings));
        builder.addSignalStateStopEvents(DailyData.fromIDCountDays(signalStateStopEventCounts, dateStrings));
        builder.addSignalStateConflictEvent(DailyData.fromIDCountDays(signalStateConflictEventCounts, dateStrings));
    
        // Add Time Change Details
        builder.addSpatTimeChangeDetailsEvent(DailyData.fromIDCountDays(timeChangeDetailsEventCounts, dateStrings));
        builder.addPageBreak();
    
        // Add Intersection Reference Alignment Event Counts
        builder.addTitle("Intersection Reference Alignment Event Counts");
        builder.addIntersectionReferenceAlignmentEvents(DailyData.fromIDCountDays(intersectionReferenceAlignmentEventCounts, dateStrings));
        builder.addPageBreak();
    
        // Add Map Broadcast Rate Events
        builder.addTitle("MAP");
        builder.addMapBroadcastRateEvents(DailyData.fromIDCountDays(mapBroadcastRateEventCount, dateStrings));
        builder.addMapMinimumDataEvents(DailyData.fromIDCountDays(mapMinimumDataEventCount, dateStrings));
        builder.addPageBreak();
        builder.addMapMinimumDataEventErrors(latestMapMinimumdataEvent);
        builder.addPageBreak();
    
        // Add Map Broadcast Rate Events
        builder.addTitle("SPaT");
        builder.addSpatBroadcastRateEvents(DailyData.fromIDCountDays(spatBroadcastRateEventCount, dateStrings));
        builder.addSpatMinimumDataEvents(DailyData.fromIDCountDays(spatMinimumDataEventCount, dateStrings));
        builder.addPageBreak();
        builder.addSpatMinimumDataEventErrors(latestSpatMinimumdataEvent);
        builder.addPageBreak();
    
        builder.write();
    
        ReportDocument doc = new ReportDocument();
        doc.setIntersectionID(intersectionID);
        doc.setRoadRegulatorID(roadRegulatorID);
        doc.setReportGeneratedAt(Instant.now().toEpochMilli());
        doc.setReportStartTime(startTime);
        doc.setReportStopTime(endTime);
        doc.setReportContents(stream.toByteArray());
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
        doc.setSignalStateEventCounts(signalstateEventCounts);
        doc.setSignalStateStopEventCounts(signalStateStopEventCounts);
        doc.setTimeChangeDetailsEventCount(timeChangeDetailsEventCounts);
        doc.setIntersectionReferenceAlignmentEventCounts(intersectionReferenceAlignmentEventCounts);
        doc.setMapBroadcastRateEventCount(mapBroadcastRateEventCount);
        doc.setMapMinimumDataEventCount(mapMinimumDataEventCount);
        doc.setSpatBroadcastRateEventCount(spatBroadcastRateEventCount);
        doc.setSpatMinimumDataEventCount(spatMinimumDataEventCount);
        doc.setLatestMapMinimumDataEventMissingElements(latestMapMinimumDataEventMissingElements);
        doc.setLatestSpatMinimumDataEventMissingElements(latestSpatMinimumDataEventMissingElements);
        
        reportRepo.add(doc);
    
        return doc;
    
    }
}
