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
import unitbv.devops.dto.PriceDTO;
import unitbv.devops.dto.PriceHistoryDTO;
import unitbv.devops.service.PriceService;
import unitbv.devops.service.WebSocketService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller pentru operații pe prețuri
 */
@RestController
@RequestMapping("/api/prices")
@Tag(name = "Price Management", description = "API pentru gestionarea datelor de preț")
@SecurityRequirement(name = "bearerAuth")
public class PriceController {

    @Autowired
    private PriceService priceService;

    @Autowired
    private WebSocketService webSocketService;

    /**
     * GET /api/prices/recent - Obține prețurile recente pentru toate simbolurile (ultima oră)
     * IMPORTANT: Acest endpoint trebuie să fie PRIMUL, înainte de /{symbol},
     * altfel "recent" va fi interpretat ca un symbol!
     */
    @GetMapping("/recent")
    @Operation(
        summary = "Get recent prices for all symbols",
        description = "Returnează toate prețurile înregistrate în ultima oră pentru toate simbolurile active"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de prețuri recente returnată cu succes"),
        @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    public ResponseEntity<List<PriceDTO>> getRecentPrices() {
        List<PriceDTO> recentPrices = priceService.getRecentPrices();
        return ResponseEntity.ok(recentPrices);
    }

    /**
     * GET /api/prices/{symbol} - Obține istoricul prețurilor cu filtre și statistici
     */
    @GetMapping("/{symbol}")
    @Operation(
        summary = "Get price history",
        description = "Returnează istoricul prețurilor pentru un simbol cu filtre opționale (date, limit) și statistici (min, max, average, total volume)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Istoric de prețuri returnat cu succes"),
        @ApiResponse(responseCode = "404", description = "Simbolul nu a fost găsit"),
        @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    public ResponseEntity<PriceHistoryDTO> getPriceHistory(
            @Parameter(description = "Codul simbolului", required = true, example = "AAPL")
            @PathVariable String symbol,

            @Parameter(description = "Data de început (format: yyyy-MM-ddTHH:mm:ss)", example = "2026-01-01T00:00:00")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,

            @Parameter(description = "Data de sfârșit (format: yyyy-MM-ddTHH:mm:ss)", example = "2026-01-09T23:59:59")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate,

            @Parameter(description = "Număr maxim de rezultate", example = "100")
            @RequestParam(required = false)
            Integer limit
    ) {
        PriceHistoryDTO history = priceService.getPriceHistory(symbol, startDate, endDate, limit);

        if (history.getPrices().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(history);
    }

    /**
     * GET /api/prices/{symbol}/latest - Obține cel mai recent preț
     */
    @GetMapping("/{symbol}/latest")
    @Operation(
        summary = "Get latest price",
        description = "Returnează cel mai recent preț înregistrat pentru un simbol specificat"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ultimul preț returnat cu succes"),
        @ApiResponse(responseCode = "404", description = "Simbolul nu există sau nu are date de preț"),
        @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    public ResponseEntity<PriceDTO> getLatestPrice(
            @Parameter(description = "Codul simbolului", required = true, example = "AAPL")
            @PathVariable String symbol
    ) {
        return priceService.getLatestPrice(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/prices/{symbol} - Creează un preț nou
     */
    @PostMapping("/{symbol}")
    @Operation(
        summary = "Create new price",
        description = "Adaugă o nouă înregistrare de preț pentru un simbol specificat"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Prețul a fost creat cu succes"),
        @ApiResponse(responseCode = "400", description = "Date invalide sau simbolul nu există"),
        @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    public ResponseEntity<PriceDTO> createPrice(
            @Parameter(description = "Codul simbolului", required = true, example = "AAPL")
            @PathVariable String symbol,
            @RequestBody PriceDTO priceDTO
    ) {
        try {
            PriceDTO created = priceService.createPrice(symbol, priceDTO);

            // Broadcast price update via WebSocket în timp real
            webSocketService.broadcastPriceUpdate(created);

            // TODO: Send price to C++ Analysis Service for processing
            // analysisServiceClient.sendPriceUpdate(created);

            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
