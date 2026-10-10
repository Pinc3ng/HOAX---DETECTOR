@echo off
title HOAX-DETECTOR MELALUI NLI - Python FastAPI Backend
echo ========================================================
echo   Memulai HOAX-DETECTOR MELALUI NLI Backend Server
echo   Algoritma: Natural Language Inference (NLI) & Multi-Source
echo   Host: 0.0.0.0 | Port: 8000
echo ========================================================
cd /d "%~dp0backend"
if exist "venv\Scripts\activate.bat" (
    call venv\Scripts\activate.bat
)
uvicorn api.app.main:app --host 0.0.0.0 --port 8000 --reload
pause
