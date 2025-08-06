#!/usr/bin/env pwsh

# Simple API test script
Write-Host "Testing Resume Parser Service API..." -ForegroundColor Cyan

$baseUrl = "http://localhost:8081"

# Test 1: Basic connectivity
Write-Host "`n1. Testing basic connectivity..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri $baseUrl -Method GET -TimeoutSec 10
    Write-Host "Server is responding (Status: $($response.StatusCode))" -ForegroundColor Green
} catch {
    Write-Host "Server connectivity failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 2: Test a simpler endpoint first
Write-Host "`n2. Testing panel members endpoint..." -ForegroundColor Yellow
try {
    $panelResponse = Invoke-RestMethod -Uri "$baseUrl/api/matching/panel-members" -Method GET
    Write-Host "Panel Members API Response:" -ForegroundColor Green
    $panelResponse | ConvertTo-Json -Depth 2
} catch {
    Write-Host "Panel Members API failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseText = $reader.ReadToEnd()
        Write-Host "Response Body: $responseText" -ForegroundColor Yellow
    }
}

# Test 3: Test the problematic jobs endpoint with error details
Write-Host "`n3. Testing jobs endpoint with detailed error info..." -ForegroundColor Yellow
try {
    $jobsResponse = Invoke-RestMethod -Uri "$baseUrl/api/matching/jobs" -Method GET
    Write-Host "Jobs API Success!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Cyan
    $jobsResponse | ConvertTo-Json -Depth 3
} catch {
    Write-Host "Jobs API failed: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        Write-Host "HTTP Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
        
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errorBody = $reader.ReadToEnd()
            Write-Host "Error Response Body:" -ForegroundColor Yellow
            Write-Host $errorBody -ForegroundColor Gray
        } catch {
            Write-Host "Could not read error response body" -ForegroundColor Red
        }
    }
}

Write-Host "`nTest completed!" -ForegroundColor Green 