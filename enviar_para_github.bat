@echo off
chcp 65001 > nul
title Enviar Alteracoes para o GitHub
echo ====================================================
echo   ENVIANDO PROJETO PARA O GITHUB (git push)
echo ====================================================
echo.
git push origin master
git push origin main
echo.
if %ERRORLEVEL% equ 0 (
    echo [SUCESSO] Projeto enviado para o GitHub com sucesso (master e main)!
) else (
    echo [ERRO] Falha ao enviar para o GitHub. Verifique o login/credenciais.
)
echo.
pause
