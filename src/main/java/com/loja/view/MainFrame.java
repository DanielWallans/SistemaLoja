package com.loja.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.loja.model.PerfilUsuario;
import com.loja.model.SessaoUsuario;
import com.loja.model.Usuario;
import com.loja.repository.*;
import com.loja.service.CaixaService;
import com.loja.view.dialogs.BackupDialog;
import com.loja.view.dialogs.GerenciadorUsuariosDialog;
import com.loja.view.dialogs.LoginDialog;
import com.loja.view.theme.ThemeTokens;
import com.loja.view.theme.UIComponents;
import com.loja.view.theme.UITheme;
import com.loja.service.update.UpdateInfo;
import com.loja.service.update.UpdateService;
import com.loja.view.dialogs.AtualizacaoDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private final ClienteDAO clienteDAO;
    private final EquipamentoDAO equipDAO;
    private final OrdemServicoDAO osDAO;
    private final ProdutoDAO produtoDAO;
    private final CaixaDAO caixaDAO;
    private final CaixaService caixaService;
    private final UsuarioDAO usuarioDAO;
    private final boolean dbConectado;

    private CardLayout cardLayout;
    private JPanel cardsPanel;
    private JPanel sidebar;
    private Map<String, JButton> navButtons = new HashMap<>();
    private String abaAtiva = "DASHBOARD";

    private DashboardPanel dashboardPanel;
    private ClientePanel clientePanel;
    private EquipamentoPanel equipPanel;
    private OrdemServicoPanel osPanel;
    private PDVPanel pdvPanel;
    private EstoquePanel estoquePanel;
    private CaixaPanel caixaPanel;

    private JLabel lblUsuarioLogado;
    private JButton btnGerenciarUsuarios;
    private JButton btnBackup;
    private JLabel lblStatusFeedback;
    private JPanel header;
    private JPanel footer;
    private JPanel pnlAtalhos;
    private JLabel lblTituloLogo;
    private JLabel lblSubLogo;

    private boolean modoEscuro = true;

    public MainFrame(ClienteDAO clienteDAO, EquipamentoDAO equipDAO, OrdemServicoDAO osDAO,
            ProdutoDAO produtoDAO, CaixaDAO caixaDAO, CaixaService caixaService,
            UsuarioDAO usuarioDAO, boolean dbConectado) {
        super("System Pro - Gestão & Assistência Técnica v2.0");
        this.clienteDAO = clienteDAO;
        this.equipDAO = equipDAO;
        this.osDAO = osDAO;
        this.produtoDAO = produtoDAO;
        this.caixaDAO = caixaDAO;
        this.caixaService = caixaService;
        this.usuarioDAO = usuarioDAO != null ? usuarioDAO : new UsuarioDAO();
        this.dbConectado = dbConectado;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 790);
        setMinimumSize(new Dimension(1000, 660));
        setLocationRelativeTo(null);

        initComponents();
        aplicarPermissoesPerfil();
        configurarAtalhosGlobais();
        iniciarVerificacaoAtualizacaoSilenciosa();
    }

    private void initComponents() {
        ThemeTokens t = UITheme.tokens();
        setLayout(new BorderLayout());

        // 1. Barra Superior (Header)
        header = new JPanel(new BorderLayout(15, 0));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, t.getBorderSubtle()),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        header.setBackground(t.getBgSidebar());

        JPanel pnlLogo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlLogo.setOpaque(false);

        lblTituloLogo = new JLabel("SYSTEM PRO");
        lblTituloLogo.setFont(UITheme.FONT_TITLE);
        lblTituloLogo.setForeground(t.getTextPrimary());

        lblSubLogo = new JLabel("| Sistema de Gestão");
        lblSubLogo.setFont(UITheme.FONT_SUBTITLE);
        lblSubLogo.setForeground(t.getTextSecondary());

        pnlLogo.add(lblTituloLogo);
        pnlLogo.add(lblSubLogo);

        JPanel pnlHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlHeaderRight.setOpaque(false);

        // Usuário Conectado
        Usuario user = SessaoUsuario.getInstancia().getUsuarioLogado();
        lblUsuarioLogado = new JLabel(formatarBadgeUsuario(user));
        lblUsuarioLogado.setFont(UITheme.FONT_CAPTION);
        lblUsuarioLogado.setForeground(t.getTextPrimary());

        // Botões Administrativos
        btnGerenciarUsuarios = new JButton("Usuários");
        btnGerenciarUsuarios.setFont(UITheme.FONT_CAPTION);
        btnGerenciarUsuarios.putClientProperty("JButton.arc", 6);
        btnGerenciarUsuarios.addActionListener(e -> abrirGerenciadorUsuarios());

        btnBackup = new JButton("Backup MySQL");
        btnBackup.setFont(UITheme.FONT_CAPTION);
        btnBackup.putClientProperty("JButton.arc", 6);
        btnBackup.addActionListener(e -> abrirBackupDialog());

        JLabel lblDbStatus = new JLabel(dbConectado ? "MySQL 3306" : "MySQL Offline");
        lblDbStatus.setFont(UITheme.FONT_CAPTION);
        lblDbStatus.setForeground(dbConectado ? t.getSuccess() : t.getDanger());

        JButton btnTema = new JButton(modoEscuro ? "Claro" : "Escuro");
        btnTema.setFont(UITheme.FONT_CAPTION);
        btnTema.putClientProperty("JButton.arc", 6);
        btnTema.addActionListener(e -> alternarTema(btnTema));

        JButton btnLogout = new JButton("Sair");
        btnLogout.setFont(UITheme.FONT_CAPTION);
        btnLogout.putClientProperty("JButton.arc", 6);
        btnLogout.addActionListener(e -> realizarLogout());

        JButton btnAtualizacoes = new JButton("Atualizações");
        btnAtualizacoes.setFont(UITheme.FONT_CAPTION);
        btnAtualizacoes.putClientProperty("JButton.arc", 6);
        btnAtualizacoes.addActionListener(e -> checarAtualizacoesManual());

        pnlHeaderRight.add(lblUsuarioLogado);
        pnlHeaderRight.add(btnGerenciarUsuarios);
        pnlHeaderRight.add(btnBackup);
        pnlHeaderRight.add(btnAtualizacoes);
        pnlHeaderRight.add(lblDbStatus);
        pnlHeaderRight.add(btnTema);
        pnlHeaderRight.add(btnLogout);

        header.add(pnlLogo, BorderLayout.WEST);
        header.add(pnlHeaderRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // 2. Menu Lateral (Sidebar)
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(t.getBgSidebar());
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, t.getBorderSubtle()),
                BorderFactory.createEmptyBorder(16, 12, 16, 12)));

        add(sidebar, BorderLayout.WEST);

        // 3. Área Central (CardLayout com as telas)
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(t.getBgApp());

        dashboardPanel = new DashboardPanel(this, clienteDAO, equipDAO, osDAO, produtoDAO, caixaDAO, caixaService);
        pdvPanel = new PDVPanel(this, produtoDAO, caixaDAO);
        clientePanel = new ClientePanel(this, clienteDAO, equipDAO);
        equipPanel = new EquipamentoPanel(this, equipDAO, clienteDAO);
        osPanel = new OrdemServicoPanel(this, osDAO, clienteDAO, equipDAO, produtoDAO, caixaService);
        estoquePanel = new EstoquePanel(this, produtoDAO);
        caixaPanel = new CaixaPanel(this, caixaService, caixaDAO);

        cardsPanel.add(dashboardPanel, "DASHBOARD");
        cardsPanel.add(pdvPanel, "PDV");
        cardsPanel.add(clientePanel, "CLIENTES");
        cardsPanel.add(equipPanel, "EQUIPAMENTOS");
        cardsPanel.add(osPanel, "OS");
        cardsPanel.add(estoquePanel, "ESTOQUE");
        cardsPanel.add(caixaPanel, "CAIXA");

        add(cardsPanel, BorderLayout.CENTER);

        // 4. Barra Inferior de Atalhos Rápidos (Footer)
        footer = new JPanel(new BorderLayout(15, 0));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, t.getBorderSubtle()),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        footer.setBackground(t.getBgSidebar());

        pnlAtalhos = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlAtalhos.setOpaque(false);

        pnlAtalhos.add(UIComponents.criarBadgeAtalho("F2", "Nova OS", this::acionarAtalhoF2NovaOS));
        pnlAtalhos.add(UIComponents.criarBadgeAtalho("F3", "Buscar Cliente", this::acionarAtalhoF3BuscarCliente));
        pnlAtalhos.add(UIComponents.criarBadgeAtalho("F4", "Frente de Caixa", this::acionarAtalhoF4FrenteCaixa));
        pnlAtalhos.add(UIComponents.criarBadgeAtalho("F5", "Atualizar Tabelas", this::acionarAtalhoF5AtualizarTabelas));

        lblStatusFeedback = new JLabel("Atalhos Rápidos Ativos • F2, F3, F4, F5");
        lblStatusFeedback.setFont(UITheme.FONT_SMALL);
        lblStatusFeedback.setForeground(t.getTextSecondary());

        footer.add(pnlAtalhos, BorderLayout.WEST);
        footer.add(lblStatusFeedback, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    private String formatarBadgeUsuario(Usuario user) {
        if (user == null)
            return "Convidado";
        String perfil = user.getPerfil() != null ? user.getPerfil().getNomeExibicao() : "Usuário";
        return String.format("%s • %s", user.getNome(), perfil);
    }

    public void aplicarPermissoesPerfil() {
        Usuario user = SessaoUsuario.getInstancia().getUsuarioLogado();
        sidebar.removeAll();
        navButtons.clear();

        boolean isAdmin = user == null || user.isAdmin();
        boolean isTecnico = user != null && user.isTecnico();
        boolean isAtendente = user != null && user.isAtendente();

        btnGerenciarUsuarios.setVisible(isAdmin);
        btnBackup.setVisible(isAdmin);
        lblUsuarioLogado.setText(formatarBadgeUsuario(user));

        if (isAdmin) {
            adicionarBotaoNavegacao(sidebar, "Visão Geral", "DASHBOARD");
            sidebar.add(Box.createVerticalStrut(4));
            adicionarBotaoNavegacao(sidebar, "PDV Balcão [F4]", "PDV");
            sidebar.add(Box.createVerticalStrut(4));
            adicionarBotaoNavegacao(sidebar, "Clientes [F3]", "CLIENTES");
            sidebar.add(Box.createVerticalStrut(4));
            adicionarBotaoNavegacao(sidebar, "Equipamentos", "EQUIPAMENTOS");
            sidebar.add(Box.createVerticalStrut(4));
            adicionarBotaoNavegacao(sidebar, "Ordens de Serviço [F2]", "OS");
            sidebar.add(Box.createVerticalStrut(4));
            adicionarBotaoNavegacao(sidebar, "Estoque de Peças", "ESTOQUE");
            sidebar.add(Box.createVerticalStrut(4));
            adicionarBotaoNavegacao(sidebar, "Caixa & Finanças", "CAIXA");
            selecionarAba("DASHBOARD");

        } else if (isTecnico) {
            adicionarBotaoNavegacao(sidebar, "Ordens de Serviço [F2]", "OS");
            sidebar.add(Box.createVerticalStrut(4));
            adicionarBotaoNavegacao(sidebar, "Estoque de Peças", "ESTOQUE");
            sidebar.add(Box.createVerticalStrut(4));
            adicionarBotaoNavegacao(sidebar, "Clientes [F3]", "CLIENTES");
            sidebar.add(Box.createVerticalStrut(4));
            adicionarBotaoNavegacao(sidebar, "Equipamentos", "EQUIPAMENTOS");
            selecionarAba("OS");

        } else if (isAtendente) {
            adicionarBotaoNavegacao(sidebar, "PDV Balcão [F4]", "PDV");
            sidebar.add(Box.createVerticalStrut(4));
            adicionarBotaoNavegacao(sidebar, "Clientes [F3]", "CLIENTES");
            sidebar.add(Box.createVerticalStrut(4));
            adicionarBotaoNavegacao(sidebar, "Equipamentos", "EQUIPAMENTOS");
            sidebar.add(Box.createVerticalStrut(4));
            adicionarBotaoNavegacao(sidebar, "Ordens de Serviço [F2]", "OS");
            selecionarAba("PDV");
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.revalidate();
        sidebar.repaint();

        if (osPanel != null) {
            osPanel.aplicarPermissoesPerfil();
        }
    }

    private void adicionarBotaoNavegacao(JPanel sidebar, String texto, String cardName) {
        JButton btn = new JButton(texto);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setPreferredSize(new Dimension(210, 36));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(UITheme.FONT_BODY);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.putClientProperty("JButton.arc", 6);

        btn.addActionListener(e -> selecionarAba(cardName));

        navButtons.put(cardName, btn);
        sidebar.add(btn);
    }

    public void selecionarAba(String cardName) {
        this.abaAtiva = cardName;
        cardLayout.show(cardsPanel, cardName);
        aplicarEstiloBotoesNav();
        recarregarTodasAsAbas();
    }

    private void aplicarEstiloBotoesNav() {
        ThemeTokens t = UITheme.tokens();
        navButtons.forEach((name, btn) -> {
            btn.putClientProperty("JButton.arc", 6);
            if (name.equals(abaAtiva)) {
                btn.setBackground(new Color(34, 35, 38)); // #222326
                btn.setForeground(new Color(241, 241, 241)); // #F1F1F1
                btn.setFont(UITheme.FONT_BODY_BOLD);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(237, 237, 237)),
                        BorderFactory.createEmptyBorder(6, 12, 6, 14)));
            } else {
                btn.setBackground(t.getBgSidebar()); // #141516
                btn.setForeground(new Color(168, 168, 168)); // #A8A8A8
                btn.setFont(UITheme.FONT_BODY);
                btn.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 14));
            }
        });
    }

    private void atualizarEstilosTema() {
        ThemeTokens t = UITheme.tokens();
        if (header != null) {
            header.setBackground(t.getBgSidebar());
            header.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, t.getBorderSubtle()),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        }
        if (lblTituloLogo != null)
            lblTituloLogo.setForeground(t.getTextPrimary());
        if (lblSubLogo != null)
            lblSubLogo.setForeground(t.getTextSecondary());
        if (lblUsuarioLogado != null)
            lblUsuarioLogado.setForeground(t.getTextPrimary());

        if (sidebar != null) {
            sidebar.setBackground(t.getBgSidebar());
            sidebar.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, t.getBorderSubtle()),
                    BorderFactory.createEmptyBorder(16, 12, 16, 12)));
        }

        if (cardsPanel != null) {
            cardsPanel.setBackground(t.getBgApp());
        }

        if (footer != null) {
            footer.setBackground(t.getBgSidebar());
            footer.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, t.getBorderSubtle()),
                    BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        }

        aplicarEstiloBotoesNav();
    }

    public void recarregarTodasAsAbas() {
        dashboardPanel.recarregarMetricas();
        clientePanel.recarregarTabela();
        equipPanel.recarregarFiltroClientes();
        equipPanel.recarregarTabela();
        osPanel.recarregarTabela();
        estoquePanel.recarregarTabela();
        caixaPanel.recarregarDados();
        pdvPanel.atualizarStatusCaixa();
    }

    private void abrirGerenciadorUsuarios() {
        GerenciadorUsuariosDialog dialog = new GerenciadorUsuariosDialog(this, usuarioDAO);
        dialog.setVisible(true);
    }

    private void abrirBackupDialog() {
        BackupDialog dialog = new BackupDialog(this);
        dialog.setVisible(true);
    }

    private void realizarLogout() {
        int opt = JOptionPane.showConfirmDialog(this,
                "Deseja realmente sair e trocar de usuário?",
                "Confirmar Saída", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (opt == JOptionPane.YES_OPTION) {
            SessaoUsuario.getInstancia().encerrarSessao();
            LoginDialog login = new LoginDialog(this, usuarioDAO);
            login.setVisible(true);

            if (login.isAutenticado()) {
                aplicarPermissoesPerfil();
            } else {
                System.exit(0);
            }
        }
    }

    private void alternarTema(JButton btnTema) {
        modoEscuro = !modoEscuro;
        UITheme.setDark(modoEscuro);
        try {
            if (modoEscuro) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                btnTema.setText("Claro");
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
                btnTema.setText("Escuro");
            }
            // Reaplicar propriedades FlatLaf modernas com cantos sutis
            UIManager.put("Button.arc", 6);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 6);
            UIManager.put("TabbedPane.tabArc", 6);
            UIManager.put("Table.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("Table.rowHeight", 34);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));

            ThemeTokens t = UITheme.tokens();
            UIManager.put("Component.accentColor", t.getPrimaryAccent());
            UIManager.put("Component.focusColor", UITheme.withAlpha(t.getPrimaryAccent(), 0.35f));
            UIManager.put("Component.focusedBorderColor", t.getPrimaryAccent());
            UIManager.put("Label.foreground", t.getTextPrimary());
            UIManager.put("Table.foreground", t.getTextPrimary());
            UIManager.put("TableHeader.foreground", t.getTextPrimary());
            UIManager.put("TextField.foreground", t.getTextPrimary());
            UIManager.put("TextArea.foreground", t.getTextPrimary());
            UIManager.put("ComboBox.foreground", t.getTextPrimary());
            UIManager.put("CheckBox.foreground", t.getTextPrimary());
            UIManager.put("TitledBorder.titleColor", t.getTextPrimary());

            atualizarEstilosTema();
            SwingUtilities.updateComponentTreeUI(this);
            recarregarTodasAsAbas();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void configurarAtalhosGlobais() {
        JRootPane root = getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        // F2 - Nova OS
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "atalho_f2_nova_os");
        am.put("atalho_f2_nova_os", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acionarAtalhoF2NovaOS();
            }
        });

        // F3 - Buscar Cliente
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "atalho_f3_buscar_cliente");
        am.put("atalho_f3_buscar_cliente", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acionarAtalhoF3BuscarCliente();
            }
        });

        // F4 - Frente de Caixa (PDV)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "atalho_f4_pdv");
        am.put("atalho_f4_pdv", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acionarAtalhoF4FrenteCaixa();
            }
        });

        // F5 - Atualizar Tabelas
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "atalho_f5_atualizar");
        am.put("atalho_f5_atualizar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acionarAtalhoF5AtualizarTabelas();
            }
        });
    }

    public void acionarAtalhoF2NovaOS() {
        Usuario user = SessaoUsuario.getInstancia().getUsuarioLogado();
        if (user != null && user.isTecnico()) {
            selecionarAba("OS");
            JOptionPane.showMessageDialog(this,
                    "O perfil Técnico possui acesso apenas para visualização e atualização de Ordens de Serviço.\n" +
                            "A abertura de novas Ordens de Serviço é de responsabilidade do Atendente ou Administrador.",
                    "Acesso Restrito", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        selecionarAba("OS");
        SwingUtilities.invokeLater(() -> osPanel.abrirNovaOS());
        atualizarStatusFeedback("Atalho [F2]: Nova Ordem de Serviço!");
    }

    public void acionarAtalhoF3BuscarCliente() {
        selecionarAba("CLIENTES");
        SwingUtilities.invokeLater(() -> clientePanel.focarBusca());
        atualizarStatusFeedback("Atalho [F3]: Campo de busca de clientes focado!");
    }

    public void acionarAtalhoF4FrenteCaixa() {
        Usuario user = SessaoUsuario.getInstancia().getUsuarioLogado();
        if (user != null && user.isTecnico()) {
            JOptionPane.showMessageDialog(this,
                    "O perfil Técnico não possui acesso à Frente de Caixa (PDV).",
                    "Acesso Restrito", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        selecionarAba("PDV");
        SwingUtilities.invokeLater(() -> pdvPanel.focarEntrada());
        atualizarStatusFeedback("Atalho [F4]: Frente de Caixa (PDV) aberta!");
    }

    public void acionarAtalhoF5AtualizarTabelas() {
        recarregarTodasAsAbas();
        atualizarStatusFeedback("Atalho [F5]: Todas as tabelas e dados foram atualizados!");
    }

    private JButton criarBadgeAtalho(String tecla, String rotulo, Runnable acao) {
        JButton btn = new JButton(
                String.format("<html><b><font color='#3498db'>[%s]</font></b> %s</html>", tecla, rotulo));
        btn.setFont(btn.getFont().deriveFont(11.5f));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusable(false);
        btn.setToolTipText("Pressione " + tecla + " no teclado ou clique aqui");
        btn.addActionListener(e -> acao.run());
        return btn;
    }

    private void atualizarStatusFeedback(String msg) {
        if (lblStatusFeedback != null) {
            lblStatusFeedback.setText(msg);
            lblStatusFeedback.setForeground(new Color(46, 204, 113));
            Timer timer = new Timer(3500, e -> {
                lblStatusFeedback.setText("Atalhos Rápidos Ativos • F2, F3, F4, F5");
                lblStatusFeedback.setForeground(new Color(160, 172, 188));
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    private void iniciarVerificacaoAtualizacaoSilenciosa() {
        Timer timer = new Timer(3000, e -> {
            SwingWorker<UpdateInfo, Void> worker = new SwingWorker<UpdateInfo, Void>() {
                @Override
                protected UpdateInfo doInBackground() {
                    return UpdateService.verificarAtualizacao();
                }

                @Override
                protected void done() {
                    try {
                        UpdateInfo info = get();
                        if (info != null) {
                            new AtualizacaoDialog(MainFrame.this, info).setVisible(true);
                        }
                    } catch (Exception ignored) {
                    }
                }
            };
            worker.execute();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void checarAtualizacoesManual() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        atualizarStatusFeedback("Verificando se há novas atualizações na nuvem...");

        SwingWorker<UpdateInfo, Void> worker = new SwingWorker<UpdateInfo, Void>() {
            @Override
            protected UpdateInfo doInBackground() {
                return UpdateService.verificarAtualizacao();
            }

            @Override
            protected void done() {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                try {
                    UpdateInfo info = get();
                    if (info != null) {
                        new AtualizacaoDialog(MainFrame.this, info).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "Você já está utilizando a versão mais recente do sistema!\n\nVersão atual: v"
                                        + UpdateService.VERSAO_ATUAL,
                                "Sistema Atualizado",
                                JOptionPane.INFORMATION_MESSAGE);
                        atualizarStatusFeedback(
                                "Sistema na versão mais recente (v" + UpdateService.VERSAO_ATUAL + ")");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Não foi possível verificar atualizações no momento.\nVerifique sua conexão com a internet.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
