@echo off
setlocal
title CompileLatestClient
cd /d "%~dp0"

rem TeaVM 0.9.2 bundled by EaglercraftX cannot consume Java 25 class files.
rem Eaglermod's Windows launcher deliberately uses JDK 17.
set "JAVA_EXE="

rem Explicit override for custom installs
if defined EAGLER_JAVA_HOME if exist "%EAGLER_JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%EAGLER_JAVA_HOME%\bin\java.exe"

rem Common JDK 17 install locations (last matching folder wins)
if not defined JAVA_EXE for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk-17*") do if exist "%%~fD\bin\java.exe" set "JAVA_EXE=%%~fD\bin\java.exe"
if not defined JAVA_EXE for /d %%D in ("%ProgramFiles%\Microsoft\jdk-17*") do if exist "%%~fD\bin\java.exe" set "JAVA_EXE=%%~fD\bin\java.exe"
if not defined JAVA_EXE for /d %%D in ("%ProgramFiles%\Java\jdk-17*") do if exist "%%~fD\bin\java.exe" set "JAVA_EXE=%%~fD\bin\java.exe"
if not defined JAVA_EXE for /d %%D in ("%ProgramFiles%\Zulu\zulu-17*") do if exist "%%~fD\bin\java.exe" set "JAVA_EXE=%%~fD\bin\java.exe"

if not defined JAVA_EXE (
    echo.
    echo [Eaglermod] ERROR: JDK 17 was not found.
    echo [Eaglermod] TeaVM 0.9.2 cannot build this client with JDK 25.
    echo [Eaglermod] Install a JDK 17, then run this file again.
    echo [Eaglermod] Or set EAGLER_JAVA_HOME to your JDK 17 folder.
    echo.
    pause
    exit /b 1
)

echo [Eaglermod] Using JDK 17:
echo %JAVA_EXE%
"%JAVA_EXE%" -version
echo.

"%JAVA_EXE%" -cp "buildtools/BuildTools.jar" net.lax1dude.eaglercraft.v1_8.buildtools.gui.CompileLatestClientGUI
set "BUILD_EXIT=%ERRORLEVEL%"

if exist "##TEAVM.TMP##" (
    del /S /Q "##TEAVM.TMP##\*" >nul 2>&1
    rmdir /S /Q "##TEAVM.TMP##" >nul 2>&1
)

if not "%BUILD_EXIT%"=="0" (
    echo.
    echo [Eaglermod] Build failed with exit code %BUILD_EXIT%.
)

pause
exit /b %BUILD_EXIT%
