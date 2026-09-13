@echo off
chcp 65001 > nul
title Gerador de Executavel (.EXE) e Setup - Sistema Loja
echo ====================================================================
echo   GERADOR DE EXECUTAVEL (.EXE) E INSTALADOR SETUP
echo   Pasta isolada: Todos os arquivos originais foram preservados!
echo ====================================================================
echo.

set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

:: 1. Detectar o compilador Java (JDK 21)
set "FOUND_JDK="

:: 1.1 Se JAVA_HOME do sistema ja estiver definido
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javac.exe" (
        set "FOUND_JDK=%JAVA_HOME%"
        goto :java_detected
    )
)

:: 1.2 Procura em diretorios padrao de instalacao de JDK no Windows
for /d %%D in (
    "C:\Java\jdk-21*"
    "C:\Java\jdk*"
    "C:\Program Files\Eclipse Adoptium\jdk-21*"
    "C:\Program Files\Java\jdk-21*"
    "C:\Program Files\Java\jdk*"
    "C:\Program Files\BellSoft\LibericaJDK-21*"
    "C:\Program Files\Amazon Corretto\jdk21*"
    "C:\Program Files\Microsoft\jdk-21*"
) do (
    if exist "%%~D\bin\javac.exe" (
        set "FOUND_JDK=%%~D"
        goto :java_detected
    )
)

:: 1.3 Procura nas extensoes do Antigravity IDE / VS Code
for /f "delims=" %%I in ('dir /b /ad /o-n "%USERPROFILE%\.antigravity-ide\extensions\redhat.java*" 2^>nul') do (
    for /f "delims=" %%J in ('dir /b /ad "%USERPROFILE%\.antigravity-ide\extensions\%%I\jre" 2^>nul') do (
        if exist "%USERPROFILE%\.antigravity-ide\extensions\%%I\jre\%%J\bin\javac.exe" (
            set "FOUND_JDK=%USERPROFILE%\.antigravity-ide\extensions\%%I\jre\%%J"
            goto :java_detected
        )
    )
)

for /f "delims=" %%I in ('dir /b /ad /o-n "%USERPROFILE%\.vscode\extensions\redhat.java*" 2^>nul') do (
    for /f "delims=" %%J in ('dir /b /ad "%USERPROFILE%\.vscode\extensions\%%I\jre" 2^>nul') do (
        if exist "%USERPROFILE%\.vscode\extensions\%%I\jre\%%J\bin\javac.exe" (
            set "FOUND_JDK=%USERPROFILE%\.vscode\extensions\%%I\jre\%%J"
            goto :java_detected
        )
    )
)

:java_detected
if defined FOUND_JDK (
    set "JAVA_HOME=%FOUND_JDK%"
    set "JAVAC_EXE=%FOUND_JDK%\bin\javac.exe"
    set "JAR_EXE=%FOUND_JDK%\bin\jar.exe"
) else (
    set "JAVAC_EXE=javac"
    set "JAR_EXE=jar"
)

:: Validar javac e jar
"%JAVAC_EXE%" -version >nul 2>&1
if %ERRORLEVEL% neq 0 goto :jdk_missing

"%JAR_EXE%" --version >nul 2>&1
if %ERRORLEVEL% neq 0 goto :jar_missing
goto :tools_detected

:jdk_missing
echo.
echo ====================================================================
echo  [ERRO CRITICO] JDK 21 NAO ENCONTRADO NESTE COMPUTADOR!
echo ====================================================================
echo  O utilitario de compilacao 'javac' nao foi localizado.
echo.
echo  MOTIVO:
echo  Ter apenas o Java comum - JRE nao permite compilar ou gerar executavel.
echo  E necessario ter o JDK 21 instalado na maquina.
echo.
echo  COMO RESOLVER:
echo  1. Baixe o instalador do JDK 21 gratuitamente:
echo     https://adoptium.net/temurin/releases/?version=21
echo  2. Durante a instalacao, marque as opcoes:
echo     - "Set JAVA_HOME variable"
echo     - "Add to PATH"
echo  3. Feche esta janela e execute novamente este script.
echo ====================================================================
echo.
pause
exit /b 1

:jar_missing
echo.
echo ====================================================================
echo  [ERRO CRITICO] FERRAMENTA 'JAR' NAO ENCONTRADA!
echo ====================================================================
echo  O utilitario 'jar' do Java nao foi localizado.
echo  Certifique-se de instalar o JDK 21 completo com PATH configurado.
echo ====================================================================
echo.
pause
exit /b 1

:tools_detected

:: 2. Detectar o Compilador C# do Windows (csc.exe) para criar o .exe nativo
set "CSC_EXE="
if exist "C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe" (
    set "CSC_EXE=C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe"
)
if not defined CSC_EXE (
    if exist "C:\Windows\Microsoft.NET\Framework\v4.0.30319\csc.exe" (
        set "CSC_EXE=C:\Windows\Microsoft.NET\Framework\v4.0.30319\csc.exe"
    )
)

if not defined CSC_EXE (
    echo [ERRO] O compilador C# do Windows csc.exe nao foi encontrado.
    pause
    exit /b 1
)

