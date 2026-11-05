@echo off
setlocal
cd /d "%~dp0"
title Campus Loop - Smart Campus Second-hand Platform

rem Port used by the application. Change here if 8080 is taken on your machine.
if "%APP_PORT%"=="" set "APP_PORT=8080"
set "APP_URL=http://localhost:%APP_PORT%"
set "APP_JAR=target\smart-campus-secondhand-1.0.0.jar"

where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Java 17 or newer was not found on PATH.
    echo         Install a JDK, then run this file again.
    pause
    exit /b 1
)

powershell -NoProfile -Command "try{(New-Object Net.Sockets.TcpClient('127.0.0.1',%APP_PORT%)).Close();exit 0}catch{exit 1}" >nul 2>nul
if not errorlevel 1 (
    echo [INFO] Port %APP_PORT% is already in use - opening %APP_URL%
    if not "%NO_BROWSER%"=="1" start "" "%APP_URL%"
    ping -n 4 127.0.0.1 >nul 2>nul
    exit /b 0
)

rem Wait for the server port, then open the default browser once.
if not "%NO_BROWSER%"=="1" (
    start "" /min powershell -NoProfile -WindowStyle Hidden -Command "for($i=0;$i -lt 90;$i++){try{(New-Object Net.Sockets.TcpClient('127.0.0.1',%APP_PORT%)).Close();Start-Process '%APP_URL%';break}catch{Start-Sleep -Milliseconds 500}}"
)

echo ================================================================
echo  Campus Loop - MUST smart campus second-hand platform
echo  URL: %APP_URL%
echo  Demo login: student number 1240016186 / Password123
echo  Close this window to stop the server.
echo ================================================================
echo.

if exist "%APP_JAR%" (
    echo [INFO] Starting the packaged application ...
    java -jar "%APP_JAR%" --server.port=%APP_PORT%
    goto done
)

if exist "pom.xml" (
    where mvn >nul 2>nul
    if errorlevel 1 (
        echo [ERROR] The packaged jar is missing and Maven is not available.
        echo         Build once with:  mvn package
        pause
        exit /b 1
    )
    echo [INFO] Starting with Maven ...
    call mvn -q spring-boot:run -Dspring-boot.run.arguments=--server.port=%APP_PORT%
    goto done
)

echo [ERROR] Nothing to run: both the jar and pom.xml are missing.
pause

:done
echo.
echo [INFO] Server stopped.
ping -n 3 127.0.0.1 >nul 2>nul
endlocal
