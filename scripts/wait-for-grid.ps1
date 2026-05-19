# Waits until Selenium Grid hub is ready (used by Jenkins pipeline)
param(
    [string]$GridUrl = "http://localhost:4444/wd/hub/status",
    [int]$MaxAttempts = 30,
    [int]$SleepSeconds = 5
)

Write-Host "Waiting for Selenium Grid at $GridUrl ..." -ForegroundColor Cyan

for ($i = 1; $i -le $MaxAttempts; $i++) {
    try {
        $response = Invoke-RestMethod -Uri $GridUrl -Method Get -TimeoutSec 10
        if ($response.value.ready -eq $true) {
            Write-Host "Selenium Grid is READY (attempt $i)" -ForegroundColor Green
            exit 0
        }
        Write-Host "Grid not ready yet (attempt $i/$MaxAttempts)..." -ForegroundColor Yellow
    } catch {
        Write-Host "Grid not reachable (attempt $i/$MaxAttempts): $_" -ForegroundColor Yellow
    }
    Start-Sleep -Seconds $SleepSeconds
}

Write-Host "Selenium Grid did not become ready in time." -ForegroundColor Red
exit 1
