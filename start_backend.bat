@echo off
echo ===================================================
echo Starting StudyHub Spring Boot Backend (Java 23)
echo ===================================================

set "JAVA_HOME=C:\Program Files\Java\jdk-23"
set "PATH=%JAVA_HOME%\bin;%PATH%"

cd /d "%~dp0backend"
mvn spring-boot:run
pause
