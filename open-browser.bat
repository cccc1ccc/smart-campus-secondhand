@echo off
rem Helper: waits until the server port answers, then opens the browser.
rem Usage: open-browser.bat <port> <url>
setlocal
set "PORT=%~1"
set "URL=%~2"
if "%PORT%"=="" set "PORT=8080"
if "%URL%"=="" set "URL=http://localhost:%PORT%"

for /l %%i in (1,1,90) do (
    powershell -NoProfile -Command "try{(New-Object Net.Sockets.TcpClient('127.0.0.1',%PORT%)).Close();exit 0}catch{exit 1}" >nul 2>nul
    if not errorlevel 1 (
        start "" "%URL%"
        exit /b 0
    )
    ping -n 2 127.0.0.1 >nul
)
exit /b 1
