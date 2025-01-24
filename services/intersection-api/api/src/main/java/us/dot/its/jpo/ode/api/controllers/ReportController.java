package us.dot.its.jpo.ode.api.controllers;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import us.dot.its.jpo.ode.api.accessors.reports.ReportRepository;
import us.dot.its.jpo.ode.api.models.ReportDocument;
import us.dot.its.jpo.ode.api.services.ReportService;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
public class ReportController {

    @Autowired
    ReportService reportService;

    @Autowired
    ReportRepository reportRepo;

    @RequestMapping(value = "/reports/generate", method = RequestMethod.GET, produces = "application/octet-stream")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID) and @PermissionService.hasRole('USER')) ")
    public byte[] generateReport(
            @RequestParam(name = "intersection_id") int intersectionID,
            @RequestParam(name = "road_regulator_id", required = false) Integer roadRegulatorID,
            @RequestParam(name = "start_time_utc_millis") long startTime,
            @RequestParam(name = "end_time_utc_millis") long endTime) {
        log.debug("Generating Report");

        if (roadRegulatorID == null) {
            roadRegulatorID = -1;
        }

        ReportDocument document = reportService.buildReport(intersectionID, roadRegulatorID.toString(), startTime,
                endTime);

        return document.getReportContents();
    }

    @RequestMapping(value = "/reports/list", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    public ResponseEntity<List<ReportDocument>> listReports(
            @RequestParam(name = "report_name", required = false) String reportName,
            @RequestParam(name = "intersection_id", required = false) int intersectionID,
            @RequestParam(name = "road_regulator_id", required = false) Integer roadRegulatorID,
            @RequestParam(name = "start_time_utc_millis") long startTime,
            @RequestParam(name = "end_time_utc_millis") long endTime,
            @RequestParam(name = "latest") boolean latest) {

        if (roadRegulatorID == null) {
            roadRegulatorID = -1;
        }

        Query query = reportRepo.getQuery(reportName, intersectionID, roadRegulatorID, startTime, endTime,
                false,
                latest);
        return ResponseEntity.ok(reportRepo.find(query));
    }

    @RequestMapping(value = "/reports/download", method = RequestMethod.GET, produces = "application/octet-stream")
    @PreAuthorize("@PermissionService.hasRole('USER')")
    public ResponseEntity<byte[]> downloadReport(
            @RequestParam(name = "report_name") String reportName) {

        Query query = reportRepo.getQuery(reportName, null, null, null, null, true, true);

        log.debug("Returning archived report for download");

        List<ReportDocument> reports = reportRepo.find(query);
        if (!reports.isEmpty()) {
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + reportName + "\"")
                    .body(reports.getFirst().getReportContents());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}