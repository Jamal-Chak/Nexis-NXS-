# Nexis Node Manager (PowerShell)
# Windows-friendly script for managing Nexis nodes

param(
    [Parameter(Position=0)]
    [string]$Command = "help"
)

$NEXIS_JAR = "target\nexis-core-0.1.0-SNAPSHOT.jar"
$NEXIS_MAIN = "com.nexis.app.NexisNode"
$LOG_DIR = "logs"
$PID_FILE = "nexis.pid"

function Print-Banner {
    Write-Host "╔═══════════════════════════════════════╗" -ForegroundColor Blue
    Write-Host "║        NEXIS BLOCKCHAIN NODE          ║" -ForegroundColor Blue
    Write-Host "║         Management Script             ║" -ForegroundColor Blue
    Write-Host "╚═══════════════════════════════════════╝" -ForegroundColor Blue
    Write-Host ""
}

function Check-Jar {
    if (-not (Test-Path $NEXIS_JAR)) {
        Write-Host "Error: JAR file not found at $NEXIS_JAR" -ForegroundColor Red
        Write-Host "Please run: mvn clean package"
        exit 1
    }
}

function Start-Node {
    Print-Banner
    Check-Jar
    
    Write-Host "Starting Nexis node..." -ForegroundColor Green
    
    # Create logs directory
    if (-not (Test-Path $LOG_DIR)) {
        New-Item -ItemType Directory -Path $LOG_DIR | Out-Null
    }
    
    # Start node
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    java -Xmx4G -Xms2G `
        -XX:+UseG1GC `
        -XX:MaxGCPauseMillis=200 `
        -Dfile.encoding=UTF-8 `
        -cp $NEXIS_JAR $NEXIS_MAIN 2>&1 | Tee-Object -FilePath "$LOG_DIR\nexis-$timestamp.log"
}

function Start-Background {
    Print-Banner
    Check-Jar
    
    if (Test-Path $PID_FILE) {
        $PID = Get-Content $PID_FILE
        $process = Get-Process -Id $PID -ErrorAction SilentlyContinue
        if ($process) {
            Write-Host "Node already running with PID $PID" -ForegroundColor Yellow
            exit 1
        }
    }
    
    Write-Host "Starting Nexis node in background..." -ForegroundColor Green
    
    if (-not (Test-Path $LOG_DIR)) {
        New-Item -ItemType Directory -Path $LOG_DIR | Out-Null
    }
    
    $process = Start-Process java -ArgumentList `
        "-Xmx4G", "-Xms2G", `
        "-XX:+UseG1GC", `
        "-XX:MaxGCPauseMillis=200", `
        "-Dfile.encoding=UTF-8", `
        "-cp", $NEXIS_JAR, $NEXIS_MAIN `
        -NoNewWindow `
        -PassThru `
        -RedirectStandardOutput "$LOG_DIR\nexis.log" `
        -RedirectStandardError "$LOG_DIR\nexis-error.log"
    
    $process.Id | Out-File -FilePath $PID_FILE
    
    Write-Host "Node started with PID $($process.Id)" -ForegroundColor Green
    Write-Host "Dashboard: http://localhost:8000" -ForegroundColor Blue
    Write-Host "View logs: Get-Content $LOG_DIR\nexis.log -Wait"
}

function Stop-Node {
    if (-not (Test-Path $PID_FILE)) {
        Write-Host "No PID file found. Node may not be running." -ForegroundColor Red
        exit 1
    }
    
    $PID = Get-Content $PID_FILE
    $process = Get-Process -Id $PID -ErrorAction SilentlyContinue
    
    if ($process) {
        Write-Host "Stopping Nexis node (PID $PID)..." -ForegroundColor Yellow
        Stop-Process -Id $PID -Force
        Remove-Item $PID_FILE
        Write-Host "Node stopped successfully" -ForegroundColor Green
    } else {
        Write-Host "Process $PID not found" -ForegroundColor Red
        Remove-Item $PID_FILE
    }
}

function Get-Status {
    if (Test-Path $PID_FILE) {
        $PID = Get-Content $PID_FILE
        $process = Get-Process -Id $PID -ErrorAction SilentlyContinue
        
        if ($process) {
            Write-Host "✓ Node is RUNNING (PID $PID)" -ForegroundColor Green
            Write-Host "Dashboard: http://localhost:8000" -ForegroundColor Blue
            
            # Check if API is responsive
            try {
                $response = Invoke-WebRequest -Uri "http://localhost:8000/api/stats" -TimeoutSec 2 -UseBasicParsing
                Write-Host "✓ API is RESPONSIVE" -ForegroundColor Green
            } catch {
                Write-Host "⚠ API not responding yet" -ForegroundColor Yellow
            }
        } else {
            Write-Host "✗ Node is NOT RUNNING (stale PID file)" -ForegroundColor Red
            Remove-Item $PID_FILE
        }
    } else {
        Write-Host "✗ Node is NOT RUNNING" -ForegroundColor Red
    }
}

function Show-Logs {
    if (Test-Path "$LOG_DIR\nexis.log") {
        Get-Content "$LOG_DIR\nexis.log" -Wait
    } else {
        Write-Host "No log file found" -ForegroundColor Red
        exit 1
    }
}

function Build-Node {
    Print-Banner
    Write-Host "Building Nexis..." -ForegroundColor Green
    mvn clean package -DskipTests
    Write-Host "Build complete!" -ForegroundColor Green
}

function Test-Node {
    Print-Banner
    Write-Host "Running tests..." -ForegroundColor Green
    mvn test
}

function Show-Help {
    Print-Banner
    Write-Host "Usage: .\nexis-node.ps1 <command>"
    Write-Host ""
    Write-Host "Commands:"
    Write-Host "  start       - Start node in foreground (interactive)"
    Write-Host "  background  - Start node in background"
    Write-Host "  stop        - Stop background node"
    Write-Host "  status      - Check node status"
    Write-Host "  logs        - Tail log file"
    Write-Host "  build       - Build the project"
    Write-Host "  test        - Run tests"
    Write-Host "  restart     - Restart background node"
    Write-Host "  help        - Show this help"
    Write-Host ""
}

switch ($Command.ToLower()) {
    "start" { Start-Node }
    "background" { Start-Background }
    "daemon" { Start-Background }
    "stop" { Stop-Node }
    "status" { Get-Status }
    "logs" { Show-Logs }
    "build" { Build-Node }
    "test" { Test-Node }
    "restart" {
        Stop-Node
        Start-Sleep -Seconds 2
        Start-Background
    }
    "help" { Show-Help }
    default { Show-Help }
}
