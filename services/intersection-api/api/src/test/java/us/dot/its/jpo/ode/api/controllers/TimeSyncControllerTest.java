package us.dot.its.jpo.ode.api.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeSyncControllerTest {

    private final TimeSyncController controller = new TimeSyncController();

    @Test
    @DisplayName("getCurrentTimeMillis returns a positive epoch millisecond value")
    void getCurrentTimeMillis_returnsPositiveEpochMillis() {
        long value = controller.getCurrentTimeMillis();

        assertTrue(value > 0L, "Expected current time in epoch milliseconds to be greater than zero");
    }

    @Test
    @DisplayName("getCurrentTimeMillis returns a value near the current system time")
    void getCurrentTimeMillis_returnsValueNearCurrentSystemTime() {
        long before = System.currentTimeMillis();
        long value = controller.getCurrentTimeMillis();
        long after = System.currentTimeMillis();

        assertTrue(value >= before, "Returned epoch time should not be earlier than the start of the call");
        assertTrue(value <= after, "Returned epoch time should not be later than the end of the call");
    }

    @Test
    @DisplayName("getCurrentTimeMillis returns a modern timestamp after 2020")
    void getCurrentTimeMillis_returnsModernTimestamp() {
        long value = controller.getCurrentTimeMillis();

        assertTrue(value > 1_577_836_800_000L,
                "Expected a modern Unix timestamp after the year 2020");
    }
}
