package no.kopseng;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RemoteServiceTest {

    RemoteService service;

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
        Thread.sleep(2100);

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
        Thread.sleep(2100);

        // Successful calls should close the circuit
        assertEquals("Success", service.callFlakyApi());
        assertEquals("Success", service.callFlakyApi());

        // Circuit should now be closed and working normally
        assertEquals("Success", service.callFlakyApi());
    }

    private static RemoteService failOnCalls(int... calls) {
        return new RemoteService() {
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

}
