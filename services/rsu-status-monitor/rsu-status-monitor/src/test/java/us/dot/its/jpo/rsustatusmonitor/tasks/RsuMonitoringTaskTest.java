package us.dot.its.jpo.rsustatusmonitor.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import us.dot.its.jpo.rsustatusmonitor.models.postgres.derived.RsuSnmpCredentials;
import us.dot.its.jpo.rsustatusmonitor.services.PostgresService;
import us.dot.its.jpo.rsustatusmonitor.services.RsuQueryService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RsuMonitoringTaskTest {

    @Mock
    private PostgresService postgresService;

    @Mock
    private RsuQueryService rsuQueryService;

    private RsuMonitoringTask task;

    @BeforeEach
    public void setup() {
        task = new RsuMonitoringTask(rsuQueryService, postgresService);
    }

    @Test
    public void testQueryRSUStats_NoCredentials() {
        when(postgresService.getRsusWithCredentials()).thenReturn(new ArrayList<>());

        task.queryRSUStats();

        verify(postgresService).getRsusWithCredentials();
        verify(rsuQueryService, never()).getRsuInformation(any());
    }

    @Test
    public void testQueryRSUStats_SingleRsu() {
        List<RsuSnmpCredentials> credentials = new ArrayList<>();
        RsuSnmpCredentials cred1 = new RsuSnmpCredentials(1, "192.168.1.1", "user1", "pass1", "encPass1", "SNMPv3",
                "12345");
        credentials.add(cred1);

        when(postgresService.getRsusWithCredentials()).thenReturn(credentials);

        task.queryRSUStats();

        verify(postgresService).getRsusWithCredentials();
        verify(rsuQueryService, times(1)).getRsuInformation(cred1);
    }

    @Test
    public void testQueryRSUStats_MultipleRsus() {
        List<RsuSnmpCredentials> credentials = new ArrayList<>();
        RsuSnmpCredentials cred1 = new RsuSnmpCredentials(1, "192.168.1.1", "user1", "pass1", "encPass1", "SNMPv3",
                "12345");
        RsuSnmpCredentials cred2 = new RsuSnmpCredentials(2, "192.168.1.2", "user2", "pass2", "encPass2", "SNMPv3",
                "12346");
        RsuSnmpCredentials cred3 = new RsuSnmpCredentials(3, "192.168.1.3", "user3", "pass3", "encPass3", "SNMPv3",
                "12347");
        credentials.add(cred1);
        credentials.add(cred2);
        credentials.add(cred3);

        when(postgresService.getRsusWithCredentials()).thenReturn(credentials);

        task.queryRSUStats();

        verify(postgresService).getRsusWithCredentials();
        verify(rsuQueryService, times(1)).getRsuInformation(cred1);
        verify(rsuQueryService, times(1)).getRsuInformation(cred2);
        verify(rsuQueryService, times(1)).getRsuInformation(cred3);
    }

    @Test
    public void testQueryRSUStats_ParallelProcessing() throws InterruptedException {
        List<RsuSnmpCredentials> credentials = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            credentials.add(new RsuSnmpCredentials(i, "192.168.1." + i, "user" + i, "pass" + i, "encPass" + i, "SNMPv3",
                    String.valueOf(10000 + i)));
        }

        when(postgresService.getRsusWithCredentials()).thenReturn(credentials);

        CountDownLatch latch = new CountDownLatch(10);
        AtomicInteger concurrentCalls = new AtomicInteger(0);
        AtomicInteger maxConcurrent = new AtomicInteger(0);

        doAnswer(invocation -> {
            int current = concurrentCalls.incrementAndGet();
            maxConcurrent.updateAndGet(max -> Math.max(max, current));

            // Simulate some processing time
            Thread.sleep(50);

            concurrentCalls.decrementAndGet();
            latch.countDown();
            return null;
        }).when(rsuQueryService).getRsuInformation(any(RsuSnmpCredentials.class));

        task.queryRSUStats();

        assertTrue(latch.await(10, TimeUnit.SECONDS), "All tasks should complete within timeout");
        verify(rsuQueryService, times(10)).getRsuInformation(any(RsuSnmpCredentials.class));

        // Verify parallel execution (should have more than 1 concurrent call)
        assertTrue(maxConcurrent.get() > 1, "Should have parallel execution with multiple concurrent calls");
    }

    @Test
    public void testQueryRSUStats_ExceptionHandling_SingleRsuFailure() {
        List<RsuSnmpCredentials> credentials = new ArrayList<>();
        RsuSnmpCredentials cred1 = new RsuSnmpCredentials(1, "192.168.1.1", "user1", "pass1", "encPass1", "SNMPv3",
                "12345");
        RsuSnmpCredentials cred2 = new RsuSnmpCredentials(2, "192.168.1.2", "user2", "pass2", "encPass2", "SNMPv3",
                "12346");
        credentials.add(cred1);
        credentials.add(cred2);

        when(postgresService.getRsusWithCredentials()).thenReturn(credentials);

        // First RSU throws exception, second succeeds
        doThrow(new RuntimeException("SNMP connection failed")).when(rsuQueryService).getRsuInformation(cred1);
        doNothing().when(rsuQueryService).getRsuInformation(cred2);

        task.queryRSUStats();

        verify(postgresService).getRsusWithCredentials();
        verify(rsuQueryService, times(1)).getRsuInformation(cred1);
        verify(rsuQueryService, times(1)).getRsuInformation(cred2);
    }

    @Test
    public void testQueryRSUStats_ExceptionHandling_AllRsusFailure() {
        List<RsuSnmpCredentials> credentials = new ArrayList<>();
        RsuSnmpCredentials cred1 = new RsuSnmpCredentials(1, "192.168.1.1", "user1", "pass1", "encPass1", "SNMPv3",
                "12345");
        RsuSnmpCredentials cred2 = new RsuSnmpCredentials(2, "192.168.1.2", "user2", "pass2", "encPass2", "SNMPv3",
                "12346");
        credentials.add(cred1);
        credentials.add(cred2);

        when(postgresService.getRsusWithCredentials()).thenReturn(credentials);
        doThrow(new RuntimeException("Network error")).when(rsuQueryService)
                .getRsuInformation(any(RsuSnmpCredentials.class));

        task.queryRSUStats();

        verify(postgresService).getRsusWithCredentials();
        verify(rsuQueryService, times(2)).getRsuInformation(any(RsuSnmpCredentials.class));
    }

    @Test
    public void testQueryRSUStats_LargeNumberOfRsus() {
        // Test with 50 RSUs to verify thread pool handling
        List<RsuSnmpCredentials> credentials = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            credentials.add(new RsuSnmpCredentials(i, "192.168.1." + i, "user" + i, "pass" + i, "encPass" + i, "SNMPv3",
                    String.valueOf(20000 + i)));
        }

        when(postgresService.getRsusWithCredentials()).thenReturn(credentials);

        task.queryRSUStats();

        verify(postgresService).getRsusWithCredentials();
        verify(rsuQueryService, times(50)).getRsuInformation(any(RsuSnmpCredentials.class));
    }

    @Test
    public void testQueryRSUStats_WaitsForAllTasksToComplete() throws InterruptedException {
        List<RsuSnmpCredentials> credentials = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            credentials.add(new RsuSnmpCredentials(i, "192.168.1." + i, "user" + i, "pass" + i, "encPass" + i, "SNMPv3",
                    String.valueOf(30000 + i)));
        }

        when(postgresService.getRsusWithCredentials()).thenReturn(credentials);

        AtomicInteger completedCount = new AtomicInteger(0);

        doAnswer(invocation -> {
            Thread.sleep(100); // Simulate processing time
            completedCount.incrementAndGet();
            return null;
        }).when(rsuQueryService).getRsuInformation(any(RsuSnmpCredentials.class));

        task.queryRSUStats();

        assertEquals(5, completedCount.get(), "All tasks should be completed before method returns");
    }

    @Test
    public void testQueryRSUStats_MixedSuccessAndFailure() {
        List<RsuSnmpCredentials> credentials = new ArrayList<>();
        RsuSnmpCredentials cred1 = new RsuSnmpCredentials(1, "192.168.1.1", "user1", "pass1", "encPass1", "SNMPv3",
                "12345");
        RsuSnmpCredentials cred2 = new RsuSnmpCredentials(2, "192.168.1.2", "user2", "pass2", "encPass2", "SNMPv3",
                "12346");
        RsuSnmpCredentials cred3 = new RsuSnmpCredentials(3, "192.168.1.3", "user3", "pass3", "encPass3", "SNMPv3",
                "12347");
        RsuSnmpCredentials cred4 = new RsuSnmpCredentials(4, "192.168.1.4", "user4", "pass4", "encPass4", "SNMPv3",
                "12348");
        credentials.add(cred1);
        credentials.add(cred2);
        credentials.add(cred3);
        credentials.add(cred4);

        when(postgresService.getRsusWithCredentials()).thenReturn(credentials);

        // Mix of success and failure
        doNothing().when(rsuQueryService).getRsuInformation(cred1);
        doThrow(new RuntimeException("Connection timeout")).when(rsuQueryService).getRsuInformation(cred2);
        doNothing().when(rsuQueryService).getRsuInformation(cred3);
        doThrow(new RuntimeException("Authentication failed")).when(rsuQueryService).getRsuInformation(cred4);

        task.queryRSUStats();

        verify(postgresService).getRsusWithCredentials();
        verify(rsuQueryService, times(1)).getRsuInformation(cred1);
        verify(rsuQueryService, times(1)).getRsuInformation(cred2);
        verify(rsuQueryService, times(1)).getRsuInformation(cred3);
        verify(rsuQueryService, times(1)).getRsuInformation(cred4);
    }

    @Test
    public void testQueryRSUStats_VerifyCorrectCredentialsPassedToService() {
        List<RsuSnmpCredentials> credentials = new ArrayList<>();
        RsuSnmpCredentials expectedCred = new RsuSnmpCredentials(1, "192.168.100.50", "testuser", "testpass",
                "testencpass", "SNMPv3", "99999");
        credentials.add(expectedCred);

        when(postgresService.getRsusWithCredentials()).thenReturn(credentials);

        task.queryRSUStats();

        verify(rsuQueryService).getRsuInformation(expectedCred);
    }

    @Test
    public void testConstructor() {
        RsuMonitoringTask newTask = new RsuMonitoringTask(rsuQueryService, postgresService);
        assertNotNull(newTask);
    }

    @Test
    public void testQueryRSUStats_ThreadPoolHandles10ConcurrentRequests() throws InterruptedException {
        // Exactly 10 RSUs (matches the thread pool size)
        List<RsuSnmpCredentials> credentials = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            credentials.add(new RsuSnmpCredentials(i, "192.168.1." + i, "user" + i, "pass" + i, "encPass" + i, "SNMPv3",
                    String.valueOf(40000 + i)));
        }

        when(postgresService.getRsusWithCredentials()).thenReturn(credentials);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(10);

        doAnswer(invocation -> {
            startLatch.await(); // Wait for all threads to be ready
            Thread.sleep(50);
            completeLatch.countDown();
            return null;
        }).when(rsuQueryService).getRsuInformation(any(RsuSnmpCredentials.class));

        Thread taskThread = new Thread(() -> task.queryRSUStats());
        taskThread.start();

        Thread.sleep(100);
        startLatch.countDown(); // Release all threads at once

        taskThread.join(5000); // Wait for completion

        assertTrue(completeLatch.await(2, TimeUnit.SECONDS), "All 10 tasks should complete");
        verify(rsuQueryService, times(10)).getRsuInformation(any(RsuSnmpCredentials.class));
    }

    @Test
    public void testQueryRSUStats_DifferentExceptionTypes() {
        List<RsuSnmpCredentials> credentials = new ArrayList<>();
        RsuSnmpCredentials cred1 = new RsuSnmpCredentials(1, "192.168.1.1", "user1", "pass1", "encPass1", "SNMPv3",
                "12345");
        RsuSnmpCredentials cred2 = new RsuSnmpCredentials(2, "192.168.1.2", "user2", "pass2", "encPass2", "SNMPv3",
                "12346");
        RsuSnmpCredentials cred3 = new RsuSnmpCredentials(3, "192.168.1.3", "user3", "pass3", "encPass3", "SNMPv3",
                "12347");
        credentials.add(cred1);
        credentials.add(cred2);
        credentials.add(cred3);

        when(postgresService.getRsusWithCredentials()).thenReturn(credentials);

        // Different types of exceptions
        doThrow(new RuntimeException("Network error")).when(rsuQueryService).getRsuInformation(cred1);
        doThrow(new IllegalArgumentException("Invalid credentials")).when(rsuQueryService).getRsuInformation(cred2);
        doThrow(new NullPointerException("Null response")).when(rsuQueryService).getRsuInformation(cred3);

        task.queryRSUStats();

        // Should handle all exception types gracefully
        verify(postgresService).getRsusWithCredentials();
        verify(rsuQueryService, times(3)).getRsuInformation(any(RsuSnmpCredentials.class));
    }
}
