package com.loja.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.loja.repository.*;
import com.loja.service.CaixaService;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private final ClienteDAO clienteDAO;
    private final EquipamentoDAO equipDAO;
    private final OrdemServicoDAO osDAO;
    private final ProdutoDAO produtoDAO;
    private final CaixaDAO caixaDAO;
    private final CaixaService caixaService;
    private final boolean dbConectado;

    private CardLayout cardLayout;
    private JPanel cardsPanel;
    private Map<String, JButton> navButtons = new HashMap<>();
    private String abaAtiva = "DASHBOARD";

    private DashboardPanel dashboardPanel;
    private ClientePanel clientePanel;
    private EquipamentoPanel equipPanel;
    private OrdemServicoPanel osPanel;
    private EstoquePanel estoquePanel;
    private CaixaPanel caixaPanel;

    private boolean modoEscuro = true;

    public MainFrame(ClienteDAO clienteDAO, EquipamentoDAO equipDAO, OrdemServicoDAO osDAO, 
                     ProdutoDAO produtoDAO, CaixaDAO caixaDAO, CaixaService caixaService, boolean dbConectado) {
        super("Sistema de Assistência Técnica & Gestão v2.0");
        this.clienteDAO = clienteDAO;
        this.equipDAO = equipDAO;
        this.osDAO = osDAO;
        this.produtoDAO = produtoDAO;
        this.caixaDAO = caixaDAO;
        this.caixaService = caixaService;
        this.dbConectado = dbConectado;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. Barra Superior (Header)
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        header.setBackground(new Color(24, 28, 36));

        JPanel pnlLogo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlLogo.setOpaque(false);
        JLabel lblIcone = new JLabel("🛠️");
        lblIcone.setFont(lblIcone.getFont().deriveFont(22f));
        JLabel lblTitulo = new JLabel("ASSISTÊNCIA PRO");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 18f));
        lblTitulo.setForeground(Color.WHITE);
        JLabel lblSub = new JLabel("| Sistema de Gestão & Ordens de Serviço");
        lblSub.setForeground(new Color(170, 178, 190));

        pnlLogo.add(lblIcone);
        pnlLogo.add(lblTitulo);
        pnlLogo.add(lblSub);

        JPanel pnlHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlHeaderRight.setOpaque(false);

        JLabel lblDbStatus = new JLabel(dbConectado ? "🟢 MySQL Conectado (3306)" : "🔴 MySQL Desconectado");
        lblDbStatus.setFont(lblDbStatus.getFont().deriveFont(Font.BOLD, 12f));
        lblDbStatus.setForeground(dbConectado ? new Color(46, 204, 113) : new Color(231, 76, 60));

        JButton btnTema = new JButton(modoEscuro ? "☀️ Modo Claro" : "🌙 Modo Escuro");
        btnTema.addActionListener(e -> alternarTema(btnTema));

        pnlHeaderRight.add(lblDbStatus);
        pnlHeaderRight.add(btnTema);

        header.add(pnlLogo, BorderLayout.WEST);
        header.add(pnlHeaderRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // 2. Menu Lateral (Sidebar)
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        adicionarBotaoNavegacao(sidebar, "🏠 Visão Geral", "DASHBOARD");
        sidebar.add(Box.createVerticalStrut(6));
        adicionarBotaoNavegacao(sidebar, "👥 Clientes", "CLIENTES");
        sidebar.add(Box.createVerticalStrut(6));
        adicionarBotaoNavegacao(sidebar, "💻 Equipamentos", "EQUIPAMENTOS");
        sidebar.add(Box.createVerticalStrut(6));
        adicionarBotaoNavegacao(sidebar, "🛠️ Ordens de Serviço", "OS");
        sidebar.add(Box.createVerticalStrut(6));
        adicionarBotaoNavegacao(sidebar, "📦 Estoque de Peças", "ESTOQUE");
        sidebar.add(Box.createVerticalStrut(6));
        adicionarBotaoNavegacao(sidebar, "💵 Caixa & Finanças", "CAIXA");

        sidebar.add(Box.createVerticalGlue());

        add(sidebar, BorderLayout.WEST);

        // 3. Área Central (CardLayout com as telas)
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        dashboardPanel = new DashboardPanel(this, clienteDAO, equipDAO, osDAO, produtoDAO, caixaDAO);
        clientePanel = new ClientePanel(this, clienteDAO, equipDAO);
        equipPanel = new EquipamentoPanel(this, equipDAO, clienteDAO);
        osPanel = new OrdemServicoPanel(this, osDAO, clienteDAO, equipDAO, produtoDAO, caixaService);
        estoquePanel = new EstoquePanel(this, produtoDAO);
        caixaPanel = new CaixaPanel(this, caixaService, caixaDAO);

        cardsPanel.add(dashboardPanel, "DASHBOARD");
        cardsPanel.add(clientePanel, "CLIENTES");
        cardsPanel.add(equipPanel, "EQUIPAMENTOS");
        cardsPanel.add(osPanel, "OS");
        cardsPanel.add(estoquePanel, "ESTOQUE");
        cardsPanel.add(caixaPanel, "CAIXA");

        add(cardsPanel, BorderLayout.CENTER);

        selecionarAba("DASHBOARD");
    }

    private void adicionarBotaoNavegacao(JPanel sidebar, String texto, String cardName) {
        JButton btn = new JButton(texto);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(200, 44));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 13f));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> selecionarAba(cardName));

        navButtons.put(cardName, btn);
        sidebar.add(btn);
    }

    public void selecionarAba(String cardName) {
        this.abaAtiva = cardName;
        cardLayout.show(cardsPanel, cardName);

        navButtons.forEach((name, btn) -> {
            if (name.equals(cardName)) {
                btn.putClientProperty("JButton.buttonType", "roundRect");
                btn.setFont(btn.getFont().deriveFont(Font.BOLD, 14f));
            } else {
                btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 13f));
            }
        });

        recarregarTodasAsAbas();
    }

    public void recarregarTodasAsAbas() {
        dashboardPanel.recarregarMetricas();
        clientePanel.recarregarTabela();
        equipPanel.recarregarFiltroClientes();
        equipPanel.recarregarTabela();
        osPanel.recarregarTabela();
        estoquePanel.recarregarTabela();
        caixaPanel.recarregarDados();
    }

    private void alternarTema(JButton btnTema) {
        modoEscuro = !modoEscuro;
        try {
            if (modoEscuro) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                btnTema.setText("☀️ Modo Claro");
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
                btnTema.setText("🌙 Modo Escuro");
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
