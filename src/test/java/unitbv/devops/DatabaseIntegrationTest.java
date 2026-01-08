package unitbv.devops;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unitbv.devops.entity.Alert;
import unitbv.devops.entity.Price;
import unitbv.devops.entity.Symbol;
import unitbv.devops.entity.User;
import unitbv.devops.repository.AlertRepository;
import unitbv.devops.repository.PriceRepository;
import unitbv.devops.repository.SymbolRepository;
import unitbv.devops.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseIntegrationTest extends DatabaseTestBase {

	@Autowired
	private SymbolRepository symbolRepository;

	@Autowired
	private PriceRepository priceRepository;

	@Autowired
	private AlertRepository alertRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void testDatabaseSchemaCreation() {
		assertNotNull(symbolRepository, "SymbolRepository should be autowired");
		assertNotNull(priceRepository, "PriceRepository should be autowired");
		assertNotNull(alertRepository, "AlertRepository should be autowired");
		assertNotNull(userRepository, "UserRepository should be autowired");
	}

	@Test
	public void testSeedDataInserted() {
		long symbolCount = symbolRepository.count();
		assertTrue(symbolCount >= 3, "Should have at least 3 symbols in database");

		long userCount = userRepository.count();
		assertTrue(userCount >= 3, "Should have at least 3 users in database");

		long priceCount = priceRepository.count();
		assertTrue(priceCount >= 9, "Should have at least 9 prices (3 per symbol)");

		long alertCount = alertRepository.count();
		assertTrue(alertCount >= 2, "Should have at least 2 alerts");
	}

	@Test
	public void testSymbolTableStructure() {
		Optional<Symbol> symbol = symbolRepository.findBySymbolCode("AAPL");
		assertTrue(symbol.isPresent());

		Symbol aapl = symbol.get();
		assertNotNull(aapl.getId(), "Symbol should have an id");
		assertEquals("AAPL", aapl.getSymbolCode(), "Symbol code should match");
		assertEquals("Apple Inc.", aapl.getName(), "Symbol name should match");
		assertEquals("STOCK", aapl.getType(), "Symbol type should match");
		assertNotNull(aapl.getCreatedAt(), "Created at should be set");
	}

	@Test
	public void testPriceTableStructure() {
		Optional<Symbol> symbol = symbolRepository.findBySymbolCode("BTC");
		assertTrue(symbol.isPresent());

		List<Price> prices = priceRepository.findBySymbolOrderByTimestampDesc(symbol.get());
		assertFalse(prices.isEmpty(), "Should have price data");

		Price price = prices.get(0);
		assertNotNull(price.getId(), "Price should have an id");
		assertNotNull(price.getSymbol(), "Price should reference a symbol");
		assertNotNull(price.getPrice(), "Price should have a price value");
		assertTrue(price.getVolume() > 0, "Volume should be positive");
		assertNotNull(price.getTimestamp(), "Timestamp should be set");
	}

	@Test
	public void testAlertTableStructure() {
		List<Alert> alerts = alertRepository.findAll();
		assertFalse(alerts.isEmpty(), "Should have alert data");

		Alert alert = alerts.get(0);
		assertNotNull(alert.getId(), "Alert should have an id");
		assertNotNull(alert.getSymbol(), "Alert should reference a symbol");
		assertNotNull(alert.getAlertType(), "Alert type should be set");
		assertNotNull(alert.getThreshold(), "Threshold should be set");
		assertNotNull(alert.getTriggeredAt(), "Triggered at should be set");
	}

	@Test
	public void testUserTableStructure() {
		Optional<User> user = userRepository.findByUsername("admin");
		assertTrue(user.isPresent(), "Admin user should exist");

		User admin = user.get();
		assertNotNull(admin.getId(), "User should have an id");
		assertEquals("admin", admin.getUsername(), "Username should match");
		assertNotNull(admin.getPasswordHash(), "Password hash should be set");
		assertNotNull(admin.getCreatedAt(), "Created at should be set");
	}

	@Test
	public void testForeignKeyRelationships() {
		Optional<Symbol> symbol = symbolRepository.findBySymbolCode("GOOGL");
		assertTrue(symbol.isPresent());

		List<Price> prices = priceRepository.findBySymbolOrderByTimestampDesc(symbol.get());
		for (Price price : prices) {
			assertEquals(symbol.get().getId(), price.getSymbol().getId(), "Price should reference correct symbol");
		}
	}
}
