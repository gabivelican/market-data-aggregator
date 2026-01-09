#ifndef HTTP_SERVER_HPP
#define HTTP_SERVER_HPP

#include "data_structures.hpp"
#include "database_manager.hpp"
#include <string>
#include <iostream>
#include <thread>
#include <atomic>
#include <sstream>
#include <ctime>

#ifdef _WIN32
    #include <winsock2.h>
    #include <ws2tcpip.h>
    #pragma comment(lib, "ws2_32.lib")
    typedef int socklen_t;
#else
    #include <sys/socket.h>
    #include <netinet/in.h>
    #include <unistd.h>
    #include <arpa/inet.h>
    #define SOCKET int
    #define INVALID_SOCKET -1
    #define SOCKET_ERROR -1
    #define closesocket close
#endif

namespace analysis {

/**
 * Simple HTTP server for receiving price data
 */
class HttpServer {
public:
    explicit HttpServer(int port, DatabaseManager* dbManager = nullptr)
        : port_(port), running_(false), serverSocket_(INVALID_SOCKET), dbManager_(dbManager) {
        #ifdef _WIN32
        WSADATA wsaData;
        WSAStartup(MAKEWORD(2, 2), &wsaData);
        #endif
    }

    ~HttpServer() {
        stop();
        #ifdef _WIN32
        WSACleanup();
        #endif
    }

    bool start() {
        std::cout << "[HTTP] Starting server on port " << port_ << std::endl;

        serverSocket_ = socket(AF_INET, SOCK_STREAM, 0);
        if (serverSocket_ == INVALID_SOCKET) {
            std::cerr << "[HTTP] Failed to create socket" << std::endl;
            return false;
        }

        int opt = 1;
        setsockopt(serverSocket_, SOL_SOCKET, SO_REUSEADDR, (char*)&opt, sizeof(opt));

        sockaddr_in address;
        address.sin_family = AF_INET;
        address.sin_addr.s_addr = INADDR_ANY;
        address.sin_port = htons(port_);

        if (bind(serverSocket_, (sockaddr*)&address, sizeof(address)) == SOCKET_ERROR) {
            std::cerr << "[HTTP] Failed to bind" << std::endl;
            closesocket(serverSocket_);
            return false;
        }

        if (listen(serverSocket_, 5) == SOCKET_ERROR) {
            std::cerr << "[HTTP] Failed to listen" << std::endl;
            closesocket(serverSocket_);
            return false;
        }

        running_ = true;
        std::cout << "[HTTP] Server started on port " << port_ << std::endl;
        std::cout << "[HTTP] Endpoints:" << std::endl;
        std::cout << "[HTTP]   POST /analyze/price" << std::endl;
        std::cout << "[HTTP]   POST /analyze/batch" << std::endl;
        std::cout << "[HTTP]   GET  /analyze/health" << std::endl;

        listenerThread_ = std::thread(&HttpServer::listenLoop, this);
        return true;
    }

    void stop() {
        if (running_) {
            std::cout << "[HTTP] Stopping..." << std::endl;
            running_ = false;

            if (serverSocket_ != INVALID_SOCKET) {
                closesocket(serverSocket_);
                serverSocket_ = INVALID_SOCKET;
            }

            if (listenerThread_.joinable()) {
                listenerThread_.join();
            }
        }
    }

    bool isRunning() const { return running_; }

private:
    int port_;
    std::atomic<bool> running_;
    SOCKET serverSocket_;
    std::thread listenerThread_;
    DatabaseManager* dbManager_;

    void listenLoop() {
        while (running_) {
            sockaddr_in clientAddr;
            socklen_t clientLen = sizeof(clientAddr);

            SOCKET clientSocket = accept(serverSocket_, (sockaddr*)&clientAddr, &clientLen);

            if (clientSocket != INVALID_SOCKET) {
                std::thread(&HttpServer::handleRequest, this, clientSocket).detach();
            }
        }
    }

    void handleRequest(SOCKET clientSocket) {
        char buffer[4096];
        int bytesRead = recv(clientSocket, buffer, sizeof(buffer) - 1, 0);

        if (bytesRead > 0) {
            buffer[bytesRead] = '\0';
            std::string request(buffer);

            std::istringstream iss(request);
            std::string method, path, version;
            iss >> method >> path >> version;

            std::cout << "[HTTP] " << method << " " << path << std::endl;

            std::string response;

            if (method == "GET" && path == "/analyze/health") {
                response = handleHealth();
            } else if (method == "POST" && path == "/analyze/price") {
                response = handlePrice(request);
            } else if (method == "POST" && path == "/analyze/batch") {
                response = handleBatch(request);
            } else {
                response = createResponse(404, "Not Found", "text/plain", "Endpoint not found");
            }

            send(clientSocket, response.c_str(), response.length(), 0);
        }

        closesocket(clientSocket);
    }

    std::string handleHealth() {
        std::string body = "{\"status\":\"UP\",\"version\":\"1.0.0\"}";
        return createResponse(200, "OK", "application/json", body);
    }

    std::string handlePrice(const std::string& request) {
        size_t bodyPos = request.find("\r\n\r\n");
        if (bodyPos == std::string::npos) {
            return createResponse(400, "Bad Request", "text/plain", "No body");
        }

        std::string body = request.substr(bodyPos + 4);
        std::cout << "[HTTP] Price data: " << body << std::endl;

        std::string responseBody = "{\"message\":\"Price received\",\"status\":\"success\"}";
        return createResponse(200, "OK", "application/json", responseBody);
    }

    std::string handleBatch(const std::string& request) {
        size_t bodyPos = request.find("\r\n\r\n");
        if (bodyPos == std::string::npos) {
            return createResponse(400, "Bad Request", "text/plain", "No body");
        }

        std::string body = request.substr(bodyPos + 4);
        std::cout << "[HTTP] Batch data: " << body << std::endl;

        std::string responseBody = "{\"message\":\"Batch received\",\"status\":\"success\"}";
        return createResponse(200, "OK", "application/json", responseBody);
    }

    std::string createResponse(int code, const std::string& status,
                               const std::string& contentType, const std::string& body) {
        std::ostringstream oss;
        oss << "HTTP/1.1 " << code << " " << status << "\r\n";
        oss << "Content-Type: " << contentType << "\r\n";
        oss << "Content-Length: " << body.length() << "\r\n";
        oss << "Connection: close\r\n";
        oss << "Access-Control-Allow-Origin: *\r\n";
        oss << "\r\n";
        oss << body;
        return oss.str();
    }
};

} // namespace analysis

#endif // HTTP_SERVER_HPP

