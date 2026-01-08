package unitbv.devops;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unitbv.devops.entity.Price;
import unitbv.devops.entity.Symbol;
import unitbv.devops.repository.PriceRepository;
import unitbv.devops.repository.SymbolRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Interactive test to visualize the application data
 * Run this to see actual data from the database
 */
public class DataVisualizationTest extends DatabaseTestBase {

    @Autowired
    private SymbolRepository symbolRepository;

    @Autowired
    private PriceRepository priceRepository;

    @Test
    public void printAllSymbols() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 ALL SYMBOLS IN DATABASE");
        System.out.println("=".repeat(80));

        Iterable<Symbol> symbols = symbolRepository.findAll();
        symbols.forEach(symbol -> {
            System.out.printf("ID: %d | Code: %-6s | Name: %-20s | Type: %s%n",
                    symbol.getId(),
                    symbol.getSymbolCode(),
                    symbol.getName(),
                    symbol.getType());
        });
    }

    @Test
    public void printAAPLPrices() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📈 APPLE (AAPL) PRICE HISTORY");
        System.out.println("=".repeat(80));

        Optional<Symbol> aapl = symbolRepository.findBySymbolCode("AAPL");
        assertTrue(aapl.isPresent(), "AAPL should exist");

        List<Price> prices = priceRepository.findBySymbolOrderByTimestampDesc(aapl.get());

        System.out.println(String.format("%-20s | %-15s | %-15s", "Timestamp", "Price", "Volume"));
        System.out.println("-".repeat(60));

        prices.forEach(price ->
            System.out.printf("%-20s | $%-14.2f | %,15d%n",
                    price.getTimestamp(),
                    price.getPrice(),
                    price.getVolume())
        );
    }

    @Test
    public void printBTCPrices() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("₿ BITCOIN (BTC) PRICE HISTORY");
        System.out.println("=".repeat(80));

        Optional<Symbol> btc = symbolRepository.findBySymbolCode("BTC");
        assertTrue(btc.isPresent(), "BTC should exist");

        List<Price> prices = priceRepository.findBySymbolOrderByTimestampDesc(btc.get());

        System.out.println(String.format("%-20s | %-15s | %-15s", "Timestamp", "Price", "Volume"));
        System.out.println("-".repeat(60));

        prices.forEach(price ->
            System.out.printf("%-20s | $%-14.2f | %,15d%n",
                    price.getTimestamp(),
                    price.getPrice(),
                    price.getVolume())
        );
    }

    @Test
    public void printGOOGLPrices() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📱 GOOGLE (GOOGL) PRICE HISTORY");
        System.out.println("=".repeat(80));

        Optional<Symbol> googl = symbolRepository.findBySymbolCode("GOOGL");
        assertTrue(googl.isPresent(), "GOOGL should exist");

        List<Price> prices = priceRepository.findBySymbolOrderByTimestampDesc(googl.get());

        System.out.println(String.format("%-20s | %-15s | %-15s", "Timestamp", "Price", "Volume"));
        System.out.println("-".repeat(60));

        prices.forEach(price ->
            System.out.printf("%-20s | $%-14.2f | %,15d%n",
                    price.getTimestamp(),
                    price.getPrice(),
                    price.getVolume())
        );
    }

    @Test
    public void printDatabaseStats() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 DATABASE STATISTICS");
        System.out.println("=".repeat(80));

        long symbolCount = symbolRepository.count();
        long priceCount = priceRepository.count();

        System.out.printf("Total Symbols: %d%n", symbolCount);
        System.out.printf("Total Price Records: %d%n", priceCount);
        System.out.printf("Average Prices per Symbol: %.1f%n", (double) priceCount / symbolCount);
    }
}

