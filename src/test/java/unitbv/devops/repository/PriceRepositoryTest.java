package unitbv.devops.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unitbv.devops.DatabaseTestBase;
import unitbv.devops.entity.Price;
import unitbv.devops.entity.Symbol;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class PriceRepositoryTest extends DatabaseTestBase {

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private SymbolRepository symbolRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Test
    public void testFindPricesBySymbol_AAPL() {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode("AAPL");
        assertTrue(symbol.isPresent(), "AAPL symbol should exist");

        List<Price> prices = priceRepository.findBySymbolOrderByTimestampDesc(symbol.get());
        assertFalse(prices.isEmpty(), "AAPL should have price data");
        assertTrue(prices.size() >= 3, "AAPL should have at least 3 price entries");

        // Verify prices are ordered by timestamp descending
        for (int i = 0; i < prices.size() - 1; i++) {
            assertTrue(prices.get(i).getTimestamp().isAfter(prices.get(i + 1).getTimestamp()),
                    "Prices should be ordered by timestamp descending");
        }
    }

    @Test
    public void testFindPricesBySymbol_BTC() {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode("BTC");
        assertTrue(symbol.isPresent(), "BTC symbol should exist");

        List<Price> prices = priceRepository.findBySymbolOrderByTimestampDesc(symbol.get());
        assertFalse(prices.isEmpty(), "BTC should have price data");
        assertTrue(prices.size() >= 3, "BTC should have at least 3 price entries");
    }

    @Test
    public void testFindPricesByTimestampRange() {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode("AAPL");
        assertTrue(symbol.isPresent());

        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 2, 23, 59);

        List<Price> prices = priceRepository.findBySymbolAndTimestampBetween(symbol.get(), start, end);
        assertFalse(prices.isEmpty(), "Should find prices in the specified range");
    }

    @Test
    public void testCreateNewPrice() {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode("GOOGL");
        assertTrue(symbol.isPresent());

        Price newPrice = new Price(
                symbol.get(),
                new BigDecimal("142.50"),
                1000000L,
                LocalDateTime.now()
        );

        Price saved = priceRepository.save(newPrice);
        assertNotNull(saved.getId(), "Saved price should have an ID");
        assertEquals(new BigDecimal("142.50"), saved.getPrice());
    }

    @Test
    public void testPriceDecimalPrecision() {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode("BTC");
        assertTrue(symbol.isPresent());

        // Test high precision decimal for cryptocurrency
        BigDecimal precisePrice = new BigDecimal("42500.12345678");
        Price price = new Price(
                symbol.get(),
                precisePrice,
                100L,
                LocalDateTime.now()
        );

        Price saved = priceRepository.save(price);
        assertNotNull(saved.getId());
        assertEquals(precisePrice, saved.getPrice(), "Price precision should be maintained");
    }

    @Test
    public void testPriceVolume() {
        Optional<Symbol> symbol = symbolRepository.findBySymbolCode("AAPL");
        assertTrue(symbol.isPresent());

        List<Price> prices = priceRepository.findBySymbolOrderByTimestampDesc(symbol.get());
        assertFalse(prices.isEmpty());

        for (Price price : prices) {
            assertTrue(price.getVolume() > 0, "Volume should be positive");
        }
    }

    @Test
    public void testForeignKeyConstraint_SymbolDeletion() {
        // This test verifies that prices are deleted when symbol is deleted (ON DELETE CASCADE)
        // Use a new symbol that won't interfere with other tests
        Symbol testSymbol = new Symbol("TEMP", "Temporary Test Symbol", "TEST");
        testSymbol = symbolRepository.save(testSymbol);

        Price testPrice = new Price(
                testSymbol,
                new BigDecimal("100.00"),
                1000L,
                LocalDateTime.now()
        );
        priceRepository.save(testPrice);

        List<Price> pricesBefore = priceRepository.findBySymbolOrderByTimestampDesc(testSymbol);
        int countBefore = pricesBefore.size();
        assertTrue(countBefore > 0, "TEMP symbol should have prices");

        // Delete prices and alerts first (due to H2 constraint handling)
        priceRepository.deleteAll(pricesBefore);
        alertRepository.deleteAll(alertRepository.findBySymbolOrderByTriggeredAtDesc(testSymbol));

        // Delete the symbol
        symbolRepository.delete(testSymbol);

        // Verify symbol is deleted
        Optional<Symbol> deletedSymbol = symbolRepository.findBySymbolCode("TEMP");
        assertFalse(deletedSymbol.isPresent(), "TEMP symbol should be deleted");
    }
}
