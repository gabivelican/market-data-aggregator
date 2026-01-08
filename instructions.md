# Real-Time Market Data Aggregator - Step-by-Step Implementation Guide

## Project Overview
Building a cloud-native application for real-time stock market data aggregation with Spring Boot Gateway, C++ Analysis Service, PostgreSQL, WebSocket, and Docker orchestration.

---

## Phase 1: Database Setup & Schema Design

### Step 1: PostgreSQL Database Setup
**Objective:** Create the database foundation for storing market data

**Tasks:**
1. Design database schema with the following tables:
    - `symbols` table: store trading symbols (id, symbol_code, name, type, created_at)
    - `prices` table: store historical price data (id, symbol_id, price, volume, timestamp)
    - `alerts` table: store anomaly alerts (id, symbol_id, alert_type, threshold, triggered_at, details)
    - `users` table: store user credentials (id, username, password_hash, created_at)

2. Create SQL migration scripts with:
    - Table creation statements
    - Indexes on symbol_code, timestamp fields
    - Foreign key constraints

3. Create seed data script with at least 3 symbols:
    - AAPL (Apple Stock)
    - BTC (Bitcoin)
    - GOOGL (Google Stock)

**Testing:**
- Verify database connection
- Run migration scripts successfully
- Confirm seed data is inserted
- Query each table to verify structure
- Test foreign key constraints by attempting invalid inserts

---

## Phase 2: Spring Boot Gateway - Basic Setup

### Step 2: Spring Boot Project Initialization
**Objective:** Set up the main gateway service

**Tasks:**
1. Initialize Spring Boot project with dependencies:
    - Spring Web
    - Spring Data JPA
    - PostgreSQL Driver
    - Spring Security
    - SpringDoc OpenAPI (Swagger)
    - WebSocket support
    - Lombok (optional)

2. Configure application properties:
    - Database connection parameters (as environment variables)
    - Server port configuration
    - JPA/Hibernate settings

3. Create project structure:
    - Controllers package
    - Services package
    - Repositories package
    - Models/Entities package
    - DTOs package
    - Configuration package

**Testing:**
- Start Spring Boot application successfully
- Verify database connection on startup
- Access basic health endpoint (Spring Actuator)
- Confirm application runs on configured port

---

## Phase 3: Spring Boot Gateway - Authentication

### Step 3: Implement Authentication System
**Objective:** Secure the API with user authentication

**Tasks:**
1. Create User entity and repository
2. Implement authentication endpoints:
    - POST `/api/auth/register`: Register new user (username, password)
    - POST `/api/auth/login`: Login and receive JWT token

