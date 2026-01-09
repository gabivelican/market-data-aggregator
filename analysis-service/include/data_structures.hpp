#ifndef DATA_STRUCTURES_HPP
#define DATA_STRUCTURES_HPP

#include <string>
#include <chrono>
#include <vector>

namespace analysis {

/**
 * Represents price data from database
 */
struct PriceData {
    std::string symbol;
    double price;
    long volume;
    std::chrono::system_clock::time_point timestamp;

    PriceData() : price(0.0), volume(0) {}

    PriceData(const std::string& sym, double p, long vol,
             std::chrono::system_clock::time_point ts)
        : symbol(sym), price(p), volume(vol), timestamp(ts) {}
};

/**
 * Represents a single market data point
 */
struct MarketDataPoint {
    std::string symbol;
    double price;
    long volume;
    std::chrono::system_clock::time_point timestamp;

    MarketDataPoint() : price(0.0), volume(0) {}

    MarketDataPoint(const std::string& sym, double p, long vol,
                   std::chrono::system_clock::time_point ts)
        : symbol(sym), price(p), volume(vol), timestamp(ts) {}
};

/**
 * Represents aggregated data with moving averages
 */
struct AggregatedData {
    std::string symbol;
    double currentPrice;
    double sma;           // Simple Moving Average
    double ema;           // Exponential Moving Average
    long volume;
    double minPrice;
    double maxPrice;
    std::chrono::system_clock::time_point timestamp;
    int windowSize;       // Window size in minutes

    AggregatedData()
        : currentPrice(0.0), sma(0.0), ema(0.0), volume(0),
          minPrice(0.0), maxPrice(0.0), windowSize(5) {}
};

/**
 * Alert types enumeration
 */
enum class AlertType {
    SPIKE_UP,
    SPIKE_DOWN,
    HIGH_VOLUME,
    VOLATILITY,
    TREND_REVERSAL
};

/**
 * Converts AlertType enum to string
 */
inline std::string alertTypeToString(AlertType type) {
    switch(type) {
        case AlertType::SPIKE_UP: return "SPIKE_UP";
        case AlertType::SPIKE_DOWN: return "SPIKE_DOWN";
        case AlertType::HIGH_VOLUME: return "HIGH_VOLUME";
        case AlertType::VOLATILITY: return "VOLATILITY";
        case AlertType::TREND_REVERSAL: return "TREND_REVERSAL";
        default: return "UNKNOWN";
    }
}

/**
 * Represents an alert/anomaly detection
 */
struct Alert {
    std::string symbol;
    AlertType alertType;
    double threshold;
    double currentValue;
    double percentageChange;
    std::string details;
    std::chrono::system_clock::time_point triggeredAt;

    Alert() : alertType(AlertType::SPIKE_UP), threshold(0.0),
              currentValue(0.0), percentageChange(0.0) {}

    Alert(const std::string& sym, AlertType type, double thresh,
          double val, double pctChange, const std::string& det)
        : symbol(sym), alertType(type), threshold(thresh),
          currentValue(val), percentageChange(pctChange), details(det),
          triggeredAt(std::chrono::system_clock::now()) {}
};

/**
 * Configuration structure
 */
struct AnalysisConfig {
    // Database settings
    std::string dbHost;
    int dbPort;  // Changed from string to int
    std::string dbName;
    std::string dbUser;
    std::string dbPassword;

    // HTTP server settings
    int serverPort;

    // Gateway settings
    std::string gatewayUrl;
    std::string internalSecret;

    // Analysis parameters
    double spikeThresholdPercent;      // e.g., 5.0 for 5%
    int volumeMultiplier;               // e.g., 3 for 3x average
    int smaWindowSize;                  // e.g., 5 minutes
    int emaWindowSize;                  // e.g., 15 minutes
    double volatilityThreshold;         // e.g., 2.0 for 2 std deviations

    // Timing
    int aggregationIntervalSeconds;     // How often to calculate aggregates

    AnalysisConfig()
        : dbHost("localhost"), dbPort(5432), dbName("market_db"),
          dbUser("postgres"), dbPassword("1q2w3e"),
          serverPort(8081),
          gatewayUrl("http://localhost:8080"),
          internalSecret("supersecret123-change-in-production"),
          spikeThresholdPercent(5.0),
          volumeMultiplier(3),
          smaWindowSize(5),
          emaWindowSize(15),
          volatilityThreshold(2.0),
          aggregationIntervalSeconds(60) {}
};

} // namespace analysis

#endif // DATA_STRUCTURES_HPP

