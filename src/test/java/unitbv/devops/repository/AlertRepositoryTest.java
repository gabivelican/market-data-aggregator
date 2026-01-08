package unitbv.devops.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unitbv.devops.DatabaseTestBase;
import unitbv.devops.entity.Alert;
import unitbv.devops.entity.Symbol;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AlertRepositoryTest extends DatabaseTestBase {

	@Autowired
	private AlertRepository alertRepository;

	@Autowired
	private SymbolRepository symbolRepository;

	@Test
	public void testFindAlertsBySymbol_AAPL() {
		Optional<Symbol> symbol = symbolRepository.findBySymbolCode("AAPL");
		assertTrue(symbol.isPresent(), "AAPL symbol should exist");

		List<Alert> alerts = alertRepository.findBySymbolOrderByTriggeredAtDesc(symbol.get());
		assertFalse(alerts.isEmpty(), "AAPL should have alert data");
	}

	@Test
	public void testFindAlertsBySymbol_BTC() {
		Optional<Symbol> symbol = symbolRepository.findBySymbolCode("BTC");
		assertTrue(symbol.isPresent(), "BTC symbol should exist");

		List<Alert> alerts = alertRepository.findBySymbolOrderByTriggeredAtDesc(symbol.get());
		assertFalse(alerts.isEmpty(), "BTC should have alert data");
	}

	@Test
	public void testAlertDetails() {
		Optional<Symbol> symbol = symbolRepository.findBySymbolCode("AAPL");
		assertTrue(symbol.isPresent());

		List<Alert> alerts = alertRepository.findBySymbolOrderByTriggeredAtDesc(symbol.get());
		assertFalse(alerts.isEmpty());

		Alert alert = alerts.get(0);
		assertNotNull(alert.getAlertType(), "Alert type should not be null");
		assertNotNull(alert.getThreshold(), "Threshold should not be null");
		assertNotNull(alert.getTriggeredAt(), "Triggered at should not be null");
		assertNotNull(alert.getDetails(), "Details should not be null");
	}

	@Test
	public void testCreateNewAlert() {
		Optional<Symbol> symbol = symbolRepository.findBySymbolCode("GOOGL");
		assertTrue(symbol.isPresent());

		Alert newAlert = new Alert(
				symbol.get(),
				"PRICE_DROP",
				new BigDecimal("140.00"),
				LocalDateTime.now(),
				"Price dropped below threshold"
		);

		Alert saved = alertRepository.save(newAlert);
		assertNotNull(saved.getId(), "Saved alert should have an ID");
		assertEquals("PRICE_DROP", saved.getAlertType());
	}

	@Test
	public void testFindAlertsByTimestampRange() {
		Optional<Symbol> symbol = symbolRepository.findBySymbolCode("AAPL");
		assertTrue(symbol.isPresent());

		LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
		LocalDateTime end = LocalDateTime.of(2026, 1, 3, 23, 59);

		List<Alert> alerts = alertRepository.findBySymbolAndTriggeredAtBetween(symbol.get(), start, end);
		assertFalse(alerts.isEmpty(), "Should find alerts in the specified range");
	}

	@Test
	public void testAlertOrderingByTriggeredAt() {
		Optional<Symbol> symbol = symbolRepository.findBySymbolCode("BTC");
		assertTrue(symbol.isPresent());

		List<Alert> alerts = alertRepository.findBySymbolOrderByTriggeredAtDesc(symbol.get());
		assertFalse(alerts.isEmpty());

		// Verify alerts are ordered by triggered_at descending
		for (int i = 0; i < alerts.size() - 1; i++) {
			assertTrue(alerts.get(i).getTriggeredAt().isAfter(alerts.get(i + 1).getTriggeredAt()),
					"Alerts should be ordered by triggered_at descending");
		}
	}

	@Test
	public void testForeignKeyConstraint_AlertSymbolRelation() {
		Optional<Symbol> symbol = symbolRepository.findBySymbolCode("GOOGL");
		assertTrue(symbol.isPresent());

		Alert alert = new Alert(
				symbol.get(),
				"ANOMALY_DETECTED",
				new BigDecimal("145.00"),
				LocalDateTime.now(),
				"Anomaly detected in trading pattern"
		);

		Alert saved = alertRepository.save(alert);
		assertNotNull(saved.getId());

		// Verify the relationship
		Alert retrieved = alertRepository.findById(saved.getId()).orElse(null);
		assertNotNull(retrieved);
		assertEquals(symbol.get().getId(), retrieved.getSymbol().getId(), "Alert should be linked to correct symbol");
	}
}
