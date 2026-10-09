@echo off
title Hoax Detector - Python ML & NLP Backend
echo ========================================================
echo   Memulai Hoax Detector Python FastAPI Server (Port 8000)...
echo   Algoritma: NLI Stance Classifier + TF-IDF SVM + MAFINDO
echo ========================================================
cd /d "%~dp0backend"
if exist "venv\Scripts\activate.bat" (
    call venv\Scripts\activate.bat
)
uvicorn api.app.main:app --host 0.0.0.0 --port 8000 --reload
pause
