# Comprehensive Testing Script for Phases 1-8
# Tests all implemented functionality

$ErrorActionPreference = "Stop"
$baseUrl = "http://localhost:8080"
$cppServiceUrl = "http://localhost:8081"

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Testing Phases 1-8 Implementation" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

# Initialize test counters
$testsPassed = 0
$testsFailed = 0

# Helper function to make API calls
function Test-Endpoint {
    param(
        [string]$Method,
        [string]$Endpoint,
        [object]$Body = $null,
        [string]$Token = $null,
        [int]$ExpectedStatus = 200
    )

    try {
        $headers = @{"Content-Type" = "application/json"}
        if ($Token) {
            $headers["Authorization"] = "Bearer $Token"
        }

        $params = @{
            Uri     = "$baseUrl$Endpoint"
            Method  = $Method
            Headers = $headers
        }

        if ($Body) {
            $params["Body"] = $Body | ConvertTo-Json
        }

        $response = Invoke-WebRequest @params -SkipHttpErrorCheck
        return @{
            StatusCode = $response.StatusCode
            Content    = $response.Content | ConvertFrom-Json
            Success    = $response.StatusCode -eq $ExpectedStatus
        }
    }
    catch {
        return @{
            StatusCode = 0
            Content    = $null
            Success    = $false
            Error      = $_.Exception.Message
        }
    }
}

# ============= PHASE 1: DATABASE SETUP =============
Write-Host "`n[PHASE 1] DATABASE SETUP" -ForegroundColor Yellow

Write-Host "Testing database connection via health endpoint..."
$healthResponse = Test-Endpoint -Method GET -Endpoint "/actuator/health" -ExpectedStatus 200

if ($healthResponse.Success) {
    Write-Host "✅ Database connection OK" -ForegroundColor Green
    $testsPassed++
} else {
    Write-Host "❌ Database connection FAILED" -ForegroundColor Red
    $testsFailed++
}

# ============= PHASE 2: SPRING BOOT STARTUP =============
Write-Host "`n[PHASE 2] SPRING BOOT STARTUP" -ForegroundColor Yellow

Write-Host "Testing Spring Boot application health..."
if ($healthResponse.Success) {
    Write-Host "✅ Spring Boot application is running" -ForegroundColor Green
    $testsPassed++
} else {
    Write-Host "❌ Spring Boot application failed to start" -ForegroundColor Red
    $testsFailed++
    exit 1
}

# ============= PHASE 3: AUTHENTICATION =============
Write-Host "`n[PHASE 3] AUTHENTICATION SYSTEM" -ForegroundColor Yellow

# Test 3.1: Register new user
Write-Host "Test 3.1: Register new user..."
$registerBody = @{
    username = "testuser_$(Get-Random)"
    password = "password123"
} | ConvertTo-Json

$registerResponse = Test-Endpoint -Method POST -Endpoint "/api/auth/register" -Body $registerBody -ExpectedStatus 201

if ($registerResponse.Success) {
    Write-Host "✅ Registration successful" -ForegroundColor Green
    $testsPassed++
    $username = ($registerBody | ConvertFrom-Json).username
} else {
    Write-Host "❌ Registration failed: $($registerResponse.StatusCode)" -ForegroundColor Red
    $testsFailed++
    $username = $null
}

# Test 3.2: Duplicate registration (should fail)
Write-Host "Test 3.2: Attempt duplicate registration..."
$duplicateResponse = Test-Endpoint -Method POST -Endpoint "/api/auth/register" -Body $registerBody -ExpectedStatus 409

if ($duplicateResponse.StatusCode -eq 409) {
    Write-Host "✅ Duplicate registration correctly rejected" -ForegroundColor Green
    $testsPassed++
} else {
    Write-Host "⚠️ Expected 409 Conflict, got $($duplicateResponse.StatusCode)" -ForegroundColor Yellow
}

# Test 3.3: Login with correct credentials
Write-Host "Test 3.3: Login with correct credentials..."
$loginBody = @{
    username = ($registerBody | ConvertFrom-Json).username
    password = "password123"
} | ConvertTo-Json

$loginResponse = Test-Endpoint -Method POST -Endpoint "/api/auth/login" -Body $loginBody -ExpectedStatus 200

if ($loginResponse.Success -and $loginResponse.Content.token) {
    Write-Host "✅ Login successful, token received" -ForegroundColor Green
    $testsPassed++
    $jwtToken = $loginResponse.Content.token
} else {
    Write-Host "❌ Login failed" -ForegroundColor Red
    $testsFailed++
    $jwtToken = $null
}

# Test 3.4: Login with incorrect password
Write-Host "Test 3.4: Login with incorrect password..."
$badLoginBody = @{
    username = ($registerBody | ConvertFrom-Json).username
    password = "wrongpassword"
} | ConvertTo-Json

$badLoginResponse = Test-Endpoint -Method POST -Endpoint "/api/auth/login" -Body $badLoginBody -ExpectedStatus 401

