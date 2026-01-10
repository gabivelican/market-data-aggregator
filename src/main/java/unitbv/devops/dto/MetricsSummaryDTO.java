package unitbv.devops.dto;

import java.util.Map;

/**
 * DTO for metrics summary from all services
 */
public class MetricsSummaryDTO {
    private int gatewayApiRequests;
    private int gatewayAlertsGenerated;
    private String averageRequestLatencyMs;
    private int websocketConnectionsActive;
    private Map<String, Object> analysisServiceMetrics;
    private String analysisServiceError;

    public MetricsSummaryDTO() {
        this.websocketConnectionsActive = 0;
        this.analysisServiceError = null;
    }

    // Getters and Setters
    public int getGatewayApiRequests() {
        return gatewayApiRequests;
    }

    public void setGatewayApiRequests(int gatewayApiRequests) {
        this.gatewayApiRequests = gatewayApiRequests;
    }

    public int getGatewayAlertsGenerated() {
        return gatewayAlertsGenerated;
    }

    public void setGatewayAlertsGenerated(int gatewayAlertsGenerated) {
        this.gatewayAlertsGenerated = gatewayAlertsGenerated;
    }

    public String getAverageRequestLatencyMs() {
        return averageRequestLatencyMs;
    }

    public void setAverageRequestLatencyMs(String averageRequestLatencyMs) {
        this.averageRequestLatencyMs = averageRequestLatencyMs;
    }

    public int getWebsocketConnectionsActive() {
        return websocketConnectionsActive;
    }

    public void setWebsocketConnectionsActive(int websocketConnectionsActive) {
        this.websocketConnectionsActive = websocketConnectionsActive;
    }

    public Map<String, Object> getAnalysisServiceMetrics() {
        return analysisServiceMetrics;
    }

    public void setAnalysisServiceMetrics(Map<String, Object> analysisServiceMetrics) {
        this.analysisServiceMetrics = analysisServiceMetrics;
    }

    public String getAnalysisServiceError() {
        return analysisServiceError;
    }

    public void setAnalysisServiceError(String analysisServiceError) {
        this.analysisServiceError = analysisServiceError;
    }
}

