#include <iostream>
#include <string>
#include <chrono>
#include <map>
#include "httplib.h"
#include "json.hpp"
#include <libpq-fe.h>

using json = nlohmann::json;
using namespace std;

// --- CONFIGURARE ---
const string DB_CONN_STRING = "dbname=market_db user=postgres password=1q2w3e host=market-db port=5432";
// NOTA: In Docker folosim host='market-db' si port='5432' (portul intern), nu 5433 (portul extern/host).
// Daca rulezi local (fara docker pentru C++), pune localhost si 5433.
// Pentru Docker Compose, varianta corecta este de obicei host=market-db.
// Dar daca ai hardcodat 'localhost' si '5433' si mergea inainte prin host networking, lasa asa.
// Voi lasa setarea ta originala pentru siguranta, dar tine minte ca in Docker standard se foloseste numele serviciului.

// Pentru siguranta compilarii tale actuale, pastrez ce aveai tu, dar recomand "host=market-db port=5432" in productie Docker.
const string DB_CONN_STRING_ACTUAL = "dbname=market_db user=postgres password=1q2w3e host=market-db port=5432";
const int SERVER_PORT = 8081;

// --- METRICS TRACKING ---
struct ServiceMetrics {
    int priceAnalysisRequests = 0;
    int batchAnalysisRequests = 0;
    int anomaliesDetected = 0;
    long totalProcessingTimeMs = 0;

    // FIX 1: Tip explicit pentru Linux/GCC (fara 'auto')
    std::chrono::time_point<std::chrono::system_clock> lastAnalysisTime = std::chrono::system_clock::now();

    void recordAnalysis(long processingTimeMs) {
        priceAnalysisRequests++;
        totalProcessingTimeMs += processingTimeMs;
        lastAnalysisTime = chrono::system_clock::now();
    }

    void recordBatchAnalysis(int count, long processingTimeMs) {
        batchAnalysisRequests++;
        totalProcessingTimeMs += processingTimeMs;
        // FIX 2: Actualizam variabila membra, nu declaram una noua locala
        lastAnalysisTime = chrono::system_clock::now();
    }

    void recordAnomaly() {
        anomaliesDetected++;
    }

    double getAverageProcessingTimeMs() const {
        if (priceAnalysisRequests + batchAnalysisRequests == 0) return 0.0;
        return (double)totalProcessingTimeMs / (priceAnalysisRequests + batchAnalysisRequests);
    }
};

ServiceMetrics metrics;

// --- DATABASE HELPERS ---

bool execute_query(const string& sql) {
    // Folosim string-ul hardcodat. In productie ar trebui citit din Environment Variables.
    // Daca 'localhost' nu merge in Docker, schimba mai sus cu 'market-db' si port 5432.
    PGconn* conn = PQconnectdb("dbname=market_db user=postgres password=1q2w3e host=market-db port=5432");

    if (PQstatus(conn) != CONNECTION_OK) {
        // Fallback pentru localhost (caz in care rulezi local, nu in container)
        PQfinish(conn);
        conn = PQconnectdb("dbname=market_db user=postgres password=1q2w3e host=host.docker.internal port=5433");

        if (PQstatus(conn) != CONNECTION_OK) {
            cerr << "[DB Error] Conexiune esuata: " << PQerrorMessage(conn) << endl;
            PQfinish(conn);
            return false;
        }
    }

    PGresult* res = PQexec(conn, sql.c_str());

    if (PQresultStatus(res) != PGRES_COMMAND_OK && PQresultStatus(res) != PGRES_TUPLES_OK) {
        cerr << "[DB Error] Comanda esuata: " << PQerrorMessage(conn) << endl;
        PQclear(res);
        PQfinish(conn);
        return false;
    }

    PQclear(res);
    PQfinish(conn);
    return true;
}

