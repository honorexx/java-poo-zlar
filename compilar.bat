@echo off
if not exist out\production\zlar_oficial mkdir out\production\zlar_oficial
javac -encoding UTF-8 -d out\production\zlar_oficial src\*.java
if errorlevel 1 pause
echo Projeto compilado.
