@echo off
chcp 65001 > nul
title Liberar Porta 3306 no Firewall do Windows - Sistema Loja

echo =====================================================================
echo    CONFIGURACAO DE REDE LOCAL - SISTEMA LOJA ASSISTENCIA
echo =====================================================================
echo.
echo Este script adiciona a regra de permissao no Firewall do Windows
echo para que outros computadores e notebooks na mesma rede Wi-Fi / Cabo
echo consigam se conectar ao banco de dados MySQL instalado nesta maquina.
echo.

:: Verifica se esta executando como Administrador
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [ATENCAO] Este script precisa ser executado como Administrador!
    echo.
    echo Clique com o botao direito neste arquivo (liberar_firewall_rede.bat)
    echo e selecione "Executar como Administrador".
    echo.
    pause
    exit /b 1
)

echo [1/2] Liberando porta TCP 3306 no Firewall do Windows...
netsh advfirewall firewall delete rule name="MySQL Server Rede Local (Porta 3306)" >nul 2>&1
netsh advfirewall firewall add rule name="MySQL Server Rede Local (Porta 3306)" dir=in action=allow protocol=TCP localport=3306 >nul

if %errorlevel% equ 0 (
    echo [OK] Regra criada com sucesso no Firewall!
) else (
    echo [ERRO] Nao foi possivel criar a regra.
)

echo.
echo [2/2] Exibindo seu IP nesta rede local para os outros computadores:
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4"') do (
    echo    - IP: %%a
)

echo.
echo =====================================================================
echo Pronto! Agora os outros computadores/notebooks na sua rede podem
echo conectar a este computador usando a porta 3306.
echo =====================================================================
echo.
pause
