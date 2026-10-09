@echo off
title Hoax Detector - Backend Server
echo ===================================================
echo   Memulai Hoax Detector Backend Server...
echo ===================================================
cd /d "%~dp0backend"
if exist "C:\Program Files\nodejs\node.exe" (
    "C:\Program Files\nodejs\node.exe" server.js
) else (
    node server.js
)
pause
