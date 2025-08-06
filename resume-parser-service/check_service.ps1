#!/usr/bin/env pwsh

Write-Host "Checking Resume Parser Service Status..." -ForegroundColor Cyan

# Check if Java processes are running
Write-Host "`n1. Checking for Java processes..." -ForegroundColor Yellow
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "Found Java processes running:" -ForegroundColor Green
    $javaProcesses | Select-Object Id, ProcessName, StartTime | Format-Table
} else {
    Write-Host "No Java processes found running" -ForegroundColor Red
}

# Check if port 8081 is in use
Write-Host "`n2. Checking port 8081..." -ForegroundColor Yellow
try {
    $portCheck = netstat -an | Select-String "8081"
    if ($portCheck) {
        Write-Host "Port 8081 is in use:" -ForegroundColor Green
        $portCheck
    } else {
        Write-Host "Port 8081 is not in use" -ForegroundColor Red
    }
} catch {
    Write-Host "Could not check port status" -ForegroundColor Yellow
}

# Try to connect to the service
Write-Host "`n3. Testing service connectivity..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -Method GET -TimeoutSec 3
    Write-Host "✓ Service is responding!" -ForegroundColor Green
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Cyan
    Write-Host "Response: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Service is not responding: $($_.Exception.Message)" -ForegroundColor Red
    
    Write-Host "`n4. Attempting to restart service..." -ForegroundColor Yellow
    Write-Host "Please restart the service manually with:" -ForegroundColor Cyan
    Write-Host "cd ResumAI/resume-parser-service" -ForegroundColor White
    Write-Host "mvn spring-boot:run" -ForegroundColor White
}

Write-Host "`nService check completed!" -ForegroundColor Green 