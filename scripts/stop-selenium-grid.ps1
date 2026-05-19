# Stop Selenium Grid containers
$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

Write-Host "Stopping Selenium Grid containers..." -ForegroundColor Cyan
docker compose down --remove-orphans
Write-Host "Selenium Grid stopped." -ForegroundColor Green
