# PowerShell script to test the send-test-link endpoint
$baseUrl = "http://localhost:8082/mail-service/api/mail"

# Test data
$testData = @{
    candidate = @{
        name = "John Doe"
        email = "john.doe@example.com"
    }
    testLink = "http://localhost:4200/interview-test?token=abc123xyz"
} | ConvertTo-Json -Depth 3

Write-Host "Testing send-test-link endpoint..." -ForegroundColor Green
Write-Host "URL: $baseUrl/send-test-link" -ForegroundColor Cyan
Write-Host "Request Body:" -ForegroundColor Yellow
Write-Host $testData -ForegroundColor Gray
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/send-test-link" -Method Post -Body $testData -ContentType "application/json"
    Write-Host "✅ SUCCESS: $response" -ForegroundColor Green
}
catch {
    Write-Host "❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response: $responseBody" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Test completed!" -ForegroundColor Green 