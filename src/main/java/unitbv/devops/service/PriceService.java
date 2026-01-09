package unitbv.devops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import unitbv.devops.entity.Price;
import unitbv.devops.entity.Symbol;
import unitbv.devops.repository.PriceRepository;
import unitbv.devops.repository.SymbolRepository;
import unitbv.devops.dto.PriceDTO;
import unitbv.devops.dto.PriceHistoryDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
     * Obține istoricul prețurilor pentru un simbol cu filtre și paginare
     */
    public PriceHistoryDTO getPriceHistory(String symbolCode, LocalDateTime startDate, LocalDateTime endDate, Integer limit) {
        Optional<Symbol> symbolOpt = symbolRepository.findBySymbolCode(symbolCode);
        if (symbolOpt.isEmpty()) {
            return new PriceHistoryDTO(symbolCode, List.of(), null);
        }

        Symbol symbol = symbolOpt.get();
        List<Price> prices;

        if (startDate != null && endDate != null) {
            prices = priceRepository.findBySymbolAndTimestampBetween(symbol, startDate, endDate);
        } else {
            prices = priceRepository.findBySymbolOrderByTimestampDesc(symbol);
        }

        // Aplică limit dacă e specificat
        if (limit != null && limit > 0 && prices.size() > limit) {
            prices = prices.subList(0, limit);
        }

        List<PriceDTO> priceDTOs = prices.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PriceHistoryDTO.PriceStatistics stats = calculateStatistics(prices);

        return new PriceHistoryDTO(symbolCode, priceDTOs, stats);
    }

    /**
     * Obține cel mai recent preț pentru un simbol
     */
    public Optional<PriceDTO> getLatestPrice(String symbolCode) {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode(symbolCode);
        if (symbol.isEmpty()) {
            return Optional.empty();
        }

        Price latestPrice = priceRepository.findFirstBySymbolOrderByTimestampDesc(symbol.get());
        if (latestPrice == null) {
            return Optional.empty();
        }

        return Optional.of(convertToDTO(latestPrice));
    }

    /**
     * Obține prețurile recente pentru toate simbolurile (ultima oră)
     */
    public List<PriceDTO> getRecentPrices() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<Symbol> allSymbols = symbolRepository.findAll();

        return allSymbols.stream()
                .flatMap(symbol -> {
                    List<Price> recentPrices = priceRepository.findBySymbolAndTimestampBetween(
                            symbol, oneHourAgo, LocalDateTime.now());
                    return recentPrices.stream();
                })
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
                priceDTO.getTimestamp() != null ? priceDTO.getTimestamp() : LocalDateTime.now()
        );
        Price saved = priceRepository.save(price);
        return convertToDTO(saved);
    }

    /**
     * Calculează statistici pentru o listă de prețuri
     */
    private PriceHistoryDTO.PriceStatistics calculateStatistics(List<Price> prices) {
        if (prices.isEmpty()) {
            return null;
        }

        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal min = prices.get(0).getPrice();
        BigDecimal max = prices.get(0).getPrice();
        Long totalVolume = 0L;

        for (Price price : prices) {
            sum = sum.add(price.getPrice());
            if (price.getPrice().compareTo(min) < 0) {
                min = price.getPrice();
            }
            if (price.getPrice().compareTo(max) > 0) {
                max = price.getPrice();
            }
            totalVolume += price.getVolume();
        }

        BigDecimal average = sum.divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);

        return new PriceHistoryDTO.PriceStatistics(
                average,
                min,
                max,
                totalVolume,
                prices.size()
        );
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

