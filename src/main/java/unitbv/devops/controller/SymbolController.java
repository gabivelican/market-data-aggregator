package unitbv.devops.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import unitbv.devops.dto.SymbolDTO;
import unitbv.devops.service.SymbolService;

import java.util.List;

/**
 * REST Controller pentru operații pe simboluri
 */
@RestController
@RequestMapping("/api/symbols")
@Tag(name = "Symbol Management", description = "API pentru gestionarea simbolurilor de tranzacționare")
public class SymbolController {

    @Autowired
    private SymbolService symbolService;

    /**
     * GET /api/symbols - Obține toate simbolurile
     */
    @GetMapping
    @Operation(summary = "Get all symbols", description = "Returnează lista cu toate simbolurile disponibile")
    public ResponseEntity<List<SymbolDTO>> getAllSymbols() {
        List<SymbolDTO> symbols = symbolService.getAllSymbols();
        return ResponseEntity.ok(symbols);
    }

    /**
     * GET /api/symbols/{id} - Obține simbol după ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get symbol by ID", description = "Returnează detaliile unui simbol specific")
    public ResponseEntity<SymbolDTO> getSymbolById(@PathVariable Long id) {
        return symbolService.getSymbolById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/symbols/code/{code} - Obține simbol după cod
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "Get symbol by code", description = "Returnează detaliile unui simbol după codul acestuia")
    public ResponseEntity<SymbolDTO> getSymbolByCode(@PathVariable String code) {
        return symbolService.getSymbolByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/symbols - Creează un simbol nou
     */
    @PostMapping
    @Operation(summary = "Create new symbol", description = "Creează un nou simbol de tranzacționare")
    public ResponseEntity<SymbolDTO> createSymbol(@RequestBody SymbolDTO symbolDTO) {
        SymbolDTO created = symbolService.createSymbol(symbolDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/symbols/{id} - Actualizează un simbol
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update symbol", description = "Actualizează detaliile unui simbol")
    public ResponseEntity<SymbolDTO> updateSymbol(@PathVariable Long id, @RequestBody SymbolDTO symbolDTO) {
        return symbolService.updateSymbol(id, symbolDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/symbols/{id} - Șterge un simbol
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete symbol", description = "Șterge un simbol din sistem")
    public ResponseEntity<Void> deleteSymbol(@PathVariable Long id) {
        symbolService.deleteSymbol(id);
        return ResponseEntity.noContent().build();
    }
}
