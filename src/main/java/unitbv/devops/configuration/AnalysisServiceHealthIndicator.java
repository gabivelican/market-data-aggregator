package unitbv.devops.configuration;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Custom health indicator for C++ Analysis Service connectivity
 * Checks if the C++ service is reachable and healthy
 */
@Component("analysisServiceHealth")
public class AnalysisServiceHealthIndicator implements HealthIndicator {

    @Value("${analysis.service.url:http://localhost:8081}")
    private String analysisServiceUrl;

    private final RestTemplate restTemplate;

    public AnalysisServiceHealthIndicator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Health health() {
        try {
            String healthEndpoint = analysisServiceUrl + "/analyze/health";
            String response = restTemplate.getForObject(healthEndpoint, String.class);

            if (response != null && !response.isEmpty()) {
                return Health.up()
                    .withDetail("service", "Analysis Service")
                    .withDetail("url", analysisServiceUrl)
                    .withDetail("endpoint", "/analyze/health")
                    .withDetail("status", "Connected")
                    .build();
            } else {
                return Health.down()
                    .withDetail("service", "Analysis Service")
                    .withDetail("reason", "Empty response from health endpoint")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("service", "Analysis Service")
                .withDetail("url", analysisServiceUrl)
                .withDetail("error", e.getMessage())
                .withException(e)
                .build();
        }
    }
}

