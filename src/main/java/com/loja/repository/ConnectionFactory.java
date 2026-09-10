package com.loja.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class ConnectionFactory {
    private static String host = "localhost";
    private static String port = "3306";
    private static String database = "banco_assistencia";
    private static String user = "root";
    private static String pass = "";
    private static boolean carregado = false;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERRO] Driver do MySQL não encontrado no projeto!");
        }
    }

    public static File getArquivoConfig() {
        // 1. Tenta verificar se já existe no AppData do usuário (local padrão seguro no Windows)
        File appDataConfig = getArquivoConfigAppData();
        if (appDataConfig != null && appDataConfig.exists()) {
            return appDataConfig;
        }

        // 2. Tenta verificar se existe na pasta do JAR
        try {
            File jarDir = new File(ConnectionFactory.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
            if (jarDir != null && jarDir.isDirectory()) {
                File localConfig = new File(jarDir, "database.properties");
                if (localConfig.exists()) {
                    return localConfig;
                }
                // Se a pasta do JAR tiver permissão de gravação, pode usar local
                if (jarDir.canWrite()) {
                    return localConfig;
                }
            }
        } catch (Exception ignored) {}

        // 3. Se a pasta do JAR não permitir escrita (ex: Program Files), usa o AppData do usuário
        if (appDataConfig != null) {
            return appDataConfig;
        }

        return new File("database.properties");
    }

    private static File getArquivoConfigAppData() {
        try {
            String appData = System.getenv("APPDATA");
            File baseDir;
            if (appData != null && !appData.trim().isEmpty()) {
                baseDir = new File(appData, "SistemaLoja");
            } else {
                baseDir = new File(System.getProperty("user.home", "."), ".sistemaloja");
            }
            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }
            return new File(baseDir, "database.properties");
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isArquivoConfigExiste() {
        File f = getArquivoConfig();
        if (f != null && f.exists()) return true;
        File appData = getArquivoConfigAppData();
        return appData != null && appData.exists();
    }

    public static synchronized void carregarConfiguracoes() {
        if (carregado) return;
        File configFile = getArquivoConfig();
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                Properties props = new Properties();
                props.load(fis);
                host = props.getProperty("db.host", "localhost").trim();
                port = props.getProperty("db.port", "3306").trim();
                database = props.getProperty("db.database", "banco_assistencia").trim();
                user = props.getProperty("db.user", "root").trim();
                pass = props.getProperty("db.password", "");
            } catch (Exception e) {
                System.err.println("[AVISO] Não foi possível ler database.properties: " + e.getMessage());
            }
        }
        carregado = true;
    }

    public static synchronized void salvarConfiguracoes(String novoHost, String novaPorta, String novoBanco, String novoUser, String novaSenha) throws IOException {
        host = (novoHost != null && !novoHost.trim().isEmpty()) ? novoHost.trim() : "localhost";
        port = (novaPorta != null && !novaPorta.trim().isEmpty()) ? novaPorta.trim() : "3306";
        database = (novoBanco != null && !novoBanco.trim().isEmpty()) ? novoBanco.trim() : "banco_assistencia";
        user = (novoUser != null && !novoUser.trim().isEmpty()) ? novoUser.trim() : "root";
        pass = (novaSenha != null) ? novaSenha : "";

        Properties props = new Properties();
        props.setProperty("db.host", host);
        props.setProperty("db.port", port);
        props.setProperty("db.database", database);
        props.setProperty("db.user", user);
        props.setProperty("db.password", pass);

        File configFile = getArquivoConfig();
        try {
            if (configFile.getParentFile() != null && !configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                props.store(fos, "Configuracoes de Conexao MySQL - Sistema Loja Assistencia");
            }
        } catch (IOException e) {
            // Se falhou por permissão (Acesso negado em C:\Program Files), salva automaticamente no AppData do usuário
            File fallbackFile = getArquivoConfigAppData();
            if (fallbackFile != null && !fallbackFile.equals(configFile)) {
                try (FileOutputStream fos = new FileOutputStream(fallbackFile)) {
                    props.store(fos, "Configuracoes de Conexao MySQL - Sistema Loja Assistencia");
                }
            } else {
                throw e;
            }
        }
        carregado = true;
    }

    public static String getUrl() {
        carregarConfiguracoes();
        return "jdbc:mysql://" + host + ":" + port + "/" + database + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    public static Connection getConnection() throws SQLException {
        carregarConfiguracoes();
        return DriverManager.getConnection(getUrl(), user, pass);
    }

    public static boolean testarConexao() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static String testarConexaoCom(String h, String p, String db, String u, String pwd) {
        String testUrl = "jdbc:mysql://" + h + ":" + p + "/" + db + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=3500";
        try (Connection conn = DriverManager.getConnection(testUrl, u, pwd)) {
            if (conn != null && !conn.isClosed()) {
                return null;
            }
            return "Não foi possível abrir a conexão com o servidor.";
        } catch (SQLException ex) {
            return ex.getMessage();
        }
    }

    public static String getHost() { carregarConfiguracoes(); return host; }
    public static String getPort() { carregarConfiguracoes(); return port; }
    public static String getDatabase() { carregarConfiguracoes(); return database; }
    public static String getUser() { carregarConfiguracoes(); return user; }
    public static String getPass() { carregarConfiguracoes(); return pass; }

    public static boolean criarTabela() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Tabela de Peças/Produtos
            stmt.execute("CREATE TABLE IF NOT EXISTS produto (" +
                    "id INT PRIMARY KEY, " +
                    "nome VARCHAR(100), " +
                    "preco DOUBLE, " +
                    "estoque INT)");

            // 2. Tabela de Clientes
            stmt.execute("CREATE TABLE IF NOT EXISTS cliente (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nome VARCHAR(100) NOT NULL, " +
                    "cpf_cnpj VARCHAR(20), " +
                    "telefone VARCHAR(20), " +
                    "email VARCHAR(100), " +
                    "endereco VARCHAR(255))");

            // 3. Tabela de Equipamentos
            stmt.execute("CREATE TABLE IF NOT EXISTS equipamento (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "cliente_id INT, " +
                    "tipo VARCHAR(50), " +
                    "marca VARCHAR(50), " +
                    "modelo VARCHAR(50), " +
                    "numero_serie VARCHAR(50), " +
                    "cor VARCHAR(50), " +
                    "avarias TEXT, " +
                    "patrimonio VARCHAR(50), " +
                    "senha_acesso VARCHAR(50), " +
                    "acessorios TEXT, " +
                    "FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE)");

            // 4. Tabela de Ordens de Serviço
            stmt.execute("CREATE TABLE IF NOT EXISTS ordem_servico (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "cliente_id INT, " +
                    "equipamento_id INT, " +
                    "problema_relatado TEXT, " +
                    "diagnostico_tecnico TEXT, " +
                    "status VARCHAR(100) DEFAULT 'Aguardando Orçamento', " +
                    "valor_servico DOUBLE DEFAULT 0.0, " +
                    "valor_total DOUBLE DEFAULT 0.0, " +
                    "checklist_entrada TEXT, " +
                    "observacoes_finais TEXT, " +
                    "data_entrada TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "data_saida TIMESTAMP NULL, " +
                    "FOREIGN KEY (cliente_id) REFERENCES cliente(id), " +
                    "FOREIGN KEY (equipamento_id) REFERENCES equipamento(id))");

            // 5. Tabela de Peças por Ordem de Serviço
            stmt.execute("CREATE TABLE IF NOT EXISTS os_pecas (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "os_id INT, " +
                    "produto_id INT, " +
                    "nome VARCHAR(200), " +
                    "quantidade INT, " +
                    "valor_unitario DOUBLE, " +
                    "FOREIGN KEY (os_id) REFERENCES ordem_servico(id) ON DELETE CASCADE)");

            // 6. Tabela de Serviços por Ordem de Serviço
            stmt.execute("CREATE TABLE IF NOT EXISTS os_servicos (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "os_id INT, " +
                    "descricao VARCHAR(200), " +
                    "valor DOUBLE, " +
                    "FOREIGN KEY (os_id) REFERENCES ordem_servico(id) ON DELETE CASCADE)");

            // 7. Catálogo Reutilizável de Serviços
            stmt.execute("CREATE TABLE IF NOT EXISTS catalogo_servicos (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "descricao VARCHAR(200) UNIQUE, " +
                    "valor_padrao DOUBLE)");

            // 8. Catálogo Reutilizável de Peças
            stmt.execute("CREATE TABLE IF NOT EXISTS catalogo_pecas (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nome VARCHAR(200) UNIQUE, " +
                    "valor_padrao DOUBLE)");

            // 9. Tabela do Caixa
            stmt.execute("CREATE TABLE IF NOT EXISTS caixa (" +
                    "id INT PRIMARY KEY, " +
                    "saldo DOUBLE)");

            // 10. Tabela de Sangrias
            stmt.execute("CREATE TABLE IF NOT EXISTS sangria (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "valor DOUBLE, " +
                    "data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 11. Tabela de Histórico / Linha do Tempo da OS
            stmt.execute("CREATE TABLE IF NOT EXISTS os_historico (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "os_id INT NOT NULL, " +
                    "status VARCHAR(100) NOT NULL, " +
                    "data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "observacao VARCHAR(255), " +
                    "FOREIGN KEY (os_id) REFERENCES ordem_servico(id) ON DELETE CASCADE)");

            // 12. Tabela de Vendas PDV (Frente de Caixa Balcão)
            stmt.execute("CREATE TABLE IF NOT EXISTS venda_pdv (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "subtotal DOUBLE NOT NULL DEFAULT 0.0, " +
                    "desconto DOUBLE NOT NULL DEFAULT 0.0, " +
                    "valor_total DOUBLE NOT NULL DEFAULT 0.0, " +
                    "valor_liquido DOUBLE NOT NULL DEFAULT 0.0, " +
                    "observacoes TEXT, " +
                    "data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 13. Tabela de Itens de Venda PDV
            stmt.execute("CREATE TABLE IF NOT EXISTS venda_pdv_itens (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "venda_id INT NOT NULL, " +
                    "produto_id INT NOT NULL, " +
                    "nome_produto VARCHAR(200) NOT NULL, " +
                    "quantidade INT NOT NULL, " +
                    "valor_unitario DOUBLE NOT NULL, " +
                    "subtotal DOUBLE NOT NULL, " +
                    "FOREIGN KEY (venda_id) REFERENCES venda_pdv(id) ON DELETE CASCADE)");

            // 14. Tabela de Pagamentos Fracionados / Múltiplos
            stmt.execute("CREATE TABLE IF NOT EXISTS venda_pagamento (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "venda_id INT NULL, " +
                    "os_id INT NULL, " +
                    "modalidade VARCHAR(50) NOT NULL, " +
                    "valor_bruto DOUBLE NOT NULL, " +
                    "taxa_percentual DOUBLE NOT NULL DEFAULT 0.0, " +
                    "valor_taxa DOUBLE NOT NULL DEFAULT 0.0, " +
                    "valor_liquido DOUBLE NOT NULL, " +
                    "parcelas INT NOT NULL DEFAULT 1, " +
                    "data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 15. Tabela de Movimentações de Caixa (Entradas, Sangrias, Suprimentos)
            stmt.execute("CREATE TABLE IF NOT EXISTS caixa_movimento (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "tipo VARCHAR(30) NOT NULL, " +
                    "modalidade VARCHAR(50) NOT NULL, " +
                    "valor DOUBLE NOT NULL, " +
                    "taxa DOUBLE NOT NULL DEFAULT 0.0, " +
                    "valor_liquido DOUBLE NOT NULL, " +
                    "justificativa VARCHAR(255), " +
                    "os_id INT NULL, " +
                    "venda_id INT NULL, " +
                    "sessao_id INT NULL, " +
                    "data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 16. Tabela de Configuração de Taxas de Maquininha
            stmt.execute("CREATE TABLE IF NOT EXISTS config_taxas (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "modalidade VARCHAR(50) UNIQUE NOT NULL, " +
                    "taxa_percentual DOUBLE NOT NULL DEFAULT 0.0)");

            // 17. Tabela de Sessões / Turnos de Caixa (Abertura e Fechamento com Contagem Cega)
            stmt.execute("CREATE TABLE IF NOT EXISTS caixa_sessao (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "status VARCHAR(20) NOT NULL DEFAULT 'ABERTO', " +
                    "data_abertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "data_fechamento TIMESTAMP NULL, " +
                    "operador_abertura VARCHAR(100) NOT NULL, " +
                    "operador_fechamento VARCHAR(100) NULL, " +
                    "saldo_inicial DOUBLE NOT NULL DEFAULT 0.0, " +
                    "saldo_final_sistema DOUBLE NOT NULL DEFAULT 0.0, " +
                    "saldo_final_informado DOUBLE NOT NULL DEFAULT 0.0, " +
                    "diferenca DOUBLE NOT NULL DEFAULT 0.0, " +
                    "justificativa_diferenca TEXT NULL, " +
                    "fundo_troco_deixado DOUBLE NOT NULL DEFAULT 0.0, " +
                    "sangria_malote DOUBLE NOT NULL DEFAULT 0.0, " +
                    "total_dinheiro DOUBLE NOT NULL DEFAULT 0.0, " +
                    "total_pix DOUBLE NOT NULL DEFAULT 0.0, " +
                    "total_debito_bruto DOUBLE NOT NULL DEFAULT 0.0, " +
                    "total_debito_liquido DOUBLE NOT NULL DEFAULT 0.0, " +
                    "total_credito_bruto DOUBLE NOT NULL DEFAULT 0.0, " +
                    "total_credito_liquido DOUBLE NOT NULL DEFAULT 0.0, " +
                    "total_sangrias DOUBLE NOT NULL DEFAULT 0.0, " +
                    "total_suprimentos DOUBLE NOT NULL DEFAULT 0.0, " +
                    "total_vendas_bruto DOUBLE NOT NULL DEFAULT 0.0, " +
                    "total_vendas_liquido DOUBLE NOT NULL DEFAULT 0.0, " +
                    "qtd_vendas INT NOT NULL DEFAULT 0, " +
                    "contagem_detalhada_texto TEXT NULL, " +
                    "observacoes TEXT NULL)");

            // 18. Tabela de Configurações Gerais da Loja (WhatsApp do Dono, Troco padrão, etc)
            stmt.execute("CREATE TABLE IF NOT EXISTS config_geral (" +
                    "chave VARCHAR(100) PRIMARY KEY, " +
                    "valor TEXT)");

            // 19. Tabela de Usuários e Autenticação com Perfis
            stmt.execute("CREATE TABLE IF NOT EXISTS usuario (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nome VARCHAR(100) NOT NULL, " +
                    "login VARCHAR(50) UNIQUE NOT NULL, " +
                    "senha_hash VARCHAR(128) NOT NULL, " +
                    "perfil VARCHAR(20) NOT NULL DEFAULT 'ATENDENTE', " +
                    "ativo BOOLEAN DEFAULT TRUE, " +
                    "data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Migrações automáticas de colunas e tamanhos
            try {
                stmt.execute("ALTER TABLE ordem_servico MODIFY COLUMN status VARCHAR(100)");
            } catch (SQLException ignored) {}

            adicionarColunaSeNaoExistir(conn, "produto", "codigo_barras", "VARCHAR(50)");
            adicionarColunaSeNaoExistir(conn, "sangria", "justificativa", "VARCHAR(255)");
            adicionarColunaSeNaoExistir(conn, "equipamento", "cor", "VARCHAR(50)");
            adicionarColunaSeNaoExistir(conn, "equipamento", "avarias", "TEXT");
            adicionarColunaSeNaoExistir(conn, "equipamento", "patrimonio", "VARCHAR(50)");
            adicionarColunaSeNaoExistir(conn, "equipamento", "senha_acesso", "VARCHAR(50)");
            adicionarColunaSeNaoExistir(conn, "equipamento", "acessorios", "TEXT");
            adicionarColunaSeNaoExistir(conn, "ordem_servico", "checklist_entrada", "TEXT");
            adicionarColunaSeNaoExistir(conn, "ordem_servico", "observacoes_finais", "TEXT");
            adicionarColunaSeNaoExistir(conn, "os_pecas", "nome", "VARCHAR(200)");
            adicionarColunaSeNaoExistir(conn, "caixa_movimento", "sessao_id", "INT NULL");
            adicionarColunaSeNaoExistir(conn, "venda_pdv", "sessao_id", "INT NULL");

            // Migração de os_pecas se tiver chave primária antiga
            migrarTabelaOsPecasSeNecessario(conn);

            // Inicializar saldo do caixa caso esteja vazio
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM caixa")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO caixa (id, saldo) VALUES (1, 100.00)");
                    System.out.println("[DB] Saldo inicial do caixa (R$ 100.00) inicializado.");
                }
            }

            // Inicializar configurações padrão caso tabela esteja vazia
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM config_geral")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO config_geral (chave, valor) VALUES " +
                            "('nome_loja', 'Assistência Técnica & Gestão Pro'), " +
                            "('whatsapp_proprietario', ''), " +
                            "('fundo_troco_padrao', '100.00'), " +
                            "('largura_cupom_mm', '80')");
                    System.out.println("[DB] Configurações gerais da loja inicializadas.");
                }
            }

            // Inicializar taxas padrão da maquininha caso tabela esteja vazia
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM config_taxas")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO config_taxas (modalidade, taxa_percentual) VALUES " +
                            "('DINHEIRO', 0.0), " +
                            "('PIX', 0.0), " +
                            "('DEBITO', 1.50), " +
                            "('CREDITO_1X', 3.20), " +
                            "('CREDITO_PARCELADO_2X_6X', 5.50), " +
                            "('CREDITO_PARCELADO_7X_12X', 9.80)");
                    System.out.println("[DB] Taxas padrão de maquininha cadastradas com sucesso.");
                }
            }

            // Inicializar usuário administrador padrão se a tabela estiver vazia
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM usuario")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String hashAdmin = com.loja.service.SegurancaService.gerarHashSenha("admin");
                    try (PreparedStatement stmtUser = conn.prepareStatement(
                            "INSERT INTO usuario (nome, login, senha_hash, perfil, ativo) VALUES (?, ?, ?, ?, ?)")) {
                        stmtUser.setString(1, "Administrador Principal");
                        stmtUser.setString(2, "admin");
                        stmtUser.setString(3, hashAdmin);
                        stmtUser.setString(4, "ADMIN");
                        stmtUser.setBoolean(5, true);
                        stmtUser.executeUpdate();
                    }
                    System.out.println("[DB] Usuário padrão 'admin' (Perfil: ADMIN) cadastrado com sucesso.");
                }
            }

            System.out.println("[DB] Tabelas de assistência técnica criadas/verificadas com sucesso no MySQL.");
            return true;

        } catch (SQLException e) {
            System.err.println("[ERRO] Falha ao conectar ou organizar o banco de dados no MySQL: " + e.getMessage());
            return false;
        }
    }

    private static void adicionarColunaSeNaoExistir(Connection conn, String tabela, String coluna, String tipo) {
        try {
            DatabaseMetaData md = conn.getMetaData();
            try (ResultSet rs = md.getColumns(null, null, tabela, coluna)) {
                if (!rs.next()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("ALTER TABLE " + tabela + " ADD COLUMN " + coluna + " " + tipo);
                    }
                }
            }
        } catch (SQLException e) {
            // Ignora erro se coluna já existir ou falhar silenciosamente
        }
    }

    private static void migrarTabelaOsPecasSeNecessario(Connection conn) {
        try {
            DatabaseMetaData md = conn.getMetaData();
            boolean temId = false;
            try (ResultSet rs = md.getColumns(null, null, "os_pecas", "id")) {
                if (rs.next()) {
                    temId = true;
                }
            }

            if (!temId) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("DROP TABLE IF EXISTS os_pecas");
                    stmt.execute("CREATE TABLE os_pecas (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "os_id INT, " +
                            "produto_id INT, " +
                            "nome VARCHAR(200), " +
                            "quantidade INT, " +
                            "valor_unitario DOUBLE, " +
                            "FOREIGN KEY (os_id) REFERENCES ordem_servico(id) ON DELETE CASCADE)");
                    System.out.println("[DB] Tabela os_pecas recriada com estrutura flexivel (id auto-increment).");
                }
            }
        } catch (SQLException e) {
            System.err.println("[AVISO DB] Migracao de os_pecas: " + e.getMessage());
        }
    }
}
