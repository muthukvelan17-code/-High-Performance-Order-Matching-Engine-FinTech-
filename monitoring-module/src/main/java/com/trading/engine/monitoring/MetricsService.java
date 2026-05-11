package com.trading.engine.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final MeterRegistry registry;

    public void recordMatchingLatency(long nanos) {
        Timer.builder("trading.engine.matching.latency")
                .description("Latency of order matching")
                .register(registry)
                .record(nanos, TimeUnit.NANOSECONDS);
    }

    public void incrementOrderCount() {
        registry.counter("trading.engine.orders.count").increment();
    }

    public void incrementTradeCount() {
        registry.counter("trading.engine.trades.count").increment();
    }
}
