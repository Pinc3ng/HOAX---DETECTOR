@echo off
title Hoax Detector - Buka di Browser HP
echo ===================================================
echo   Memulai Mobile Server untuk HP...
echo ===================================================
cd /d "%~dp0..\hoax_detector_mobile"
if exist "C:\Program Files\nodejs\node.exe" (
    "C:\Program Files\nodejs\node.exe" serve.js
) else (
    node serve.js
)
pause
