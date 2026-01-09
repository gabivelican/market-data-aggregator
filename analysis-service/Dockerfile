# Stage 1: Build stage
FROM gcc:13-bookworm AS build

WORKDIR /app

# Install build dependencies
RUN apt-get update && apt-get install -y \
    cmake \
    make \
    && rm -rf /var/lib/apt/lists/*

# Copy source code
COPY analysis-service/ ./

# Create build directory and build
RUN mkdir -p build && cd build && \
    cmake .. && \
    cmake --build . --config Release

# Stage 2: Runtime stage (use full bookworm for libstdc++ compatibility)
FROM debian:bookworm

WORKDIR /app

# Install runtime dependencies (libstdc++6 from bookworm has GLIBCXX_3.4.32)
RUN apt-get update && apt-get install -y \
    libstdc++6 \
    curl \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*


# Copy binary from build stage
COPY --from=build /app/build/analysis_service /app/analysis_service

# Create non-root user
RUN useradd -r -s /bin/false appuser && \
    chown appuser:appuser /app/analysis_service && \
    chmod +x /app/analysis_service

USER appuser

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD curl -f http://localhost:8081/analyze/health || exit 1

# Run application
ENTRYPOINT ["/app/analysis_service"]

