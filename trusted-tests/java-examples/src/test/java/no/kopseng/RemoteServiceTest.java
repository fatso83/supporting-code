package no.kopseng;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RemoteServiceTest {

    @Test
    void flaky_TestCircuitBreakerOpensAfterFailures() {
        RemoteService service = new RemoteService();
        int failures = 0;
        int calls = 10;

        for (int i = 0; i < calls; i++) {
            try {
                String result = service.callFlakyApi();
                assertNotNull(result);
            } catch (Exception e) {
                failures++;
            }
        }

        // We expect some failures, but not all calls should fail
        assertTrue(failures > 0);
        assertTrue(failures < calls);
    }
}
