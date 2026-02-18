@echo off
setlocal

where java >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found. Install JDK 17+ and add it to PATH.
    pause
    exit /b 1
)

where mvn >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven not found. Install Maven 3.8+ and add it to PATH.
    echo        https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

mvn -q compile javafx:run

if errorlevel 1 (
    echo Build or launch failed. Check the errors above.
    pause
    exit /b 1
)

endlocal