:: 3. Criar pastas de trabalho
if not exist "aplicacao_pronta" mkdir "aplicacao_pronta"
if exist "staging" rd /s /q "staging"
mkdir "staging"

set "MYSQL_JAR=%SCRIPT_DIR%..\mysql-connector-j-8.3.0.jar"
set "FLATLAF_JAR=%SCRIPT_DIR%..\flatlaf-3.5.4.jar"
set "OPENPDF_JAR=%SCRIPT_DIR%..\openpdf-1.3.40.jar"

echo [1/5] Compilando codigo-fonte Java de src...
dir /s /b ..\src\*.java > sources.txt
"%JAVAC_EXE%" --release 21 -d staging -cp "%MYSQL_JAR%;%FLATLAF_JAR%;%OPENPDF_JAR%" @sources.txt
set "COMP_ERR=%ERRORLEVEL%"
if exist sources.txt del sources.txt
if %COMP_ERR% neq 0 (
    echo [ERRO] Falha ao compilar o codigo Java! (Codigo: %COMP_ERR%)
    pause
    exit /b %COMP_ERR%
)

echo [2/5] Extraindo bibliotecas para criar o pacote unico (Fat-JAR)...
cd staging
tar -xf "%MYSQL_JAR%"
tar -xf "%FLATLAF_JAR%"
tar -xf "%OPENPDF_JAR%"
del /q /f META-INF\*.SF 2>nul
del /q /f META-INF\*.DSA 2>nul
del /q /f META-INF\*.RSA 2>nul
cd ..

echo [3/5] Gerando SystemPro.jar autocontido...
"%JAR_EXE%" --create --file "aplicacao_pronta\SystemPro.jar" --main-class com.loja.app.Main -C staging .
copy /Y "aplicacao_pronta\SystemPro.jar" "aplicacao_pronta\SistemaLoja.jar" > nul
if %ERRORLEVEL% neq 0 (
    echo [ERRO] Falha ao empacotar o JAR!
    pause
    exit /b %ERRORLEVEL%
)

:: Limpar pasta temporaria staging
rd /s /q staging

echo [4/5] Compilando executavel nativo Windows (SystemPro.exe)...
"%CSC_EXE%" /nologo /target:winexe /out:"aplicacao_pronta\SystemPro.exe" Launcher.cs
copy /Y "aplicacao_pronta\SystemPro.exe" "aplicacao_pronta\SistemaLoja.exe" > nul
if %ERRORLEVEL% neq 0 (
    echo [ERRO] Falha ao compilar o executavel .exe!
    pause
    exit /b %ERRORLEVEL%
)

if not exist "aplicacao_pronta\jre\bin\javaw.exe" (
    if defined JAVA_HOME (
        echo [INFO] Embutindo Java 21 para funcionamento 100% offline em qualquer PC...
        robocopy "%JAVA_HOME%" "aplicacao_pronta\jre" /E /NFL /NDL /NJH /NJS /nc /ns /np > nul
    )
)

echo.
echo ====================================================================
echo  [SUCESSO] EXECUTAVEL E PACOTE GERADOS COM SUCESSO!
echo  Pasta de saida: gerador_executavel\aplicacao_pronta\
echo    - SystemPro.exe     (Executavel nativo do Windows - 2 cliques)
echo    - SystemPro.jar     (Todas as bibliotecas embutidas)
echo ====================================================================
echo.

:: 4. Verificar se o Inno Setup esta instalado para compilar o Setup.exe automaticamente
set "ISCC_EXE="
if exist "C:\Program Files\Inno Setup 7\ISCC.exe" (
    set "ISCC_EXE=C:\Program Files\Inno Setup 7\ISCC.exe"
)
if not defined ISCC_EXE (
    if exist "C:\Program Files (x86)\Inno Setup 6\ISCC.exe" (
        set "ISCC_EXE=C:\Program Files (x86)\Inno Setup 6\ISCC.exe"
    )
)
if not defined ISCC_EXE (
    if exist "C:\Program Files\Inno Setup 6\ISCC.exe" (
        set "ISCC_EXE=C:\Program Files\Inno Setup 6\ISCC.exe"
    )
)

if defined ISCC_EXE (
    echo [5/5] Inno Setup detectado! Compilando instalador Setup.exe...
    if not exist "instalador" mkdir "instalador"
    "%ISCC_EXE%" inno_setup.iss
    echo.
    echo ====================================================================
    echo  [INSTALADOR PRONTO] Instalador gerado com sucesso!
    echo  Arquivo: gerador_executavel\instalador\SystemPro_Setup_v1.2.3.exe
    echo ====================================================================
) else (
    echo [AVISO - PASSO OPCIONAL]
    echo O Inno Setup ainda nao esta instalado neste computador.
    echo Para gerar o instalador Setup.exe:
    echo   1. Baixe o Inno Setup em: https://jrsoftware.org/isdl.php
    echo   2. Apos instalar, clique com o botao direito em inno_setup.iss
    echo      e selecione 'Compile' ou execute este script novamente.
)

echo.
pause
