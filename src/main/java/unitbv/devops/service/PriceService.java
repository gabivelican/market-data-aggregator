package unitbv.devops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import unitbv.devops.entity.Price;
import unitbv.devops.entity.Symbol;
import unitbv.devops.repository.PriceRepository;
import unitbv.devops.repository.SymbolRepository;
import unitbv.devops.dto.PriceDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer pentru operații pe prețuri
 */
@Service
public class PriceService {

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private SymbolRepository symbolRepository;

    /**
     * Obține istoricul prețurilor pentru un simbol
     */
    public List<PriceDTO> getPricesBySymbol(String symbolCode) {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode(symbolCode);
        if (symbol.isEmpty()) {
            return List.of();
        }

        return priceRepository.findBySymbolOrderByTimestampDesc(symbol.get())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obține cel mai recent preț pentru un simbol
     */
    public Optional<PriceDTO> getLatestPrice(String symbolCode) {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode(symbolCode);
        if (symbol.isEmpty()) {
            return Optional.empty();
        }

        List<Price> prices = priceRepository.findBySymbolOrderByTimestampDesc(symbol.get());
        if (prices.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(convertToDTO(prices.get(0)));
    }

    /**
     * Obține prețuri în interval de date
     */
    public List<PriceDTO> getPricesByDateRange(String symbolCode, LocalDateTime startDate, LocalDateTime endDate) {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode(symbolCode);
        if (symbol.isEmpty()) {
            return List.of();
        }

        return priceRepository.findBySymbolAndTimestampBetween(symbol.get(), startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Creează un preț nou
     */
    public PriceDTO createPrice(String symbolCode, PriceDTO priceDTO) {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode(symbolCode);
        if (symbol.isEmpty()) {
            throw new IllegalArgumentException("Symbol not found: " + symbolCode);
        }

        Price price = new Price(
                symbol.get(),
                priceDTO.getPrice(),
                priceDTO.getVolume(),
                priceDTO.getTimestamp()
        );
        Price saved = priceRepository.save(price);
        return convertToDTO(saved);
    }

    /**
     * Convertor de Entity la DTO
     */
    private PriceDTO convertToDTO(Price price) {
        return new PriceDTO(
                price.getId(),
                price.getSymbol().getSymbolCode(),
                price.getPrice(),
                price.getVolume(),
                price.getTimestamp()
        );
    }
}

