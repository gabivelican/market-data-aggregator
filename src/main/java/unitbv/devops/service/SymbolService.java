package unitbv.devops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import unitbv.devops.entity.Symbol;
import unitbv.devops.entity.Price;
import unitbv.devops.repository.SymbolRepository;
import unitbv.devops.repository.PriceRepository;
import unitbv.devops.dto.SymbolDTO;
import unitbv.devops.dto.PriceDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer pentru operații pe simboluri
 */
@Service
public class SymbolService {

    @Autowired
    private SymbolRepository symbolRepository;

    @Autowired
    private PriceRepository priceRepository;

    /**
     * Obține toate simbolurile
     */
    public List<SymbolDTO> getAllSymbols() {
        return symbolRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obține simbol după ID
     */
    public Optional<SymbolDTO> getSymbolById(Long id) {
        return symbolRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Obține simbol după cod
     */
    public Optional<SymbolDTO> getSymbolByCode(String code) {
        return symbolRepository.findBySymbolCode(code)
                .map(this::convertToDTO);
    }

    /**
     * Creează un simbol nou
     */
    public SymbolDTO createSymbol(SymbolDTO symbolDTO) {
        Symbol symbol = new Symbol(
                symbolDTO.getSymbolCode(),
                symbolDTO.getName(),
                symbolDTO.getType()
        );
        Symbol saved = symbolRepository.save(symbol);
        return convertToDTO(saved);
    }

    /**
     * Actualizează un simbol
     */
    public Optional<SymbolDTO> updateSymbol(Long id, SymbolDTO symbolDTO) {
        return symbolRepository.findById(id)
                .map(symbol -> {
                    symbol.setSymbolCode(symbolDTO.getSymbolCode());
                    symbol.setName(symbolDTO.getName());
                    symbol.setType(symbolDTO.getType());
                    Symbol updated = symbolRepository.save(symbol);
                    return convertToDTO(updated);
                });
    }

    /**
     * Șterge un simbol
     */
    public void deleteSymbol(Long id) {
        symbolRepository.deleteById(id);
    }

    /**
     * Obține prețul curent pentru un simbol (ultimul preț din baza de date)
     */
    public Optional<PriceDTO> getCurrentPrice(String symbolCode) {
        Optional<Symbol> symbolOpt = symbolRepository.findBySymbolCode(symbolCode);
        if (symbolOpt.isEmpty()) {
            return Optional.empty();
        }

        Symbol symbol = symbolOpt.get();
        Price latestPrice = priceRepository.findFirstBySymbolOrderByTimestampDesc(symbol);

        if (latestPrice == null) {
            return Optional.empty();
        }

        return Optional.of(new PriceDTO(
                latestPrice.getId(),
                symbolCode,
                latestPrice.getPrice(),
                latestPrice.getVolume(),
                latestPrice.getTimestamp()
        ));
    }

    /**
     * Convertor de Entity la DTO
     */
    private SymbolDTO convertToDTO(Symbol symbol) {
        return new SymbolDTO(
                symbol.getId(),
                symbol.getSymbolCode(),
                symbol.getName(),
                symbol.getType()
        );
    }
}
