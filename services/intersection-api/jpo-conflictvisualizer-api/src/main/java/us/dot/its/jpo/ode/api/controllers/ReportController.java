package us.dot.its.jpo.ode.api.controllers;

import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.accessors.reports.ReportRepository;
import us.dot.its.jpo.ode.api.models.ReportDocument;
import us.dot.its.jpo.ode.api.services.ReportService;

@RestController
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    ConflictMonitorApiProperties props;

    @Autowired
    ReportService reportService;

    @Autowired
    ReportRepository reportRepo;

    public String getCurrentTime() {
        return ZonedDateTime.now().toInstant().toEpochMilli() + "";
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/reports/generate", method = RequestMethod.GET, produces = "application/octet-stream")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public byte[] generateReport(
            @RequestParam(name = "intersection_id", required = true) int intersectionID,
            @RequestParam(name = "road_regulator_id", required = false) Integer roadRegulatorID,
            @RequestParam(name = "start_time_utc_millis", required = true) long startTime,
            @RequestParam(name = "end_time_utc_millis", required = true) long endTime) {
        logger.info("Generating Report");

        if (roadRegulatorID == null) {
            roadRegulatorID = -1;
        }

        ReportDocument document = reportService.buildReport(intersectionID, roadRegulatorID.toString(), startTime,
                endTime);

        return document.getReportContents();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/reports/list", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<List<ReportDocument>> listReports(
            @RequestParam(name = "report_name", required = false) String reportName,
            @RequestParam(name = "intersection_id", required = false) int intersectionID,
            @RequestParam(name = "road_regulator_id", required = false) Integer roadRegulatorID,
            @RequestParam(name = "start_time_utc_millis", required = true) long startTime,
            @RequestParam(name = "end_time_utc_millis", required = true) long endTime,
            @RequestParam(name = "latest", required = true) boolean latest) {

        if (roadRegulatorID == null) {
            roadRegulatorID = -1;
        }

        Query query = reportRepo.getQuery(reportName, intersectionID, roadRegulatorID, startTime, endTime,
                false,
                latest);
        long count = reportRepo.getQueryResultCount(query);

        logger.info("Returning Report List with Size: " + count);
        return ResponseEntity.ok(reportRepo.find(query));
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/reports/download", method = RequestMethod.GET, produces = "application/octet-stream")
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadReport(
            @RequestParam(name = "report_name", required = true) String reportName) {

        Query query = reportRepo.getQuery(reportName, null, null, null, null, true, true);

        logger.info("Returning archived report for download");

        List<ReportDocument> reports = reportRepo.find(query);
        if (reports.size() > 0) {
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + reportName + "\"")
                    .body(reports.get(0).getReportContents());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // @Bean
    // public void testReportDownload(){

    // // Query query = reportRepo.getQuery(null, null, null, null, null, false,
    // false);
    // // long count = reportRepo.getQueryResultCount(query);

    // // System.out.println("Returning Report List with Size: " + count);

    // // for(ReportDocument doc : reportRepo.find(query)){
    // // System.out.println(doc);

    // // }

    // // Query query =
    // reportRepo.getQuery("CmReport_1234_-1_1698710400000_1699315200000", null,
    // null, null, null, true, true);
    // // long count = reportRepo.getQueryResultCount(query);
    // // System.out.println("Returning Report List with Size: " + count);
    // // System.out.println(reportRepo.find(query));

    // }

    // @Bean
    // public void test(){
    // System.out.println("Generating Test PDF");

    // int intersectionID = 6311;
    // long startTime = 0;
    // // long startTime = 1678233600000L;
    // long endTime = Instant.now().toEpochMilli();

    // ReportDocument document = buildReport(intersectionID, roadRegulatorID,
    // startTime, endTime);

    // // List<IDCount> laneDirectionOfTravelEventCounts =
    // laneDirectionOfTravelEventRepo.getLaneDirectionOfTravelEventsByDay(intersectionID,
    // startTime, endTime);
    // // List<IDCount> laneDirectionOfTravelMedianDistanceDistribution =
    // laneDirectionOfTravelEventRepo.getMedianDistanceByFoot(intersectionID,
    // startTime, endTime);
    // // List<IDCount> laneDirectionOfTravelMedianHeadingDistribution =
    // laneDirectionOfTravelEventRepo.getMedianDistanceByDegree(intersectionID,
    // startTime, endTime);
    // // List<LaneDirectionOfTravelAssessment> laneDirectionOfTravelAssessmentCount
    // =
    // laneDirectionOfTravelAssessmentRepo.getLaneDirectionOfTravelOverTime(intersectionID,
    // startTime, endTime);

    // // List<IDCount> connectionOfTravelEventCounts =
    // connectionOfTravelEventRepo.getConnectionOfTravelEventsByDay(intersectionID,
    // startTime, endTime);
    // // List<LaneConnectionCount> laneConnectionCounts =
    // connectionOfTravelEventRepo.getConnectionOfTravelEventsByConnection(intersectionID,
    // startTime, endTime);

    // // List<IDCount> signalstateEventCounts =
    // signalStateEventRepo.getSignalStateEventsByDay(intersectionID, startTime,
    // endTime);

    // // List<IDCount> signalStateStopEventCounts =
    // signalStateStopEventRepo.getSignalStateStopEventsByDay(intersectionID,
    // startTime, endTime);

    // // List<IDCount> signalStateConflictEventCounts =
    // signalStateConflictEventRepo.getSignalStateConflictEventsByDay(intersectionID,
    // startTime, endTime);

    // // List<IDCount> timeChangeDetailsEventCounts=
    // timeChangeDetailsEventRepo.getTimeChangeDetailsEventsByDay(intersectionID,
    // startTime, endTime);

    // // List<IDCount> mapCounts =
    // processedMapRepo.getMapBroadcastRates(intersectionID, startTime, endTime);
    // // List<IDCount> spatCounts =
    // processedSpatRepo.getSpatBroadcastRates(intersectionID, startTime, endTime);

    // // List<IDCount> spatCountDistribution =
    // processedSpatRepo.getSpatBroadcastRateDistribution(intersectionID, startTime,
    // endTime);
    // // List<IDCount> mapCountDistribution =
    // processedMapRepo.getMapBroadcastRateDistribution(intersectionID, startTime,
    // endTime);

    // try {

    // ReportBuilder builder = new ReportBuilder(new FileOutputStream("test.pdf"));
    // List<String> dateStrings = builder.getDayStringsInRange(startTime, endTime);
    // builder.addTitlePage("Conflict Monitor Report", startTime, endTime);

    // // // Add Lane Direction of Travel Information
    // // builder.addTitle("Lane Direction of Travel");
    // //
    // builder.addLaneDirectionOfTravelEvent(DailyData.fromIDCountDays(laneDirectionOfTravelEventCounts,
    // dateStrings));
    // //
    // builder.addLaneDirectionOfTravelMedianDistanceDistribution(ChartData.fromIDCountList(laneDirectionOfTravelMedianDistanceDistribution));
    // //
    // builder.addLaneDirectionOfTravelMedianHeadingDistribution(ChartData.fromIDCountList(laneDirectionOfTravelMedianHeadingDistribution));
    // //
    // builder.addDistanceFromCenterlineOverTime(laneDirectionOfTravelAssessmentCount);
    // // builder.addHeadingOverTime(laneDirectionOfTravelAssessmentCount);
    // // builder.addPageBreak();

    // // // Add Lane Connection of Travel Information
    // // builder.addTitle("Connection of Travel");
    // //
    // builder.addConnectionOfTravelEvent(DailyData.fromIDCountDays(connectionOfTravelEventCounts,
    // dateStrings));
    // // builder.addLaneConnectionOfTravelMap(laneConnectionCounts);
    // // builder.addPageBreak();

    // // // Add Signal State Events
    // // builder.addTitle("Signal State Events");
    // //
    // builder.addSignalStateEvents(DailyData.fromIDCountDays(signalstateEventCounts,
    // dateStrings));
    // //
    // builder.addSignalStateStopEvents(DailyData.fromIDCountDays(signalStateStopEventCounts,
    // dateStrings));
    // //
    // builder.addSignalStateConflictEvent(DailyData.fromIDCountDays(signalStateConflictEventCounts,
    // dateStrings));
    // // builder.addPageBreak();

    // // // Add Time Change Details
    // //
    // builder.addSpatTimeChangeDetailsEvent(DailyData.fromIDCountDays(timeChangeDetailsEventCounts,
    // dateStrings));
    // // builder.addPageBreak();

    // // builder.addTitle("Map");
    // // builder.addMapBroadcastRate(mapCounts);
    // // builder.addMapBroadcastRateDistribution(mapCountDistribution, startTime,
    // endTime);
    // // builder.addPageBreak();

    // // builder.addTitle("SPaT");
    // // builder.addSpatBroadcastRate(spatCounts);
    // // builder.addSpatBroadcastRateDistribution(spatCountDistribution, startTime,
    // endTime);
    // // builder.addPageBreak();

    // // List<Long> secondStrings = builder.getSecondsStringInRange(startTime,
    // endTime);

    // // builder.addSpatMinimumDataEventErrors(latestSpatMinimumdataEvent);
    // // builder.addMapMinimumDataEventErrors(latestMapMinimumdataEvent);

    // builder.write();

    // } catch (FileNotFoundException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // System.out.println("Test PDF Generation Complete");

    // }
}