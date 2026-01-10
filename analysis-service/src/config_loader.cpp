#include "config_loader.hpp"
#include <cstdlib>
#include <iomanip>
#include <fstream>

namespace analysis {

std::string ConfigLoader::getEnv(const char* name, const std::string& defaultValue) {
    const char* value = std::getenv(name);
    return value ? std::string(value) : defaultValue;
}

/**
 * Read secret from file or environment variable
 * Priority: Docker Secret file > Environment variable > Default value
 */
std::string ConfigLoader::getSecret(const char* filePath, const char* envVarName, const std::string& defaultValue) {
    // Try to read from Docker secret file first
    std::ifstream secretFile(filePath);
    if (secretFile.is_open()) {
        std::string content((std::istreambuf_iterator<char>(secretFile)),
                           std::istreambuf_iterator<char>());
        // Trim whitespace
        if (!content.empty()) {
            content.erase(0, content.find_first_not_of(" \n\r\t"));
            content.erase(content.find_last_not_of(" \n\r\t") + 1);
            return content;
        }
    }

    // Fallback to environment variable
    const char* envValue = std::getenv(envVarName);
    if (envValue) {
        return std::string(envValue);
    }

    // Finally use default value
    return defaultValue;
}

int ConfigLoader::getEnvInt(const char* name, int defaultValue) {
    const char* value = std::getenv(name);
    return value ? std::atoi(value) : defaultValue;
}

double ConfigLoader::getEnvDouble(const char* name, double defaultValue) {
    const char* value = std::getenv(name);
    return value ? std::atof(value) : defaultValue;
}

AnalysisConfig ConfigLoader::load() {
    AnalysisConfig config;

    // Database configuration
    config.dbHost = getEnv("DB_HOST", "localhost");
    config.dbPort = getEnvInt("DB_PORT", 5432);
    config.dbName = getEnv("DB_NAME", "market_db");
    config.dbUser = getEnv("DB_USER", "postgres");

    // Read database password from Docker secret file
    config.dbPassword = getSecret(
        "/run/secrets/db_password",
        "DB_PASSWORD",
        "postgres"
    );

    // Server configuration
    config.serverPort = getEnvInt("SERVER_PORT", 8081);

    // Gateway configuration
    config.gatewayUrl = getEnv("GATEWAY_URL", "http://localhost:8080");

    // Read internal API secret from Docker secret file
    config.internalSecret = getSecret(
        "/run/secrets/api_key",
        "INTERNAL_SECRET",
        "supersecret123"
    );

    // Analysis parameters
    config.spikeThresholdPercent = getEnvDouble("SPIKE_THRESHOLD_PERCENT", 5.0);
    config.volumeMultiplier = getEnvInt("VOLUME_MULTIPLIER", 3);
    config.smaWindowSize = getEnvInt("SMA_WINDOW_SIZE", 5);
    config.emaWindowSize = getEnvInt("EMA_WINDOW_SIZE", 15);
    config.volatilityThreshold = getEnvDouble("VOLATILITY_THRESHOLD", 2.0);
    config.aggregationIntervalSeconds = getEnvInt("AGGREGATION_INTERVAL_SECONDS", 60);

    return config;
}

void ConfigLoader::printConfig(const AnalysisConfig& config) {
    std::cout << "Configuration:" << std::endl;
    std::cout << "  Database:" << std::endl;
    std::cout << "    Host: " << config.dbHost << ":" << config.dbPort << std::endl;
    std::cout << "    Name: " << config.dbName << std::endl;
    std::cout << "    User: " << config.dbUser << std::endl;
    std::cout << std::endl;

    std::cout << "  Server:" << std::endl;
    std::cout << "    Port: " << config.serverPort << std::endl;
    std::cout << std::endl;

    std::cout << "  Gateway:" << std::endl;
    std::cout << "    URL: " << config.gatewayUrl << std::endl;
    std::cout << std::endl;

    std::cout << "  Analysis Parameters:" << std::endl;
    std::cout << "    Spike Threshold: " << config.spikeThresholdPercent << "%" << std::endl;
    std::cout << "    Volume Multiplier: " << config.volumeMultiplier << "x" << std::endl;
    std::cout << "    SMA Window: " << config.smaWindowSize << " points" << std::endl;
    std::cout << "    EMA Window: " << config.emaWindowSize << " points" << std::endl;
    std::cout << "    Volatility Threshold: " << config.volatilityThreshold << " std dev" << std::endl;
    std::cout << "    Aggregation Interval: " << config.aggregationIntervalSeconds << "s" << std::endl;
}

} // namespace analysis
