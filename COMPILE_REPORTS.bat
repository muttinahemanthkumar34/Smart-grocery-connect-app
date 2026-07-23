@echo off
title Compiling E2E Master Excel Sheet...
cd /d "C:\projects\PDD\MyApplication"
echo Running report generator...
python automation/runners/generate_reports.py
echo.
echo Excel sheet compiled successfully!
echo Opening folder...
C:\Windows\explorer.exe "C:\projects\PDD\MyApplication\automation_reports\Excel"
pause
