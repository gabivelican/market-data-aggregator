package unitbv.devops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import unitbv.devops.configuration.SecretsConfiguration;

@SpringBootApplication
public class MarketDataAggregatorApplication {

    public static void main(String[] args) {
        // Load secrets from Docker secret files and set as environment variables
        // This allows Spring properties to access them before application startup
        loadSecretsBeforeStartup();

        SpringApplication.run(MarketDataAggregatorApplication.class, args);
    }

    /**
     * Load secrets from Docker secret files and set as system properties
     * This is called before Spring initialization to ensure secrets are available
     */
    private static void loadSecretsBeforeStartup() {
        // Load database password from secret file
        String dbPassword = SecretsConfiguration.readSecretFromFile(
            "/run/secrets/db_password",
            System.getenv("DB_PASSWORD")
        );
        if (dbPassword != null && !dbPassword.isEmpty()) {
            System.setProperty("spring.datasource.password", dbPassword);
        }

        // Load JWT secret from secret file
        String jwtSecret = SecretsConfiguration.readSecretFromFile(
            "/run/secrets/jwt_secret",
            System.getenv("JWT_SECRET")
        );
        if (jwtSecret != null && !jwtSecret.isEmpty()) {
            System.setProperty("jwt.secret", jwtSecret);
        }

        // Load internal API secret from secret file
        String internalSecret = SecretsConfiguration.readSecretFromFile(
            "/run/secrets/api_key",
            System.getenv("INTERNAL_SECRET")
        );
        if (internalSecret != null && !internalSecret.isEmpty()) {
            System.setProperty("app.internal.secret", internalSecret);
        }
    }
}
