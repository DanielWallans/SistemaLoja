; =====================================================================
; SCRIPT DO INNO SETUP - SISTEMA DE ASSISTÊNCIA TÉCNICA E LOJA
; =====================================================================
; Este script gera o arquivo "Instalador_SistemaLoja_Setup_v1.0.exe"
; que o seu cliente usará para instalar o sistema com 2 cliques.
; =====================================================================

#define MyAppName "Sistema de Assistência Técnica e Loja"
#define MyAppVersion "1.0"
#define MyAppPublisher "Assistência Técnica"
#define MyAppExeName "SistemaLoja.exe"

[Setup]
AppId={{D1A39F74-B368-4F7D-89C4-32B8DF129F01}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\SistemaLoja
DisableProgramGroupPage=yes
OutputDir=instalador
OutputBaseFilename=Instalador_SistemaLoja_Setup_v1.0
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "brazilianportuguese"; MessagesFile: "compiler:Languages\BrazilianPortuguese.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"

[Dirs]
Name: "{app}"; Permissions: users-modify

[Files]
; Copia o executável nativo e o JAR com todas as dependências embutidas
Source: "aplicacao_pronta\SistemaLoja.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "aplicacao_pronta\SistemaLoja.jar"; DestDir: "{app}"; Flags: ignoreversion
; Inclui a JRE 21 embutida para rodar 100% offline em qualquer computador sem precisar instalar Java!
Source: "aplicacao_pronta\jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Filename: "{app}\{#MyAppExeName}"; Flags: nowait postinstall skipifsilent
