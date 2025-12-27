@echo off
echo ========================================
echo Compilation du mod Raid Mod
echo ========================================
echo.

REM Check for Java
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERREUR: Java n'est pas trouve dans le PATH.
    echo.
    echo Veuillez installer Java 17 (JDK) et l'ajouter au PATH.
    echo Ou definissez JAVA_HOME vers votre installation Java.
    echo.
    echo Telechargez Java 17 depuis: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

REM Check Java version
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
    set JAVA_VERSION=!JAVA_VERSION:"=!
    echo Version Java detectee: !JAVA_VERSION!
)

echo.
echo Demarrage de la compilation...
echo.

call gradlew.bat build --no-daemon

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Compilation reussie!
    echo ========================================
    echo.
    echo Le fichier .jar se trouve dans: build\libs\
    echo.
    dir /b build\libs\*.jar
    echo.
) else (
    echo.
    echo ========================================
    echo Erreur lors de la compilation
    echo ========================================
    echo.
)

pause


