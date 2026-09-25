@echo off
if exist "%~dp0ApexsionsPanel.exe" (
    start "" "%~dp0ApexsionsPanel.exe"
) else (
    start "" pythonw "%~dp0scripts\panel_lite.py"
)
