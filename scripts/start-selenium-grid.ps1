# Start Selenium Grid via Docker Compose
$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

Write-Host "Starting Selenium Grid (hub + chrome)..." -ForegroundColor Cyan
docker compose up -d

& "$PSScriptRoot\wait-for-grid.ps1"
Write-Host "Selenium Grid started. Hub: http://localhost:4444" -ForegroundColor Green
