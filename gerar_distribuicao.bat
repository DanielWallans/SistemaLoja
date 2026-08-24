@echo off
chcp 65001 > nul
title Criar Pacote de Instalacao
echo ====================================================
echo   GERANDO PACOTE PARA OUTRO COMPUTADOR
echo ====================================================
echo.

set "JAVA_HOME=C:\Users\User\.antigravity-ide\extensions\redhat.java-1.55.0-win32-x64\jre\21.0.11-win32-x86_64"

if not exist "dist" mkdir "dist"
if not exist "dist\lib" mkdir "dist\lib"

echo [1/3] Copiando bibliotecas e JARs...
copy /Y "mysql-connector-j-8.3.0.jar" "dist\lib\" > nul
copy /Y "flatlaf-3.5.4.jar" "dist\lib\" > nul
copy /Y "openpdf-1.3.40.jar" "dist\lib\" > nul

echo [2/3] Gerando SistemaAssistente.jar...
"%JAVA_HOME%\bin\jar.exe" --create --file dist\SistemaAssistente.jar --main-class com.loja.app.Main -C target\classes .

echo [3/3] Criando executavel Iniciar_Sistema.bat...
echo @echo off > "dist\Iniciar_Sistema.bat"
echo chcp 65001 ^> nul >> "dist\Iniciar_Sistema.bat"
echo title Sistema de Assistencia Tecnica >> "dist\Iniciar_Sistema.bat"
echo java -cp "SistemaAssistente.jar;lib\*" com.loja.app.Main >> "dist\Iniciar_Sistema.bat"
echo if %%ERRORLEVEL%% neq 0 pause >> "dist\Iniciar_Sistema.bat"

echo.
echo ====================================================
echo   PACOTE CRIADO COM SUCESSO NA PASTA 'dist'!
echo ====================================================
echo.
echo Para levar para outro computador:
echo   1. Copie a pasta 'dist' (ou compacte em .ZIP)
echo   2. No outro PC, basta dar 2 cliques em 'Iniciar_Sistema.bat'
echo ====================================================
pause
