#include "data_structures.hpp"
#include "config_loader.hpp"
#include "database_manager.hpp"
#include "moving_average.hpp"
#include "anomaly_detector.hpp"
#include "gateway_client.hpp"
#include "http_server.hpp"  // MUST be after database_manager.hpp

#include <iostream>
#include <thread>
#include <chrono>
#include <csignal>
#include <atomic>

using namespace analysis;

std::atomic<bool> running(true);

void signalHandler(int signal) {
    std::cout << "\n[MAIN] Received signal " << signal << ", shutting down..." << std::endl;
    running = false;
}

int main(int argc, char* argv[]) {
    std::cout << "========================================" << std::endl;
    std::cout << "   Market Data Analysis Service v1.0   " << std::endl;
    std::cout << "========================================" << std::endl;
    std::cout << std::endl;

    std::signal(SIGINT, signalHandler);
    std::signal(SIGTERM, signalHandler);

    std::cout << "[MAIN] Loading configuration..." << std::endl;
    AnalysisConfig config = ConfigLoader::load();
    ConfigLoader::printConfig(config);
    std::cout << std::endl;

    std::cout << "[MAIN] Initializing database connection..." << std::endl;
    DatabaseManager dbManager(config);
    if (!dbManager.connect()) {
        std::cerr << "[MAIN] ERROR: Failed to connect to database" << std::endl;
        return 1;
    }
    std::cout << std::endl;

    std::cout << "[MAIN] Initializing HTTP server..." << std::endl;
    HttpServer httpServer(config.serverPort, &dbManager);
    if (!httpServer.start()) {
        std::cerr << "[MAIN] ERROR: Failed to start HTTP server" << std::endl;
        return 1;
    }
    std::cout << std::endl;

    std::cout << "[MAIN] Service is ready!" << std::endl;
    std::cout << "[MAIN] Press Ctrl+C to stop" << std::endl;
    std::cout << std::endl;

    int iteration = 0;
    while (running) {
        try {
            iteration++;
            std::cout << "[MAIN] === Cycle " << iteration << " ===" << std::endl;

            std::vector<std::string> symbols = dbManager.getActiveSymbols();
            std::cout << "[MAIN] Active symbols: " << symbols.size() << std::endl;

            for (const std::string& symbol : symbols) {
                std::cout << "[MAIN] Processing: " << symbol << std::endl;

                std::vector<PriceData> recentPrices =
                    dbManager.getRecentPrices(symbol, config.smaWindowSize);

                if (!recentPrices.empty()) {
                    std::cout << "[MAIN]   Found " << recentPrices.size() << " prices" << std::endl;
                    // Analysis logic would go here in full implementation
                } else {
                    std::cout << "[MAIN]   No data" << std::endl;
                }

                std::cout << std::endl;
            }

            std::cout << "[MAIN] Sleeping " << config.aggregationIntervalSeconds << "s..." << std::endl;
            std::cout << std::endl;

            for (int i = 0; i < config.aggregationIntervalSeconds && running; ++i) {
                std::this_thread::sleep_for(std::chrono::seconds(1));
            }

        } catch (const std::exception& e) {
            std::cerr << "[MAIN] ERROR: " << e.what() << std::endl;
        }
    }

    std::cout << "[MAIN] Shutting down..." << std::endl;
    httpServer.stop();
    dbManager.disconnect();

    std::cout << "[MAIN] Stopped" << std::endl;
    return 0;
}

