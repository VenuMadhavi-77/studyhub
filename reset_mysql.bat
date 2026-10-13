@echo off
echo ===================================================
echo Resetting MySQL Root Password to: root
echo ===================================================

:: Check for Administrator privileges
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo Administrator privileges required! Requesting elevation...
    powershell -Command "Start-Process cmd -ArgumentList '/c \"%~dpnx0\"' -Verb RunAs"
    exit /b
)

echo Stopping MySQL80 service...
net stop MySQL80

echo Applying new root password and setting up studyhub_db...
start "" "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" --defaults-file="C:\ProgramData\MySQL\MySQL Server 8.0\my.ini" --init-file="d:\tt-project\tt-project\mysql-init.sql" --console

timeout /t 6 /nobreak >nul

echo Stopping temporary instance...
taskkill /f /im mysqld.exe >nul 2>&1

echo Restarting official MySQL80 service...
net start MySQL80

echo ===================================================
echo SUCCESS! MySQL root password is now: root
echo Database studyhub_db has been created.
echo ===================================================
pause