3. Configure Spring Security:
    - JWT token generation and validation
    - Password encryption (BCrypt)
    - Security filter chain
    - Public endpoints: /api/auth/**, /swagger-ui/**, /v3/api-docs/**
    - Protected endpoints: all others

4. Create authentication DTOs:
    - LoginRequest (username, password)
    - LoginResponse (token, username)
    - RegisterRequest (username, password)

**Testing:**
- Register a new user successfully
- Attempt registration with duplicate username (should fail)
- Login with correct credentials (receive token)
- Login with incorrect credentials (should fail)
- Access protected endpoint without token (should return 401)
- Access protected endpoint with valid token (should succeed)

---

## Phase 4: Spring Boot Gateway - REST API Implementation

### Step 4: Implement Symbol Management Endpoints
**Objective:** Create APIs for managing trading symbols

**Tasks:**
1. Create Symbol entity and repository
2. Implement endpoints:
    - GET `/api/symbols`: List all available symbols
    - GET `/api/symbols/{id}`: Get specific symbol details
    - GET `/api/symbols/{symbol}/current-price`: Get latest price for symbol

3. Create DTOs:
    - SymbolDTO (id, code, name, type, currentPrice)
    - SymbolDetailDTO (includes price history summary)

**Testing:**
- Retrieve all symbols (should return seeded data)
- Get specific symbol by ID
- Get symbol by code (AAPL, BTC, GOOGL)
- Request non-existent symbol (should return 404)
- Test without authentication token (should fail)

---

### Step 5: Implement Price History Endpoints
**Objective:** Create APIs for accessing historical price data

**Tasks:**
1. Create Price entity and repository
2. Implement endpoints:
    - GET `/api/prices/{symbol}`: Get price history for symbol
        - Query parameters: startDate, endDate, limit
    - GET `/api/prices/{symbol}/latest`: Get most recent price
    - GET `/api/prices/recent`: Get recent prices for all symbols (last hour)

3. Create DTOs:
    - PriceDTO (symbolCode, price, volume, timestamp)
    - PriceHistoryDTO (symbol, prices list, statistics)

4. Implement pagination for history endpoint

**Testing:**
- Retrieve price history for a symbol with date filters
- Get latest price for each symbol
- Test pagination parameters
- Query with invalid date ranges (should handle gracefully)
- Verify response format matches DTO structure

---

### Step 6: Implement Alert Management Endpoints
**Objective:** Create APIs for viewing and managing alerts

**Tasks:**
1. Create Alert entity and repository
2. Implement endpoints:
    - GET `/api/alerts`: Get all alerts
        - Query parameters: symbolCode, alertType, startDate, endDate
    - GET `/api/alerts/{id}`: Get specific alert details
    - GET `/api/alerts/active`: Get currently active alerts
    - POST `/api/alerts/acknowledge/{id}`: Mark alert as acknowledged

3. Create DTOs:
    - AlertDTO (id, symbolCode, type, threshold, triggeredAt, details, acknowledged)
    - AlertSummaryDTO (count by type, most recent alerts)

**Testing:**
- Retrieve all alerts (initially should be empty)
- Filter alerts by symbol
- Filter alerts by date range
- Test alert acknowledgment
- Verify alert details include complete information

---

### Step 7: OpenAPI/Swagger Documentation
**Objective:** Complete API documentation

**Tasks:**
1. Configure SpringDoc OpenAPI:
    - Set API title, description, version
    - Configure security scheme (JWT Bearer)
    - Group endpoints logically

2. Add Swagger annotations to all endpoints:
    - @Operation descriptions
    - @ApiResponse for different status codes
    - @Parameter descriptions
    - @Schema for DTOs

3. Configure Swagger UI customization:
    - Enable authorization persistence
    - Set up example values
    - Add API usage guidelines

**Testing:**
- Access Swagger UI at `/swagger-ui/index.html`
- Verify all endpoints are documented
- Test "Try it out" functionality for each endpoint
- Authenticate through Swagger UI using JWT token
- Execute sample requests for each endpoint type
- Verify response schemas match actual responses

---

## Phase 5: C++ Analysis Service - Core Implementation

### Step 8: C++ Project Setup & Data Structures
**Objective:** Initialize the C++ microservice for data analysis

**Tasks:**
1. Set up C++ project structure:
    - Use CMake for build configuration
    - Include libraries: libpqxx (PostgreSQL), JSON library (nlohmann/json), HTTP library (cpp-httplib or Boost.Beast)

2. Create core data structures:
    - MarketDataPoint class (symbol, price, volume, timestamp)
    - AggregatedData class (symbol, average, min, max, volume, period)
    - Alert class (symbol, alertType, threshold, currentValue, timestamp)

3. Implement database connection manager:
    - Connection pool for PostgreSQL
    - Query execution methods
    - Transaction handling
    - Connection retry logic

4. Create configuration loader:
    - Read database credentials from environment variables
    - Load analysis parameters (window sizes, thresholds)
    - Configure HTTP server settings

**Testing:**
- Compile C++ project successfully
- Establish database connection
- Execute simple SELECT query on symbols table
- Insert test price record
- Verify connection pool functionality (multiple concurrent connections)
- Test configuration loading from environment variables

---

### Step 9: C++ HTTP API for Data Ingestion
**Objective:** Create endpoint to receive price data

**Tasks:**
1. Implement HTTP server:
    - POST `/analyze/price`: Receive single price update
        - Request body: {symbol, price, volume, timestamp}
    - POST `/analyze/batch`: Receive multiple price updates
        - Request body: [{symbol, price, volume, timestamp}, ...]
    - GET `/analyze/health`: Health check endpoint

2. Implement request validation:
    - Validate symbol exists in database
    - Validate price is positive
    - Validate timestamp format
    - Return appropriate error codes (400 for validation errors)

3. Store received data:
    - Insert price data into prices table
    - Timestamp normalization
    - Batch insert optimization for /batch endpoint

**Testing:**
- Start C++ service on configured port
- Send single price update via POST request
- Verify price is stored in database
- Send batch of 10 price updates
- Verify all batch prices are stored
- Send invalid data (negative price, invalid symbol)
- Verify error responses
- Test health endpoint returns 200 OK

---

### Step 10: C++ Moving Average Calculation
**Objective:** Implement time-series analysis

**Tasks:**
1. Create MovingAverageCalculator class:
    - Simple Moving Average (SMA) over configurable window
    - Exponential Moving Average (EMA) with configurable alpha
    - Support multiple timeframes (5-minute, 15-minute, 1-hour)

2. Implement calculation logic:
    - Fetch last N price points for symbol from database
    - Calculate SMA: sum of prices / count
    - Calculate EMA: recursive formula
    - Cache recent calculations to optimize performance

3. Create endpoint:
    - GET `/analyze/moving-average/{symbol}`: Get current moving averages
        - Query parameters: window (5, 15, 60 minutes), type (SMA/EMA)
    - Response: {symbol, currentPrice, sma, ema, window, calculatedAt}

**Testing:**
- Insert series of prices for a symbol (at least 20 data points)
- Calculate 5-point SMA manually
- Request SMA from service and verify matches calculation
- Test EMA calculation
- Request moving average for symbol with insufficient data
- Test different window sizes
- Verify caching improves response time for repeated requests

---

### Step 11: C++ Anomaly Detection
**Objective:** Detect significant price movements

**Tasks:**
1. Create AnomalyDetector class with detection strategies:
    - Spike detection: price change > threshold percentage (e.g., 5% in 1 minute)
    - Volume anomaly: volume > N times average volume
    - Volatility detection: price variance exceeds threshold
    - Trend reversal detection: moving average crossover

2. Implement detection logic:
    - Calculate percentage change from previous price
    - Compare current volume to historical average
    - Calculate rolling standard deviation
    - Detect MA crossover signals

3. Alert generation:
    - When anomaly detected, insert record into alerts table
    - Alert types: SPIKE_UP, SPIKE_DOWN, HIGH_VOLUME, VOLATILITY, TREND_REVERSAL
    - Include details: threshold, actual value, percentage change

4. Create endpoint:
    - POST `/analyze/detect`: Trigger anomaly detection for symbol
    - GET `/analyze/anomalies/{symbol}`: Get recent anomalies

**Testing:**
- Insert normal price progression (1% changes)
- Insert sudden spike (10% increase)
- Trigger anomaly detection
- Verify alert created in database
- Test spike down detection
- Insert high volume data point
- Verify volume anomaly detected
- Test with symbol having no anomalies (should return empty)
- Verify alert details include threshold and actual values

---

### Step 12: C++ Results Communication
**Objective:** Send analysis results back to Gateway

**Tasks:**
1. Implement HTTP client for Gateway communication:
    - POST to Gateway endpoint `/internal/analysis-results`
    - Send aggregated data periodically
    - Send alerts immediately when detected

2. Create result payload structure:
    - AggregatedDataPayload: {symbol, sma, ema, price, volume, timestamp}
    - AlertPayload: {symbol, alertType, threshold, value, details, timestamp}

3. Implement result sender service:
    - Queue system for pending results
    - Retry logic with exponential backoff
    - Error logging for failed transmissions

4. Schedule periodic aggregation:
    - Every minute, calculate moving averages for all active symbols
    - Send results to Gateway
    - Update database with calculated metrics

**Testing:**
- Start both C++ service and Gateway
- Manually trigger analysis
- Verify Gateway receives HTTP POST request
- Check Gateway logs for received data
- Simulate Gateway downtime
- Verify retry logic attempts reconnection
- Verify queued results are sent after Gateway returns
- Test periodic aggregation timer (wait 1+ minutes)

---

## Phase 6: Gateway Integration with C++ Service

### Step 13: Gateway Internal Endpoints
**Objective:** Create Gateway endpoints to receive C++ analysis results

**Tasks:**
1. Implement internal controller (not publicly documented):
    - POST `/internal/analysis-results`: Receive aggregated data from C++
    - POST `/internal/alerts`: Receive alerts from C++
    - Add IP whitelist or shared secret for security

2. Process received data:
    - Store aggregated metrics in database (or cache)
    - Store alerts in alerts table
    - Trigger WebSocket notifications for new alerts

3. Create service to trigger C++ analysis:
    - POST `/api/admin/trigger-analysis/{symbol}`: Manually trigger analysis
    - Calls C++ service POST `/analyze/detect` endpoint

**Testing:**
- Send aggregated data from C++ to Gateway internal endpoint
- Verify data is stored/cached in Gateway
- Send alert from C++ to Gateway
- Verify alert is stored in database
- Trigger manual analysis from Gateway admin endpoint
- Verify C++ service receives request and processes
- Test authentication/authorization for internal endpoints

---

### Step 14: Data Provider Simulator
**Objective:** Create simulated data source for testing

**Tasks:**
1. Create DataProviderSimulator component in Gateway:
    - Scheduled task that runs every 5-10 seconds
    - Generates random price updates for all symbols
    - Price changes: ±0.5% to ±3% from last price
    - Occasional spikes: ±5% to ±10% (5% probability)
    - Random volume: 1000-10000 units

2. Send generated data to C++ service:
    - POST to C++ `/analyze/batch` endpoint
    - Include all symbols in each batch
    - Log data generation activity

3. Create control endpoints:
    - POST `/api/admin/simulator/start`: Start data generation
    - POST `/api/admin/simulator/stop`: Stop data generation
    - GET `/api/admin/simulator/status`: Get simulator status
    - POST `/api/admin/simulator/spike/{symbol}`: Force spike for testing

**Testing:**
- Start simulator via admin endpoint
- Verify prices are generated every interval
- Check C++ service logs for received data
- Verify new prices appear in database
- Check moving averages update over time
- Force a spike event
- Verify alert is generated and stored
- Stop simulator and verify data generation ceases

---

## Phase 7: WebSocket Implementation

### Step 15: WebSocket Server Setup
**Objective:** Enable real-time data streaming to clients

**Tasks:**
1. Configure WebSocket in Spring Boot:
    - WebSocket message broker configuration
    - STOMP protocol setup
    - Configure message destinations

2. Create WebSocket endpoints:
    - `/ws`: WebSocket connection endpoint
    - Subscribe destinations:
        - `/topic/prices`: Real-time price updates for all symbols
        - `/topic/prices/{symbol}`: Price updates for specific symbol
        - `/topic/alerts`: Real-time alerts
        - `/topic/alerts/{symbol}`: Alerts for specific symbol

3. Implement WebSocket service:
    - Method to broadcast price updates to subscribers
    - Method to broadcast alerts to subscribers
    - Connection management (track connected clients)

4. Integrate with data flow:
    - When C++ sends aggregated data to Gateway, broadcast via WebSocket
    - When alert is received, broadcast via WebSocket
    - When new price stored, broadcast update

**Testing:**
- Connect WebSocket client to `/ws` endpoint
- Subscribe to `/topic/prices`
- Start data simulator
- Verify price updates are received in real-time
- Subscribe to `/topic/alerts`
- Force a spike event
- Verify alert is received via WebSocket
- Test multiple simultaneous client connections
- Disconnect and reconnect (verify no data loss)
- Subscribe to specific symbol topic, verify only that symbol's data received

---

## Phase 8: Docker Containerization

### Step 16: PostgreSQL Docker Configuration
**Objective:** Containerize the database

**Tasks:**
1. Create Dockerfile for PostgreSQL (or use official image):
    - Use official postgres:15 image
    - Copy SQL migration scripts into `/docker-entrypoint-initdb.d/`
    - Copy seed data scripts

2. Configure database initialization:
    - Schema creation runs automatically on first startup
    - Seed data inserts after schema creation

3. Define volume for data persistence:
    - Mount volume for `/var/lib/postgresql/data`

**Testing:**
- Build PostgreSQL container
- Start container with docker run
- Connect to database from host machine
- Verify tables exist
- Verify seed data is present
- Stop and remove container
- Restart container with same volume
- Verify data persists

---

### Step 17: Spring Boot Gateway Dockerization
**Objective:** Containerize the Java gateway service

**Tasks:**
1. Create Dockerfile for Spring Boot:
    - Use multi-stage build
    - Stage 1: Maven/Gradle build
    - Stage 2: Lightweight JRE image with compiled JAR
    - EXPOSE port 8080

2. Configure environment variables:
    - Database connection parameters
    - C++ service URL
    - JWT secret
    - Server port

3. Add health check:
    - HEALTHCHECK command using curl to `/actuator/health`

**Testing:**
- Build Gateway Docker image
- Run container with environment variables
- Verify application starts successfully
- Access Swagger UI from host machine
- Test API endpoints through container
- Check container logs for errors
- Verify health check passes

---

### Step 18: C++ Analysis Service Dockerization
**Objective:** Containerize the C++ microservice

**Tasks:**
1. Create Dockerfile for C++ service:
    - Use multi-stage build
    - Stage 1: Build environment with CMake, g++, libraries
    - Copy source code and build
    - Stage 2: Lightweight runtime image
    - Copy compiled binary and dependencies
    - EXPOSE port 8081

2. Configure environment variables:
    - Database connection parameters
    - Gateway URL for result posting
    - Analysis parameters (thresholds, windows)
    - HTTP server port

3. Add health check:
    - HEALTHCHECK command using curl to `/analyze/health`

**Testing:**
- Build C++ Docker image
- Run container with environment variables
- Send test POST request to `/analyze/price`
- Verify response received
- Check container logs
- Verify health check passes
- Test database connectivity from container

---

### Step 19: Docker Compose Orchestration
**Objective:** Orchestrate all services with Docker Compose

**Tasks:**
1. Create docker-compose.yml with services:
    - `postgres`: Database service
        - Volumes for persistence
        - Environment variables for credentials
        - Health check configuration
    - `gateway`: Spring Boot gateway
        - Depends on postgres
        - Environment variables linking to postgres service
        - Port mapping: 8080:8080
    - `analysis-service`: C++ service
        - Depends on postgres
        - Environment variables linking to postgres and gateway
        - Port mapping: 8081:8081

2. Configure networking:
    - Create custom bridge network
    - All services on same network
    - Service discovery by service name

3. Configure startup order:
    - postgres starts first
    - gateway and analysis-service wait for postgres health check
    - Use depends_on with condition: service_healthy

4. Configure volume management:
    - Named volume for postgres data
    - Bind mounts for development (optional)

**Testing:**
- Run `docker-compose up`
- Verify all three containers start successfully
- Check logs for each service
- Access Gateway Swagger UI at localhost:8080
- Test end-to-end flow:
    - Start data simulator via Gateway API
    - Verify prices flow: Gateway → C++ → Database → WebSocket
    - Force spike and verify alert generated
- Run `docker-compose down`
- Run `docker-compose up` again
- Verify database data persists

---

## Phase 9: Secret Management

### Step 20: Docker Secrets Implementation
**Objective:** Secure sensitive configuration

**Tasks:**
1. Create secret files:
    - `db_password.txt`: PostgreSQL password
    - `jwt_secret.txt`: JWT signing key
    - `api_key.txt`: Internal API key for C++↔Gateway communication

2. Configure Docker Compose secrets:
    - Define secrets at top level
    - Reference secret files
    - Mount secrets to services

3. Update application configurations:
    - Gateway: Read JWT secret from `/run/secrets/jwt_secret`
    - Gateway: Read DB password from `/run/secrets/db_password`
    - C++ service: Read DB password from secret
    - C++ service: Read API key from secret

4. Update Dockerfiles:
    - Add code to read secrets from mounted files
    - Fallback to environment variables for local development

5. Create .env.example file:
    - Template for non-secret environment variables
    - Comments explaining each variable
    - Instructions to create .env file

**Testing:**
- Create secret files with test values
- Run docker-compose with secrets
- Verify services can read secrets
- Verify database authentication works
- Test JWT token generation with secret
- Verify internal API authentication
- Attempt to access secrets from host (should not be accessible)
- Check container logs don't expose secrets

---

## Phase 10: Observability

### Step 21: Health Checks & Logging
**Objective:** Implement monitoring and diagnostics

**Tasks:**
1. Spring Boot Gateway observability:
    - Enable Spring Boot Actuator
    - Configure health check endpoints:
        - `/actuator/health`: Overall health
        - `/actuator/health/db`: Database connectivity
        - `/actuator/health/diskSpace`: Disk space
    - Implement custom health indicator for C++ service connectivity
    - Configure structured JSON logging (Logback)
    - Log levels: INFO for normal operations, ERROR for exceptions
    - Include request IDs for tracing

2. C++ service observability:
    - Implement `/analyze/health` endpoint with details:
        - Database connectivity status
        - Last successful analysis timestamp
        - Queue size (pending results)
    - Implement structured logging (spdlog or similar)
    - Log all incoming requests
    - Log analysis results
    - Log errors with stack traces

3. PostgreSQL monitoring:
    - Enable connection logging
    - Configure slow query logging

**Testing:**
- Access Gateway `/actuator/health`
- Verify all components show UP status
- Stop C++ service
- Verify custom health indicator shows DOWN
- Restart C++ service
- Stop database
- Verify both services health checks show database DOWN
- Review log files for proper JSON structure
- Trigger error condition
- Verify error is logged with appropriate details

---

### Step 22: Metrics Implementation
**Objective:** Track performance metrics

**Tasks:**
1. Spring Boot metrics:
    - Enable Micrometer metrics
    - Expose metrics endpoint: `/actuator/metrics`
    - Track custom metrics:
        - Counter: `api.requests.total` (by endpoint, status)
        - Gauge: `websocket.connections.active`
        - Timer: `api.request.duration` (by endpoint)
        - Counter: `alerts.generated.total` (by type)

2. C++ service metrics:
    - Implement metrics tracking:
        - Counter: analysis requests processed
        - Histogram: analysis processing time (latency)
        - Gauge: current price for each symbol
        - Counter: anomalies detected (by type)
    - Expose metrics endpoint: GET `/analyze/metrics`
    - Return JSON format with all metrics

3. Add metrics endpoint to Gateway:
    - GET `/api/admin/metrics/summary`: Aggregate metrics from both services
    - Include: total requests, average latency, active WebSocket connections, alerts count

**Testing:**
- Access Gateway `/actuator/metrics`
- Verify metrics list is returned
- Access specific metric: `/actuator/metrics/api.requests.total`
- Generate API traffic (multiple requests)
- Verify counter increments
- Connect WebSocket clients
- Verify websocket.connections.active gauge
- Access C++ `/analyze/metrics`
- Verify metrics are returned
- Trigger analyses
- Verify processing time histogram updates
- Access aggregate metrics summary endpoint
- Verify data from both services combined

---

## Phase 11: CI/CD Pipeline

### Step 23: CI/CD Configuration
**Objective:** Automate build and test process

**Tasks:**
1. Create CI/CD pipeline configuration (GitHub Actions / GitLab CI / Jenkins):
    - Pipeline stages:
        - Build: Compile all services
        - Test: Run unit tests
        - Docker Build: Create Docker images
        - Push: Push images to registry (optional)

2. Build stage:
    - Spring Boot: `mvn clean package` or `./gradlew build`
    - C++: CMake build with test target
    - Fail pipeline if build errors occur

3. Test stage:
    - Spring Boot: Run JUnit tests (`mvn test`)
    - C++: Run unit tests with Google Test or Catch2
    - Generate test coverage reports
    - Fail pipeline if tests fail or coverage < threshold

4. Docker Build stage:
    - Build Docker images for all services
    - Tag images with commit SHA and branch name
    - Verify images build successfully

5. Optional Push stage:
    - Push images to Docker registry
    - Only on main/master branch
    - Tag with version number

**Testing:**
- Commit code change to repository
- Verify pipeline triggers automatically
- Check build stage completes successfully
- Verify test stage runs all tests
- Check test results in pipeline output
- Verify Docker images are built
- Introduce intentional test failure
- Verify pipeline fails at test stage
- Fix test and verify pipeline succeeds
- Check Docker image tags are correct

---

## Phase 12: Frontend Dashboard

### Step 24: Frontend Implementation
**Objective:** Create web dashboard for visualization

**Tasks:**
1. Set up frontend project:
    - Create React/Vue/Angular application (or vanilla HTML/JS)
    - Configure CORS in Gateway for frontend origin
    - Set up build process

2. Implement authentication:
    - Login form (username, password)
    - Store JWT token in localStorage/sessionStorage
    - Add token to API request headers
    - Logout functionality

3. Create main dashboard layout:
    - Header: App title, username, logout button
    - Sidebar: List of symbols (AAPL, BTC, GOOGL)
    - Main area: Price chart and alerts list
    - Footer: Connection status, last update time

4. Implement real-time price display:
    - Connect to WebSocket endpoint
    - Subscribe to `/topic/prices`
    - Display current price for each symbol
    - Update prices in real-time as data arrives
    - Color code: green for price increase, red for decrease
    - Show percentage change

5. Implement price chart:
    - Use charting library (Chart.js, Recharts, etc.)
    - Display price history as line chart
    - Fetch historical data from `/api/prices/{symbol}` endpoint
    - Update chart with real-time data points
    - Time range selector (1 hour, 4 hours, 24 hours)

6. Implement alerts panel:
    - Subscribe to WebSocket `/topic/alerts`
    - Display alerts in real-time
    - Show alert type, symbol, value, timestamp
    - Color code by severity (red for spikes, yellow for volume)
    - Filter alerts by symbol
    - Acknowledge button for each alert

7. Add statistics cards:
    - Total active symbols
    - Total alerts today
    - Most volatile symbol
    - Highest volume symbol

**Testing:**
- Build frontend application
- Serve on local development server
- Login with valid credentials
- Verify redirect to dashboard
- Check all symbols displayed
- Start data simulator from backend
- Verify prices update in real-time
- Check price change indicators (colors)
- Verify chart updates with new data
- Force spike event
- Verify alert appears immediately in alerts panel
- Click acknowledge button
- Verify alert marked as acknowledged
- Test logout functionality
- Test with invalid credentials
- Test WebSocket reconnection (restart backend)

---

## Phase 13: Final Integration & Testing

### Step 25: End-to-End Testing
**Objective:** Verify complete system functionality

**Tasks:**
1. Full system startup test:
    - Run `docker-compose down -v` (clean state)
    - Run `docker-compose up`
    - Verify all services start without errors
    - Verify database schema created
    - Verify seed data present

2. Complete data flow test:
    - Access frontend dashboard
    - Login as user
    - Start data simulator via API or admin panel
    - Verify prices appear in frontend
    - Verify C++ service receives and processes data
    - Verify moving averages calculated
    - Verify prices stored in database
    - Verify WebSocket delivers updates

3. Anomaly detection test:
    - Force spike event for specific symbol
    - Verify C++ detects anomaly
    - Verify alert created in database
    - Verify alert sent to Gateway
    - Verify alert broadcast via WebSocket
    - Verify alert appears in frontend dashboard
    - Verify alert details are correct

4. Load testing:
    - Increase simulator frequency (every 1 second)
    - Monitor system performance
    - Check CPU and memory usage
    - Verify no data loss
    - Verify WebSocket remains stable
    - Check database query performance
    - Verify metrics reflect increased load

5. Failure recovery testing:
    - Stop C++ service mid-operation
    - Verify Gateway handles unavailability gracefully
    - Restart C++ service
    - Verify system resumes normal operation
    - Simulate database connection loss
    - Verify both services attempt reconnection
    - Restore database
    - Verify services reconnect automatically

6. Security testing:
    - Attempt API access without authentication (should fail)
    - Attempt WebSocket connection without token (should fail)
    - Try SQL injection in API parameters
    - Verify secrets not exposed in logs or API responses
    - Test JWT token expiration

**Testing:**
- Execute each test scenario above
- Document results for each test
- Fix any issues discovered
- Re-test after fixes
- Verify all functionality works as expected
- Confirm system meets all requirements

---

## Phase 14: Documentation

### Step 26: Complete Documentation
**Objective:** Create comprehensive project documentation

**Tasks:**
1. README.md file with:
    - Project title and description
    - Architecture overview diagram (ASCII or embedded image)
    - Technologies used:
        - Spring Boot (Gateway, version)
        - C++ (Analysis Service, version, libraries)
        - PostgreSQL (version)
        - Docker & Docker Compose
        - Frontend framework
    - System requirements
    - Installation instructions:
        - Clone repository command
        - Environment setup steps
        - Docker installation verification
    - Quick start guide:
        - `docker-compose up` command
        - URLs for accessing services
        - Default credentials
    - API documentation link (Swagger UI)
    - Troubleshooting section

2. Architecture documentation:
    - Component diagram showing:
        - Frontend ↔ Gateway (REST + WebSocket)
        - Gateway ↔ C++ Service (HTTP)
        - Gateway ↔ Database
        - C++ Service ↔ Database
    - Data flow diagram for:
        - Price data ingestion
        - Analysis processing
        - Alert generation
        - Real-time updates
    - Sequence diagrams for key operations:
        - User authentication flow
        - Price update flow
        - Anomaly detection flow

3. API documentation:
    - Already covered by Swagger/OpenAPI
    - Include examples for each endpoint
    - Document WebSocket topics and message formats

4. Deployment guide:
    - Production deployment considerations
    - Environment variable configuration
    - Secret management setup
    - Scaling recommendations
    - Backup and recovery procedures

5. Developer guide:
    - Project structure explanation
    - How to add new endpoints
    - How to add new anomaly detection strategies
    - Testing guidelines
    - Code style conventions

6. Optional: LaTeX document:
    - Professional report format
    - Include all sections above
    - Add screenshots of running application
    - Include test results and metrics

7. Optional: Video demonstration:
    - Record 3-5 minute video showing:
        - System startup
        - Login to dashboard
        - Real-time price updates
        - Triggering and viewing alerts
        - API testing via Swagger
        - Metrics and health checks

**Testing:**
- Follow README instructions on fresh machine/environment
- Verify all steps work correctly
- Check all links in documentation are valid
- Review diagrams for accuracy
- Test example API requests from documentation
- Verify troubleshooting section addresses common issues

---

## Phase 15: Final Requirements Verification

### Step 27: Requirements Checklist
**Objective:** Verify all project requirements are met

**Verification Checklist:**

**Gateway with REST API documented (15p):**
- [ ] OpenAPI/Swagger UI accessible
- [ ] Endpoints for current prices: GET `/api/prices/{symbol}/latest`
- [ ] Endpoints for recent variations: GET `/api/prices/recent`
- [ ] Authentication implemented with JWT
- [ ] All endpoints documented with examples

**Persistence in Postgres (10p):**
- [ ] Symbols table created and populated
- [ ] Prices table stores historical data
- [ ] Alerts table stores anomalies
- [ ] Seed data with 3+ symbols (AAPL, BTC, GOOGL)
- [ ] Foreign key relationships defined

**C++ Microservice in separate container (20p):**
- [ ] C++ service runs in own Docker container
- [ ] Receives data via HTTP endpoints
- [ ] Calculates moving averages (SMA/EMA)
- [ ] Detects anomalies (spikes, volume, volatility)
- [ ] Sends results back to Gateway
- [ ] Saves prices and analysis to database

**Active WebSocket (10p):**
- [ ] WebSocket server configured in Gateway
- [ ] Transmits aggregated prices in real-time
- [ ] Notifies users about significant variations
- [ ] Multiple clients can connect simultaneously
- [ ] Stable connection with reconnection logic

**Complete orchestration with Docker Compose (10p):**
- [ ] Single `docker-compose up` starts entire application
- [ ] All services defined (postgres, gateway, analysis-service)
- [ ] Configuration via environment variables
- [ ] Proper startup order with health checks
- [ ] Network configuration for service discovery

**Secret management (10p):**
- [ ] Passwords managed via Docker Secrets or Vault
- [ ] JWT secret externalized
- [ ] No hardcoded secrets in code
- [ ] API keys for internal communication secured
- [ ] .env.example file provided

**Basic observability (10p):**
- [ ] Health checks for all services
- [ ] Structured logging implemented
- [ ] At least one metric: processing latency or throughput
- [ ] Metrics endpoint accessible
- [ ] Logs provide adequate debugging information

**Minimal CI/CD (5p):**
- [ ] Pipeline configuration file exists
- [ ] Build stage compiles all services
- [ ] Test stage runs unit tests
- [ ] Docker image build stage
- [ ] Pipeline fails on errors

**Frontend (5p):**
- [ ] Dashboard displays prices
- [ ] Real-time updates via WebSocket
- [ ] Alerts displayed live
- [ ] Authentication/login page
- [ ] Responsive and functional UI

**Documentation & Demonstration (5p):**
- [ ] README with architecture and technologies
- [ ] Setup instructions complete
- [ ] Troubleshooting guide included
- [ ] Optional: LaTeX document prepared
- [ ] Optional: Video demo (max 5 min) recorded

**Base points (10p):**
- [ ] Automatically awarded

**Final Testing:**
- Perform one complete end-to-end test
- Verify each requirement from checklist
- Document any missing items
- Complete remaining tasks
- Re-verify checklist
- Confirm 100/100 points achieved

---

## Summary

This step-by-step guide provides a logical, testable path to implement the Real-Time Market Data Aggregator project. Each phase builds upon the previous one, with clear testing criteria to validate functionality before proceeding.

**Key Implementation Order:**
1. Database foundation
2. Gateway REST API & authentication
3. C++ analysis service core functionality
4. Integration between services
5. Real-time WebSocket layer
6. Containerization & orchestration
7. Security & observability
8. CI/CD automation
9. Frontend dashboard
10. Final testing & documentation

Follow each step sequentially, testing thoroughly before moving to the next phase. This approach ensures early detection of issues and a solid foundation for each subsequent feature.