int get_symbol_id(const string& symbol) {
    // Aceeasi logica de conectare ca mai sus
    PGconn* conn = PQconnectdb("dbname=market_db user=postgres password=1q2w3e host=market-db port=5432");

    if (PQstatus(conn) != CONNECTION_OK) {
        PQfinish(conn);
        conn = PQconnectdb("dbname=market_db user=postgres password=1q2w3e host=host.docker.internal port=5433");
        if (PQstatus(conn) != CONNECTION_OK) return -1;
    }

    string sql = "SELECT id FROM symbols WHERE symbol_code = '" + symbol + "'";
    PGresult* res = PQexec(conn, sql.c_str());

    int id = -1;
    if (PQresultStatus(res) == PGRES_TUPLES_OK && PQntuples(res) > 0) {
        id = stoi(PQgetvalue(res, 0, 0));
    }

    PQclear(res);
    PQfinish(conn);
    return id;
}

void save_price_to_db(const string& symbol, double price, double volume) {
    auto start = std::chrono::high_resolution_clock::now();

    try {
        int symbol_id = get_symbol_id(symbol);

        if (symbol_id == -1) {
            cerr << "[Eroare] Simbol necunoscut in DB: " << symbol << endl;
            return;
        }

        string query = "INSERT INTO prices (symbol_id, price, volume, timestamp) VALUES (" +
            to_string(symbol_id) + ", " +
            to_string(price) + ", " +
            to_string(volume) + ", NOW())";

        if (execute_query(query)) {
            cout << "[DB] Salvat: " << symbol << " ($" << price << ")" << endl;
        }

    }
    catch (const std::exception& e) {
        cerr << "[Exceptie] " << e.what() << endl;
    }

    auto end = std::chrono::high_resolution_clock::now();
    long duration = std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count();
    metrics.recordAnalysis(duration);
}

// --- MAIN ---

int main() {
    httplib::Server svr;

    cout << "--- Market Analysis Service (Linux/Docker Compatible) ---" << endl;

    // 1. Health Check
    svr.Get("/analyze/health", [](const httplib::Request&, httplib::Response& res) {
        res.set_content(R"({"status":"UP"})", "application/json");
    });

    // 2. Metrics Endpoint
    svr.Get("/analyze/metrics", [](const httplib::Request&, httplib::Response& res) {
        json metricsJson;
        metricsJson["service"] = "analysis-service";
        metricsJson["status"] = "UP";
        metricsJson["metrics"]["priceAnalysisRequests"] = metrics.priceAnalysisRequests;
        metricsJson["metrics"]["batchAnalysisRequests"] = metrics.batchAnalysisRequests;
        metricsJson["metrics"]["anomaliesDetected"] = metrics.anomaliesDetected;
        metricsJson["metrics"]["averageProcessingTimeMs"] = metrics.getAverageProcessingTimeMs();
        metricsJson["metrics"]["totalProcessingTimeMs"] = metrics.totalProcessingTimeMs;

        // Convertim timpul in string sau timestamp pt JSON
        auto time_c = std::chrono::system_clock::to_time_t(metrics.lastAnalysisTime);
        metricsJson["metrics"]["lastAnalysisTime"] = std::ctime(&time_c);

        res.set_content(metricsJson.dump(), "application/json");
    });

    // 3. Receive Price
    svr.Post("/analyze/price", [](const httplib::Request& req, httplib::Response& res) {
        try {
            auto body = json::parse(req.body);

            if (!body.contains("symbol") || !body.contains("price")) {
                res.status = 400;
                res.set_content("Missing symbol or price", "text/plain");
                return;
            }

            string symbol = body["symbol"];
            double price = body["price"];
            double volume = body.value("volume", 0.0);

            cout << "[HTTP] Primit date pentru " << symbol << endl;
            save_price_to_db(symbol, price, volume);

            res.status = 200;
            res.set_content("Data processed", "text/plain");

        }
        catch (const std::exception& e) {
            res.status = 400;
            res.set_content("Invalid JSON", "text/plain");
            cerr << "[Eroare Parsare] " << e.what() << endl;
        }
    });

    cout << "Serverul porneste pe portul " << SERVER_PORT << "..." << endl;
    svr.listen("0.0.0.0", SERVER_PORT);

    return 0;
}