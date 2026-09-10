$ErrorActionPreference = "Stop"
Set-Location -Path $PSScriptRoot

$jdk = "C:\Users\User\.antigravity-ide\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64"
$javac = "$jdk\bin\javac.exe"
$jar = "$jdk\bin\jar.exe"
$csc = "C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe"
$iscc = "C:\Program Files (x86)\Inno Setup 6\ISCC.exe"

Write-Host "[1/5] Compilando codigo Java..." -ForegroundColor Cyan
if (Test-Path "staging") {
    cmd /c "rd /s /q staging"
}
New-Item -ItemType Directory -Path "staging" -Force | Out-Null

cmd /c "dir /s /b ..\src\*.java > sources.txt"
& $javac --release 21 -d staging -cp "..\mysql-connector-j-8.3.0.jar;..\flatlaf-3.5.4.jar;..\openpdf-1.3.40.jar" "@sources.txt"
if (Test-Path "sources.txt") { Remove-Item "sources.txt" -Force }

Write-Host "[2/5] Extraindo bibliotecas para Fat-JAR..." -ForegroundColor Cyan
Set-Location -Path "staging"
tar -xf "..\..\mysql-connector-j-8.3.0.jar"
tar -xf "..\..\flatlaf-3.5.4.jar"
tar -xf "..\..\openpdf-1.3.40.jar"
Remove-Item -Path "META-INF\*.SF", "META-INF\*.DSA", "META-INF\*.RSA" -Force -ErrorAction SilentlyContinue
Set-Location -Path $PSScriptRoot

Write-Host "[3/5] Gerando aplicacao_pronta\SystemPro.jar..." -ForegroundColor Cyan
if (-not (Test-Path "aplicacao_pronta")) {
    New-Item -ItemType Directory -Path "aplicacao_pronta" -Force | Out-Null
}
& $jar --create --file "aplicacao_pronta\SystemPro.jar" --main-class com.loja.app.Main -C staging .
Copy-Item "aplicacao_pronta\SystemPro.jar" "aplicacao_pronta\SistemaLoja.jar" -Force
cmd /c "rd /s /q staging"

Write-Host "[4/5] Compilando executavel nativo SystemPro.exe..." -ForegroundColor Cyan
& $csc /nologo /target:winexe /out:"aplicacao_pronta\SystemPro.exe" Launcher.cs
Copy-Item "aplicacao_pronta\SystemPro.exe" "aplicacao_pronta\SistemaLoja.exe" -Force

Write-Host "[5/5] Compilando Setup.exe com Inno Setup..." -ForegroundColor Cyan
& $iscc inno_setup.iss

Write-Host "`nBUILD FINALIZADO COM SUCESSO!" -ForegroundColor Green
