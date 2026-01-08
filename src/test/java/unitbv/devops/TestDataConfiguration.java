package unitbv.devops;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
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

@TestConfiguration
public class TestDataConfiguration {

    @Bean
    public TestDataInitializer testDataInitializer(
            SymbolRepository symbolRepository,
            PriceRepository priceRepository,
            AlertRepository alertRepository,
            UserRepository userRepository) {
        return new TestDataInitializer(symbolRepository, priceRepository, alertRepository, userRepository);
    }

    public static class TestDataInitializer {
        private final SymbolRepository symbolRepository;
        private final PriceRepository priceRepository;
        private final AlertRepository alertRepository;
        private final UserRepository userRepository;

        public TestDataInitializer(
                SymbolRepository symbolRepository,
                PriceRepository priceRepository,
                AlertRepository alertRepository,
                UserRepository userRepository) {
            this.symbolRepository = symbolRepository;
            this.priceRepository = priceRepository;
            this.alertRepository = alertRepository;
            this.userRepository = userRepository;
            initializeTestData();
        }

        private void initializeTestData() {
            // Insert symbols
            Symbol aapl = new Symbol("AAPL", "Apple Inc.", "STOCK");
            Symbol btc = new Symbol("BTC", "Bitcoin", "CRYPTOCURRENCY");
            Symbol googl = new Symbol("GOOGL", "Alphabet Inc.", "STOCK");

            symbolRepository.save(aapl);
            symbolRepository.save(btc);
            symbolRepository.save(googl);

            // Insert prices for AAPL
            priceRepository.save(new Price(aapl, new BigDecimal("150.25"), 1000000L, LocalDateTime.of(2026, 1, 1, 10, 0)));
            priceRepository.save(new Price(aapl, new BigDecimal("151.50"), 1200000L, LocalDateTime.of(2026, 1, 2, 10, 0)));
            priceRepository.save(new Price(aapl, new BigDecimal("149.75"), 950000L, LocalDateTime.of(2026, 1, 3, 10, 0)));

            // Insert prices for BTC
            priceRepository.save(new Price(btc, new BigDecimal("42500.50"), 500L, LocalDateTime.of(2026, 1, 1, 10, 0)));
            priceRepository.save(new Price(btc, new BigDecimal("43200.75"), 480L, LocalDateTime.of(2026, 1, 2, 10, 0)));
            priceRepository.save(new Price(btc, new BigDecimal("41800.25"), 520L, LocalDateTime.of(2026, 1, 3, 10, 0)));

            // Insert prices for GOOGL
            priceRepository.save(new Price(googl, new BigDecimal("140.30"), 800000L, LocalDateTime.of(2026, 1, 1, 10, 0)));
            priceRepository.save(new Price(googl, new BigDecimal("141.80"), 850000L, LocalDateTime.of(2026, 1, 2, 10, 0)));
            priceRepository.save(new Price(googl, new BigDecimal("139.50"), 900000L, LocalDateTime.of(2026, 1, 3, 10, 0)));

            // Insert alerts
            alertRepository.save(new Alert(aapl, "PRICE_SPIKE", new BigDecimal("150.00"),
                    LocalDateTime.of(2026, 1, 2, 10, 0), "Price exceeded threshold by 2%"));
            alertRepository.save(new Alert(btc, "VOLUME_ANOMALY", new BigDecimal("500"),
                    LocalDateTime.of(2026, 1, 3, 10, 0), "Unusual volume detected"));

            // Insert users
            userRepository.save(new User("admin", "hashed_password_123"));
            userRepository.save(new User("trader1", "hashed_password_456"));
            userRepository.save(new User("analyst", "hashed_password_789"));
        }
    }
}

