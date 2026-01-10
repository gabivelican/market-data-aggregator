package unitbv.devops.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Configuration for reading secrets from Docker Secret files
 * Fallback to environment variables for local development
 */
@Configuration
public class SecretsConfiguration {

    /**
     * Read secret from file or environment variable
     * Priority: Docker Secret file > Environment variable
     */
    public static String readSecret(String secretPath, String envVarName, String defaultValue) {
        // First, try to read from Docker Secret file
        try {
            String content = new String(Files.readAllBytes(Paths.get(secretPath))).trim();
            if (!content.isEmpty()) {
                return content;
            }
        } catch (IOException e) {
            // File not found, fall back to environment variable
        }

        // Second, try to read from environment variable
        String envValue = System.getenv(envVarName);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        // Finally, use default value
        return defaultValue;
    }

    /**
     * Read secret from file for _FILE suffixed environment variables
     * Handles paths like /run/secrets/db_password
     */
    public static String readSecretFromFile(String filePath, String defaultValue) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath))).trim();
            if (!content.isEmpty()) {
                return content;
            }
        } catch (IOException e) {
            // File not found, use default
        }
        return defaultValue;
    }
}

