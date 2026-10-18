@echo off
chcp 65001 >nul
title Полная сборка (Clean)
set "JAVA_HOME=C:\Program Files\Java\Oracle_JDK-24"

echo Запуск ПОЛНОЙ сборки...
call mvn clean package

echo.
echo Готово! Нажми любую клавишу...
pause >nul