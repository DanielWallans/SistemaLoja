# 🚀 Manual do Gerador de Executável (.EXE) e Setup

Todos os arquivos e modificações foram criados **exclusivamente dentro desta pasta (`gerador_executavel`)**, mantendo o seu projeto original **100% intacto e sem alterações**.

---

## 📁 O que há nesta pasta?

1. **`1_GERAR_EXECUTAVEL_E_JAR.bat`**
   - Script de 1 clique com suporte a **novas versões**.
   - Sempre que você fizer alterações no código Java em `src/`, basta dar 2 cliques neste arquivo.
   - Ele detecta a versão atual (ex: `1.2.3`), pergunta se deseja manter (ENTER) ou digitar uma nova versão (ex: `1.2.4`).
   - Se digitar uma nova versão, ele atualiza automaticamente o `versao.json` e o `UpdateService.java`.
   - Compila o código Java, junta todas as bibliotecas (`MySQL`, `FlatLaf`, `OpenPDF`) no Fat-JAR e compila os executáveis nativos `SystemPro.exe` e `SistemaLoja.exe`.
   - Detecta o **Inno Setup** e compila o instalador oficial nomeado com a versão exata: `SystemPro_Setup_v[VERSAO].exe`.

2. **`aplicacao_pronta/`**
   - **`SystemPro.exe` / `SistemaLoja.exe`**: Executáveis nativos do Windows. Dá 2 cliques e o sistema abre silenciosamente (sem prompt de comando preto).
   - **`SystemPro.jar` / `SistemaLoja.jar`**: Os pacotes autocontidos (Fat-JAR) com todas as dependências embutidas.
   - **`jre/`**: Java 21 embutido para funcionamento 100% offline em qualquer computador.

3. **`inno_setup.iss`**
   - Script do **Inno Setup** para gerar o instalador oficial (`SystemPro_Setup_v[VERSAO].exe`).

4. **`Launcher.cs`**
   - Código-fonte em C# do inicializador Windows nativo.

---

## 📦 Como gerar o Instalador Oficial (`Setup.exe`)

Para gerar o arquivo de instalação único (`SystemPro_Setup_v[VERSAO].exe`) com assistente de instalação, atalho na Área de Trabalho e Desinstalador:

### Passo 1: Inno Setup
* O Inno Setup já está instalado em sua máquina (`C:\Program Files\Inno Setup 7`). Caso precise reinstalar futuramente, o download gratuito é em: **https://jrsoftware.org/isdl.php**.

### Passo 2: Gerar o Instalador e Executável
* Dê 2 cliques no arquivo `1_GERAR_EXECUTAVEL_E_JAR.bat`.
* Pressione ENTER para manter a versão atual ou digite a nova versão (ex: `1.2.4`).
* O instalador pronto será salvo automaticamente na pasta:
👉 `gerador_executavel\instalador\SystemPro_Setup_v[VERSAO].exe`

---

## 🎨 Dica: Como colocar um Ícone Personalizado (.ico)

1. Coloque o seu arquivo de ícone (exemplo: `icone.ico`) dentro da pasta `gerador_executavel`.
2. Para adicionar ao `.exe`:
   - No script `1_GERAR_EXECUTAVEL_E_JAR.bat`, onde tem a linha do `csc.exe`, adicione o parâmetro `/win32icon:icone.ico`.
3. Para adicionar ao instalador:
   - Abra o `inno_setup.iss` e adicione na seção `[Setup]`:
     `SetupIconFile=icone.ico`
