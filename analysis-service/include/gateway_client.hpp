#ifndef GATEWAY_CLIENT_HPP
#define GATEWAY_CLIENT_HPP

#include "data_structures.hpp"
#include <string>
#include <iostream>
#include <sstream>

#ifdef _WIN32
    #include <winsock2.h>
    #include <ws2tcpip.h>
    #pragma comment(lib, "ws2_32.lib")
#else
    #include <sys/socket.h>
    #include <netinet/in.h>
    #include <arpa/inet.h>
    #include <unistd.h>
    #include <netdb.h>
    #define SOCKET int
    #define INVALID_SOCKET -1
    #define SOCKET_ERROR -1
    #define closesocket close
#endif

namespace analysis {

/**
 * HTTP client for sending results to Gateway
 */
class GatewayClient {
public:
    explicit GatewayClient(const std::string& gatewayUrl, const std::string& secret)
        : gatewayUrl_(gatewayUrl), internalSecret_(secret) {
        parseUrl(gatewayUrl);

        #ifdef _WIN32
        WSADATA wsaData;
        WSAStartup(MAKEWORD(2, 2), &wsaData);
        #endif
    }

    ~GatewayClient() {
        #ifdef _WIN32
        WSACleanup();
        #endif
    }

    /**
     * Send aggregated analysis results to Gateway
     */
    bool sendAnalysisResults(const AggregatedData& data) {
        std::cout << "[Gateway] Sending analysis for " << data.symbol << std::endl;
        std::cout << "  Price: $" << data.currentPrice << ", SMA: $" << data.sma
                  << ", EMA: $" << data.ema << std::endl;

        // Create JSON body
        std::ostringstream json;
        json << "{"
             << "\"symbolCode\":\"" << data.symbol << "\","
             << "\"currentPrice\":" << data.currentPrice << ","
             << "\"sma\":" << data.sma << ","
             << "\"ema\":" << data.ema << ","
             << "\"volume\":" << data.volume << ","
             << "\"windowSize\":" << data.windowSize << ","
             << "\"timestamp\":\"" << getCurrentTimestamp() << "\""
             << "}";

        return sendPost("/internal/analysis-results", json.str());
    }

    /**
     * Send alert to Gateway
     */
    bool sendAlert(const Alert& alert) {
        std::cout << "[Gateway] Sending alert for " << alert.symbol << std::endl;
        std::cout << "  Type: " << alertTypeToString(alert.alertType) << std::endl;

        // Create JSON body
        std::ostringstream json;
        json << "{"
             << "\"symbolCode\":\"" << alert.symbol << "\","
             << "\"alertType\":\"" << alertTypeToString(alert.alertType) << "\","
             << "\"threshold\":" << alert.threshold << ","
             << "\"triggeredAt\":\"" << getCurrentTimestamp() << "\","
             << "\"details\":\"" << alert.details << "\","
             << "\"acknowledged\":false"
             << "}";

        return sendPost("/internal/alerts", json.str());
    }

private:
    std::string gatewayUrl_;
    std::string internalSecret_;
    std::string host_;
    int port_;

    void parseUrl(const std::string& url) {
        // Simple URL parsing: http://host:port
        size_t protocolEnd = url.find("://");
        if (protocolEnd == std::string::npos) {
            host_ = "localhost";
            port_ = 8080;
            return;
        }

        std::string remaining = url.substr(protocolEnd + 3);
        size_t colonPos = remaining.find(':');

        if (colonPos != std::string::npos) {
            host_ = remaining.substr(0, colonPos);
            size_t slashPos = remaining.find('/', colonPos);
            if (slashPos != std::string::npos) {
                port_ = std::stoi(remaining.substr(colonPos + 1, slashPos - colonPos - 1));
            } else {
                port_ = std::stoi(remaining.substr(colonPos + 1));
            }
        } else {
            size_t slashPos = remaining.find('/');
            host_ = (slashPos != std::string::npos) ? remaining.substr(0, slashPos) : remaining;
            port_ = 8080;
        }
    }

    bool sendPost(const std::string& path, const std::string& body) {
        SOCKET sock = socket(AF_INET, SOCK_STREAM, 0);
        if (sock == INVALID_SOCKET) {
            std::cerr << "[Gateway] Failed to create socket" << std::endl;
            return false;
        }

        // Resolve hostname
        struct hostent* he = gethostbyname(host_.c_str());
        if (!he) {
            std::cerr << "[Gateway] Failed to resolve host: " << host_ << std::endl;
            closesocket(sock);
            return false;
        }

        // Connect
        sockaddr_in addr;
        addr.sin_family = AF_INET;
        addr.sin_port = htons(port_);
        addr.sin_addr = *((struct in_addr*)he->h_addr);

        if (connect(sock, (sockaddr*)&addr, sizeof(addr)) == SOCKET_ERROR) {
            std::cerr << "[Gateway] Connection failed to " << host_ << ":" << port_ << std::endl;
            closesocket(sock);
            return false;
        }

        // Build HTTP request
        std::ostringstream request;
        request << "POST " << path << " HTTP/1.1\r\n";
        request << "Host: " << host_ << ":" << port_ << "\r\n";
        request << "Content-Type: application/json\r\n";
        request << "X-Internal-Secret: " << internalSecret_ << "\r\n";
        request << "Content-Length: " << body.length() << "\r\n";
        request << "Connection: close\r\n";
        request << "\r\n";
        request << body;

        std::string requestStr = request.str();

        // Send request
        int sent = send(sock, requestStr.c_str(), requestStr.length(), 0);
        if (sent == SOCKET_ERROR) {
            std::cerr << "[Gateway] Failed to send request" << std::endl;
            closesocket(sock);
            return false;
        }

        // Receive response
        char buffer[4096];
        int received = recv(sock, buffer, sizeof(buffer) - 1, 0);
        if (received > 0) {
            buffer[received] = '\0';
            std::string response(buffer);

            // Check if response contains "200 OK"
            bool success = response.find("200 OK") != std::string::npos;
            if (success) {
                std::cout << "[Gateway] Request successful" << std::endl;
            } else {
                std::cout << "[Gateway] Request failed: " << response.substr(0, 100) << std::endl;
            }

            closesocket(sock);
            return success;
        }

        closesocket(sock);
        return false;
    }

    std::string getCurrentTimestamp() {
        std::time_t now = std::time(nullptr);
        char buf[100];
        std::strftime(buf, sizeof(buf), "%Y-%m-%dT%H:%M:%SZ", std::gmtime(&now));
        return std::string(buf);
    }
};

} // namespace analysis

#endif // GATEWAY_CLIENT_HPP

