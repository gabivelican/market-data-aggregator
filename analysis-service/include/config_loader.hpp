#ifndef CONFIG_LOADER_HPP
#define CONFIG_LOADER_HPP

#include "data_structures.hpp"
#include <string>
#include <iostream>

namespace analysis {

/**
 * Configuration loader from environment variables and Docker Secrets
 */
class ConfigLoader {
public:
    // Load configuration from environment variables and secret files
    static AnalysisConfig load();

    // Print current configuration
    static void printConfig(const AnalysisConfig& config);

private:
    // Helper to get environment variable with default value
    static std::string getEnv(const char* name, const std::string& defaultValue);

    // Helper to read secret from Docker Secret file or environment variable
    // Priority: file > environment variable > default value
    static std::string getSecret(const char* filePath, const char* envVarName, const std::string& defaultValue);

    static int getEnvInt(const char* name, int defaultValue);
    static double getEnvDouble(const char* name, double defaultValue);
};

} // namespace analysis

#endif // CONFIG_LOADER_HPP
