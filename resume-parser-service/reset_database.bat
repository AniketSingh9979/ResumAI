@echo off
echo Resetting SQLite database to fix schema migration issues...

echo.
echo Stopping any running Java processes (optional)...
REM Uncomment the next line if you want to force stop Java processes
REM taskkill /F /IM java.exe

echo.
echo Deleting existing database files...
if exist "resume_parser.db" (
    del "resume_parser.db"
    echo Deleted resume_parser.db
) else (
    echo resume_parser.db not found
)

if exist "resume_parser.db-journal" (
    del "resume_parser.db-journal"
    echo Deleted resume_parser.db-journal
)

if exist "resume_parser.db-wal" (
    del "resume_parser.db-wal"
    echo Deleted resume_parser.db-wal
)

if exist "resume_parser.db-shm" (
    del "resume_parser.db-shm"
    echo Deleted resume_parser.db-shm
)

echo.
echo Database reset complete. 
echo When you restart the application, Hibernate will create a fresh database with the correct schema.
echo.
pause 