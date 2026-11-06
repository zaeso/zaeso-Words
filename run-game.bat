@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
cd /d "%~dp0"

set "JAR="
for %%f in (zaeso-words-*.jar) do set "JAR=%%~ff"
if not defined JAR (
    echo Рядом с этим файлом не найден zaeso-words-*.jar.
    echo Скачайте JAR из того же релиза и положите его в эту папку.
    pause
    exit /b 1
)

set "VER="
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do if not defined VER set "VER=%%~v"
if not defined VER (
    echo Java не найдена. Установите Java 17 или новее: https://adoptium.net/
    pause
    exit /b 1
)

for /f "tokens=1 delims=." %%m in ("!VER!") do set "MAJOR=%%m"
if !MAJOR! LSS 17 (
    echo Найдена Java !VER!, а нужна Java 17 или новее.
    echo Установите Temurin JRE 17+ с https://adoptium.net/
    pause
    exit /b 1
)

where javaw >nul 2>nul
if errorlevel 1 (
    start "" java -jar "!JAR!"
) else (
    start "" javaw -jar "!JAR!"
)
