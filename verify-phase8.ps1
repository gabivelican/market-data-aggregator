# Phase 8 Verification Script
# Run this to verify all services are working

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  PHASE 8 - VERIFICATION SCRIPT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Check Docker is running
Write-Host "[1/7] Checking Docker..." -ForegroundColor Yellow
try {
    $dockerVersion = docker version --format '{{.Server.Version}}' 2>$null
    if ($dockerVersion) {
        Write-Host "  ✓ Docker is running (v$dockerVersion)" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Docker is not running!" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "  ✗ Docker is not installed or not running!" -ForegroundColor Red
    exit 1
}

# 2. Check containers are running
Write-Host "[2/7] Checking containers..." -ForegroundColor Yellow
$containers = docker ps --format "{{.Names}}" 2>$null
$expectedContainers = @("market-db", "market-gateway", "market-analysis")

foreach ($container in $expectedContainers) {
    if ($containers -contains $container) {
        $status = docker inspect -f '{{.State.Status}}' $container 2>$null
        Write-Host "  ✓ $container : $status" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $container : NOT FOUND" -ForegroundColor Red
    }
}

# 3. Check PostgreSQL health
Write-Host "[3/7] Checking PostgreSQL..." -ForegroundColor Yellow
try {
    $dbHealth = docker inspect -f '{{.State.Health.Status}}' market-db 2>$null
    if ($dbHealth -eq "healthy") {
        Write-Host "  ✓ PostgreSQL is healthy" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ PostgreSQL health: $dbHealth" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ✗ Cannot check PostgreSQL health" -ForegroundColor Red
}

# 4. Check Gateway health
Write-Host "[4/7] Checking Gateway..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 5 2>$null
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✓ Gateway is healthy (HTTP 200)" -ForegroundColor Green
    }
} catch {
    Write-Host "  ⚠ Gateway health check failed (might be starting...)" -ForegroundColor Yellow
}

# 5. Check Swagger UI
Write-Host "[5/7] Checking Swagger UI..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/swagger-ui.html" -UseBasicParsing -TimeoutSec 5 2>$null
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✓ Swagger UI is accessible" -ForegroundColor Green
    }
} catch {
    Write-Host "  ⚠ Swagger UI check failed" -ForegroundColor Yellow
}

# 6. Check Database connection
Write-Host "[6/7] Checking database..." -ForegroundColor Yellow
try {
    $dbList = docker exec market-db psql -U postgres -l 2>$null | Select-String "market_db"
    if ($dbList) {
        Write-Host "  ✓ Database 'market_db' exists" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Database 'market_db' not found" -ForegroundColor Red
    }
} catch {
    Write-Host "  ⚠ Cannot check database" -ForegroundColor Yellow
}

# 7. Check C++ service logs
Write-Host "[7/7] Checking C++ service..." -ForegroundColor Yellow
$logs = docker logs market-analysis --tail 5 2>$null
if ($logs -match "error|GLIBCXX|not found") {
    Write-Host "  ⚠ C++ service has errors (check logs)" -ForegroundColor Yellow
} else {
    Write-Host "  ✓ C++ service is running" -ForegroundColor Green
}

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  VERIFICATION COMPLETE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Access Points:" -ForegroundColor White
Write-Host "  Swagger UI:  http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host "  Health:      http://localhost:8080/actuator/health" -ForegroundColor Cyan
Write-Host "  WebSocket:   http://localhost:8080/websocket-test-client.html" -ForegroundColor Cyan
Write-Host ""
Write-Host "Useful Commands:" -ForegroundColor White
Write-Host "  docker ps                 # Check containers" -ForegroundColor Gray
Write-Host "  docker-compose logs       # View all logs" -ForegroundColor Gray
Write-Host "  docker-compose down       # Stop all services" -ForegroundColor Gray
Write-Host "  docker-compose up -d      # Start all services" -ForegroundColor Gray
Write-Host ""

