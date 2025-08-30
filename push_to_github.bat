@echo off
title CyberShield GitHub Pusher

echo ==================================================
echo      CyberShield - Auto Push to GitHub
echo ==================================================
echo.

REM This command ensures the 'origin' remote is pointing to the correct URL.
REM It does NOT create a new repository.
git remote remove origin >nul 2>&1
git remote add origin https://github.com/pham-thai-duong-2010-vietnam/Cyber_Shield_backup

echo Adding all new and modified files to Git...
git add .
echo.

echo Please enter your commit message (e.g., "Update feature X") and press Enter:
set /p commit_message="Commit message: "
echo.

REM Check if the commit message is empty
if not defined commit_message (
    echo No commit message entered. Aborting.
    goto :end
)

echo Committing files with message: "%commit_message%"
call git commit -m "%commit_message%"
echo.

echo Pushing changes to GitHub...
git push origin main -u
echo.

echo ==================================================
echo      Push complete!
echo ==================================================

:end
pause
