#ifndef DATABASE_MANAGER_HPP
#define DATABASE_MANAGER_HPP

#include "data_structures.hpp"
#include <string>
#include <vector>
#include <memory>

namespace analysis {

/**
 * Database manager for PostgreSQL connections
 * Handles fetching recent prices for analysis
 */
class DatabaseManager {
public:
    explicit DatabaseManager(const AnalysisConfig& config);
    ~DatabaseManager();

    // Connection management
    bool connect();
    void disconnect();
    bool isConnected() const;

    // Data retrieval
    std::vector<PriceData> getRecentPrices(const std::string& symbol, int minutes);
    std::vector<PriceData> getAllRecentPrices(int minutes);
    std::vector<std::string> getActiveSymbols();

private:
    AnalysisConfig config_;
    void* connection_;  // PGconn* (opaque pointer to avoid PostgreSQL header dependency)
    bool connected_;

    // Helper methods
    std::string buildConnectionString() const;
};

} // namespace analysis

#endif // DATABASE_MANAGER_HPP

