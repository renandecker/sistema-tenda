@echo off
setlocal
title Sistema Tenda - Producao
echo ==================================================
echo           SISTEMA TENDA
echo ==================================================
echo.
echo Iniciando aplicacao...
cd /d "%~dp0"
echo.
java -jar app\quarkus-run.jar
pause
