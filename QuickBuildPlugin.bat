@echo off
chcp 65001 >nul
title Быстрая сборка (Quick)
set "JAVA_HOME=C:\Program Files\Java\Oracle_JDK-24"

echo Запуск БЫСТРОЙ сборки...
call mvn package

echo.
echo Готово! Нажми любую клавишу...
pause >nul