@REM JayasriMart Maven Wrapper for Windows
@echo off
setlocal

set "MAVEN_CMD=mvn"
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    mvn %*
    exit /b %ERRORLEVEL%
)

if exist "%USERPROFILE%\apache-maven-3.9.9\bin\mvn.cmd" (
    "%USERPROFILE%\apache-maven-3.9.9\bin\mvn.cmd" %*
    exit /b %ERRORLEVEL%
)

if exist "C:\Users\ELCOT\apache-maven-3.9.9\bin\mvn.cmd" (
    "C:\Users\ELCOT\apache-maven-3.9.9\bin\mvn.cmd" %*
    exit /b %ERRORLEVEL%
)

echo ERROR: Maven could not be found. Please ensure Apache Maven is installed.
exit /b 1
