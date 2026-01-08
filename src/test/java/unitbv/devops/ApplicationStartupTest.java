package unitbv.devops;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test pentru verificarea că aplicația se pornește și endpoints-urile sunt accesibile
 */
@SpringBootTest(classes = MarketDataAggregatorApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ApplicationStartupTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testApplicationStarts() {
        System.out.println("\n✅ Testing Application Startup:");
        System.out.println("   ✓ Spring Boot application started successfully");
    }

    @Test
    public void testHealthEndpoint() throws Exception {
        System.out.println("\n✅ Testing Health Endpoint:");
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
        System.out.println("   ✓ Health endpoint is accessible");
    }

    @Test
    public void testSymbolsEndpoint() throws Exception {
        System.out.println("\n✅ Testing Symbols Endpoint:");
        mockMvc.perform(get("/api/symbols"))
                .andExpect(status().isOk());
        System.out.println("   ✓ /api/symbols endpoint is accessible");
    }

    @Test
    public void testUsersEndpoint() throws Exception {
        System.out.println("\n✅ Testing Users Endpoint:");
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
        System.out.println("   ✓ /api/users endpoint is accessible");
    }

    @Test
    public void testAlertsEndpoint() throws Exception {
        System.out.println("\n✅ Testing Alerts Endpoint:");
        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk());
        System.out.println("   ✓ /api/alerts endpoint is accessible");
    }

    @Test
    public void testPricesEndpoint() throws Exception {
        System.out.println("\n✅ Testing Prices Endpoint:");
        mockMvc.perform(get("/api/prices/AAPL"))
                .andExpect(status().isOk());
        System.out.println("   ✓ /api/prices/{symbol} endpoint is accessible");
    }

    @Test
    public void testSwaggerEndpoint() throws Exception {
        System.out.println("\n✅ Testing Swagger Endpoint:");
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
        System.out.println("   ✓ Swagger API docs endpoint is accessible");
    }
}
