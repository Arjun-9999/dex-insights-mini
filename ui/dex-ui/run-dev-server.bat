@echo off
REM Find Node.js installation - checking D:\Program Files\angular-projects first
if exist "D:\Program Files\angular-projects\node.exe" (
    set NODE_PATH=D:\Program Files\angular-projects
    goto found
)
if exist "D:\Program Files\nodejs\node.exe" (
    set NODE_PATH=D:\Program Files\nodejs
    goto found
)
if exist "D:\nodejs\node.exe" (
    set NODE_PATH=D:\nodejs
    goto found
)
if exist "C:\Program Files\nodejs\node.exe" (
    set NODE_PATH=C:\Program Files\nodejs
    goto found
)
if exist "C:\Program Files (x86)\nodejs\node.exe" (
    set NODE_PATH=C:\Program Files (x86)\nodejs
    goto found
)
if exist "%APPDATA%\Local\Programs\nodejs\node.exe" (
    set NODE_PATH=%APPDATA%\Local\Programs\nodejs
    goto found
)

echo ERROR: Node.js not found. Please install Node.js v20+ from https://nodejs.org/
echo Or manually add Node.js to your PATH and try again.
pause
exit /b 1

:found
echo Found Node.js at: %NODE_PATH%
set PATH=%NODE_PATH%;%PATH%
node --version
echo Starting Angular dev server...
cd /d D:\dex-insights-mini\ui\dex-ui
npm start -- --proxy-config proxy.conf.json
pause

