@echo off
cd /d "%~dp0FrontendCuidar-"
set BROWSER=none
set CI=true
npm start > "%~dp0frontend-dev.log" 2>&1