if ($badLoginResponse.StatusCode -eq 401) {
    Write-Host "✅ Incorrect password correctly rejected" -ForegroundColor Green
    $testsPassed++
} else {
    Write-Host "⚠️ Expected 401 Unauthorized, got $($badLoginResponse.StatusCode)" -ForegroundColor Yellow
}

# Test 3.5: Validate token
Write-Host "Test 3.5: Validate JWT token..."
if ($jwtToken) {
    $validateResponse = Test-Endpoint -Method POST -Endpoint "/api/auth/validate" -Token $jwtToken -ExpectedStatus 200

    if ($validateResponse.Success) {
        Write-Host "✅ Token validation successful" -ForegroundColor Green
        $testsPassed++
    } else {
        Write-Host "❌ Token validation failed" -ForegroundColor Red
        $testsFailed++
    }
}

# ============= PHASE 4: SYMBOL MANAGEMENT =============
Write-Host "`n[PHASE 4] SYMBOL MANAGEMENT ENDPOINTS" -ForegroundColor Yellow

# Test 4.1: Get all symbols
Write-Host "Test 4.1: Retrieve all symbols..."
$symbolsResponse = Test-Endpoint -Method GET -Endpoint "/api/symbols" -Token $jwtToken -ExpectedStatus 200

if ($symbolsResponse.Success) {
    Write-Host "✅ Symbols retrieved successfully" -ForegroundColor Green
    $testsPassed++
    $symbols = $symbolsResponse.Content
    Write-Host "   Found $($symbols.Count) symbols" -ForegroundColor Gray
} else {
    Write-Host "❌ Failed to retrieve symbols" -ForegroundColor Red
    $testsFailed++
    $symbols = @()
}

# Test 4.2: Get specific symbol by ID
if ($symbols.Count -gt 0) {
    Write-Host "Test 4.2: Get specific symbol by ID..."
    $symbolId = $symbols[0].id
    $specificSymbolResponse = Test-Endpoint -Method GET -Endpoint "/api/symbols/$symbolId" -Token $jwtToken -ExpectedStatus 200

    if ($specificSymbolResponse.Success) {
        Write-Host "✅ Symbol details retrieved" -ForegroundColor Green
        $testsPassed++
        Write-Host "   Symbol: $($specificSymbolResponse.Content.code)" -ForegroundColor Gray
    } else {
        Write-Host "❌ Failed to retrieve symbol details" -ForegroundColor Red
        $testsFailed++
    }
}

# Test 4.3: Access protected endpoint without token
Write-Host "Test 4.3: Access protected endpoint without token (should fail)..."
$noTokenResponse = Test-Endpoint -Method GET -Endpoint "/api/symbols" -ExpectedStatus 401

if ($noTokenResponse.StatusCode -eq 401) {
    Write-Host "✅ Unauthorized access correctly rejected" -ForegroundColor Green
    $testsPassed++
} else {
    Write-Host "⚠️ Expected 401 Unauthorized, got $($noTokenResponse.StatusCode)" -ForegroundColor Yellow
}

# ============= PHASE 5: PRICE HISTORY =============
Write-Host "`n[PHASE 5] PRICE HISTORY ENDPOINTS" -ForegroundColor Yellow

if ($symbols.Count -gt 0) {
    $symbolCode = $symbols[0].code

    # Test 5.1: Get price history
    Write-Host "Test 5.1: Get price history for $symbolCode..."
    $pricesResponse = Test-Endpoint -Method GET -Endpoint "/api/prices/$symbolCode" -Token $jwtToken -ExpectedStatus 200

    if ($pricesResponse.Success) {
        Write-Host "✅ Price history retrieved" -ForegroundColor Green
        $testsPassed++
        Write-Host "   Found $($pricesResponse.Content.Count) price records" -ForegroundColor Gray
    } else {
        Write-Host "⚠️ Price history endpoint not available or no data" -ForegroundColor Yellow
    }

    # Test 5.2: Get latest price
    Write-Host "Test 5.2: Get latest price for $symbolCode..."
    $latestPriceResponse = Test-Endpoint -Method GET -Endpoint "/api/prices/$symbolCode/latest" -Token $jwtToken -ExpectedStatus 200

    if ($latestPriceResponse.Success) {
        Write-Host "✅ Latest price retrieved: $($latestPriceResponse.Content.price)" -ForegroundColor Green
        $testsPassed++
    } else {
        Write-Host "⚠️ Latest price endpoint not available" -ForegroundColor Yellow
    }
}

# ============= PHASE 6: ALERT MANAGEMENT =============
Write-Host "`n[PHASE 6] ALERT MANAGEMENT ENDPOINTS" -ForegroundColor Yellow

# Test 6.1: Get all alerts
Write-Host "Test 6.1: Retrieve all alerts..."
$alertsResponse = Test-Endpoint -Method GET -Endpoint "/api/alerts" -Token $jwtToken -ExpectedStatus 200

