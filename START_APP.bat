@echo off
REM DEX INSIGHTS MINI - ONE-CLICK STARTUP

echo.
echo ========================================
echo DEX INSIGHTS MINI - Application Startup
echo ========================================
echo.

echo [1/3] Starting Backend Server (Port 8080)...
cd /d D:\dex-insights-mini
start "Backend - DEX Insights Mini" mvn spring-boot:run
timeout /t 15 /nobreak

echo.
echo [2/3] Installing Frontend Dependencies...
cd /d D:\dex-insights-mini\ui\dex-ui
call npm install --silent
echo.

echo [3/3] Starting Frontend Server (Port 4200)...
start "Frontend - DEX Insights Mini" npm start

timeout /t 10 /nobreak

echo.
echo ========================================
echo SUCCESS! Opening Browser...
echo ========================================
echo.
echo Backend: http://localhost:8080
echo Frontend: http://localhost:4200
echo.

start http://localhost:4200

echo.
echo Press any key to close this window...
pause

