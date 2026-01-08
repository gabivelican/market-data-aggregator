package unitbv.devops.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import unitbv.devops.dto.PriceDTO;
import unitbv.devops.service.PriceService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller pentru operații pe prețuri
 */
@RestController
@RequestMapping("/api/prices")
@Tag(name = "Price Management", description = "API pentru gestionarea datelor de preț")
public class PriceController {

    @Autowired
    private PriceService priceService;

    /**
     * GET /api/prices/{symbol} - Obține istoricul prețurilor
     */
    @GetMapping("/{symbol}")
    @Operation(summary = "Get price history", description = "Returnează istoricul prețurilor pentru un simbol")
    public ResponseEntity<List<PriceDTO>> getPricesBySymbol(
            @PathVariable String symbol,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        if (startDate != null && endDate != null) {
            List<PriceDTO> prices = priceService.getPricesByDateRange(symbol, startDate, endDate);
            return ResponseEntity.ok(prices);
        }

        List<PriceDTO> prices = priceService.getPricesBySymbol(symbol);
        return ResponseEntity.ok(prices);
    }

    /**
     * GET /api/prices/{symbol}/latest - Obține cel mai recent preț
     */
    @GetMapping("/{symbol}/latest")
    @Operation(summary = "Get latest price", description = "Returnează cel mai recent preț pentru un simbol")
    public ResponseEntity<PriceDTO> getLatestPrice(@PathVariable String symbol) {
        return priceService.getLatestPrice(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/prices/{symbol} - Creează un preț nou
     */
    @PostMapping("/{symbol}")
    @Operation(summary = "Create new price", description = "Adaugă o nouă înregistrare de preț")
    public ResponseEntity<PriceDTO> createPrice(
            @PathVariable String symbol,
            @RequestBody PriceDTO priceDTO) {
        try {
            PriceDTO created = priceService.createPrice(symbol, priceDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
