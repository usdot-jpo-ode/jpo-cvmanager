package us.dot.its.jpo.ode.api.tasks;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.api.models.IntersectionReferenceData;
import us.dot.its.jpo.ode.api.services.ReportService;

@Component
@ConditionalOnProperty(name = "enable.report", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class ReportTask {

    private final ReportService reportService;
    private final ProcessedMapRepository processedMapRepo;

    private static final Logger log = LoggerFactory.getLogger(ReportTask.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private static final String DAILY_NOTIFICATION_CRON = "0 0 0 * * ?"; // every day at midnight
    private static final String WEEKLY_NOTIFICATION_CRON = "0 0 0 * * 0"; // every sunday at midnight
    private static final String MONTHLY_NOTIFICATION_CRON = "0 0 0 1 * ?"; // first day of the month at midnight

    public ZonedDateTime generateTimestampMidnightUTC() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalTime time = LocalTime.of(0, 0, 0);
        return LocalDateTime.of(today, time).atZone(ZoneOffset.UTC);
    }

    @Scheduled(cron = DAILY_NOTIFICATION_CRON)
    public void generateDailyReports() {
        log.info("Generating Daily Report", dateFormat.format(new Date()));
        ZonedDateTime midnight = generateTimestampMidnightUTC();
        ZonedDateTime midnightYesterday = midnight.minusDays(1);
        generateReportForTimeRange(midnightYesterday.toInstant(), midnight.toInstant());
    }

    @Scheduled(cron = WEEKLY_NOTIFICATION_CRON)
    public void generateWeeklyReports() {
        log.info("Generating Weekly Report", dateFormat.format(new Date()));
        ZonedDateTime midnight = generateTimestampMidnightUTC();
        ZonedDateTime midnightLastWeek = midnight.minusWeeks(1);
        generateReportForTimeRange(midnightLastWeek.toInstant(), midnight.toInstant());
    }

    @Scheduled(cron = MONTHLY_NOTIFICATION_CRON)
    public void generateMonthlyReports() {
        log.info("Generating Monthly Report", dateFormat.format(new Date()));
        ZonedDateTime midnight = generateTimestampMidnightUTC();
        ZonedDateTime midnightLastMonth = midnight.minusMonths(1);
        generateReportForTimeRange(midnightLastMonth.toInstant(), midnight.toInstant());
    }

    public void generateReportForTimeRange(Instant start, Instant end) {
        for (IntersectionReferenceData data : processedMapRepo.getIntersectionIDs()) {
            log.info("Generating Report for Intersection {} Start Time: {} End Time: {}", data.getIntersectionID(),
                    start, end);

            // build report and save it back to the database.
            reportService.buildReport(data.getIntersectionID(), start.toEpochMilli(), end.toEpochMilli());
        }
    }
}
