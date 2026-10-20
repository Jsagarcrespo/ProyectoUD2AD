
@echo off
title Iniciar Asociacion Juegos de Mesa

cd /d "%~dp0"

echo =====================================
echo ASOCIACION DE JUEGOS DE MESA
echo =====================================
echo.

echo Comprobando Docker Desktop...

docker info >nul 2>&1

if errorlevel 1 (
    echo ERROR: Docker Desktop no esta iniciado.
    pause
    exit /b 1
)

echo Docker Desktop esta funcionando.
echo.

if not exist "asociacion_juegos.sql" (
    echo ERROR: No se encuentra el archivo SQL.
    pause
    exit /b 1
)

echo Comprobando configuracion...

docker compose config --quiet

if errorlevel 1 (
    echo ERROR: Configuracion de Docker incorrecta.
    pause
    exit /b 1
)

echo Iniciando MariaDB...

docker compose up -d

if errorlevel 1 (
    echo ERROR: No se ha podido iniciar MariaDB.
    pause
    exit /b 1
)

echo.
echo Estado de MariaDB:
docker compose ps

echo.
echo Arranque solicitado correctamente.
echo Comprueba que MariaDB este lista
echo antes de ejecutar la aplicacion Java.
echo.

pause
