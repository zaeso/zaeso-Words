@echo off
chcp 65001 >nul
setlocal DisableDelayedExpansion
pushd "%~dp0"

set "JAR="
for /f "delims=" %%f in ('dir /b /a-d /o-d "target\zaeso-words-*.jar" 2^>nul') do if not defined JAR set "JAR=target\%%f"
if not defined JAR for /f "delims=" %%f in ('dir /b /a-d /o-d "zaeso-words-*.jar" 2^>nul') do if not defined JAR set "JAR=%%f"
if not defined JAR (
    echo Не найден файл zaeso-words-*.jar.
    echo Соберите игру командой mvn verify или положите JAR рядом с этим файлом.
    goto failed
)

set "JAVA=java.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA=%JAVA_HOME%\bin\java.exe"
set "VER="
for /f "tokens=3" %%v in ('^""%JAVA%" -version 2^>^&1 ^| findstr /i "version"^"') do if not defined VER set "VER=%%~v"
if not defined VER (
    echo Java не найдена. Установите Java 8 или новее с https://adoptium.net/
    goto failed
)

for /f "tokens=1,2 delims=.-" %%m in ("%VER%") do (
    set "MAJOR=%%m"
    if "%%m"=="1" set "MAJOR=%%n"
)
if %MAJOR% LSS 8 (
    echo Найдена Java %VER%. Для запуска нужна Java 8 или новее.
    goto failed
)

"%JAVA%" -Dfile.encoding=UTF-8 -jar "%JAR%" %*
if errorlevel 1 goto failed
popd
exit /b 0

:failed
pause
popd
exit /b 1
