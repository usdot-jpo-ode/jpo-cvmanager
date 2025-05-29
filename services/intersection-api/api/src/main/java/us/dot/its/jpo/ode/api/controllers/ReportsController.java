package us.dot.its.jpo.ode.api.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
@RequestMapping("/reports")
public class ReportsController {

    private final ReportService reportService;
    private final ReportRepository reportRepo;

    @Autowired
    public ReportsController(
            ReportService reportService,
            ReportRepository reportRepo) {
        this.reportService = reportService;
        this.reportRepo = reportRepo;
    }

    @Operation(summary = "Generate a Report", description = "Generates a new report for the intersection specified, within the start and end time. This can take upwards of 15 minutes to complete for longer reports")
    @RequestMapping(value = "/intersection/generate", method = RequestMethod.GET, produces = "application/octet-stream")
    @PreAuthorize("@PermissionService.isSuperUser() || (@PermissionService.hasIntersection(#intersectionID, 'USER') and @PermissionService.hasRole('USER')) ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role with access to the intersection requested"),
    })
    public byte[] generateReport(
            @RequestParam(name = "intersection_id") int intersectionID,
            @RequestParam(name = "start_time_utc_millis") long startTime,
            @RequestParam(name = "end_time_utc_millis") long endTime) {
        log.debug("Generating Report");

        ReportDocument document = reportService.buildReport(intersectionID, startTime, endTime);

        return document.getReportContents();
    }

    @Operation(summary = "List Reports", description = "Returns a list of existing intersection reports, as aggregated data, filtered by name, intersection ID, start time, and end time. The latest parameter will return the most recent report.")
    @RequestMapping(value = "/intersection", method = RequestMethod.GET, produces = "application/json")
    @PreAuthorize("@PermissionService.isSuperUser() || @PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_USER or USER role"),
    })
    public ResponseEntity<Page<ReportDocument>> listReports(
            @RequestParam(name = "report_name", required = false) String reportName,
            @RequestParam(name = "intersection_id") int intersectionID,
            @RequestParam(name = "start_time_utc_millis") long startTime,
            @RequestParam(name = "end_time_utc_millis") long endTime,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") int size,
            @RequestParam(name = "latest") boolean latest) {

        if (latest) {
            return ResponseEntity.ok(reportRepo.findLatest(reportName, intersectionID, startTime, endTime, false));
        } else {
            // Retrieve a paginated result from the repository
            PageRequest pageable = PageRequest.of(page, size);
            Page<ReportDocument> response = reportRepo.find(reportName, intersectionID, startTime, endTime,
                    false, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Download a Report", description = "Returns the a report by name, as aggregated data")
    @RequestMapping(value = "/intersection/download", method = RequestMethod.GET, produces = "application/octet-stream")
    @PreAuthorize("@PermissionService.hasRole('USER')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role"),
            @ApiResponse(responseCode = "404", description = "Report not found"),
    })
    public ResponseEntity<byte[]> downloadReport(
            @RequestParam(name = "report_name") String reportName) {

        Page<ReportDocument> reports = reportRepo.findLatest(reportName, null, null, null, true);

        if (!reports.isEmpty()) {
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + reportName + "\"")
                    .body(reports.getContent().getFirst().getReportContents());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}