package com.loja.repository;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionFactory {
    private static final String URL = "jdbc:mysql://localhost:3306/banco_assistencia?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERRO] Driver do MySQL não encontrado no projeto!");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static boolean testarConexao() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

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

            // Migrações automáticas de colunas e tamanhos
            try {
                stmt.execute("ALTER TABLE ordem_servico MODIFY COLUMN status VARCHAR(100)");
            } catch (SQLException ignored) {}

            adicionarColunaSeNaoExistir(conn, "equipamento", "cor", "VARCHAR(50)");
            adicionarColunaSeNaoExistir(conn, "equipamento", "avarias", "TEXT");
            adicionarColunaSeNaoExistir(conn, "equipamento", "patrimonio", "VARCHAR(50)");
            adicionarColunaSeNaoExistir(conn, "equipamento", "senha_acesso", "VARCHAR(50)");
            adicionarColunaSeNaoExistir(conn, "equipamento", "acessorios", "TEXT");
            adicionarColunaSeNaoExistir(conn, "ordem_servico", "checklist_entrada", "TEXT");
            adicionarColunaSeNaoExistir(conn, "ordem_servico", "observacoes_finais", "TEXT");
            adicionarColunaSeNaoExistir(conn, "os_pecas", "nome", "VARCHAR(200)");

            // Migração de os_pecas se tiver chave primária antiga
            migrarTabelaOsPecasSeNecessario(conn);

            // Inicializar saldo do caixa caso esteja vazio
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM caixa")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO caixa (id, saldo) VALUES (1, 100.00)");
                    System.out.println("[DB] Saldo inicial do caixa (R$ 100.00) inicializado.");
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
