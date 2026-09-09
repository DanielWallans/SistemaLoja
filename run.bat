@echo off
chcp 65001 > nul
:: Tenta detectar o Java automaticamente nas extensoes ou variaveis de ambiente
set "JAVA_HOME="
for /f "delims=" %%I in ('dir /b /ad /o-n "%USERPROFILE%\.antigravity-ide\extensions\redhat.java*" 2^>nul') do (
    for /f "delims=" %%J in ('dir /b /ad "%USERPROFILE%\.antigravity-ide\extensions\%%I\jre" 2^>nul') do (
        if exist "%USERPROFILE%\.antigravity-ide\extensions\%%I\jre\%%J\bin\javac.exe" (
            set "JAVA_HOME=%USERPROFILE%\.antigravity-ide\extensions\%%I\jre\%%J"
            goto :java_detected
        )
    )
)

for /f "delims=" %%I in ('dir /b /ad /o-n "%USERPROFILE%\.vscode\extensions\redhat.java*" 2^>nul') do (
    for /f "delims=" %%J in ('dir /b /ad "%USERPROFILE%\.vscode\extensions\%%I\jre" 2^>nul') do (
        if exist "%USERPROFILE%\.vscode\extensions\%%I\jre\%%J\bin\javac.exe" (
            set "JAVA_HOME=%USERPROFILE%\.vscode\extensions\%%I\jre\%%J"
            goto :java_detected
        )
    )
)

:java_detected
if defined JAVA_HOME (
    set "JAVAC_EXE=%JAVA_HOME%\bin\javac.exe"
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVAC_EXE=javac"
    set "JAVA_EXE=java"
)

set "MYSQL_JAR=mysql-connector-j-8.3.0.jar"
set "FLATLAF_JAR=flatlaf-3.5.4.jar"
set "OPENPDF_JAR=openpdf-1.3.40.jar"
set "CLASSPATH=target\classes;%MYSQL_JAR%;%FLATLAF_JAR%;%OPENPDF_JAR%"

if not exist "%MYSQL_JAR%" (
    echo [SISTEMA] Baixando driver do MySQL...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar' -OutFile 'mysql-connector-j-8.3.0.jar'"
)

if not exist "%FLATLAF_JAR%" (
    echo [SISTEMA] Baixando biblioteca FlatLaf...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/formdev/flatlaf/3.5.4/flatlaf-3.5.4.jar' -OutFile 'flatlaf-3.5.4.jar'"
)

if not exist "%OPENPDF_JAR%" (
    echo [SISTEMA] Baixando biblioteca OpenPDF...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/github/librepdf/openpdf/1.3.40/openpdf-1.3.40.jar' -OutFile 'openpdf-1.3.40.jar'"
)

echo [SISTEMA] Compilando arquivos Java...
if not exist "target\classes" mkdir "target\classes"

dir /s /b src\*.java > sources.txt
"%JAVAC_EXE%" --release 21 -d target\classes -cp "%MYSQL_JAR%;%FLATLAF_JAR%;%OPENPDF_JAR%" @sources.txt
del sources.txt

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERRO] Falha ao compilar o projeto!
    pause
    exit /b %ERRORLEVEL%
)

echo [SISTEMA] Iniciando a aplicacao...
echo ====================================================
"%JAVA_EXE%" -cp "%CLASSPATH%" com.loja.app.Main
echo ====================================================
pause
