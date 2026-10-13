@echo off
cd /d "%~dp0"
echo ======================================================================
echo   Pushing StudyHub (Author: VenuMadhavi-77) to GitHub...
echo ======================================================================
echo.
git push --force origin main
echo.
echo ======================================================================
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Your repository is now updated on GitHub!
    echo Refresh https://github.com/VenuMadhavi-77/studyhub in your browser.
) else (
    echo [NOTICE] If prompted to log in, please sign in with VenuMadhavi-77.
)
echo ======================================================================
pause
