package us.dot.its.jpo.ode.api.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.api.models.IntersectionReferenceData;
import us.dot.its.jpo.ode.api.services.ReportService;

import java.time.*;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ReportTaskTest {

    private ReportService reportService;
    private ProcessedMapRepository processedMapRepo;
    private ReportTask reportTask;

    @BeforeEach
    void setUp() {
        reportService = mock(ReportService.class);
        processedMapRepo = mock(ProcessedMapRepository.class);
        reportTask = new ReportTask(reportService, processedMapRepo);
    }

    @Test
    void testGenerateTimestampMidnightUTC() {
        LocalDate fixedDate = LocalDate.of(2025, 9, 2);
        LocalTime fixedTime = LocalTime.of(0, 0, 0);
        ZoneId zone = ZoneOffset.UTC;

        try (var mockedLocalDate = Mockito.mockStatic(LocalDate.class);
                var mockedLocalTime = Mockito.mockStatic(LocalTime.class)) {

            mockedLocalDate.when(() -> LocalDate.now(zone)).thenReturn(fixedDate);
            mockedLocalTime.when(() -> LocalTime.of(0, 0, 0)).thenReturn(fixedTime);

            ZonedDateTime result = reportTask.generateTimestampMidnightUTC();
            assert (result.getYear() == 2025);
            assert (result.getMonthValue() == 9);
            assert (result.getDayOfMonth() == 2);
            assert (result.getHour() == 0);
            assert (result.getMinute() == 0);
            assert (result.getSecond() == 0);
            assert (result.getZone().equals(ZoneOffset.UTC));
        }
    }

    @Test
    void testGenerateDailyReports() {
        IntersectionReferenceData intersection = new IntersectionReferenceData();
        intersection.setIntersectionID(101);
        when(processedMapRepo.getIntersectionIDs()).thenReturn(Collections.singletonList(intersection));

        ZonedDateTime midnight = ZonedDateTime.of(2025, 9, 2, 0, 0, 0, 0, ZoneOffset.UTC);
        ReportTask spyTask = Mockito.spy(reportTask);
        doReturn(midnight).when(spyTask).generateTimestampMidnightUTC();

        spyTask.generateDailyReports();

        long endMillis = midnight.toInstant().toEpochMilli();
        long startMillis = midnight.minusDays(1).toInstant().toEpochMilli();

        verify(reportService).buildReport(101, startMillis, endMillis);
    }

    @Test
    void testGenerateWeeklyReports() {
        IntersectionReferenceData intersection = new IntersectionReferenceData();
        intersection.setIntersectionID(202);
        when(processedMapRepo.getIntersectionIDs()).thenReturn(Collections.singletonList(intersection));

        ZonedDateTime midnight = ZonedDateTime.of(2025, 9, 2, 0, 0, 0, 0, ZoneOffset.UTC);
        ReportTask spyTask = Mockito.spy(reportTask);
        doReturn(midnight).when(spyTask).generateTimestampMidnightUTC();

        spyTask.generateWeeklyReports();

        long endMillis = midnight.toInstant().toEpochMilli();
        long startMillis = midnight.minusWeeks(1).toInstant().toEpochMilli();

        verify(reportService).buildReport(202, startMillis, endMillis);
    }

    @Test
    void testGenerateMonthlyReports() {
        IntersectionReferenceData intersection = new IntersectionReferenceData();
        intersection.setIntersectionID(303);
        when(processedMapRepo.getIntersectionIDs()).thenReturn(Collections.singletonList(intersection));

        ZonedDateTime midnight = ZonedDateTime.of(2025, 9, 2, 0, 0, 0, 0, ZoneOffset.UTC);
        ReportTask spyTask = Mockito.spy(reportTask);
        doReturn(midnight).when(spyTask).generateTimestampMidnightUTC();

        spyTask.generateMonthlyReports();

        long endMillis = midnight.toInstant().toEpochMilli();
        long startMillis = midnight.minusMonths(1).toInstant().toEpochMilli();

        verify(reportService).buildReport(303, startMillis, endMillis);
    }

    @Test
    void testGenerateReportForTimeRangeWithMultipleIntersections() {
        IntersectionReferenceData intersection1 = new IntersectionReferenceData();
        intersection1.setIntersectionID(1);
        IntersectionReferenceData intersection2 = new IntersectionReferenceData();
        intersection2.setIntersectionID(2);

        when(processedMapRepo.getIntersectionIDs()).thenReturn(List.of(intersection1, intersection2));

        long startMillis = 1000L;
        long endMillis = 2000L;

        reportTask.generateReportForTimeRange(Instant.ofEpochMilli(startMillis), Instant.ofEpochMilli(endMillis));

        verify(reportService).buildReport(1, startMillis, endMillis);
        verify(reportService).buildReport(2, startMillis, endMillis);
    }

    @Test
    void testGenerateReportForTimeRangeWithNoIntersections() {
        when(processedMapRepo.getIntersectionIDs()).thenReturn(Collections.emptyList());
        reportTask.generateReportForTimeRange(Instant.ofEpochMilli(1000L), Instant.ofEpochMilli(2000L));
        verify(reportService, never()).buildReport(anyInt(), anyLong(), anyLong());
    }
}