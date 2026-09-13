package com.loja.view.dialogs;

import com.loja.repository.ConnectionFactory;
import com.loja.service.NetworkDiscoveryService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class ConfigBancoDialog extends JDialog {

    private JTextField txtHost;
    private JTextField txtPorta;
    private JTextField txtBanco;
    private JTextField txtUsuario;
    private JPasswordField txtSenha;
    private JCheckBox chkSsl;
    private JLabel lblStatus;
    private JButton btnTestar;
    private JButton btnSalvar;

    // Componentes de descoberta de rede
    private JButton btnBuscarServidores;
    private JPanel pnlResultadoRede;
    private JLabel lblStatusRede;
    private JComboBox<NetworkDiscoveryService.ServidorDescoberto> cbServidoresDescobertos;

    private boolean configuradoComSucesso = false;

    public ConfigBancoDialog(Window owner) {
        super(owner, "Configuração de Servidor / Banco de Dados", ModalityType.APPLICATION_MODAL);

        setSize(590, 700);
        setLocationRelativeTo(owner);
        setResizable(false);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
        carregarDadosAtuais();
    }

    private void initComponents() {
        // 1. Cabeçalho
        JPanel pnlHeader = new JPanel(new BorderLayout(10, 6));
        pnlHeader.setBackground(new Color(24, 28, 36));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JPanel pnlTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTitulo.setOpaque(false);

        JLabel lblTit = new JLabel("CONFIGURAÇÃO DE SERVIDOR / BANCO DE DADOS");
        lblTit.setFont(lblTit.getFont().deriveFont(Font.BOLD, 14.5f));
        lblTit.setForeground(Color.WHITE);
        pnlTitulo.add(lblTit);

        JLabel lblSub = new JLabel("Conecte a um servidor remoto, rede local da empresa ou banco de dados local");
        lblSub.setFont(lblSub.getFont().deriveFont(11.5f));
        lblSub.setForeground(new Color(170, 180, 195));

        pnlHeader.add(pnlTitulo, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Painel Central
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 10));
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(12, 20, 10, 20));

        JPanel pnlTop = new JPanel();
        pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.Y_AXIS));

        // 2.1 Identificação Dinâmica da Rede desta Máquina
        String ipPrincipal = NetworkDiscoveryService.obterIpPrincipal();
        List<String> todosIps = NetworkDiscoveryService.obterIpsLocais();
        String textoIps = todosIps.isEmpty() ? ipPrincipal : String.join(" | ", todosIps);

        JPanel pnlIpInfo = new JPanel(new BorderLayout(6, 2));
        pnlIpInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlIpInfo.setBackground(new Color(240, 245, 250));
        pnlIpInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 220, 235), 1),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));

        JLabel lblIpInfo = new JLabel("Endereço IP desta máquina na rede: " + textoIps);
        lblIpInfo.setFont(lblIpInfo.getFont().deriveFont(Font.BOLD, 11.5f));
        lblIpInfo.setForeground(new Color(30, 50, 80));

        JLabel lblIpDica = new JLabel("Utilize este endereço para conectar outros terminais caso este computador seja o servidor principal.");
        lblIpDica.setFont(lblIpDica.getFont().deriveFont(Font.PLAIN, 10.5f));
        lblIpDica.setForeground(new Color(90, 110, 135));

        pnlIpInfo.add(lblIpInfo, BorderLayout.NORTH);
        pnlIpInfo.add(lblIpDica, BorderLayout.SOUTH);
        pnlTop.add(pnlIpInfo);

        pnlTop.add(Box.createVerticalStrut(10));

        // 2.2 Barra de Ações Rápidas (Apenas os dois botões profissionais)
        JPanel pnlAcoesRapidas = new JPanel(new BorderLayout(8, 6));
        pnlAcoesRapidas.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pnlBotoesRapidos = new JPanel(new GridLayout(1, 2, 8, 0));

        btnBuscarServidores = new JButton("Localizar Servidores na Rede");
        btnBuscarServidores.setFont(btnBuscarServidores.getFont().deriveFont(Font.BOLD, 11f));
        btnBuscarServidores.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuscarServidores.setFocusPainted(false);
        btnBuscarServidores.addActionListener(e -> executarBuscaServidoresRede());

        JButton btnPadraoLocal = new JButton("Restaurar Padrão Local");
        btnPadraoLocal.setFont(btnPadraoLocal.getFont().deriveFont(Font.BOLD, 11f));
        btnPadraoLocal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPadraoLocal.setFocusPainted(false);
        btnPadraoLocal.addActionListener(e -> restaurarPadraoLocal());

        pnlBotoesRapidos.add(btnBuscarServidores);
        pnlBotoesRapidos.add(btnPadraoLocal);
        pnlAcoesRapidas.add(pnlBotoesRapidos, BorderLayout.NORTH);

        // Painel dinâmico que exibe os servidores descobertos APENAS quando a busca é acionada
        pnlResultadoRede = new JPanel(new BorderLayout(6, 4));
        pnlResultadoRede.setBorder(BorderFactory.createEmptyBorder(6, 0, 2, 0));
        pnlResultadoRede.setVisible(false);

        lblStatusRede = new JLabel(" ");
        lblStatusRede.setFont(lblStatusRede.getFont().deriveFont(11f));

        cbServidoresDescobertos = new JComboBox<>();
        cbServidoresDescobertos.setFont(cbServidoresDescobertos.getFont().deriveFont(11.5f));
        cbServidoresDescobertos.addActionListener(e -> {
            Object sel = cbServidoresDescobertos.getSelectedItem();
            if (sel instanceof NetworkDiscoveryService.ServidorDescoberto) {
                NetworkDiscoveryService.ServidorDescoberto s = (NetworkDiscoveryService.ServidorDescoberto) sel;
                if (!s.getHost().isEmpty()) {
                    txtHost.setText(s.getHost());
                    txtPorta.setText(String.valueOf(s.getPorta()));
                    lblStatus.setText("Servidor " + s.getHost() + ":" + s.getPorta() + " selecionado.");
                    lblStatus.setForeground(new Color(40, 160, 90));
                }
            }
        });

        pnlResultadoRede.add(lblStatusRede, BorderLayout.NORTH);
        pnlResultadoRede.add(cbServidoresDescobertos, BorderLayout.CENTER);

        pnlAcoesRapidas.add(pnlResultadoRede, BorderLayout.SOUTH);
        pnlTop.add(pnlAcoesRapidas);

        pnlCenter.add(pnlTop, BorderLayout.NORTH);

        // 2.3 Formulário de Parâmetros de Conexão
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), " Parâmetros de Conexão: ",
                0, 0, new Font("SansSerif", Font.BOLD, 11)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Servidor / Host e Porta
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.72;
        JLabel lblHost = new JLabel("Servidor / Host (Endereço IP ou Domínio):");
        lblHost.setFont(lblHost.getFont().deriveFont(Font.BOLD, 12f));
        pnlForm.add(lblHost, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.28;
        JLabel lblPorta = new JLabel("Porta (Opcional):");
        lblPorta.setFont(lblPorta.getFont().deriveFont(Font.BOLD, 12f));
        pnlForm.add(lblPorta, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.72;
        txtHost = new JTextField();
        txtHost.setFont(txtHost.getFont().deriveFont(13f));
        txtHost.putClientProperty("JTextField.placeholderText", "Ex: localhost, 192.168.1.50 ou dominio.com");
        txtHost.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                sanitizarCamposHostPorta();
            }
        });
        pnlForm.add(txtHost, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.28;
        txtPorta = new JTextField();
        txtPorta.setFont(txtPorta.getFont().deriveFont(13f));
        txtPorta.putClientProperty("JTextField.placeholderText", "3306 (padrão)");
        pnlForm.add(txtPorta, gbc);

        // Nome do Banco
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        JLabel lblBanco = new JLabel("Nome do Banco de Dados:");
        lblBanco.setFont(lblBanco.getFont().deriveFont(Font.BOLD, 12f));
        pnlForm.add(lblBanco, gbc);

        gbc.gridy = 3;
        txtBanco = new JTextField();
        txtBanco.setFont(txtBanco.getFont().deriveFont(13f));
        txtBanco.putClientProperty("JTextField.placeholderText", "Ex: banco_assistencia");
        pnlForm.add(txtBanco, gbc);

        // Usuário
        gbc.gridy = 4;
        JLabel lblUsuario = new JLabel("Usuário:");
        lblUsuario.setFont(lblUsuario.getFont().deriveFont(Font.BOLD, 12f));
        pnlForm.add(lblUsuario, gbc);

        gbc.gridy = 5;
        txtUsuario = new JTextField();
        txtUsuario.setFont(txtUsuario.getFont().deriveFont(13f));
        txtUsuario.putClientProperty("JTextField.placeholderText", "Ex: root ou usuario_sistema");
        pnlForm.add(txtUsuario, gbc);

        // Senha
        gbc.gridy = 6;
        JLabel lblSenha = new JLabel("Senha:");
        lblSenha.setFont(lblSenha.getFont().deriveFont(Font.BOLD, 12f));
        pnlForm.add(lblSenha, gbc);

        gbc.gridy = 7;
        txtSenha = new JPasswordField();
        txtSenha.setFont(txtSenha.getFont().deriveFont(13f));
        txtSenha.putClientProperty("JTextField.placeholderText", "Deixe em branco se não houver senha");
        pnlForm.add(txtSenha, gbc);

        // Checkbox SSL
        gbc.gridy = 8;
        chkSsl = new JCheckBox("Usar Conexão Segura SSL (Recomendado para conexões remotas)");
        chkSsl.setFont(chkSsl.getFont().deriveFont(11.5f));
        pnlForm.add(chkSsl, gbc);

        // Status de Teste / Conexão
        gbc.gridy = 9;
        lblStatus = new JLabel(" ");
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD, 11f));
        pnlForm.add(lblStatus, gbc);

        pnlCenter.add(pnlForm, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        // 3. Rodapé com Botões de Ação
        JPanel pnlFooter = new JPanel(new BorderLayout(8, 8));
        pnlFooter.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));

        JPanel pnlBotoesAcao = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        btnTestar = new JButton("Testar Conexão");
        btnTestar.setFont(btnTestar.getFont().deriveFont(Font.BOLD, 12f));
        btnTestar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTestar.addActionListener(e -> testarConexaoAsync());

        btnSalvar = new JButton("Salvar e Conectar");
        btnSalvar.setFont(btnSalvar.getFont().deriveFont(Font.BOLD, 12f));
        btnSalvar.setBackground(new Color(39, 174, 96));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalvar.addActionListener(e -> salvarEConectar());

        pnlBotoesAcao.add(btnTestar);
        pnlBotoesAcao.add(btnSalvar);

        JButton btnAjuda = new JButton("Ajuda / Instruções");
        btnAjuda.setFont(btnAjuda.getFont().deriveFont(11f));
        btnAjuda.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAjuda.addActionListener(e -> exibirAjuda());

        pnlFooter.add(btnAjuda, BorderLayout.WEST);
        pnlFooter.add(pnlBotoesAcao, BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);

        // Listener para Enter nos campos
        KeyAdapter enterAction = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    salvarEConectar();
                }
            }
        };
        txtHost.addKeyListener(enterAction);
        txtPorta.addKeyListener(enterAction);
        txtBanco.addKeyListener(enterAction);
        txtUsuario.addKeyListener(enterAction);
        txtSenha.addKeyListener(enterAction);
    }

    private void restaurarPadraoLocal() {
        txtHost.setText("localhost");
        txtPorta.setText("3306");
        txtBanco.setText("banco_assistencia");
        txtUsuario.setText("root");
        txtSenha.setText("");
        chkSsl.setSelected(false);
        lblStatus.setText("Parâmetros de conexão padrão local carregados.");
        lblStatus.setForeground(new Color(52, 152, 219));
    }

    private void sanitizarCamposHostPorta() {
        String rawHost = txtHost.getText().trim();
        String rawPort = txtPorta.getText().trim();
        if (rawHost.contains(":") && !rawHost.startsWith("[")) {
            String[] parsed = ConnectionFactory.parseHostPort(rawHost, rawPort);
            txtHost.setText(parsed[0]);
            txtPorta.setText(parsed[1]);
        }
    }

    private void carregarDadosAtuais() {
        txtHost.setText(ConnectionFactory.getHost());
        txtPorta.setText(ConnectionFactory.getPort());
        txtBanco.setText(ConnectionFactory.getDatabase());
        txtUsuario.setText(ConnectionFactory.getUser());
        txtSenha.setText(ConnectionFactory.getPass());
        chkSsl.setSelected(ConnectionFactory.isSsl());
    }

    private void executarBuscaServidoresRede() {
        btnBuscarServidores.setEnabled(false);
        pnlResultadoRede.setVisible(true);
        lblStatusRede.setText("Varrendo a rede local em busca de servidores...");
        lblStatusRede.setForeground(new Color(52, 152, 219));
        cbServidoresDescobertos.setVisible(false);

        // Revalida o layout para acomodar o painel
        revalidate();
        repaint();

        NetworkDiscoveryService.buscarServidoresAsync(encontrados -> {
            btnBuscarServidores.setEnabled(true);
            cbServidoresDescobertos.removeAllItems();

            if (encontrados == null || encontrados.isEmpty()) {
                lblStatusRede.setText("Nenhum servidor remoto ativo respondeu na rede local.");
                lblStatusRede.setForeground(new Color(230, 126, 34));
                cbServidoresDescobertos.setVisible(false);
            } else {
                lblStatusRede.setText("Selecione um dos servidores encontrados na rede (" + encontrados.size() + "):");
                lblStatusRede.setForeground(new Color(40, 160, 90));

                cbServidoresDescobertos.addItem(new NetworkDiscoveryService.ServidorDescoberto(
                        "-- Selecione um Servidor Detectado --", "", 0, ""));
                for (NetworkDiscoveryService.ServidorDescoberto s : encontrados) {
                    cbServidoresDescobertos.addItem(s);
                }
                cbServidoresDescobertos.setVisible(true);
                if (cbServidoresDescobertos.getItemCount() > 1) {
                    cbServidoresDescobertos.setSelectedIndex(1);
                }
            }
            revalidate();
            repaint();
        }, progresso -> {
            lblStatusRede.setText(progresso);
        });
    }

    private void testarConexaoAsync() {
        sanitizarCamposHostPorta();
        String rawHost = txtHost.getText().trim();
        String rawPorta = txtPorta.getText().trim();
        String banco = txtBanco.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String senha = new String(txtSenha.getPassword());
        boolean useSsl = chkSsl.isSelected();

        String[] parsed = ConnectionFactory.parseHostPort(rawHost, rawPorta);
        String host = parsed[0];
        String porta = parsed[1];

        if (host.isEmpty() || banco.isEmpty() || usuario.isEmpty()) {
            lblStatus.setText("Preencha Servidor, Banco de Dados e Usuário!");
            lblStatus.setForeground(new Color(230, 126, 34));
            return;
        }

        btnTestar.setEnabled(false);
        btnSalvar.setEnabled(false);
        lblStatus.setText("Testando conexão com " + host + ":" + porta + "...");
        lblStatus.setForeground(new Color(52, 152, 219));

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return ConnectionFactory.testarConexaoCom(host, porta, banco, usuario, senha, useSsl);
            }

            @Override
            protected void done() {
                btnTestar.setEnabled(true);
                btnSalvar.setEnabled(true);
                try {
                    String erro = get();
                    if (erro == null) {
                        lblStatus.setText("Conexão bem-sucedida com " + host + ":" + porta + "!");
                        lblStatus.setForeground(new Color(46, 204, 113));
                    } else {
                        lblStatus.setText("Erro: " + erro);
                        lblStatus.setForeground(new Color(231, 76, 60));
                    }
                } catch (Exception ex) {
                    lblStatus.setText("Erro ao testar conexão.");
                    lblStatus.setForeground(new Color(231, 76, 60));
                }
            }
        };
        worker.execute();
    }

    private void salvarEConectar() {
        sanitizarCamposHostPorta();
        String rawHost = txtHost.getText().trim();
        String rawPorta = txtPorta.getText().trim();
        String banco = txtBanco.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String senha = new String(txtSenha.getPassword());
        boolean useSsl = chkSsl.isSelected();

        String[] parsed = ConnectionFactory.parseHostPort(rawHost, rawPorta);
        String host = parsed[0];
        String porta = parsed[1];

        if (host.isEmpty() || banco.isEmpty() || usuario.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Preencha os campos de Servidor, Banco e Usuário antes de continuar!",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnSalvar.setEnabled(false);
        lblStatus.setText("Salvando configurações e inicializando servidor em " + host + ":" + porta + "...");
        lblStatus.setForeground(new Color(52, 152, 219));

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            private String erroMsg = null;

            @Override
            protected Boolean doInBackground() {
                String erro = ConnectionFactory.testarConexaoCom(host, porta, banco, usuario, senha, useSsl);
                if (erro != null) {
                    erroMsg = erro;
                    return false;
                }
                try {
                    ConnectionFactory.salvarConfiguracoes(host, porta, banco, usuario, senha, useSsl);
                    return ConnectionFactory.criarTabela();
                } catch (Exception e) {
                    erroMsg = e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                btnSalvar.setEnabled(true);
                try {
                    boolean ok = get();
                    if (ok) {
                        configuradoComSucesso = true;
                        JOptionPane.showMessageDialog(ConfigBancoDialog.this,
                                "Configurações salvas com sucesso!\n" +
                                        "Conectado ao servidor: " + host + ":" + porta + "\n" +
                                        "Banco de dados: " + banco,
                                "Conexão Estabelecida",
                                JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        lblStatus.setText("Erro: " + (erroMsg != null ? erroMsg : "Falha na conexão."));
                        lblStatus.setForeground(new Color(231, 76, 60));
                        JOptionPane.showMessageDialog(ConfigBancoDialog.this,
                                "Não foi possível conectar com estes dados:\n\n" + erroMsg +
                                        "\n\nVerifique se o host, porta, usuário, senha e permissões de rede estão corretos.",
                                "Falha na Conexão",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    lblStatus.setText("Falha crítica: " + ex.getMessage());
                    lblStatus.setForeground(new Color(231, 76, 60));
                }
            }
        };
        worker.execute();
    }

    private void exibirAjuda() {
        String msg = "INSTRUÇÕES DE CONEXÃO COM O BANCO DE DADOS:\n\n" +
                "1. SERVIDOR REMOTO / HOSPEDADO (Nuvem ou VPS):\n" +
                "   • Servidor / Host: Digite o endereço ou domínio fornecido pelo seu provedor (Ex: db.empresa.com ou IP remoto).\n" +
                "   • Porta: Se não houver indicação diferente, mantenha 3306.\n" +
                "   • SSL: Marque a opção 'Usar Conexão Segura SSL' se o provedor exigir conexão criptografada.\n" +
                "   • Certifique-se de que o usuário possui permissão de conexão remota no servidor.\n\n" +
                "2. REDE LOCAL / TERMINAIS DA EMPRESA (Wi-Fi ou Cabo de Rede):\n" +
                "   • No computador Servidor Central:\n" +
                "     - Abra esta tela para visualizar o endereço IP exibido no topo.\n" +
                "     - Certifique-se de que a porta 3306 está liberada no Firewall do Windows do servidor.\n" +
                "   • Nos outros computadores ou notebooks da empresa:\n" +
                "     - Clique em 'Localizar Servidores na Rede' para listar os servidores disponíveis;\n" +
                "     - Ou digite o endereço IP do computador servidor no campo 'Servidor / Host'.\n\n" +
                "3. BANCO DE DADOS LOCAL (Monousuário):\n" +
                "   • Clique no botão 'Restaurar Padrão Local' para preencher automaticamente os parâmetros locais padrão (localhost:3306).";

        JOptionPane.showMessageDialog(this, msg, "Ajuda • Configuração de Servidor / Banco de Dados", JOptionPane.INFORMATION_MESSAGE);
    }

    public boolean isConfiguradoComSucesso() {
        return configuradoComSucesso;
    }
}
