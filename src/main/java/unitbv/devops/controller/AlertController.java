package unitbv.devops.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import unitbv.devops.dto.AlertDTO;
import unitbv.devops.service.AlertService;
import unitbv.devops.service.WebSocketService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller pentru operații pe alerte
 */
@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alert Management", description = "API pentru gestionarea alertelor")
@SecurityRequirement(name = "bearerAuth")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @Autowired
    private WebSocketService webSocketService;

    /**
     * GET /api/alerts/active - Obține alertele active (neconfirmate)
     * IMPORTANT: Trebuie să fie PRIMUL, înainte de /{id}
     */
    @GetMapping("/active")
    @Operation(
        summary = "Get active alerts",
        description = "Returnează toate alertele active (neconfirmate) din sistem"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de alerte active returnată cu succes"),
        @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    public ResponseEntity<List<AlertDTO>> getActiveAlerts() {
        List<AlertDTO> activeAlerts = alertService.getActiveAlerts();
        return ResponseEntity.ok(activeAlerts);
    }

    /**
     * GET /api/alerts - Obține toate alertele cu filtre opționale
     */
    @GetMapping
    @Operation(
        summary = "Get all alerts",
        description = "Returnează lista cu toate alertele, cu posibilitate de filtrare după simbol, tip, și interval de date"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de alerte returnată cu succes"),
        @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    public ResponseEntity<List<AlertDTO>> getAllAlerts(
            @Parameter(description = "Codul simbolului (opțional)", example = "AAPL")
            @RequestParam(required = false) String symbolCode,
            
            @Parameter(description = "Tipul alertei (opțional)", example = "SPIKE_UP")
            @RequestParam(required = false) String alertType,
            
            @Parameter(description = "Data de început (format: yyyy-MM-ddTHH:mm:ss)", example = "2026-01-01T00:00:00")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            
            @Parameter(description = "Data de sfârșit (format: yyyy-MM-ddTHH:mm:ss)", example = "2026-01-09T23:59:59")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate
    ) {
        List<AlertDTO> alerts = alertService.getAllAlerts(symbolCode, alertType, startDate, endDate);
        return ResponseEntity.ok(alerts);
    }

    /**
     * GET /api/alerts/{id} - Obține alertă după ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get alert by ID",
        description = "Returnează detaliile unei alerte specifice pe baza ID-ului"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Alerta a fost găsită"),
        @ApiResponse(responseCode = "404", description = "Alerta nu a fost găsită"),
        @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    public ResponseEntity<AlertDTO> getAlertById(
            @Parameter(description = "ID-ul alertei", required = true, example = "1")
            @PathVariable Long id
    ) {
        return alertService.getAlertById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/alerts/acknowledge/{id} - Marchează alertă ca fiind confirmată
     */
    @PostMapping("/acknowledge/{id}")
    @Operation(
        summary = "Acknowledge alert",
        description = "Marchează o alertă ca fiind văzută/confirmată de către utilizator"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Alerta a fost confirmată cu succes"),
        @ApiResponse(responseCode = "404", description = "Alerta nu a fost găsită"),
        @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    public ResponseEntity<AlertDTO> acknowledgeAlert(
            @Parameter(description = "ID-ul alertei de confirmat", required = true, example = "1")
            @PathVariable Long id
    ) {
        return alertService.acknowledgeAlert(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/alerts - Creează o alertă nouă
     */
    @PostMapping
    @Operation(
        summary = "Create new alert",
        description = "Creează o alertă nouă în sistem (de obicei generat automat de sistemul de analiză)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Alerta a fost creată cu succes"),
        @ApiResponse(responseCode = "400", description = "Date invalide sau simbolul nu există"),
        @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    public ResponseEntity<AlertDTO> createAlert(@RequestBody AlertDTO alertDTO) {
        try {
            AlertDTO createdAlert = alertService.createAlert(alertDTO);

            // Broadcast alert via WebSocket în timp real
            webSocketService.broadcastAlert(createdAlert);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdAlert);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/alerts/{id} - Actualizează o alertă existentă
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update alert",
        description = "Actualizează detaliile unei alerte existente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Alerta a fost actualizată"),
        @ApiResponse(responseCode = "404", description = "Alerta nu a fost găsită"),
        @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    public ResponseEntity<AlertDTO> updateAlert(
            @Parameter(description = "ID-ul alertei", required = true)
            @PathVariable Long id,
            @RequestBody AlertDTO alertDTO
    ) {
        return alertService.updateAlert(id, alertDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/alerts/{id} - Șterge o alertă
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete alert",
        description = "Șterge o alertă existentă din sistem"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Alerta a fost ștearsă"),
        @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    public ResponseEntity<Void> deleteAlert(
            @Parameter(description = "ID-ul alertei de șters", required = true)
            @PathVariable Long id
    ) {
        alertService.deleteAlert(id);
        return ResponseEntity.noContent().build();
    }
}

