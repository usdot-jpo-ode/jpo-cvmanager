package us.dot.its.jpo.ode.api.controllers;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;
import us.dot.its.jpo.ode.api.accessors.reports.ReportRepository;
import us.dot.its.jpo.ode.api.models.ReportDocument;
import us.dot.its.jpo.ode.api.services.ReportService;

@Slf4j
@RestController
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
})
public class ReportController {

    private final ReportService reportService;
    private final ReportRepository reportRepo;
    private final ConflictMonitorApiProperties props;

    @Autowired
    public ReportController(
            ReportService reportService,
            ReportRepository reportRepo,
            ConflictMonitorApiProperties props) {
        this.reportService = reportService;
        this.reportRepo = reportRepo;
        this.props = props;
    }

    @Operation(summary = "Generate a Report", description = "Generates a new report for the intersection specified, within the start and end time. This can take upwards of 15 minutes to complete for longer reports")
    @RequestMapping(value = "/reports/generate", method = RequestMethod.GET, produces = "application/octet-stream")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public byte[] generateReport(
            @RequestParam(name = "intersection_id") int intersectionID,
            @RequestParam(name = "road_regulator_id", required = false) Integer roadRegulatorID,
            @RequestParam(name = "start_time_utc_millis") long startTime,
            @RequestParam(name = "end_time_utc_millis") long endTime) {
        log.debug("Generating Report");

        if (roadRegulatorID == null) {
            roadRegulatorID = -1;
        }

        ReportDocument document = reportService.buildReport(intersectionID, roadRegulatorID.toString(),
                startTime,
                endTime);

        return document.getReportContents();
    }

    @Operation(summary = "List Reports", description = "Returns a list of existing reports, as aggregated data, filtered by name, intersection ID, start time, and end time. The latest parameter will return the most recent report.")
    @RequestMapping(value = "/reports/list", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "206", description = "Partial Content - The requested query may have more results than allowed by server. Please reduce the query bounds and try again."),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role"),
    })
    public ResponseEntity<List<ReportDocument>> listReports(
            @RequestParam(name = "report_name", required = false) String reportName,
            @RequestParam(name = "intersection_id") int intersectionID,
            @RequestParam(name = "start_time_utc_millis") long startTime,
            @RequestParam(name = "end_time_utc_millis") long endTime,
            @RequestParam(name = "latest") boolean latest) {

        Query query = reportRepo.getQuery(reportName, intersectionID, startTime, endTime,
                false,
                latest);
        List<ReportDocument> results = reportRepo.find(query);
        return new ResponseEntity<>(results, new HttpHeaders(),
                results.size() == props.getMaximumResponseSize() ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK);
    }

    @Operation(summary = "Download a Report", description = "Returns the a report by name, as aggregated data")
    @RequestMapping(value = "/reports/download", method = RequestMethod.GET, produces = "application/octet-stream")
    @PreAuthorize("@PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role"),
            @ApiResponse(responseCode = "404", description = "Report not found"),
    })
    public ResponseEntity<byte[]> downloadReport(
            @RequestParam(name = "report_name") String reportName) {

        Query query = reportRepo.getQuery(reportName, null, null, null, true, true);

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