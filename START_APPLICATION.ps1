$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "DEX INSIGHTS MINI - Application Startup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check prerequisites
Write-Host "[1/5] Checking prerequisites..." -ForegroundColor Yellow

$javaCheck = java -version 2>&1 | Select-String "version"
if ($javaCheck) {
    Write-Host "✓ Java is installed" -ForegroundColor Green
} else {
    Write-Host "✗ Java is NOT installed" -ForegroundColor Red
    exit 1
}

$nodeCheck = node --version 2>&1
if ($nodeCheck) {
    Write-Host "✓ Node.js is installed: $nodeCheck" -ForegroundColor Green
} else {
    Write-Host "✗ Node.js is NOT installed" -ForegroundColor Red
    exit 1
}

# Build backend
Write-Host ""
Write-Host "[2/5] Building backend..." -ForegroundColor Yellow
cd D:\dex-insights-mini
mvn clean install -DskipTests -q
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Backend built successfully" -ForegroundColor Green
} else {
    Write-Host "✗ Backend build failed" -ForegroundColor Red
    exit 1
}

# Install frontend dependencies
Write-Host ""
Write-Host "[3/5] Installing frontend dependencies..." -ForegroundColor Yellow
cd D:\dex-insights-mini\ui\dex-ui
npm install --silent
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Frontend dependencies installed" -ForegroundColor Green
} else {
    Write-Host "✗ Frontend dependencies installation failed" -ForegroundColor Red
    exit 1
}

# Start backend
Write-Host ""
Write-Host "[4/5] Starting backend server..." -ForegroundColor Yellow
cd D:\dex-insights-mini
Start-Process -WindowStyle Minimized -FilePath cmd.exe -ArgumentList "/c mvn spring-boot:run" -PassThru
Start-Sleep -Seconds 10
Write-Host "✓ Backend started on http://localhost:8080" -ForegroundColor Green

# Start frontend
Write-Host ""
Write-Host "[5/5] Starting frontend server..." -ForegroundColor Yellow
cd D:\dex-insights-mini\ui\dex-ui
Start-Process -WindowStyle Minimized -FilePath cmd.exe -ArgumentList "/c npm start -- --proxy-config proxy.conf.json" -PassThru
Start-Sleep -Seconds 10
Write-Host "✓ Frontend started on http://localhost:4200" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✓ APPLICATION STARTED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Frontend URL: http://localhost:4200" -ForegroundColor Yellow
Write-Host "Backend URL:  http://localhost:8080" -ForegroundColor Yellow
Write-Host ""
Write-Host "Press Enter to keep the windows open..." -ForegroundColor Cyan
Read-Host

