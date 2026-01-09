#ifndef CONFIG_LOADER_HPP
#define CONFIG_LOADER_HPP

#include "data_structures.hpp"
#include <string>
#include <iostream>

namespace analysis {

/**
 * Configuration loader from environment variables
 */
class ConfigLoader {
public:
    // Load configuration from environment variables
    static AnalysisConfig load();

    // Print current configuration
    static void printConfig(const AnalysisConfig& config);

private:
    // Helper to get environment variable with default value
    static std::string getEnv(const char* name, const std::string& defaultValue);
    static int getEnvInt(const char* name, int defaultValue);
    static double getEnvDouble(const char* name, double defaultValue);
};

} // namespace analysis

#endif // CONFIG_LOADER_HPP

