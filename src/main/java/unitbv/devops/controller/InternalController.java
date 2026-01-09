package unitbv.devops.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Hidden;
import unitbv.devops.dto.AnalysisResultDTO;
import unitbv.devops.dto.AlertDTO;
import unitbv.devops.service.AlertService;
import unitbv.devops.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller INTERN pentru primirea rezultatelor de la serviciul C++ de analiză
 * Acestea NU sunt documentate în Swagger public și sunt securizate cu shared secret
 */
@RestController
@RequestMapping("/internal")
@Hidden // Ascunde din documentația Swagger publică
public class InternalController {

    private static final Logger logger = LoggerFactory.getLogger(InternalController.class);

    @Value("${app.internal.secret:default-secret-change-in-production}")
    private String internalSecret;

    @Autowired
    private AlertService alertService;

    @Autowired
    private WebSocketService webSocketService;

    /**
     * POST /internal/analysis-results
     * Primește rezultate de analiză (moving averages, etc.) de la serviciul C++
     */
    @PostMapping("/analysis-results")
    public ResponseEntity<Map<String, String>> receiveAnalysisResults(
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret,
            @RequestBody AnalysisResultDTO analysisResult
    ) {
        // Verificare secret pentru securitate
        if (secret == null || !secret.equals(internalSecret)) {
            logger.warn("Unauthorized access attempt to /internal/analysis-results");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.info("Received analysis result for symbol: {} - SMA: {}, EMA: {}, Window: {}",
                analysisResult.getSymbolCode(),
                analysisResult.getSma(),
                analysisResult.getEma(),
                analysisResult.getWindowSize());

        // Aici poți procesa rezultatele: salvare în cache, database, etc.
        // Pentru moment doar logăm

        Map<String, String> response = new HashMap<>();
        response.put("status", "received");
        response.put("symbol", analysisResult.getSymbolCode());
        response.put("timestamp", analysisResult.getTimestamp().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /internal/alerts
     * Primește alerte detectate de serviciul C++ (anomalii, spike-uri, etc.)
     */
    @PostMapping("/alerts")
    public ResponseEntity<Map<String, Object>> receiveAlert(
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret,
            @RequestBody AlertDTO alertDTO
    ) {
        // Verificare secret pentru securitate
        if (secret == null || !secret.equals(internalSecret)) {
            logger.warn("Unauthorized access attempt to /internal/alerts");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.info("Received alert from C++ service - Symbol: {}, Type: {}, Threshold: {}",
                alertDTO.getSymbolCode(),
                alertDTO.getAlertType(),
                alertDTO.getThreshold());

        try {
            // Salvează alerta în baza de date
            AlertDTO createdAlert = alertService.createAlert(alertDTO);

            // Broadcast alert via WebSocket în timp real către toți clienții conectați
            webSocketService.broadcastAlert(createdAlert);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "created");
            response.put("alertId", createdAlert.getId());
            response.put("symbol", createdAlert.getSymbolCode());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating alert from C++ service: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * GET /internal/health
     * Health check pentru serviciul C++ să verifice dacă Gateway-ul e up
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Market Data Aggregator Gateway");
        return ResponseEntity.ok(response);
    }
}

