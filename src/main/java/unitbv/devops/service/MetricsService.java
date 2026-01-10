package unitbv.devops.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final AtomicInteger activeWebSocketConnections;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.activeWebSocketConnections = new AtomicInteger(0);

        // Initializam Gauge-ul pentru conexiuni active
        // Gauge-ul urmareste valoarea din AtomicInteger
        meterRegistry.gauge("websocket.connections.active", activeWebSocketConnections);
    }

    public void recordApiRequest(String endpoint, String status) {
        // Corect: Construim counter-ul cu tag-uri la momentul folosirii
        Counter.builder("api.requests.total")
                .tag("endpoint", endpoint)
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    public void recordApiRequestDuration(String endpoint, long time, TimeUnit unit) {
        // Corect: Timer cu tag-uri
        Timer.builder("api.request.duration")
                .tag("endpoint", endpoint)
                .register(meterRegistry)
                .record(time, unit);
    }

    public void recordAlertGenerated(String type) {
        Counter.builder("alerts.generated.total")
                .tag("type", type)
                .register(meterRegistry)
                .increment();
    }

    public void incrementWebSocketConnections() {
        activeWebSocketConnections.incrementAndGet();
    }

    public void decrementWebSocketConnections() {
        activeWebSocketConnections.decrementAndGet();
    }
}