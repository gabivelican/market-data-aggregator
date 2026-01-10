cmake_minimum_required(VERSION 3.10)
project(MarketAnalysisService)

set(CMAKE_CXX_STANDARD 17)

# --- 1. SETARI SPECIFICE WINDOWS ---
if(WIN32)
    # Calea ta locala de Windows
    set(PostgreSQL_ROOT "D:/POSTGRESQL")
    add_compile_options(/W3)
    add_definitions(-D_CRT_SECURE_NO_WARNINGS)
else()
    # Setari pentru Linux (Docker)
    add_compile_options(-Wall -Wextra)
endif()

# --- 2. DEPENDINTE ---
find_package(PostgreSQL REQUIRED)
find_package(Threads REQUIRED) # Necesar pentru Linux networking

# Includem directoarele
include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/src
    ${CMAKE_CURRENT_SOURCE_DIR}/include
    ${PostgreSQL_INCLUDE_DIRS}
)

# --- 3. EXECUTABIL ---
add_executable(analysis_service src/main.cpp)

# --- 4. LINKARE (Aici era eroarea ta!) ---
target_link_libraries(analysis_service PRIVATE 
    ${PostgreSQL_LIBRARIES}
    Threads::Threads
)

# Adaugam librariile de Windows DOAR daca suntem pe Windows
if(WIN32)
    target_link_libraries(analysis_service PRIVATE ws2_32 Crypt32)
endif()