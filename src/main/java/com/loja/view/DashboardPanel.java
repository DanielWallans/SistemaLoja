package com.loja.view;

import com.loja.model.OrdemServico;
import com.loja.repository.CaixaDAO;
import com.loja.repository.ClienteDAO;
import com.loja.repository.EquipamentoDAO;
import com.loja.repository.OrdemServicoDAO;
import com.loja.repository.ProdutoDAO;
import com.loja.view.dialogs.ClienteDialog;
import com.loja.view.dialogs.NovaOSDialog;
import com.loja.view.dialogs.ProdutoDialog;
import com.loja.view.theme.ThemeTokens;
import com.loja.view.theme.UIComponents;
import com.loja.view.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {
    private final ClienteDAO clienteDAO;
    private final EquipamentoDAO equipDAO;
    private final OrdemServicoDAO osDAO;
    private final ProdutoDAO produtoDAO;
    private final CaixaDAO caixaDAO;
    private final Frame owner;
    private final MainFrame mainFrame;

    private JLabel lblCardOS;
    private JLabel lblCardClientes;
    private JLabel lblCardEstoque;
    private JLabel lblCardCaixa;

    public DashboardPanel(MainFrame mainFrame, ClienteDAO clienteDAO, EquipamentoDAO equipDAO, 
                          OrdemServicoDAO osDAO, ProdutoDAO produtoDAO, CaixaDAO caixaDAO) {
        this.mainFrame = mainFrame;
        this.owner = mainFrame;
        this.clienteDAO = clienteDAO;
        this.equipDAO = equipDAO;
        this.osDAO = osDAO;
        this.produtoDAO = produtoDAO;
        this.caixaDAO = caixaDAO;

        setLayout(new BorderLayout(0, UITheme.SPACE_24));
        setBorder(BorderFactory.createEmptyBorder(UITheme.SPACE_24, UITheme.SPACE_32, UITheme.SPACE_24, UITheme.SPACE_32));

        initComponents();
        recarregarMetricas();
    }

    private void initComponents() {
        ThemeTokens t = UITheme.tokens();

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // 1. Saudação / Banner Superior
        JPanel pnlHeader = new JPanel(new BorderLayout(0, UITheme.SPACE_4));
        pnlHeader.setOpaque(false);

        JLabel lblOla = new JLabel("Painel Geral de Controle");
        lblOla.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblOla.setForeground(t.getTextPrimary());

        JLabel lblSub = new JLabel("Visão executiva do fluxo de trabalho e métricas da assistência técnica.");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(t.getTextSecondary());

        pnlHeader.add(lblOla, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        centerPanel.add(pnlHeader);
        centerPanel.add(Box.createVerticalStrut(UITheme.SPACE_24));

        // 2. Grid com 4 Cards de Indicadores
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, UITheme.SPACE_16, 0));
        pnlCards.setOpaque(false);

        lblCardOS = new JLabel("0");
        lblCardClientes = new JLabel("0");
        lblCardEstoque = new JLabel("0");
        lblCardCaixa = new JLabel("R$ 0,00");

        pnlCards.add(UIComponents.criarCardMetrica("OS EM ABERTO", lblCardOS, "🛠️", t.getPrimaryAccent()));
        pnlCards.add(UIComponents.criarCardMetrica("CLIENTES", lblCardClientes, "👥", t.getInfo()));
        pnlCards.add(UIComponents.criarCardMetrica("PEÇAS EM ESTOQUE", lblCardEstoque, "📦", t.getWarning()));
        pnlCards.add(UIComponents.criarCardMetrica("SALDO EM CAIXA", lblCardCaixa, "💵", t.getSuccess()));

        centerPanel.add(pnlCards);
        centerPanel.add(Box.createVerticalStrut(UITheme.SPACE_32));

        // 3. Seção de Ações Rápidas (Card Moderno)
        JPanel pnlAcoesContainer = new JPanel(new BorderLayout(0, UITheme.SPACE_16)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(t.getBgCard());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(t.getBorderSubtle());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pnlAcoesContainer.setOpaque(false);
        pnlAcoesContainer.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACE_24, UITheme.SPACE_24, UITheme.SPACE_24, UITheme.SPACE_24));

        JPanel pnlAcoesHeader = new JPanel(new BorderLayout(0, UITheme.SPACE_4));
        pnlAcoesHeader.setOpaque(false);

        JLabel lblAcoesTit = new JLabel("⚡ Ações & Acessos Rápidos");
        lblAcoesTit.setFont(UITheme.FONT_TITLE);
        lblAcoesTit.setForeground(t.getTextPrimary());

        JLabel lblAcoesSub = new JLabel("Inicie novos atendimentos, cadastros e movimentações com um clique.");
        lblAcoesSub.setFont(UITheme.FONT_SUBTITLE);
        lblAcoesSub.setForeground(t.getTextSecondary());

        pnlAcoesHeader.add(lblAcoesTit, BorderLayout.NORTH);
        pnlAcoesHeader.add(lblAcoesSub, BorderLayout.SOUTH);
        pnlAcoesContainer.add(pnlAcoesHeader, BorderLayout.NORTH);

        JPanel pnlBotoes = new JPanel(new GridLayout(1, 3, UITheme.SPACE_16, 0));
        pnlBotoes.setOpaque(false);

        JButton btnNovaOS = UIComponents.criarBotaoPrimario("🛠️ Abrir Nova OS [F2]", () -> {
            NovaOSDialog d = new NovaOSDialog(owner, clienteDAO, equipDAO, osDAO);
            d.setVisible(true);
            recarregarMetricas();
            mainFrame.recarregarTodasAsAbas();
        });
        btnNovaOS.setPreferredSize(new Dimension(180, 46));

        JButton btnNovoCli = UIComponents.criarBotaoSecundario("👤 Cadastrar Cliente", () -> {
            ClienteDialog d = new ClienteDialog(owner, clienteDAO, null);
            d.setVisible(true);
            recarregarMetricas();
            mainFrame.recarregarTodasAsAbas();
        });
        btnNovoCli.setPreferredSize(new Dimension(180, 46));

        JButton btnNovoProd = UIComponents.criarBotaoSecundario("📦 Cadastrar Produto / Peça", () -> {
            ProdutoDialog d = new ProdutoDialog(owner, produtoDAO, null);
            d.setVisible(true);
            recarregarMetricas();
            mainFrame.recarregarTodasAsAbas();
        });
        btnNovoProd.setPreferredSize(new Dimension(180, 46));

        pnlBotoes.add(btnNovaOS);
        pnlBotoes.add(btnNovoCli);
        pnlBotoes.add(btnNovoProd);
        pnlAcoesContainer.add(pnlBotoes, BorderLayout.CENTER);

        centerPanel.add(pnlAcoesContainer);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void recarregarMetricas() {
        List<OrdemServico> oss = osDAO.buscarTodos();
        long osAbertas = oss.stream().filter(os -> !"Entregue".equalsIgnoreCase(os.getStatus()) && !"Cancelada".equalsIgnoreCase(os.getStatus())).count();
        lblCardOS.setText(String.valueOf(osAbertas));

        int totalClientes = clienteDAO.buscarTodos().size();
        lblCardClientes.setText(String.valueOf(totalClientes));

        int totalPecas = produtoDAO.buscarTodos().size();
        lblCardEstoque.setText(String.valueOf(totalPecas));

        double saldo = caixaDAO.obterSaldo();
        lblCardCaixa.setText(String.format("R$ %.2f", saldo));
    }
}
