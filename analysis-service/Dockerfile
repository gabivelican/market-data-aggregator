# Stage 1: Build stage
FROM gcc:13-bookworm AS build

WORKDIR /app

# Instalam dependintele de build
RUN apt-get update && apt-get install -y \
    cmake \
    make \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

COPY analysis-service/ ./

RUN mkdir -p build && cd build && \
    cmake .. && \
    cmake --build . --config Release

# Stage 2: Runtime stage
# FIX CRITIC: Folosim gcc:13-bookworm si la runtime.
# Asta rezolva eroarea GLIBCXX_3.4.32 instantaneu.
FROM gcc:13-bookworm

WORKDIR /app

# Instalam doar libraria de Postgres necesara la rulare
RUN apt-get update && apt-get install -y \
    libpq5 \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/build/analysis_service /app/analysis_service

# Setup user
RUN useradd -r -s /bin/false appuser && \
    chown appuser:appuser /app/analysis_service && \
    chmod +x /app/analysis_service

USER appuser

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD curl -f http://localhost:8081/analyze/health || exit 1

ENTRYPOINT ["/app/analysis_service"]