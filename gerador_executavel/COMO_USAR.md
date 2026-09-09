# 🚀 Manual do Gerador de Executável (.EXE) e Setup

Todos os arquivos e modificações foram criados **exclusivamente dentro desta pasta (`gerador_executavel`)**, mantendo o seu projeto original **100% intacto e sem alterações**.

---

## 📁 O que há nesta pasta?

1. **`1_GERAR_EXECUTAVEL_E_JAR.bat`**
   - Script de 1 clique.
   - Sempre que você fizer alterações no código Java em `src/`, basta dar 2 cliques neste arquivo.
   - Ele detecta o Java, compila o código, junta todas as bibliotecas (`MySQL`, `FlatLaf`, `OpenPDF`) em um único pacote e compila o executável `SistemaLoja.exe`.

2. **`aplicacao_pronta/`**
   - **`SistemaLoja.exe`**: Executável nativo do Windows. Dá 2 cliques e o sistema abre silenciosamente (sem aquela janela preta de prompt do DOS). Se a máquina de destino não tiver Java 21, ele mostra um aviso na tela com opção de baixar.
   - **`SistemaLoja.jar`**: O pacote completo (Fat-JAR) com tudo embutido.

3. **`inno_setup.iss`**
   - Script do **Inno Setup** para gerar o instalador oficial (`Setup.exe`).

4. **`Launcher.cs`**
   - Código-fonte em C# do inicializador Windows nativo.

---

## 📦 Como gerar o Instalador Oficial (`Setup.exe`)

Para transformar a pasta `aplicacao_pronta` em um arquivo de instalação único (`Instalador_SistemaLoja_Setup_v1.0.exe`) com assistente de instalação, atalho na Área de Trabalho e Desinstalador:

### Passo 1: Baixar e Instalar o Inno Setup
* O Inno Setup é a ferramenta gratuita padrão da indústria para criar instaladores no Windows.
* Baixe em: **https://jrsoftware.org/isdl.php** (clique em *Inno Setup - Self-contained installer*).
* Instale normalmente (Avançar, Avançar, Concluir).

### Passo 2: Gerar o Instalador
Após instalar o Inno Setup, você tem duas opções:
* **Opção A:** Execute novamente o arquivo `1_GERAR_EXECUTAVEL_E_JAR.bat` (ele detectará o Inno Setup e compilará o instalador automaticamente!).
* **Opção B:** Clique com o botão direito no arquivo `inno_setup.iss` e selecione **"Compile"**.

O instalador pronto será salvo na pasta:
👉 `gerador_executavel\instalador\Instalador_SistemaLoja_Setup_v1.0.exe`

---

## 🎨 Dica: Como colocar um Ícone Personalizado (.ico)

1. Coloque o seu arquivo de ícone (exemplo: `icone.ico`) dentro da pasta `gerador_executavel`.
2. Para adicionar ao `.exe`:
   - No script `1_GERAR_EXECUTAVEL_E_JAR.bat`, onde tem a linha do `csc.exe`, adicione o parâmetro `/win32icon:icone.ico`.
3. Para adicionar ao instalador:
   - Abra o `inno_setup.iss` e adicione na seção `[Setup]`:
     `SetupIconFile=icone.ico`
