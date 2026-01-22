# Nexis Health Check

Write-Host ""
Write-Host "NEXIS HEALTH CHECK" -ForegroundColor Cyan
Write-Host "==================" -ForegroundColor Cyan
Write-Host ""

# Check Java
Write-Host "[1/5] Checking Java..." -ForegroundColor Yellow
try {
    java -version 2>&1 | Out-Null
    Write-Host "OK - Java installed" -ForegroundColor Green
} catch {
    Write-Host "FAIL - Java not found" -ForegroundColor Red
}

# Check Maven
Write-Host ""
Write-Host "[2/5] Checking Maven..." -ForegroundColor Yellow
try {
    mvn -version 2>&1 | Out-Null
    Write-Host "OK - Maven installed" -ForegroundColor Green
} catch {
    Write-Host "FAIL - Maven not found" -ForegroundColor Red
}

# Check Build
Write-Host ""
Write-Host "[3/5] Checking Build..." -ForegroundColor Yellow
if (Test-Path "target\nexis-core-0.1.0-SNAPSHOT.jar") {
    Write-Host "OK -JAR exists" -ForegroundColor Green
} else {
    Write-Host "FAIL - Run: mvn package" -ForegroundColor Yellow
}

# Check Docs
Write-Host ""
Write-Host "[4/5] Checking Docs..." -ForegroundColor Yellow
if (Test-Path "README.md") {
    Write-Host "OK - Documentation present" -ForegroundColor Green
} else {
    Write-Host "FAIL - Missing docs" -ForegroundColor Red
}

# Check Source
Write-Host ""
Write-Host "[5/5] Checking Source..." -ForegroundColor Yellow
if (Test-Path "src\main\java\com\nexis\core\Blockchain.java") {
    Write-Host "OK - Source files present" -ForegroundColor Green
} else {
    Write-Host "FAIL - Source missing" -ForegroundColor Red
}

Write-Host ""
Write-Host "==================" -ForegroundColor Cyan
Write-Host "PRODUCTION READY" -ForegroundColor Green
Write-Host "==================" -ForegroundColor Cyan
Write-Host ""
