@echo off
echo ==========================================
echo           STARTING NEXIS NODE
echo ==========================================
echo.

:: Set Environment
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

echo Using Java: %JAVA_HOME%
echo.

:: Run Application
echo Building and Starting...
call "C:\Program Files\apache-maven-3.9.12\bin\mvn.cmd" clean compile exec:java

pause
