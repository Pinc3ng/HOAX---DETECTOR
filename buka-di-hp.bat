@echo off
title Hoax Detector - 100% Python NLI (Buka di HP)
setlocal enabledelayedexpansion

echo ===================================================================
echo   HOAX DETECTOR - NATURAL LANGUAGE INFERENCE (NLI) PYTHON ENGINE
echo   100%% Python Backend & Responsive Web UI (Bebas Kompilasi APK)
echo ===================================================================
echo.

cd /d "%~dp0backend"

:: Deteksi Alamat IP Wi-Fi / Jaringan Lokal
for /f "tokens=*" %%i in ('powershell -Command "Get-NetIPAddress -AddressFamily IPv4 -InterfaceAlias '*Wi-Fi*','*Wireless*','*Ethernet*' -ErrorAction SilentlyContinue | Where-Object { $_.IPAddress -notlike '127.*' -and $_.IPAddress -notlike '169.254.*' } | Select-Object -ExpandProperty IPAddress -First 1"') do set LOCAL_IP=%%i

if "%LOCAL_IP%"=="" (
    for /f "tokens=*" %%i in ('powershell -Command "(Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -notlike '127.*' -and $_.IPAddress -notlike '169.254.*' } | Select-Object -ExpandProperty IPAddress -First 1)"') do set LOCAL_IP=%%i
)

if "%LOCAL_IP%"=="" set LOCAL_IP=localhost

echo -------------------------------------------------------------------
echo  CARA BUKA DI HP:
echo  1. Pastikan HP dan Laptop terhubung ke Wi-Fi yang SAMA.
echo  2. Buka browser di HP (Chrome / Safari), lalu ketik alamat ini:
echo.
echo     ==^>  http://%LOCAL_IP%:8000/
echo.
echo  Untuk buka di Laptop:
echo     ==^>  http://localhost:8000/
echo -------------------------------------------------------------------
echo.
echo Menjalankan Python FastAPI Uvicorn Server di port 8000...
echo.

:: Otomatis buka browser di laptop setelah 2 detik
start "" powershell -Command "Start-Sleep -Seconds 2; Start-Process 'http://localhost:8000/'"

:: Jalankan Server Python
if exist "python_embed\python.exe" (
    python_embed\python.exe -m uvicorn api.app.main:app --host 0.0.0.0 --port 8000
) else if exist "venv\Scripts\python.exe" (
    venv\Scripts\python.exe -m uvicorn api.app.main:app --host 0.0.0.0 --port 8000
) else (
    python -m uvicorn api.app.main:app --host 0.0.0.0 --port 8000
)

pause
