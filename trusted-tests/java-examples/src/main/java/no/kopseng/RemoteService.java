package no.kopseng;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

import java.time.Duration;

public class RemoteService {
    private final CircuitBreaker circuitBreaker;

    public RemoteService() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(2))
                .slidingWindowSize(2)
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
