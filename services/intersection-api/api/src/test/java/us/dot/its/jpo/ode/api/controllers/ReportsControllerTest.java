package us.dot.its.jpo.ode.api.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.api.accessors.reports.ReportRepository;
import us.dot.its.jpo.ode.api.models.ReportDocument;
import us.dot.its.jpo.ode.api.services.ReportService;

public class ReportsControllerTest {

    @Mock
    ReportService reportService;

    @Mock
    ReportRepository reportRepo;

    ReportsController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ReportsController(reportService, reportRepo);
    }

    @Test
    void testGenerateReport() {
        ReportDocument doc = new ReportDocument();
        doc.setReportContents(new byte[] { 1, 2, 3 });
        when(reportService.buildReport(anyInt(), anyLong(), anyLong())).thenReturn(doc);

        byte[] result = controller.generateReport(1, 1000L, 2000L);

        assertThat(result).containsExactly(1, 2, 3);
        verify(reportService).buildReport(1, 1000L, 2000L);
    }

    @Test
    void testListReportsLatest() {
        ReportDocument doc = new ReportDocument();
        Page<ReportDocument> page = new PageImpl<>(Collections.singletonList(doc));
        when(reportRepo.findLatest(any(), anyInt(), anyLong(), anyLong(), eq(false))).thenReturn(page);

        ResponseEntity<Page<ReportDocument>> response = controller.listReports("name", 1, 1000L, 2000L, 0, 10, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).contains(doc);
    }

    @Test
    void testListReportsPaginated() {
        ReportDocument doc = new ReportDocument();
        Page<ReportDocument> page = new PageImpl<>(Collections.singletonList(doc));
        when(reportRepo.find(any(), anyInt(), anyLong(), anyLong(), eq(false), any(PageRequest.class)))
                .thenReturn(page);

        ResponseEntity<Page<ReportDocument>> response = controller.listReports("name", 1, 1000L, 2000L, 0, 10, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).contains(doc);
    }

    @Test
    void testDownloadReportFound() {
        ReportDocument doc = new ReportDocument();
        doc.setReportContents(new byte[] { 4, 5, 6 });
        Page<ReportDocument> page = new PageImpl<>(Collections.singletonList(doc));
        when(reportRepo.findLatest(eq("report.pdf"), isNull(), isNull(), isNull(), eq(true))).thenReturn(page);

        ResponseEntity<byte[]> response = controller.downloadReport("report.pdf");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst("Content-Disposition")).contains("report.pdf");
        assertThat(response.getBody()).containsExactly(4, 5, 6);
    }

    @Test
    void testDownloadReportNotFound() {
        Page<ReportDocument> page = new PageImpl<>(Collections.emptyList());
        when(reportRepo.findLatest(eq("missing.pdf"), isNull(), isNull(), isNull(), eq(true))).thenReturn(page);

        ResponseEntity<byte[]> response = controller.downloadReport("missing.pdf");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }
}