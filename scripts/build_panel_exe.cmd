@echo off
echo ========================================================
echo   Building Ultra-Fast Single-Process ApexsionsPanel.exe
echo ========================================================
python -m PyInstaller --noconsole --onedir --name "ApexsionsPanel" --icon "%~dp0..\Website\public\favicon.ico" --version-file "%~dp0version_info.txt" --distpath "%~dp0..\build\dist" --workpath "%~dp0..\build\work" --specpath "%~dp0..\build" "%~dp0panel_lite.py"

if %ERRORLEVEL% EQU 0 (
    echo [COPYING] Moving binary and _internal to project root...
    copy /y "%~dp0..\build\dist\ApexsionsPanel\ApexsionsPanel.exe" "%~dp0..\ApexsionsPanel.exe" >nul
    xcopy /e /i /y "%~dp0..\build\dist\ApexsionsPanel\_internal" "%~dp0..\_internal" >nul
    echo.
    echo [SUCCESS] Single-process ApexsionsPanel.exe ready in root folder!
) else (
    echo.
    echo [ERROR] Build failed.
)
pause
