@echo off
title Hoax Detector - Python NLI Server
cd /d "%~dp0backend"

echo ========================================================
echo   Memulai Hoax Detector Python NLI Server (Port 8000)...
echo ========================================================

if exist "python_embed\python.exe" (
    python_embed\python.exe -m uvicorn api.app.main:app --host 0.0.0.0 --port 8000 --reload
) else if exist "venv\Scripts\activate.bat" (
    call venv\Scripts\activate.bat
    uvicorn api.app.main:app --host 0.0.0.0 --port 8000 --reload
) else (
    uvicorn api.app.main:app --host 0.0.0.0 --port 8000 --reload
)

pause
