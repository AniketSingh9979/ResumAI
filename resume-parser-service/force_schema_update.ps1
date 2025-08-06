# Force database schema update script
Write-Host "Forcing database schema update..." -ForegroundColor Green

# Step 1: Stop the application if running
Write-Host "`n1. Please stop your Spring Boot application first" -ForegroundColor Yellow
Write-Host "Press any key after stopping the application..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Step 2: Delete existing database files
Write-Host "`n2. Deleting existing database files..." -ForegroundColor Yellow
$dbFiles = @(
    "resume_parser.db",
    "resume_parser.db-journal", 
    "resume_parser.db-wal",
    "resume_parser.db-shm"
)

foreach ($file in $dbFiles) {
    if (Test-Path $file) {
        Remove-Item $file -Force
        Write-Host "✅ Deleted: $file" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Not found: $file" -ForegroundColor Yellow
    }
}

# Step 3: Temporarily change DDL setting for clean creation
Write-Host "`n3. Updating application.properties for clean schema creation..." -ForegroundColor Yellow

$propsFile = "src/main/resources/application.properties"
if (Test-Path $propsFile) {
    # Backup original
    Copy-Item $propsFile "$propsFile.backup"
    
    # Read and modify
    $content = Get-Content $propsFile
    $newContent = $content -replace "spring.jpa.hibernate.ddl-auto=update", "spring.jpa.hibernate.ddl-auto=create-drop"
    Set-Content $propsFile $newContent
    
    Write-Host "✅ Modified application.properties (backup created)" -ForegroundColor Green
    Write-Host "   Changed: ddl-auto=update → ddl-auto=create-drop" -ForegroundColor Cyan
} else {
    Write-Host "❌ application.properties not found!" -ForegroundColor Red
    exit 1
}

Write-Host "`n4. Ready for restart!" -ForegroundColor Green
Write-Host "Now:" -ForegroundColor Cyan
Write-Host "  1. Start your Spring Boot application" -ForegroundColor White
Write-Host "  2. Wait for it to fully start up" -ForegroundColor White
Write-Host "  3. Test uploading a job description" -ForegroundColor White
Write-Host "  4. Run this script again with 'restore' parameter to restore settings" -ForegroundColor White

Write-Host "`nTo restore original settings later, run:" -ForegroundColor Yellow
Write-Host "  .\force_schema_update.ps1 restore" -ForegroundColor Yellow 