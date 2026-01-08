package unitbv.devops;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import unitbv.devops.dto.LoginRequest;
import unitbv.devops.dto.RegisterRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Test pentru sistemul de autentificare cu JWT
 */
@SpringBootTest(classes = MarketDataAggregatorApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_USERNAME = "testuser_" + System.currentTimeMillis();
    private static final String TEST_PASSWORD = "TestPassword123!";

    @Test
    public void testRegisterNewUserSuccessfully() throws Exception {
        System.out.println("\n✅ Testing User Registration:");

        RegisterRequest registerRequest = new RegisterRequest(TEST_USERNAME, TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value(TEST_USERNAME));

        System.out.println("   ✓ User registered successfully");
    }

    @Test
    public void testRegisterDuplicateUsernameFails() throws Exception {
        System.out.println("\n✅ Testing Duplicate Username Registration:");

        // Primeira înregistrare
        RegisterRequest firstRequest = new RegisterRequest(TEST_USERNAME + "_dup", TEST_PASSWORD);
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // A doua înregistrare cu același username
        RegisterRequest duplicateRequest = new RegisterRequest(TEST_USERNAME + "_dup", "DifferentPassword");
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));

        System.out.println("   ✓ Duplicate username correctly rejected");
    }

    @Test
    public void testLoginWithCorrectCredentials() throws Exception {
        System.out.println("\n✅ Testing Login with Correct Credentials:");

        // Înregistrează utilizator
        String testUser = "testuser_login_" + System.currentTimeMillis();
        RegisterRequest registerRequest = new RegisterRequest(testUser, TEST_PASSWORD);
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Încercare login cu credențiale corecte
        LoginRequest loginRequest = new LoginRequest(testUser, TEST_PASSWORD);
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value(testUser))
                .andExpect(jsonPath("$.type").value("Bearer"));

        System.out.println("   ✓ Login successful with JWT token generated");
    }

    @Test
    public void testLoginWithIncorrectPassword() throws Exception {
        System.out.println("\n✅ Testing Login with Incorrect Password:");

        // Înregistrează utilizator
        String testUser = "testuser_wrongpass_" + System.currentTimeMillis();
        RegisterRequest registerRequest = new RegisterRequest(testUser, TEST_PASSWORD);
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Încercare login cu parolă incorectă
        LoginRequest wrongPasswordRequest = new LoginRequest(testUser, "WrongPassword123!");
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
                .andExpect(status().isUnauthorized());

        System.out.println("   ✓ Login correctly rejected with wrong password");
    }

    @Test
    public void testLoginWithNonexistentUser() throws Exception {
        System.out.println("\n✅ Testing Login with Nonexistent User:");

        LoginRequest loginRequest = new LoginRequest("nonexistent_user_" + System.currentTimeMillis(), TEST_PASSWORD);
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        System.out.println("   ✓ Login correctly rejected for nonexistent user");
    }

    @Test
    public void testAccessProtectedEndpointWithoutToken() throws Exception {
        System.out.println("\n✅ Testing Protected Endpoint Without Token:");

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());

        System.out.println("   ✓ Protected endpoint correctly returns 401 without token");
    }

    @Test
    public void testAccessProtectedEndpointWithValidToken() throws Exception {
        System.out.println("\n✅ Testing Protected Endpoint With Valid Token:");

        // Înregistrează și face login
        String testUser = "testuser_token_" + System.currentTimeMillis();
        RegisterRequest registerRequest = new RegisterRequest(testUser, TEST_PASSWORD);
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest(testUser, TEST_PASSWORD);
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();

        // Accesează endpoint protejat cu token
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        System.out.println("   ✓ Protected endpoint accessible with valid JWT token");
    }

    @Test
    public void testAccessProtectedEndpointWithInvalidToken() throws Exception {
        System.out.println("\n✅ Testing Protected Endpoint With Invalid Token:");

        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());

        System.out.println("   ✓ Protected endpoint correctly rejects invalid token");
    }

    @Test
    public void testValidateTokenEndpoint() throws Exception {
        System.out.println("\n✅ Testing Token Validation Endpoint:");

        // Înregistrează și face login
        String testUser = "testuser_validate_" + System.currentTimeMillis();
        RegisterRequest registerRequest = new RegisterRequest(testUser, TEST_PASSWORD);
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest(testUser, TEST_PASSWORD);
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();

        // Validează token
        mockMvc.perform(post("/api/auth/validate")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        System.out.println("   ✓ Token validation endpoint works correctly");
    }
}

