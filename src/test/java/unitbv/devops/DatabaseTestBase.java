package unitbv.devops;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = MarketDataAggregatorApplication.class)
@Import(TestDataConfiguration.class)
@ActiveProfiles("test")
public class DatabaseTestBase {
}
