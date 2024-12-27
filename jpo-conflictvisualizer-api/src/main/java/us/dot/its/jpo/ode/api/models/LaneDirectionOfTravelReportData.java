package us.dot.its.jpo.ode.api.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessmentGroup;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
public class LaneDirectionOfTravelReportData {
    private long timestamp;
    private int laneID;
    private int segmentID;
    private double headingDelta;
    private double medianCenterlineDistance;

    public static List<LaneDirectionOfTravelReportData> processLaneDirectionOfTravelData(List<LaneDirectionOfTravelAssessment> assessments) {
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
}