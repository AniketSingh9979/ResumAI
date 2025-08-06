# Restore original application.properties settings
Write-Host "Restoring original application.properties..." -ForegroundColor Green

$propsFile = "src/main/resources/application.properties"
$backupFile = "$propsFile.backup"

if (Test-Path $backupFile) {
    # Restore from backup
    Copy-Item $backupFile $propsFile -Force
    Remove-Item $backupFile -Force
    
    Write-Host "✅ Original settings restored!" -ForegroundColor Green
    Write-Host "   Changed back: ddl-auto=create-drop → ddl-auto=update" -ForegroundColor Cyan
    Write-Host "   Backup file removed" -ForegroundColor Cyan
} else {
    Write-Host "❌ Backup file not found!" -ForegroundColor Red
    Write-Host "Manually change 'create-drop' back to 'update' in application.properties" -ForegroundColor Yellow
}

Write-Host "`n✅ Setup complete!" -ForegroundColor Green
Write-Host "Your application is now using the stable 'update' mode." -ForegroundColor Cyan 