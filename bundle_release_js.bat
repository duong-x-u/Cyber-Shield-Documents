@echo off
title React Native Release JS Bundler

echo ==================================================
echo      React Native - Bundling JS for Release
echo ==================================================
echo.

echo This script will bundle your JavaScript code for Android Release builds.
echo It will create index.android.bundle and place assets in app/src/main/res/
echo.

call npx react-native bundle --platform android --dev false --entry-file index.js --bundle-output android/app/src/main/assets/index.android.bundle --assets-dest android/app/src/main/res/

if %errorlevel% equ 0 (
    echo.
    echo ==================================================
    echo      JS Bundling Complete Successfully!
    echo ==================================================
) else (
    echo.
    echo ==================================================
    echo      JS Bundling Failed! Check the output above.
    echo ==================================================
)

pause
