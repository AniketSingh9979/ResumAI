#!/usr/bin/env pwsh

# Test various endpoints to find the correct ones
Write-Host "Testing Resume Parser Service Endpoints..." -ForegroundColor Cyan

$baseUrl = "http://localhost:8081"

# List of possible endpoints to test
$endpoints = @(
    "/",
    "/api",
    "/api/matching",
    "/api/matching/jobs",
    "/actuator/health",
    "/actuator",
    "/health"
)

foreach ($endpoint in $endpoints) {
    $url = $baseUrl + $endpoint
    Write-Host "`nTesting: $url" -ForegroundColor Yellow
    
    try {
        $response = Invoke-WebRequest -Uri $url -Method GET -TimeoutSec 5
        Write-Host "SUCCESS - Status: $($response.StatusCode)" -ForegroundColor Green
        
        # Show a preview of the response
        $content = $response.Content
        if ($content.Length -gt 200) {
            $preview = $content.Substring(0, 200) + "..."
        } else {
            $preview = $content
        }
        Write-Host "Response preview: $preview" -ForegroundColor Gray
        
    } catch {
        $statusCode = "Unknown"
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode
        }
        Write-Host "FAILED - Status: $statusCode - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`nEndpoint testing completed!" -ForegroundColor Green 