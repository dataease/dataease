@echo off

rem Build script for dojox.mobile

if "%1"=="separate" goto ok
if "%1"=="single" goto ok
echo Usage: build separate^|single
echo   separate  Create mobile.js that includes only dojox.mobile
echo   single    Create a single dojo.js layer that includes dojox.mobile
goto end
:ok

rem set optimize=shrinksafe
set optimize=closure
set profile=mobile
set dir=release-mobile-separate
rem set standalone=standaloneScrollable=true
if "%~1"=="single" set profile=mobile-all
if "%~1"=="single" set dir=release-mobile-single
shift

cd ..\..\..\util\buildscripts

call build profile=%profile% action=release optimize=%optimize% layerOptimize=%optimize% cssOptimize=comments releaseDir=../../%dir%/ %standalone% %~1 %~2 %~3 %~4 %~5 %~6 %~7 %~8 %~9

cd ..\..\dojox\mobile\build

:end
