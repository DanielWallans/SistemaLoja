@echo off
chcp 65001 > nul
title Criar Pacote de Instalacao
echo ====================================================
echo   GERANDO PACOTE PARA OUTRO COMPUTADOR
echo ====================================================
echo.

:: Tenta detectar o Java automaticamente (JAVA_HOME, Program Files, extensoes IDE ou PATH)
set "FOUND_JDK="

if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\jar.exe" (
        set "FOUND_JDK=%JAVA_HOME%"
        goto :java_detected
    )
)

for /d %%D in (
    "C:\Program Files\Eclipse Adoptium\jdk-21*"
    "C:\Program Files\Java\jdk-21*"
    "C:\Program Files\Java\jdk*"
    "C:\Program Files\BellSoft\LibericaJDK-21*"
    "C:\Program Files\Amazon Corretto\jdk21*"
    "C:\Program Files\Microsoft\jdk-21*"
) do (
    if exist "%%~D\bin\jar.exe" (
        set "FOUND_JDK=%%~D"
        goto :java_detected
    )
)

for /f "delims=" %%I in ('dir /b /ad /o-n "%USERPROFILE%\.antigravity-ide\extensions\redhat.java*" 2^>nul') do (
    for /f "delims=" %%J in ('dir /b /ad "%USERPROFILE%\.antigravity-ide\extensions\%%I\jre" 2^>nul') do (
        if exist "%USERPROFILE%\.antigravity-ide\extensions\%%I\jre\%%J\bin\jar.exe" (
            set "FOUND_JDK=%USERPROFILE%\.antigravity-ide\extensions\%%I\jre\%%J"
            goto :java_detected
        )
    )
)

for /f "delims=" %%I in ('dir /b /ad /o-n "%USERPROFILE%\.vscode\extensions\redhat.java*" 2^>nul') do (
    for /f "delims=" %%J in ('dir /b /ad "%USERPROFILE%\.vscode\extensions\%%I\jre" 2^>nul') do (
        if exist "%USERPROFILE%\.vscode\extensions\%%I\jre\%%J\bin\jar.exe" (
            set "FOUND_JDK=%USERPROFILE%\.vscode\extensions\%%I\jre\%%J"
            goto :java_detected
        )
    )
)

:java_detected
if defined FOUND_JDK (
    set "JAR_EXE=%FOUND_JDK%\bin\jar.exe"
) else (
    set "JAR_EXE=jar"
)

"%JAR_EXE%" --version >nul 2>&1
if %ERRORLEVEL% neq 0 goto :jar_missing
goto :jar_ok

:jar_missing
echo.
echo ====================================================================
echo  [ERRO] O utilitario 'jar' do Java nao foi localizado!
echo  Instale o JDK 21: https://adoptium.net/temurin/releases/?version=21
echo ====================================================================
echo.
pause
exit /b 1

:jar_ok

if not exist "dist" mkdir "dist"
if not exist "dist\lib" mkdir "dist\lib"

echo [1/3] Copiando bibliotecas e JARs...
copy /Y "mysql-connector-j-8.3.0.jar" "dist\lib\" > nul
copy /Y "flatlaf-3.5.4.jar" "dist\lib\" > nul
copy /Y "openpdf-1.3.40.jar" "dist\lib\" > nul

echo [2/3] Gerando SistemaAssistente.jar...
"%JAR_EXE%" --create --file dist\SistemaAssistente.jar --main-class com.loja.app.Main -C target\classes .

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
