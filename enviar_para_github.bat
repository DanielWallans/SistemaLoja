@echo off
chcp 65001 > nul
title Enviar Alteracoes para o GitHub - System Pro
echo ====================================================================
echo   ENVIANDO PROJETO PARA O GITHUB - MASTER E MAIN
echo ====================================================================
echo.

cd /d "%~dp0"

:: 1. Verificar se o Git esta instalado
where git >nul 2>&1
if errorlevel 1 goto :git_missing

:: 2. Verificar se ha alteracoes locais pendentes
set "HAS_CHANGES="
for /f "delims=" %%I in ('git status --porcelain 2^>nul') do (
    set "HAS_CHANGES=1"
    goto :changes_detected
)

goto :no_changes

:changes_detected
echo ====================================================================
echo   ALTERACOES DETECTADAS PARA ENVIO
echo ====================================================================
git status -s
echo.
echo Pressione ENTER para usar mensagem padrao com data e hora
echo ou digite uma descricao para este commit:
set "COMMIT_MSG="
set /p "COMMIT_MSG=* Mensagem: "

if not defined COMMIT_MSG set "COMMIT_MSG=Atualizacao do sistema - %date% %time%"

echo.
echo [1/4] Adicionando arquivos alterados - git add...
git add -A

echo [2/4] Criando commit...
git commit -m "%COMMIT_MSG%"
goto :sync_branches

:no_changes
echo [INFO] Nenhuma nova alteracao de codigo pendente para commit.

:sync_branches
echo.
echo [3/4] Sincronizando branch local main com master...
git branch -f main master >nul 2>&1

echo [4/4] Enviando alteracoes para o GitHub...
echo.
echo * Enviando branch master...
git push origin master
set "ERR_MASTER=%ERRORLEVEL%"

echo.
echo * Enviando branch main...
git push origin main
set "ERR_MAIN=%ERRORLEVEL%"

echo.
echo ====================================================================
if %ERR_MASTER% equ 0 goto :check_main
goto :push_failed

:check_main
if %ERR_MAIN% equ 0 goto :push_success
echo  [AVISO] Branch master enviado com sucesso, mas ocorreu aviso em main.
echo ====================================================================
goto :finish

:push_success
echo  [SUCESSO] Projeto enviado para o GitHub com sucesso!
echo  Branches atualizados: master e main
echo ====================================================================
goto :finish

:push_failed
echo  [ERRO] Falha ao enviar para o GitHub.
echo  Verifique sua conexao com a internet ou credenciais do Git.
echo ====================================================================
goto :finish

:git_missing
echo [ERRO] O Git nao foi encontrado no PATH do Windows!
echo Instale o Git: https://git-scm.com/download/win
pause
exit /b 1

:finish
echo.
pause
