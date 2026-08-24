package com.loja.view;

import com.loja.model.OrdemServico;
import com.loja.repository.CaixaDAO;
import com.loja.repository.ClienteDAO;
import com.loja.repository.EquipamentoDAO;
import com.loja.repository.OrdemServicoDAO;
import com.loja.repository.ProdutoDAO;
import com.loja.service.CaixaService;
import com.loja.view.dialogs.ClienteDialog;
import com.loja.view.dialogs.NovaOSDialog;
import com.loja.view.dialogs.ProdutoDialog;

import javax.swing.*;
import javax.swing.border.TitledBorder;
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

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        initComponents();
        recarregarMetricas();
    }

    private void initComponents() {
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // 1. Saudação / Banner Superior
        JPanel pnlBoasVindas = new JPanel(new BorderLayout());
        JLabel lblOla = new JLabel("Painel Geral de Controle");
        lblOla.setFont(lblOla.getFont().deriveFont(Font.BOLD, 22f));
        JLabel lblSub = new JLabel("Visão rápida do fluxo de trabalho da sua assistência técnica.");
        lblSub.setFont(lblSub.getFont().deriveFont(Font.PLAIN, 13f));
        pnlBoasVindas.add(lblOla, BorderLayout.NORTH);
        pnlBoasVindas.add(lblSub, BorderLayout.SOUTH);
        centerPanel.add(pnlBoasVindas);
        centerPanel.add(Box.createVerticalStrut(20));

        // 2. Grid com 4 Cards de Indicadores
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 15, 0));

        lblCardOS = new JLabel("0");
        lblCardClientes = new JLabel("0");
        lblCardEstoque = new JLabel("0");
        lblCardCaixa = new JLabel("R$ 0,00");

        pnlCards.add(criarCard("OS em Aberto", lblCardOS, new Color(41, 128, 185)));
        pnlCards.add(criarCard("Clientes Cadastrados", lblCardClientes, new Color(142, 68, 173)));
        pnlCards.add(criarCard("Peças em Estoque", lblCardEstoque, new Color(230, 126, 34)));
        pnlCards.add(criarCard("Saldo em Caixa", lblCardCaixa, new Color(39, 174, 96)));

        centerPanel.add(pnlCards);
        centerPanel.add(Box.createVerticalStrut(25));

        // 3. Bloco de Ações Rápidas
        JPanel pnlAcoesRapidas = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlAcoesRapidas.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), " Atalhos e Ações Rápidas ",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 13)
        ));

        JButton btnNovaOSRapida = new JButton("🛠️ Abrir Nova OS");
        btnNovaOSRapida.setFont(btnNovaOSRapida.getFont().deriveFont(Font.BOLD, 14f));
        btnNovaOSRapida.setPreferredSize(new Dimension(150, 50));
        btnNovaOSRapida.addActionListener(e -> {
            NovaOSDialog d = new NovaOSDialog(owner, clienteDAO, equipDAO, osDAO);
            d.setVisible(true);
            recarregarMetricas();
            mainFrame.recarregarTodasAsAbas();
        });

        JButton btnNovoCliRapido = new JButton("👤 Novo Cliente");
        btnNovoCliRapido.setFont(btnNovoCliRapido.getFont().deriveFont(Font.BOLD, 14f));
        btnNovoCliRapido.addActionListener(e -> {
            ClienteDialog d = new ClienteDialog(owner, clienteDAO, null);
            d.setVisible(true);
            recarregarMetricas();
            mainFrame.recarregarTodasAsAbas();
        });

        JButton btnNovoProdRapido = new JButton("📦 Cadastrar Peça");
        btnNovoProdRapido.setFont(btnNovoProdRapido.getFont().deriveFont(Font.BOLD, 14f));
        btnNovoProdRapido.addActionListener(e -> {
            ProdutoDialog d = new ProdutoDialog(owner, produtoDAO, null);
            d.setVisible(true);
            recarregarMetricas();
            mainFrame.recarregarTodasAsAbas();
        });

        pnlAcoesRapidas.add(btnNovaOSRapida);
        pnlAcoesRapidas.add(btnNovoCliRapido);
        pnlAcoesRapidas.add(btnNovoProdRapido);

        centerPanel.add(pnlAcoesRapidas);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel criarCard(String titulo, JLabel lblValor, Color corBorda) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(corBorda, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(lblTit.getFont().deriveFont(Font.BOLD, 12f));

        lblValor.setFont(lblValor.getFont().deriveFont(Font.BOLD, 22f));
        lblValor.setForeground(corBorda);

        card.add(lblTit, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        return card;
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
