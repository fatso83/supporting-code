package no.kopseng;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class RemoteServiceTest {

    @Spy
    RemoteService service;

    @Test
    void circuitBreaker_ShouldOpen_AfterFailures() {
        // Configure service to fail
        doThrow(new RuntimeException("Service is unavailable"))
                .when(service).makeRemoteCallToSlowFlakyService();

        // First two calls fail - this should open the circuit
        assertThrows(RuntimeException.class, () -> service.callFlakyApi());
        assertThrows(RuntimeException.class, () -> service.callFlakyApi());

        // an intentionally unused stubbing strictly for pedagogical reasons
        lenient() // needed to not throw UnnecessaryStubbingException
                // Configure service to succeed, but circuit should be open
                .doReturn("Success").when(service).makeRemoteCallToSlowFlakyService();

        // Next calls should fail fast with CircuitBreakerOpenException
        Exception e = assertThrows(RuntimeException.class,
                () -> service.callFlakyApi());
        assertTrue(e.getMessage().contains("Error calling remote API"));
    }

    @Test
    void circuitBreaker_ShouldAllowSomeTraffic_WhenHalfOpen() throws InterruptedException {
        // Configure service to fail initially
        doThrow(new RuntimeException("Service is unavailable"))
                .when(service).makeRemoteCallToSlowFlakyService();

        // Open the circuit
        assertThrows(RuntimeException.class, () -> service.callFlakyApi());
        assertThrows(RuntimeException.class, () -> service.callFlakyApi());

        // Configure service to succeed
        doReturn("Success").when(service).makeRemoteCallToSlowFlakyService();

        // Wait for the circuit to potentially transition to half-open
        Thread.sleep(2100);

        // This should succeed and help close the circuit
        String result = service.callFlakyApi();
        assertEquals("Success", result);
    }

    @Test
    void circuitBreaker_ShouldClose_AfterSuccess() throws InterruptedException {
        // Configure service to fail initially
        doThrow(new RuntimeException("Service is unavailable"))
                .when(service).makeRemoteCallToSlowFlakyService();

        // Open the circuit
        assertThrows(RuntimeException.class, () -> service.callFlakyApi());
        assertThrows(RuntimeException.class, () -> service.callFlakyApi());

        // Configure service to succeed
        doReturn("Success").when(service).makeRemoteCallToSlowFlakyService();

        // Wait for the circuit to potentially transition to half-open
        Thread.sleep(2100);

        // Successful calls should close the circuit
        assertEquals("Success", service.callFlakyApi());
        assertEquals("Success", service.callFlakyApi());

        // Circuit should now be closed and working normally
        assertEquals("Success", service.callFlakyApi());
    }
}
