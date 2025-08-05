@echo off
echo ====================================
echo 🔍 RESUME PARSER SERVICE QUICK TEST
echo ====================================
echo.

echo ⏳ Testing Application Health...
curl -s http://localhost:8081/actuator/health
echo.
echo.

echo 🗂️ Testing Database Tables...
curl -s http://localhost:8081/api/database/tables
echo.
echo.

echo 📊 Testing Database Schema...
curl -s http://localhost:8081/api/database/tables/parsed_resumes/schema
echo.
echo.

echo 📄 Testing Resume Upload and Parse (PDF/DOC only)...
if exist test_resume.doc (
    curl -X POST http://localhost:8081/api/parseTextOnly -F "file=@test_resume.doc"
) else (
    echo ⚠️ test_resume.doc not found. Run setup_test_files.ps1 first.
)
echo.
echo.

echo 💼 Creating Test Job Description...
curl -X POST http://localhost:8081/api/matching/jobs -H "Content-Type: application/json" -d "{\"title\":\"Java Developer\",\"company\":\"TestCorp\",\"description\":\"Java developer position\",\"requirements\":\"Java, Spring Boot, SQL\"}"
echo.
echo.

echo 📈 Testing Resume Statistics...
curl -s http://localhost:8081/api/resumes/statistics
echo.
echo.

echo ✅ Testing Complete!
echo.
echo 🌐 Open these URLs in your browser:
echo - Application Health: http://localhost:8081/actuator/health
echo - Database Tables: http://localhost:8081/api/database/tables
echo - Resume Statistics: http://localhost:8081/api/resumes/statistics
echo.
pause 