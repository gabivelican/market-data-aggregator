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
 * Test pentru verificarea că aplicația se pornește și infrastructura de bază e activă
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
        // Endpoint-ul de health este public
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
        System.out.println("   ✓ Health endpoint is accessible");
    }

    @Test
    public void testSwaggerEndpoint() throws Exception {
        System.out.println("\n✅ Testing Swagger Endpoint:");
        // Docs API sunt publice pentru documentare
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
        System.out.println("   ✓ Swagger API docs endpoint is accessible");
    }

    // NOTĂ: Testele pentru /api/symbols, /api/users, etc. au fost mutate
    // în AuthenticationTest deoarece acum necesită Token JWT (Security Phase 3).
}