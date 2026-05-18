@echo off
setlocal
echo =======================================================
echo   Ultra-Low-Latency Trading Engine - Quick Start
echo =======================================================
echo.

:: Check if Maven is installed globally
call mvn -version >nul 2>&1
if %errorlevel% equ 0 (
    set MAVEN_CMD=mvn
    goto :build
)

:: Check if local maven exists
if exist ".maven\bin\mvn.cmd" (
    set MAVEN_CMD=.maven\bin\mvn.cmd
    goto :build
)

echo [INFO] Maven not found globally. Downloading Apache Maven locally (this is a one-time process)...
powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip' -OutFile 'maven.zip'"
if not exist "maven.zip" (
    echo [ERROR] Failed to download Maven.
    pause
    exit /b 1
)

echo [INFO] Extracting Maven...
powershell -Command "Expand-Archive -Path 'maven.zip' -DestinationPath '.maven_temp' -Force"
move .maven_temp\apache-maven-3.9.6 .maven >nul 2>&1
rmdir /s /q .maven_temp >nul 2>&1
del maven.zip >nul 2>&1
set MAVEN_CMD=.maven\bin\mvn.cmd

:build
echo.
echo Step 1: Building the project with Maven...
call "%MAVEN_CMD%" clean package -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Maven build failed!
    pause
    exit /b %errorlevel%
)

echo.
echo Step 2: Starting the Trading Engine...
echo (Press Ctrl+C to stop the engine)
echo.
call "%MAVEN_CMD%" spring-boot:run -pl trading-engine-app
pause
