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
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and (@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN'))) ")
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
    @PreAuthorize("@PermissionService.isSuperUser() @PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN')")
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
    @PreAuthorize("@PermissionService.hasRole('USER') || @PermissionService.hasRole('ADMIN')")
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
}