@echo off
chcp 65001 > nul
set "JAVA_HOME=C:\Users\User\.antigravity-ide\extensions\redhat.java-1.55.0-win32-x64\jre\21.0.11-win32-x86_64"
set "JAVAC_EXE=%JAVA_HOME%\bin\javac.exe"
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

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
