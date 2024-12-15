package no.kopseng;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RemoteServiceTest {

    private TickableClock clock;

    private static @NotNull TickableClock createTickableClock() {
        return new TickableClock(LocalDateTime.of(2024, 12, 15, 12, 0, 0));
    }

    private RemoteService service;

    @Test
    void circuitBreaker_ShouldOpen_AfterFailures() {
        service = failOnCalls(1, 2);

        // First two calls fail - this should open the circuit
        assertThrows(RuntimeException.class, () -> service.callFlakyApi());
        assertThrows(RuntimeException.class, () -> service.callFlakyApi());

        // Next calls should fail fast with CircuitBreakerOpenException
        Exception e = assertThrows(RuntimeException.class, () -> service.callFlakyApi());
        assertTrue(e.getMessage().contains("Error calling remote API"));
    }

    @Test
    void circuitBreaker_ShouldAllowSomeTraffic_WhenHalfOpen() {
        service = failOnCalls(1, 2);

        // Open the circuit
        assertThrows(RuntimeException.class, () -> service.callFlakyApi());
        assertThrows(RuntimeException.class, () -> service.callFlakyApi());

        // Wait for the circuit to potentially transition to half-open
        clock.advance(Duration.ofMillis(2100));

        // This should succeed and help close the circuit
        String result = service.callFlakyApi();
        assertEquals("Success", result);
    }

    @Test
    void circuitBreaker_ShouldClose_AfterSuccess() {
        service = failOnCalls(1, 2);

        // Open the circuit
        assertThrows(RuntimeException.class, () -> service.callFlakyApi());
        assertThrows(RuntimeException.class, () -> service.callFlakyApi());

        // Wait for the circuit to potentially transition to half-open
        clock.advance(Duration.ofMillis(2100));

        // Successful calls should close the circuit
        assertEquals("Success", service.callFlakyApi());
        assertEquals("Success", service.callFlakyApi());

        // Circuit should now be closed and working normally
        assertEquals("Success", service.callFlakyApi());
    }

    private RemoteService failOnCalls(int... calls) {
        clock = createTickableClock();
        return new RemoteService(clock) {
            int numberOfCalls = 0;

            @Override
            public String makeRemoteCallToSlowFlakyService() {
                numberOfCalls++;
                if (IntStream.of(calls).anyMatch(i -> i == numberOfCalls)) {
                    throw new RuntimeException("Service unavailable");
                }
                return "Success";
            }
        };
    }

    private static class TickableClock extends Clock {

        private long epochMillis;

        TickableClock(LocalDateTime localDateTime) {
            this.epochMillis = localDateTime.toEpochSecond(ZoneOffset.UTC) * 1000;
        }

        @Override
        public ZoneId getZone() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(epochMillis);
        }

        public void advance(Duration duration) {
            epochMillis += duration.toMillis();
        }
    }
}
