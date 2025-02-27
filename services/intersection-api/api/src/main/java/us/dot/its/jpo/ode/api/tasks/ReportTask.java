package us.dot.its.jpo.ode.api.tasks;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.api.models.IntersectionReferenceData;
import us.dot.its.jpo.ode.api.services.ReportService;

@Component
@ConditionalOnProperty(name = "enable.report", havingValue = "true", matchIfMissing = false)
public class ReportTask {

    private final ReportService reportService;
    private final ProcessedMapRepository processedMapRepo;

    private static final Logger log = LoggerFactory.getLogger(ReportTask.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private static final String DAILY_NOTIFICATION_CRON = "0 0 0 * * ?"; // every day at midnight
    private static final String WEEKLY_NOTIFICATION_CRON = "0 0 0 * * 0"; // every sunday at midnight
    private static final String MONTHLY_NOTIFICATION_CRON = "0 0 0 1 * ?"; // first day of the month at midnight

    @Autowired
    public ReportTask(ReportService reportService, ProcessedMapRepository processedMapRepo) {
        this.reportService = reportService;
        this.processedMapRepo = processedMapRepo;
    }

    @Scheduled(cron = DAILY_NOTIFICATION_CRON)
    public void generateDailyReports() {
        log.info("Generating Daily Report", dateFormat.format(new Date()));
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalTime time = LocalTime.of(0, 0, 0);
        LocalDateTime dateTime = LocalDateTime.of(today, time);
        long endMillis = dateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
        long startMillis = dateTime.minusDays(1).atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
        generateReportForTimeRange(startMillis, endMillis);
    }

    @Scheduled(cron = WEEKLY_NOTIFICATION_CRON)
    public void generateWeeklyReports() {
        log.info("Generating Weekly Report", dateFormat.format(new Date()));
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalTime time = LocalTime.of(0, 0, 0);
        LocalDateTime dateTime = LocalDateTime.of(today, time);
        long endMillis = dateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
        long startMillis = dateTime.minusWeeks(1).atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
        generateReportForTimeRange(startMillis, endMillis);
    }

    @Scheduled(cron = MONTHLY_NOTIFICATION_CRON)
    public void generateMonthlyReports() {
        log.info("Generating Monthly Report", dateFormat.format(new Date()));
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalTime time = LocalTime.of(0, 0, 0);
        LocalDateTime dateTime = LocalDateTime.of(today, time);
        long endMillis = dateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
        long startMillis = dateTime.minusMonths(1).atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
        generateReportForTimeRange(startMillis, endMillis);
    }

    public void generateReportForTimeRange(long startMillis, long endMillis) {
        for (IntersectionReferenceData data : processedMapRepo.getIntersectionIDs()) {
            log.info("Generating Report for Intersection {} Start Time: {} End Time: {}", data.getIntersectionID(),
                    startMillis, endMillis);

            // build report and save it back to the database.
            reportService.buildReport(data.getIntersectionID(), data.getRoadRegulatorID(), startMillis, endMillis);
        }
    }
}
