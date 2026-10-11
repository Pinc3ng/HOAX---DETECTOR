@echo off
title Atur Laptop Tetap Menyala Saat Tutup Layar
echo ========================================================
echo  Mengatur Laptop Agar Tetap Berjalan Saat Ditutup
echo ========================================================
echo.
echo Menerapkan konfigurasi powercfg...

:: Set lid close action to Do Nothing (0) untuk charger (AC) dan baterai (DC)
powercfg /setacvalueindex SCHEME_CURRENT 4f971e89-eebd-4455-a8de-9e59040e7347 5ca83367-6e45-459f-a27b-476b1d01c936 0
powercfg /setdcvalueindex SCHEME_CURRENT 4f971e89-eebd-4455-a8de-9e59040e7347 5ca83367-6e45-459f-a27b-476b1d01c936 0
powercfg /setactive SCHEME_CURRENT

echo.
echo ========================================================
echo [SUKSES] Pengaturan berhasil diterapkan!
echo Laptop TIDAK AKAN sleep ketika layarnya ditutup.
echo Backend, AI, dan aplikasi tetap berjalan normal.
echo ========================================================
pause
