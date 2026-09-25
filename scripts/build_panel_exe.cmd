@echo off
echo ========================================================
echo   Building ApexsionsPanel.exe via PyInstaller
echo ========================================================
python -m PyInstaller --noconsole --onefile --name "ApexsionsPanel" --icon "%~dp0..\Website\public\favicon.ico" --version-file "%~dp0version_info.txt" --distpath "%~dp0.." --workpath "%~dp0..\build\pyinstaller" --specpath "%~dp0..\build\pyinstaller" "%~dp0panel_lite.py"
echo.
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] ApexsionsPanel.exe successfully compiled to project root!
) else (
    echo [ERROR] Build failed.
)
pause
