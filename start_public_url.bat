@echo off
echo ========================================================
echo       StudyHub Instant Public HTTPS Tunnel
echo ========================================================
echo Opening secure public HTTPS tunnel on port 3000...
echo Both the React frontend and Spring Boot backend will be accessible!
echo.
ssh -o StrictHostKeyChecking=no -R 80:localhost:3000 nokey@localhost.run
pause
