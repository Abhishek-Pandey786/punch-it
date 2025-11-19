@echo off
REM Windows batch script to run Attendance Manager

echo ====================================
echo   Attendance Manager - Launcher
echo ====================================
echo.

REM Check if Maven is installed
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo [1/3] Checking Java version...
java -version
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17+ from: https://adoptium.net/
    pause
    exit /b 1
)

echo.
echo [2/3] Building project...
call mvn clean compile
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo [3/3] Launching Attendance Manager...
echo.
call mvn javafx:run

pause
