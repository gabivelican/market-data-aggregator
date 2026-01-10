package unitbv.devops.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.search.Search;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import unitbv.devops.dto.MetricsSummaryDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/admin/metrics")
public class MetricsController {

    private final MeterRegistry meterRegistry;

    public MetricsController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/gateway")
    public ResponseEntity<Map<String, Object>> getGatewayMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Total Requests
        double totalRequests = meterRegistry.get("api.requests.total")
                .counters()
                .stream()
                .mapToDouble(c -> c.count())
                .sum();
        metrics.put("totalRequests", totalRequests);

        // Active Websockets
        double activeConnections = meterRegistry.get("websocket.connections.active")
                .gauge()
                .value();
        metrics.put("activeWebSocketConnections", activeConnections);

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/summary")
    public ResponseEntity<MetricsSummaryDTO> getMetricsSummary() {
        MetricsSummaryDTO summary = new MetricsSummaryDTO();

        // 1. Gateway Requests
        // Folosim search pentru a evita exceptia daca metrica nu exista inca
        Search searchRequests = meterRegistry.find("api.requests.total");
        if (searchRequests.counter() != null) {
            summary.setGatewayApiRequests((int) searchRequests.counter().count());
        } else {
            // Daca avem mai multe counters cu tag-uri diferite, le insumam
            double total = searchRequests.counters().stream().mapToDouble(c -> c.count()).sum();
            summary.setGatewayApiRequests((int) total);
        }

        // 2. Alerts
        Search searchAlerts = meterRegistry.find("alerts.generated.total");
        if (searchAlerts.counter() != null) {
            summary.setGatewayAlertsGenerated((int) searchAlerts.counter().count());
        } else {
            double total = searchAlerts.counters().stream().mapToDouble(c -> c.count()).sum();
            summary.setGatewayAlertsGenerated((int) total);
        }

        // 3. Latency (Average)
        Search searchTimer = meterRegistry.find("api.request.duration");
        Timer timer = searchTimer.timer();
        if (timer != null) {
            summary.setAverageRequestLatencyMs(String.format("%.2f", timer.mean(TimeUnit.MILLISECONDS)));
        } else {
            summary.setAverageRequestLatencyMs("0.00");
        }

        // 4. Websockets
        Search searchWs = meterRegistry.find("websocket.connections.active");
        if (searchWs.gauge() != null) {
            summary.setWebsocketConnectionsActive((int) searchWs.gauge().value());
        }

        return ResponseEntity.ok(summary);
    }
}