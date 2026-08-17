package us.dot.its.jpo.rsustatusmonitor.tasks;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import us.dot.its.jpo.rsustatusmonitor.models.postgres.derived.RsuSnmpCredentials;
import us.dot.its.jpo.rsustatusmonitor.services.PostgresService;
import us.dot.its.jpo.rsustatusmonitor.services.RsuQueryService;

@Component
@ConditionalOnProperty(name = "enable.monitoring", havingValue = "true", matchIfMissing = false)
@Slf4j
public class RsuMonitoringTask {

    private PostgresService postgresService;
    private RsuQueryService rsuQueryService;
    private final Executor taskExecutor;

    @Autowired
    public RsuMonitoringTask(
            RsuQueryService rsuQueryService,
            PostgresService postgresService) {
        this.rsuQueryService = rsuQueryService;
        this.postgresService = postgresService;
        // Create a fixed thread pool with 10 threads for RSU monitoring
        this.taskExecutor = Executors.newFixedThreadPool(10);
    }

    @Scheduled(fixedRateString = "${monitor.interval}")
    public void queryRSUStats() {
        List<RsuSnmpCredentials> credentials = postgresService.getRsusWithCredentials();

        // Process all RSU credentials in parallel using CompletableFuture
        List<CompletableFuture<Void>> futures = credentials.stream()
                .map(cred -> CompletableFuture.runAsync(() -> {
                    try {
                        log.debug("Processing RSU: {}", cred.getIpv4_address());
                        rsuQueryService.getRsuInformation(cred);
                    } catch (Exception e) {
                        log.error("Error processing status for RSU {}: {}", cred.getIpv4_address(), e.getMessage(), e);
                    }
                }, taskExecutor))
                .toList();

        // Wait for all tasks to complete before the method returns
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(throwable -> {
                    log.error("Error in RSU monitoring task execution: {}", throwable.getMessage(), throwable);
                    return null;
                })
                .join();

        log.debug("Completed RSU Monitoring Task for {} RSUs", credentials.size());
    }
}