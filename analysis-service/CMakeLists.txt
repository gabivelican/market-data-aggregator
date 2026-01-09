cmake_minimum_required(VERSION 3.15)
project(AnalysisService VERSION 1.0.0 LANGUAGES CXX)

# C++ Standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Build type
if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE Release)
endif()

# Compiler flags
set(CMAKE_CXX_FLAGS_DEBUG "-g -O0 -Wall -Wextra")
set(CMAKE_CXX_FLAGS_RELEASE "-O3 -Wall")

# Find required packages
find_package(Threads REQUIRED)

# Optional: Find PostgreSQL (libpq)
# find_package(PostgreSQL REQUIRED)

# Include directories
include_directories(${PROJECT_SOURCE_DIR}/include)

# Source files
set(SOURCES
    src/main.cpp
    src/data_structures.cpp
    src/database_manager.cpp
    src/config_loader.cpp
    src/http_server.cpp
    src/moving_average.cpp
    src/anomaly_detector.cpp
    src/gateway_client.cpp
)

# Create executable
add_executable(analysis_service ${SOURCES})

# Link libraries
target_link_libraries(analysis_service
    Threads::Threads
    # PostgreSQL::PostgreSQL  # Uncomment if using libpq
)

# Installation
install(TARGETS analysis_service DESTINATION bin)

# Print configuration
message(STATUS "Build type: ${CMAKE_BUILD_TYPE}")
message(STATUS "C++ Standard: ${CMAKE_CXX_STANDARD}")
message(STATUS "Compiler: ${CMAKE_CXX_COMPILER_ID}")

