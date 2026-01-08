package unitbv.devops.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import unitbv.devops.dto.AlertDTO;
import unitbv.devops.service.AlertService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller pentru operații pe alerte
 */
@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alert Management", description = "API pentru gestionarea alertelor")
public class AlertController {

    @Autowired
    private AlertService alertService;

    /**
     * GET /api/alerts - Obține toate alertele
     */
    @GetMapping
    @Operation(summary = "Get all alerts", description = "Returnează lista cu toate alertele")
    public ResponseEntity<List<AlertDTO>> getAllAlerts() {
        List<AlertDTO> alerts = alertService.getAllAlerts();
        return ResponseEntity.ok(alerts);
    }

    /**
     * GET /api/alerts/{id} - Obține alertă după ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get alert by ID", description = "Returnează detaliile unei alerte specifice")
    public ResponseEntity<AlertDTO> getAlertById(@PathVariable Long id) {
        return alertService.getAlertById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/alerts/symbol/{symbolCode} - Obține alerte pentru un simbol
     */
    @GetMapping("/symbol/{symbolCode}")
    @Operation(summary = "Get alerts by symbol", description = "Returnează alertele pentru un simbol specific")
    public ResponseEntity<List<AlertDTO>> getAlertsBySymbol(
            @PathVariable String symbolCode,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        if (startDate != null && endDate != null) {
            List<AlertDTO> alerts = alertService.getAlertsByDateRange(symbolCode, startDate, endDate);
            return ResponseEntity.ok(alerts);
        }

        List<AlertDTO> alerts = alertService.getAlertsBySymbol(symbolCode);
        return ResponseEntity.ok(alerts);
    }

    /**
     * POST /api/alerts - Creează o alertă nouă
     */
    @PostMapping
    @Operation(summary = "Create new alert", description = "Creează o alertă nouă în sistem")
    public ResponseEntity<AlertDTO> createAlert(@RequestBody AlertDTO alertDTO) {
        AlertDTO createdAlert = alertService.createAlert(alertDTO);
        return ResponseEntity.status(201).body(createdAlert);
    }

    /**
     * PUT /api/alerts/{id} - Actualizează o alertă existentă
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update alert", description = "Actualizează detaliile unei alerte existente")
    public ResponseEntity<AlertDTO> updateAlert(
            @PathVariable Long id,
            @RequestBody AlertDTO alertDTO) {

        return alertService.updateAlert(id, alertDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/alerts/{id} - Șterge o alertă
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete alert", description = "Șterge o alertă existentă din sistem")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        if (alertService.deleteAlert(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
