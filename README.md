# 🛠️ Sistema de Gestão para Assistência Técnica e Loja

Olá! Seja muito bem-vindo(a) ao projeto. 👋

Este é um **sistema desktop completo e intuitivo** desenvolvido em **Java** para facilitar a rotina de assistências técnicas e lojas de manutenção. O objetivo do sistema é organizar o fluxo de atendimento, gerenciar clientes, equipamentos e ordens de serviço, além de controlar estoque e caixa.

---

## 📌 O que o sistema faz atualmente (Funcionalidades)

* 📊 **Dashboard:** Visão geral rápida do negócio com total de OS abertas, faturamento do dia e saldo em caixa.
* 📋 **Ordens de Serviço (OS):** Abertura, acompanhamento de status (Aberta, Em Análise, Aguardando Peças, Concluída, Cancelada) e finalização de serviços.
* 👥 **Gestão de Clientes:** Cadastro, consulta, edição e exclusão de clientes com validação de dados.
* 💻 **Gestão de Equipamentos:** Vínculo de aparelhos (Notebook, Celular, Computador, Console) diretamente ao cliente dono.
* 📄 **Orçamento e PDF:** Adição de peças e serviços à OS com cálculo automático e geração de PDF de orçamento profissional.
* 💬 **Integração WhatsApp:** Envio do resumo do orçamento diretamente para o WhatsApp do cliente em 1 clique.
* 📦 **Estoque de Peças e Produtos:** Controle de quantidade em estoque e valores unitários.
* 💰 **Controle de Caixa:** Abertura de caixa, registro de entradas, sangrias (retiradas) e fechamento diário.

---

## 📋 Pré-requisitos (O que você precisa ter instalado)

Para rodar este sistema na sua máquina, você só precisa de duas ferramentas gratuitas:

### 1. Java JDK 21 (ou superior)
* O Java é o motor necessário para executar o sistema.
* 📥 **Onde baixar:** [Oracle Java JDK 21](https://www.oracle.com/java/technologies/downloads/#java21) ou [Adoptium Eclipse Temurin 21](https://adoptium.net/temurin/releases/?version=21)
* 👉 Basta baixar o instalador para o seu sistema operacional (ex: Windows `.msi` ou `.exe`), instalar avançando as telas e pronto!

### 2. MySQL (Banco de Dados)
* O sistema utiliza o MySQL para guardar com segurança todos os dados.
* A forma mais fácil e recomendada para testar é usando o **XAMPP**:
  * 📥 **Onde baixar o XAMPP:** [Baixar XAMPP para Windows](https://www.apachefriends.org/pt_br/index.html)
  * Após instalar o XAMPP, abra o **XAMPP Control Panel** e clique em **"Start"** ao lado de **MySQL**.
  * *(O sistema já está pré-configurado para conectar no usuário padrão `root` sem senha e cria o banco de dados `banco_assistencia` e todas as tabelas sozinho na primeira vez que abrir!).*

---

## 🚀 Passo a Passo para Baixar e Executar

### 1. Baixar o Projeto
Você pode obter o projeto de duas formas:
* **Opção A (Via Download ZIP):** No GitHub, clique no botão verde **Code** > **Download ZIP**, depois extraia a pasta em qualquer lugar do seu computador.
* **Opção B (Via Git):** Abra o terminal / Prompt de Comando e digite:
  ```bash
  git clone https://github.com/DanielWallans/SistemaLoja.git
  ```

---

### 2. Executando o Sistema (Modo Mais Fácil - 1 Clique) ⚡

Se você estiver no **Windows**, preparamos um inicializador automático:

1. Abra a pasta do projeto que você baixou.
2. Dê **dois cliques no arquivo `run.bat`**.
3. O script irá:
   * Baixar as bibliotecas visuais necessárias (FlatLaf, OpenPDF e Driver MySQL) automaticamente se não estiverem presentes.
   * Compilar todos os códigos Java.
   * Abrir a tela principal do sistema automaticamente!

---

### 3. Executando pelo Terminal (Opção Alternativa)

Caso prefira rodar manualmente pelo Prompt de Comando ou PowerShell:

1. Abra o terminal dentro da pasta do projeto.
2. Certifique-se de que o MySQL está ligado.
3. Execute o script pelo terminal:
   ```cmd
   run.bat
   ```

*(Ou se você utilizar o Apache Maven)*:
```bash
mvn compile exec:java -Dexec.mainClass="com.loja.app.Main"
```

---

## 🖥️ Onde o sistema vai abrir?

Por ser uma **aplicação Desktop (Desktop App)**, o sistema não roda em páginas do navegador nem portas como `localhost:3000`. 
Assim que você executar o comando ou o arquivo `run.bat`, **uma janela moderna com interface gráfica personalizada se abrirá na sua área de trabalho**.

---

## ⚠️ Aviso sobre a Versão (Protótipo em Validação)

> **Nota:** Este projeto é um **protótipo funcional** desenvolvido para apresentação e validação do fluxo de processos de uma assistência técnica. 
> Ele contempla todas as operações essenciais do dia a dia, mas novas melhorias visuais, relatórios avançados e funcionalidades adicionais continuam em constante desenvolvimento e aprimoramento.

---

## 🤝 Dúvidas ou Contato

Ficou com alguma dúvida ou precisa de ajuda para rodar na sua máquina?
* **Desenvolvedor:** Daniel Wallans
* **Repositório do Projeto:** [GitHub - DanielWallans/SistemaLoja](https://github.com/DanielWallans/SistemaLoja)

Obrigado por testar o sistema! Esperamos que a experiência seja excelente. ✨
