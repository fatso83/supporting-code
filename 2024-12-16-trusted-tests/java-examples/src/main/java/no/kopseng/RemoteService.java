package no.kopseng;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class RemoteService {
    private final CircuitBreaker circuitBreaker;

    public RemoteService(Clock clock) {
        // Try un-commenting this line and see the archunit test fail!
        // final var now = LocalDateTime.now();

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(2))
                .slidingWindowSize(2)
                .clock(clock)
                .build();

        this.circuitBreaker = CircuitBreaker.of("remoteService", config);
    }

    public String callFlakyApi() {
        try {
            return CircuitBreaker.decorateSupplier(circuitBreaker, this::makeRemoteCallToSlowFlakyService).get();
        } catch (Exception e) {
            throw new RuntimeException("Error calling remote API", e);
        }
    }

    protected String makeRemoteCallToSlowFlakyService() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (Math.random() < 0.7) { // 70% chance of success
            return "Success";
        }
        throw new RuntimeException("Service is unavailable");
    }
}
