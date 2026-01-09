package unitbv.devops.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import unitbv.devops.dto.SymbolDTO;
import unitbv.devops.dto.PriceDTO;
import unitbv.devops.service.SymbolService;

import java.util.List;

/**
 * REST Controller pentru operații pe simboluri
 */
@RestController
@RequestMapping("/api/symbols")
@Tag(name = "Symbol Management", description = "API pentru gestionarea simbolurilor de tranzacționare")
@SecurityRequirement(name = "bearerAuth")
public class SymbolController {

    @Autowired
    private SymbolService symbolService;

    /**
     * GET /api/symbols - Obține toate simbolurile
     */
    @GetMapping
    @Operation(
        summary = "Get all symbols",
        description = "Returnează lista cu toate simbolurile disponibile în sistem (acțiuni, crypto, etc.)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de simboluri returnată cu succes"),
        @ApiResponse(responseCode = "401", description = "Neautorizat - token JWT invalid sau lipsă")
    })
    public ResponseEntity<List<SymbolDTO>> getAllSymbols() {
        List<SymbolDTO> symbols = symbolService.getAllSymbols();
        return ResponseEntity.ok(symbols);
    }

    /**
     * GET /api/symbols/{id} - Obține simbol după ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get symbol by ID",
        description = "Returnează detaliile unui simbol specific pe baza ID-ului"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Simbolul a fost găsit"),
        @ApiResponse(responseCode = "404", description = "Simbolul nu a fost găsit"),
        @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    public ResponseEntity<SymbolDTO> getSymbolById(
        @Parameter(description = "ID-ul simbolului", required = true, example = "1")
        @PathVariable Long id
    ) {
        return symbolService.getSymbolById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/symbols/code/{code} - Obține simbol după cod
     */
    @GetMapping("/code/{code}")
    @Operation(
        summary = "Get symbol by code",
        description = "Returnează detaliile unui simbol după codul acestuia (ex: AAPL, BTC, GOOGL)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Simbolul a fost găsit"),
        @ApiResponse(responseCode = "404", description = "Simbolul nu a fost găsit"),
        @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    public ResponseEntity<SymbolDTO> getSymbolByCode(
        @Parameter(description = "Codul simbolului", required = true, example = "AAPL")
        @PathVariable String code
    ) {
        return symbolService.getSymbolByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/symbols/current-price/{symbol} - Obține prețul curent
     */
    @GetMapping("/current-price/{symbol}")
    @Operation(
        summary = "Get current price for symbol",
        description = "Returnează ultimul preț înregistrat pentru un simbol specificat"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Prețul curent a fost returnat"),
        @ApiResponse(responseCode = "404", description = "Simbolul nu există sau nu are date de preț"),
        @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    public ResponseEntity<PriceDTO> getCurrentPrice(
        @Parameter(description = "Codul simbolului", required = true, example = "AAPL")
        @PathVariable String symbol
    ) {
        return symbolService.getCurrentPrice(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/symbols - Creează un simbol nou
     */
    @PostMapping
    @Operation(
        summary = "Create new symbol",
        description = "Creează un nou simbol de tranzacționare în sistem"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Simbolul a fost creat cu succes"),
        @ApiResponse(responseCode = "400", description = "Date invalide"),
        @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    public ResponseEntity<SymbolDTO> createSymbol(@RequestBody SymbolDTO symbolDTO) {
        SymbolDTO created = symbolService.createSymbol(symbolDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/symbols/{id} - Actualizează un simbol
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update symbol",
        description = "Actualizează detaliile unui simbol existent"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Simbolul a fost actualizat"),
        @ApiResponse(responseCode = "404", description = "Simbolul nu a fost găsit"),
        @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    public ResponseEntity<SymbolDTO> updateSymbol(
        @Parameter(description = "ID-ul simbolului", required = true)
        @PathVariable Long id,
        @RequestBody SymbolDTO symbolDTO
    ) {
        return symbolService.updateSymbol(id, symbolDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/symbols/{id} - Șterge un simbol
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete symbol",
        description = "Șterge un simbol din sistem"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Simbolul a fost șters"),
        @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    public ResponseEntity<Void> deleteSymbol(
        @Parameter(description = "ID-ul simbolului de șters", required = true)
        @PathVariable Long id
    ) {
        symbolService.deleteSymbol(id);
        return ResponseEntity.noContent().build();
    }
}
