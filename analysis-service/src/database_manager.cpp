#include "database_manager.hpp"
#include <iostream>
#include <sstream>
#include <cstring>

namespace analysis {

DatabaseManager::DatabaseManager(const AnalysisConfig& config)
    : config_(config), connection_(nullptr), connected_(false) {
}

DatabaseManager::~DatabaseManager() {
    disconnect();
}

std::string DatabaseManager::buildConnectionString() const {
    std::ostringstream oss;
    oss << "host=" << config_.dbHost
        << " port=" << config_.dbPort
        << " dbname=" << config_.dbName
        << " user=" << config_.dbUser
        << " password=" << config_.dbPassword
        << " connect_timeout=10";
    return oss.str();
}

bool DatabaseManager::connect() {
    std::cout << "[DB] Connecting to PostgreSQL..." << std::endl;
    std::cout << "[DB] Host: " << config_.dbHost << ":" << config_.dbPort << std::endl;
    std::cout << "[DB] Database: " << config_.dbName << std::endl;

    // Note: In a real implementation, you would use libpq here
    // For now, we'll simulate a connection
    connected_ = true;

    std::cout << "[DB] Connection successful (simulated)" << std::endl;
    return true;
}

void DatabaseManager::disconnect() {
    if (connected_) {
        std::cout << "[DB] Disconnecting..." << std::endl;
        connected_ = false;
    }
}

bool DatabaseManager::isConnected() const {
    return connected_;
}

std::vector<PriceData> DatabaseManager::getRecentPrices(const std::string& symbol, int minutes) {
    std::vector<PriceData> prices;

    if (!connected_) {
        std::cerr << "[DB] ERROR: Not connected to database" << std::endl;
        return prices;
    }

    std::cout << "[DB] Fetching prices for " << symbol << " (last " << minutes << " minutes)" << std::endl;

    // Note: In a real implementation, you would:
    // 1. Prepare SQL query with parameterized statement
    // 2. Execute: SELECT price, volume, timestamp FROM prices WHERE symbol_code = $1 AND timestamp > NOW() - INTERVAL '$2 minutes' ORDER BY timestamp DESC
    // 3. Parse results into PriceData objects

    // For now, return empty vector
    std::cout << "[DB] Found 0 prices (simulated query)" << std::endl;
    return prices;
}

std::vector<PriceData> DatabaseManager::getAllRecentPrices(int minutes) {
    std::vector<PriceData> prices;

    if (!connected_) {
        std::cerr << "[DB] ERROR: Not connected to database" << std::endl;
        return prices;
    }

    std::cout << "[DB] Fetching all prices (last " << minutes << " minutes)" << std::endl;

    // Note: In a real implementation, you would:
    // 1. Prepare SQL query
    // 2. Execute: SELECT s.symbol_code, p.price, p.volume, p.timestamp FROM prices p JOIN symbols s ON p.symbol_fk = s.id WHERE p.timestamp > NOW() - INTERVAL '$1 minutes' ORDER BY p.timestamp DESC
    // 3. Parse results

    std::cout << "[DB] Found 0 prices (simulated query)" << std::endl;
    return prices;
}

std::vector<std::string> DatabaseManager::getActiveSymbols() {
    std::vector<std::string> symbols;

    if (!connected_) {
        std::cerr << "[DB] ERROR: Not connected to database" << std::endl;
        return symbols;
    }

    std::cout << "[DB] Fetching active symbols" << std::endl;

    // Note: In a real implementation, you would:
    // 1. Execute: SELECT DISTINCT symbol_code FROM symbols
    // 2. Parse results

    // For now, return test symbols
    symbols.push_back("AAPL");
    symbols.push_back("BTC");
    symbols.push_back("GOOGL");

    std::cout << "[DB] Found " << symbols.size() << " active symbols" << std::endl;
    return symbols;
}

} // namespace analysis

