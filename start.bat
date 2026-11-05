@echo off
setlocal
cd /d "%~dp0"
title Campus Loop - Smart Campus Second-hand Platform

rem Port used by the application. Change here if 8080 is taken on your machine.
if "%APP_PORT%"=="" set "APP_PORT=8080"
set "APP_URL=http://localhost:%APP_PORT%"
set "APP_JAR=target\smart-campus-secondhand-1.0.0.jar"

where java >nul 2>nul
if errorlevel 1 goto no_java

rem If the port already answers, just open the browser.
powershell -NoProfile -Command "try{(New-Object Net.Sockets.TcpClient('127.0.0.1',%APP_PORT%)).Close();exit 0}catch{exit 1}" >nul 2>nul
if not errorlevel 1 goto already_running

rem Background helper: waits for the port and opens the default browser.
if not "%NO_BROWSER%"=="1" start "" /min "%~dp0open-browser.bat" %APP_PORT% "%APP_URL%"

echo ================================================================
echo  Campus Loop - MUST smart campus second-hand platform
echo  URL: %APP_URL%
echo  Demo login: student number 1240016186 / Password123
echo  Close this window to stop the server.
echo ================================================================
echo.

if not exist "%APP_JAR%" goto try_maven
echo [INFO] Starting the packaged application ...
java -jar "%APP_JAR%" --server.port=%APP_PORT%
goto done

:try_maven
if not exist "pom.xml" goto nothing_to_run
where mvn >nul 2>nul
if errorlevel 1 goto no_maven
echo [INFO] Starting with Maven ...
call mvn -q spring-boot:run -Dspring-boot.run.arguments=--server.port=%APP_PORT%
goto done

:no_java
echo [ERROR] Java 17 or newer was not found on PATH.
echo         Install a JDK, then run this file again.
pause
exit /b 1

:already_running
echo [INFO] Port %APP_PORT% is already in use - opening %APP_URL%
if not "%NO_BROWSER%"=="1" start "" "%APP_URL%"
ping -n 4 127.0.0.1 >nul 2>nul
exit /b 0

:no_maven
echo [ERROR] The packaged jar is missing and Maven is not available.
echo         Build once with:  mvn package
pause
exit /b 1

:nothing_to_run
echo [ERROR] Nothing to run: both the jar and pom.xml are missing.
pause
exit /b 1

:done
echo.
echo [INFO] Server stopped.
ping -n 3 127.0.0.1 >nul 2>nul
endlocal
