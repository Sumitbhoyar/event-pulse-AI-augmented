# EventPulse Docker Management Script
# PowerShell script to manage Docker deployment

param(
    [Parameter(Position=0)]
    [ValidateSet('start', 'stop', 'restart', 'logs', 'status', 'clean', 'rebuild', 'test', 'backup', 'help')]
    [string]$Action = 'help'
)

function Show-Help {
    Write-Host "EventPulse Docker Management Script" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Usage: .\docker-manage.ps1 [action]" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Actions:" -ForegroundColor Green
    Write-Host "  start    - Start all services" -ForegroundColor White
    Write-Host "  stop     - Stop all services" -ForegroundColor White
    Write-Host "  restart  - Restart all services" -ForegroundColor White
    Write-Host "  rebuild  - Rebuild and restart all services" -ForegroundColor White
    Write-Host "  logs     - View logs from all services" -ForegroundColor White
    Write-Host "  status   - Show status of all services" -ForegroundColor White
    Write-Host "  test     - Test the application endpoints" -ForegroundColor White
    Write-Host "  backup   - Backup PostgreSQL database" -ForegroundColor White
    Write-Host "  clean    - Stop and remove all containers, networks, and volumes" -ForegroundColor White
    Write-Host "  help     - Show this help message" -ForegroundColor White
    Write-Host ""
}

function Start-Services {
    Write-Host "Starting EventPulse services..." -ForegroundColor Green
    docker compose up -d --build
    Write-Host ""
    Write-Host "Services started successfully!" -ForegroundColor Green
    Write-Host "Backend: http://localhost:8080" -ForegroundColor Cyan
    Write-Host "Database: localhost:5432" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Use '.\docker-manage.ps1 status' to check service health" -ForegroundColor Yellow
}

function Stop-Services {
    Write-Host "Stopping EventPulse services..." -ForegroundColor Yellow
    docker compose stop
    Write-Host "Services stopped successfully!" -ForegroundColor Green
}

function Restart-Services {
    Write-Host "Restarting EventPulse services..." -ForegroundColor Yellow
    docker compose restart
    Write-Host "Services restarted successfully!" -ForegroundColor Green
}

function Rebuild-Services {
    Write-Host "Rebuilding and restarting EventPulse services..." -ForegroundColor Yellow
    docker compose down
    docker compose up -d --build
    Write-Host "Services rebuilt and restarted successfully!" -ForegroundColor Green
}

function Show-Logs {
    Write-Host "Showing logs (Ctrl+C to exit)..." -ForegroundColor Cyan
    docker compose logs -f
}

function Show-Status {
    Write-Host "EventPulse Service Status:" -ForegroundColor Cyan
    Write-Host ""
    docker compose ps
    Write-Host ""
    
    Write-Host "Health Status:" -ForegroundColor Cyan
    $backend = docker inspect eventpulse-backend --format='{{.State.Health.Status}}' 2>$null
    $postgres = docker inspect eventpulse-postgres --format='{{.State.Health.Status}}' 2>$null
    
    if ($backend) {
        Write-Host "Backend: $backend" -ForegroundColor $(if ($backend -eq 'healthy') { 'Green' } else { 'Red' })
    }
    if ($postgres) {
        Write-Host "PostgreSQL: $postgres" -ForegroundColor $(if ($postgres -eq 'healthy') { 'Green' } else { 'Red' })
    }
}

function Test-Application {
    Write-Host "Testing EventPulse application..." -ForegroundColor Cyan
    Write-Host ""
    
    # Test health endpoint
    Write-Host "1. Testing health endpoint..." -ForegroundColor Yellow
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:8080/api/health" -TimeoutSec 5
        Write-Host "   ✓ Health check passed: $($health.status)" -ForegroundColor Green
    } catch {
        Write-Host "   ✗ Health check failed: $($_.Exception.Message)" -ForegroundColor Red
        return
    }
    
    # Test login
    Write-Host "2. Testing authentication..." -ForegroundColor Yellow
    try {
        $loginBody = @{
            username = "testuser"
            password = "testpass"
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Method POST `
            -Uri "http://localhost:8080/api/auth/login" `
            -Headers @{ "Content-Type" = "application/json" } `
            -Body $loginBody `
            -TimeoutSec 5
        
        $token = $response.token
        Write-Host "   ✓ Authentication successful" -ForegroundColor Green
    } catch {
        Write-Host "   ✗ Authentication failed: $($_.Exception.Message)" -ForegroundColor Red
        return
    }
    
    # Test creating an event
    Write-Host "3. Testing event creation..." -ForegroundColor Yellow
    try {
        $eventBody = @{
            source = "docker-test"
            type = "TEST"
            message = "Docker management script test"
            timestamp = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
        } | ConvertTo-Json
        
        $event = Invoke-RestMethod -Method POST `
            -Uri "http://localhost:8080/api/events" `
            -Headers @{ 
                "Authorization" = "Bearer $token"
                "Content-Type" = "application/json" 
            } `
            -Body $eventBody `
            -TimeoutSec 5
        
        Write-Host "   ✓ Event created successfully (ID: $($event.id))" -ForegroundColor Green
    } catch {
        Write-Host "   ✗ Event creation failed: $($_.Exception.Message)" -ForegroundColor Red
        return
    }
    
    # Test metrics
    Write-Host "4. Testing metrics endpoint..." -ForegroundColor Yellow
    try {
        $metrics = Invoke-RestMethod -Uri "http://localhost:8080/api/metrics" `
            -Headers @{ "Authorization" = "Bearer $token" } `
            -TimeoutSec 5
        
        Write-Host "   ✓ Metrics retrieved (Total events: $($metrics.totalCount))" -ForegroundColor Green
    } catch {
        Write-Host "   ✗ Metrics failed: $($_.Exception.Message)" -ForegroundColor Red
        return
    }
    
    Write-Host ""
    Write-Host "All tests passed successfully! ✓" -ForegroundColor Green
}

function Backup-Database {
    Write-Host "Backing up PostgreSQL database..." -ForegroundColor Cyan
    $backupFile = "backup-$(Get-Date -Format 'yyyyMMdd-HHmmss').sql"
    
    docker compose exec -T postgres pg_dump -U eventpulse eventpulse > $backupFile
    
    if (Test-Path $backupFile) {
        $size = (Get-Item $backupFile).Length / 1KB
        Write-Host "Backup completed successfully!" -ForegroundColor Green
        Write-Host "File: $backupFile ($([math]::Round($size, 2)) KB)" -ForegroundColor Cyan
    } else {
        Write-Host "Backup failed!" -ForegroundColor Red
    }
}

function Clean-Services {
    Write-Host "WARNING: This will remove all containers, networks, and volumes!" -ForegroundColor Red
    $confirm = Read-Host "Are you sure? (yes/no)"
    
    if ($confirm -eq 'yes') {
        Write-Host "Cleaning up EventPulse services..." -ForegroundColor Yellow
        docker compose down -v
        Write-Host "Cleanup completed successfully!" -ForegroundColor Green
    } else {
        Write-Host "Cleanup cancelled" -ForegroundColor Yellow
    }
}

# Main script execution
switch ($Action) {
    'start' { Start-Services }
    'stop' { Stop-Services }
    'restart' { Restart-Services }
    'rebuild' { Rebuild-Services }
    'logs' { Show-Logs }
    'status' { Show-Status }
    'test' { Test-Application }
    'backup' { Backup-Database }
    'clean' { Clean-Services }
    'help' { Show-Help }
    default { Show-Help }
}

