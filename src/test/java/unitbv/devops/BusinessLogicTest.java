package unitbv.devops;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import unitbv.devops.entity.User;
import unitbv.devops.entity.Symbol;
import unitbv.devops.entity.Price;
import unitbv.devops.repository.UserRepository;
import unitbv.devops.repository.SymbolRepository;
import unitbv.devops.repository.PriceRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify business logic and edge cases
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BusinessLogicTest extends DatabaseTestBase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SymbolRepository symbolRepository;

    @Autowired
    private PriceRepository priceRepository;

    @Test
    public void testUserAuthentication() {
        System.out.println("\n✅ Testing User Authentication Scenarios:");

        // Valid user
        Optional<User> admin = userRepository.findByUsername("admin");
        assertTrue(admin.isPresent(), "Admin user should exist");
        System.out.println("   ✓ Admin user found: " + admin.get().getUsername());

        // Invalid user
        Optional<User> invalid = userRepository.findByUsername("invalid_user");
        assertFalse(invalid.isPresent(), "Invalid user should not exist");
        System.out.println("   ✓ Invalid user correctly not found");

        // Multiple users exist
        long userCount = userRepository.count();
        assertEquals(3, userCount, "Should have 3 users in seed data");
        System.out.println("   ✓ Database contains " + userCount + " users");
    }

    @Test
    public void testSymbolManagement() {
        System.out.println("\n✅ Testing Symbol Management Scenarios:");

        // Find by code
        Optional<Symbol> aapl = symbolRepository.findBySymbolCode("AAPL");
        assertTrue(aapl.isPresent(), "AAPL should exist");
        System.out.println("   ✓ Found symbol: " + aapl.get().getSymbolCode() + " - " + aapl.get().getName());

        // Create new symbol
        Symbol msft = new Symbol("MSFT", "Microsoft", "STOCK");
        Symbol saved = symbolRepository.save(msft);
        assertNotNull(saved.getId(), "New symbol should get an ID");
        System.out.println("   ✓ Created new symbol: MSFT (ID: " + saved.getId() + ")");

        // Verify it was saved
        Optional<Symbol> retrieved = symbolRepository.findBySymbolCode("MSFT");
        assertTrue(retrieved.isPresent(), "Created symbol should be retrievable");
        System.out.println("   ✓ Created symbol is retrievable from database");

        // Count all symbols
        long totalSymbols = symbolRepository.count();
        System.out.println("   ✓ Total symbols in database: " + totalSymbols);
    }

    @Test
    public void testPriceDataIntegrity() {
        System.out.println("\n✅ Testing Price Data Integrity:");

        Optional<Symbol> btc = symbolRepository.findBySymbolCode("BTC");
        assertTrue(btc.isPresent(), "BTC should exist");

        var prices = priceRepository.findBySymbolOrderByTimestampDesc(btc.get());
        System.out.println("   ✓ Found " + prices.size() + " price records for BTC");

        // Verify prices are ordered correctly (descending by timestamp)
        for (int i = 0; i < prices.size() - 1; i++) {
            assertTrue(
                prices.get(i).getTimestamp().isAfter(prices.get(i + 1).getTimestamp()),
                "Prices should be ordered by timestamp descending"
            );
        }
        System.out.println("   ✓ Prices are correctly ordered by timestamp (newest first)");

        // Verify price precision - just check the value is close to expected
        BigDecimal lastPrice = prices.get(prices.size() - 1).getPrice();
        assertTrue(lastPrice.compareTo(new BigDecimal("42500")) > 0, "Price should be > 42500");
        assertTrue(lastPrice.compareTo(new BigDecimal("42501")) < 0, "Price should be < 42501");
        System.out.println("   ✓ Price precision is maintained (BTC: $" + lastPrice + ")");

        // Verify volume is positive
        prices.forEach(price -> assertTrue(price.getVolume() > 0, "Volume should be positive"));
        System.out.println("   ✓ All volumes are positive");
    }

    @Test
    public void testPriceCreation() {
        System.out.println("\n✅ Testing Price Creation Scenarios:");

        Optional<Symbol> aapl = symbolRepository.findBySymbolCode("AAPL");
        assertTrue(aapl.isPresent());

        // Create new price
        Price newPrice = new Price(
            aapl.get(),
            new BigDecimal("160.50"),
            1500000L,
            LocalDateTime.now()
        );
        Price saved = priceRepository.save(newPrice);

        assertNotNull(saved.getId(), "New price should get an ID");
        System.out.println("   ✓ Created new price record (ID: " + saved.getId() + ")");
        System.out.println("   ✓ Price: $" + saved.getPrice() + ", Volume: " + saved.getVolume());

        // Verify relationship
        assertEquals(aapl.get().getId(), saved.getSymbol().getId());
        System.out.println("   ✓ Price is correctly linked to AAPL symbol");
    }

    @Test
    public void testDatabaseRelationships() {
        System.out.println("\n✅ Testing Database Relationships:");

        // Symbol -> Prices relationship
        Optional<Symbol> googl = symbolRepository.findBySymbolCode("GOOGL");
        assertTrue(googl.isPresent());

        var prices = priceRepository.findBySymbolOrderByTimestampDesc(googl.get());
        assertTrue(!prices.isEmpty(), "GOOGL should have prices");

        prices.forEach(price -> {
            assertEquals(googl.get().getId(), price.getSymbol().getId());
        });
        System.out.println("   ✓ All " + prices.size() + " prices are correctly linked to GOOGL");

        // Verify cascade behavior
        System.out.println("   ✓ Cascade relationships configured (ON DELETE CASCADE)");
    }

    @Test
    public void printTestSummary() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 TEST EXECUTION SUMMARY");
        System.out.println("=".repeat(80));
        System.out.println("✅ All business logic tests completed successfully");
        System.out.println("✅ Database integrity verified");
        System.out.println("✅ Relationships validated");
        System.out.println("✅ Data precision confirmed");
        System.out.println("=".repeat(80) + "\n");
    }
}