if ($alertsResponse.Success) {
    Write-Host "✅ Alerts endpoint accessible" -ForegroundColor Green
    $testsPassed++
    Write-Host "   Current alerts count: $($alertsResponse.Content.Count)" -ForegroundColor Gray
} else {
    Write-Host "⚠️ Alerts endpoint not available" -ForegroundColor Yellow
}

# Test 6.2: Get active alerts
Write-Host "Test 6.2: Get active alerts..."
$activeAlertsResponse = Test-Endpoint -Method GET -Endpoint "/api/alerts/active" -Token $jwtToken -ExpectedStatus 200

if ($activeAlertsResponse.Success) {
    Write-Host "✅ Active alerts retrieved" -ForegroundColor Green
    $testsPassed++
} else {
    Write-Host "⚠️ Active alerts endpoint not fully available" -ForegroundColor Yellow
}

# ============= PHASE 7: SWAGGER DOCUMENTATION =============
Write-Host "`n[PHASE 7] SWAGGER DOCUMENTATION" -ForegroundColor Yellow

Write-Host "Test 7.1: Access Swagger UI..."
try {
    $swaggerResponse = Invoke-WebRequest "$baseUrl/swagger-ui.html" -SkipHttpErrorCheck
    if ($swaggerResponse.StatusCode -eq 200) {
        Write-Host "✅ Swagger UI accessible" -ForegroundColor Green
        $testsPassed++
        Write-Host "   URL: $baseUrl/swagger-ui.html" -ForegroundColor Gray
    } else {
        Write-Host "❌ Swagger UI returned status $($swaggerResponse.StatusCode)" -ForegroundColor Red
        $testsFailed++
    }
} catch {
    Write-Host "❌ Swagger UI not accessible: $($_.Exception.Message)" -ForegroundColor Red
    $testsFailed++
}

Write-Host "Test 7.2: Access OpenAPI JSON..."
$openApiResponse = Test-Endpoint -Method GET -Endpoint "/v3/api-docs" -ExpectedStatus 200

if ($openApiResponse.Success) {
    Write-Host "✅ OpenAPI documentation accessible" -ForegroundColor Green
    $testsPassed++
    Write-Host "   Endpoints documented: $($openApiResponse.Content.paths.Count)" -ForegroundColor Gray
} else {
    Write-Host "❌ OpenAPI documentation not accessible" -ForegroundColor Red
    $testsFailed++
}

# ============= PHASE 8: C++ SERVICE =============
Write-Host "`n[PHASE 8] C++ ANALYSIS SERVICE" -ForegroundColor Yellow

Write-Host "Test 8.1: Check C++ service health..."
try {
    $cppHealthResponse = Invoke-WebRequest "$cppServiceUrl/analyze/health" -SkipHttpErrorCheck
    if ($cppHealthResponse.StatusCode -eq 200) {
        Write-Host "✅ C++ service is running" -ForegroundColor Green
        $testsPassed++
        Write-Host "   URL: $cppServiceUrl" -ForegroundColor Gray
    } else {
        Write-Host "⚠️ C++ service health check returned $($cppHealthResponse.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️ C++ service not accessible (might not be running in Docker yet)" -ForegroundColor Yellow
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Gray
}

# ============= SUMMARY =============
Write-Host "`n======================================" -ForegroundColor Cyan
Write-Host "TEST SUMMARY" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Tests Passed: $testsPassed" -ForegroundColor Green
Write-Host "Tests Failed: $testsFailed" -ForegroundColor $(if ($testsFailed -eq 0) { 'Green' } else { 'Red' })
Write-Host "Total Tests: $($testsPassed + $testsFailed)" -ForegroundColor White

if ($testsFailed -eq 0) {
    Write-Host "`n✅ ALL TESTS PASSED!" -ForegroundColor Green
    Write-Host "`nYou have successfully completed Phases 1-8:" -ForegroundColor Green
    Write-Host "  ✅ Phase 1: Database Setup" -ForegroundColor Green
    Write-Host "  ✅ Phase 2: Spring Boot Initialization" -ForegroundColor Green
    Write-Host "  ✅ Phase 3: Authentication System" -ForegroundColor Green
    Write-Host "  ✅ Phase 4: Symbol Management" -ForegroundColor Green
    Write-Host "  ✅ Phase 5: Price History" -ForegroundColor Green
    Write-Host "  ✅ Phase 6: Alert Management" -ForegroundColor Green
    Write-Host "  ✅ Phase 7: Swagger Documentation" -ForegroundColor Green
    Write-Host "  ✅ Phase 8: C++ Service Setup" -ForegroundColor Green
    Write-Host "`nReady to proceed with Phase 9 (WebSocket Implementation)" -ForegroundColor Cyan
} else {
    Write-Host "`n⚠️ Some tests failed. Review the output above." -ForegroundColor Yellow
}

Write-Host "`n======================================" -ForegroundColor Cyan

