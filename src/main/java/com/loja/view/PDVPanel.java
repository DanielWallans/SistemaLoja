package com.loja.view;

import com.loja.model.ItemVenda;
import com.loja.model.PagamentoItem;
import com.loja.model.Produto;
import com.loja.model.VendaPDV;
import com.loja.repository.CaixaDAO;
import com.loja.repository.ProdutoDAO;
import com.loja.repository.VendaPDVDAO;
import com.loja.service.CaixaService;
import com.loja.view.dialogs.AberturaCaixaDialog;
import com.loja.view.dialogs.FechamentoCegoDialog;
import com.loja.view.dialogs.PagamentoPDVDialog;

import com.loja.view.theme.UIComponents;
import com.loja.view.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PDVPanel extends JPanel {
    private static final DateTimeFormatter FORMATTER_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Frame owner;
    private final ProdutoDAO produtoDAO;
    private final CaixaDAO caixaDAO;
    private final VendaPDVDAO vendaPDVDAO;

    // Componentes de Entrada
    private JTextField txtEntradaCodigoOuNome;
    private JSpinner spQuantidade;
    private JButton btnAdicionarItem;
    private JButton btnBuscarEstoque;

    // Carrinho
    private final List<ItemVenda> carrinho = new ArrayList<>();
    private DefaultTableModel modelCarrinho;
    private JTable tabelaCarrinho;

    // Totais e Fechamento
    private JLabel lblStatusCaixa;
    private JButton btnAcaoCaixa;
    private JLabel lblTotalItensQtd;
    private JLabel lblSubtotalValor;
    private JTextField txtDescontoValor;
    private JLabel lblTotalPagarValor;

    private VendaPDV ultimaVendaConcluida = null;

    public PDVPanel(Frame owner, ProdutoDAO produtoDAO, CaixaDAO caixaDAO) {
        this.owner = owner;
        this.produtoDAO = produtoDAO;
        this.caixaDAO = caixaDAO;
        this.vendaPDVDAO = new VendaPDVDAO();

        setLayout(new BorderLayout(UITheme.SPACE_16, UITheme.SPACE_16));
        setBorder(BorderFactory.createEmptyBorder(UITheme.SPACE_16, UITheme.SPACE_20, UITheme.SPACE_16,
                UITheme.SPACE_20));

        initComponents();
        configurarAtalhosTeclado();
    }

    private void initComponents() {
        // 1. Cabeçalho do PDV
        JPanel pnlHeader = new JPanel(new BorderLayout(UITheme.SPACE_12, 0));
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.tokens().getBorderSubtle()),
                BorderFactory.createEmptyBorder(0, 0, UITheme.SPACE_12, 0)));

        JPanel pnlTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.SPACE_8, 0));
        pnlTitulo.setOpaque(false);
        JLabel lblTit = new JLabel("PDV de Balcão • Venda Rápida");
        lblTit.setFont(UITheme.FONT_TITLE);
        pnlTitulo.add(lblTit);

        JPanel pnlStatusCaixa = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.SPACE_8, 0));
        pnlStatusCaixa.setOpaque(false);
        lblStatusCaixa = new JLabel("● Caixa Aberto");
        lblStatusCaixa.setFont(UITheme.FONT_SUBTITLE);
        lblStatusCaixa.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblStatusCaixa.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                alternarAcaoCaixa();
            }
        });

        btnAcaoCaixa = UIComponents.criarBotaoPrimario("Abrir Caixa");
        btnAcaoCaixa.putClientProperty("JButton.arc", 6);
        btnAcaoCaixa.addActionListener(e -> alternarAcaoCaixa());

        pnlStatusCaixa.add(lblStatusCaixa);
        pnlStatusCaixa.add(btnAcaoCaixa);
        atualizarStatusCaixa();

        pnlHeader.add(pnlTitulo, BorderLayout.WEST);
        pnlHeader.add(pnlStatusCaixa, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Área Central (Esquerda: Entrada + Vitrine + Carrinho | Direita: Totais +
        // Pagamento)
        JPanel pnlCentro = new JPanel(new BorderLayout(UITheme.SPACE_16, UITheme.SPACE_16));
        pnlCentro.setOpaque(false);

        // Lado Esquerdo
        JPanel pnlEsquerda = new JPanel(new BorderLayout(UITheme.SPACE_12, UITheme.SPACE_12));
        pnlEsquerda.setOpaque(false);

        // Barra de Leitor de Código de Barras / Busca
        JPanel pnlBarraEntrada = new JPanel(new BorderLayout(UITheme.SPACE_8, UITheme.SPACE_8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.tokens().getBgCard());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(UITheme.tokens().getBorderSubtle());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pnlBarraEntrada.setOpaque(false);
        pnlBarraEntrada.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACE_12, UITheme.SPACE_16, UITheme.SPACE_12,
                UITheme.SPACE_16));

        JLabel lblEntradaTit = new JLabel("LEITOR DE CÓDIGO DE BARRAS / BUSCA DE PRODUTO");
        lblEntradaTit.setFont(UITheme.FONT_SMALL);
        lblEntradaTit.setForeground(UITheme.tokens().getTextSecondary());

        JPanel pnlCamposEntrada = new JPanel(new BorderLayout(UITheme.SPACE_8, 0));
        pnlCamposEntrada.setOpaque(false);

        txtEntradaCodigoOuNome = new JTextField();
        txtEntradaCodigoOuNome.setFont(UITheme.FONT_BODY);
        txtEntradaCodigoOuNome.putClientProperty("JTextField.placeholderText",
                "Bipe o código de barras ou digite o código/nome e tecle Enter...");

        JPanel pnlQtdEAdicionar = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.SPACE_8, 0));
        pnlQtdEAdicionar.setOpaque(false);
        spQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spQuantidade.setPreferredSize(new Dimension(65, 34));
        spQuantidade.setFont(UITheme.FONT_BODY);

        btnAdicionarItem = UIComponents.criarBotaoPrimario("Adicionar [Enter]");
        btnBuscarEstoque = UIComponents.criarBotaoSecundario("Buscar [F6]");

        JLabel lblQtd = new JLabel("Qtd:");
        lblQtd.setFont(UITheme.FONT_CAPTION);
        lblQtd.setForeground(UITheme.tokens().getTextSecondary());
        pnlQtdEAdicionar.add(lblQtd);
        pnlQtdEAdicionar.add(spQuantidade);
        pnlQtdEAdicionar.add(btnAdicionarItem);
        pnlQtdEAdicionar.add(btnBuscarEstoque);

        pnlCamposEntrada.add(txtEntradaCodigoOuNome, BorderLayout.CENTER);
        pnlCamposEntrada.add(pnlQtdEAdicionar, BorderLayout.EAST);

        pnlBarraEntrada.add(lblEntradaTit, BorderLayout.NORTH);
        pnlBarraEntrada.add(pnlCamposEntrada, BorderLayout.CENTER);

        pnlEsquerda.add(pnlBarraEntrada, BorderLayout.NORTH);

        // Vitrine de Acessórios Rápidos
        JPanel pnlVitrineBox = new JPanel(new BorderLayout(0, UITheme.SPACE_8));
        pnlVitrineBox.setOpaque(false);

        JLabel lblVitrineTit = new JLabel("ACESSÓRIOS RÁPIDOS DE BALCÃO");
        lblVitrineTit.setFont(UITheme.FONT_SMALL);
        lblVitrineTit.setForeground(UITheme.tokens().getTextSecondary());
        pnlVitrineBox.add(lblVitrineTit, BorderLayout.NORTH);

        JPanel pnlVitrine = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.SPACE_8, UITheme.SPACE_4));
        pnlVitrine.setOpaque(false);

        adicionarBotaoAcessorioRapido(pnlVitrine, "Película 3D", 25.00);
        adicionarBotaoAcessorioRapido(pnlVitrine, "Película Cerâmica", 35.00);
        adicionarBotaoAcessorioRapido(pnlVitrine, "Película Privacidade", 45.00);
        adicionarBotaoAcessorioRapido(pnlVitrine, "Cabo USB-C Turbo", 30.00);
        adicionarBotaoAcessorioRapido(pnlVitrine, "Cabo Lightning iPhone", 35.00);
        adicionarBotaoAcessorioRapido(pnlVitrine, "Fonte 20W USB-C", 65.00);
        adicionarBotaoAcessorioRapido(pnlVitrine, "Fone de Ouvido", 35.00);
        adicionarBotaoAcessorioRapido(pnlVitrine, "Capinha Silicone", 35.00);
        pnlVitrineBox.add(pnlVitrine, BorderLayout.CENTER);

        // Tabela do Carrinho
        JPanel pnlTabelaBox = new JPanel(new BorderLayout(UITheme.SPACE_8, UITheme.SPACE_8));
        pnlTabelaBox.setOpaque(false);

        String[] colunas = { "Item #", "Cód / ID", "Descrição do Produto", "Qtd", "Preço Unit. (R$)", "Subtotal (R$)" };
        modelCarrinho = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaCarrinho = new JTable(modelCarrinho);
        tabelaCarrinho.getColumnModel().getColumn(0).setPreferredWidth(55);
        tabelaCarrinho.getColumnModel().getColumn(1).setPreferredWidth(85);
        tabelaCarrinho.getColumnModel().getColumn(2).setPreferredWidth(260);
        tabelaCarrinho.getColumnModel().getColumn(3).setPreferredWidth(50);
        tabelaCarrinho.getColumnModel().getColumn(4).setPreferredWidth(100);
        tabelaCarrinho.getColumnModel().getColumn(5).setPreferredWidth(110);

        UIComponents.formatarTabelaModerna(tabelaCarrinho, -1);

        JPanel pnlCarrinhoAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.SPACE_8, 0));
        pnlCarrinhoAcoes.setOpaque(false);
        JButton btnRemoverItem = UIComponents.criarBotaoSecundario("Remover Item [Del]");
        JButton btnLimparCarrinho = UIComponents.criarBotaoSecundario("Limpar Carrinho [F4]");
        btnRemoverItem.addActionListener(e -> removerItemSelecionado());
        btnLimparCarrinho.addActionListener(e -> limparCarrinho());
        pnlCarrinhoAcoes.add(btnRemoverItem);
        pnlCarrinhoAcoes.add(btnLimparCarrinho);

        pnlTabelaBox.add(new JScrollPane(tabelaCarrinho), BorderLayout.CENTER);
        pnlTabelaBox.add(pnlCarrinhoAcoes, BorderLayout.SOUTH);

        JPanel pnlCentroEsquerda = new JPanel(new BorderLayout(0, UITheme.SPACE_12));
        pnlCentroEsquerda.setOpaque(false);
        pnlCentroEsquerda.add(pnlVitrineBox, BorderLayout.NORTH);
        pnlCentroEsquerda.add(pnlTabelaBox, BorderLayout.CENTER);

        pnlEsquerda.add(pnlCentroEsquerda, BorderLayout.CENTER);
        pnlCentro.add(pnlEsquerda, BorderLayout.CENTER);

        // Lado Direito: Resumo Financeiro & Finalização
        JPanel pnlDireita = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.tokens().getBgCard());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(UITheme.tokens().getBorderSubtle());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pnlDireita.setOpaque(false);
        pnlDireita.setLayout(new BoxLayout(pnlDireita, BoxLayout.Y_AXIS));
        pnlDireita.setPreferredSize(new Dimension(320, 0));
        pnlDireita.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACE_16, UITheme.SPACE_16, UITheme.SPACE_16,
                UITheme.SPACE_16));

        JLabel lblTitResumo = new JLabel("Resumo da Venda");
        lblTitResumo.setFont(UITheme.FONT_TITLE);
        lblTitResumo.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDireita.add(lblTitResumo);
        pnlDireita.add(Box.createVerticalStrut(UITheme.SPACE_16));

        // Total de Itens
        lblTotalItensQtd = new JLabel("Total de Itens: 0");
        lblTotalItensQtd.setFont(UITheme.FONT_BODY);
        lblTotalItensQtd.setForeground(UITheme.tokens().getTextSecondary());
        lblTotalItensQtd.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDireita.add(lblTotalItensQtd);
        pnlDireita.add(Box.createVerticalStrut(UITheme.SPACE_12));

        // Subtotal
        JPanel pnlSub = new JPanel(new BorderLayout());
        pnlSub.setOpaque(false);
        pnlSub.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        pnlSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblSubTit = new JLabel("Subtotal:");
        lblSubTit.setFont(UITheme.FONT_BODY);
        pnlSub.add(lblSubTit, BorderLayout.WEST);
        lblSubtotalValor = new JLabel("R$ 0,00");
        lblSubtotalValor.setFont(UITheme.FONT_TITLE);
        pnlSub.add(lblSubtotalValor, BorderLayout.EAST);
        pnlDireita.add(pnlSub);
        pnlDireita.add(Box.createVerticalStrut(UITheme.SPACE_12));

        // Desconto
        JPanel pnlDesc = new JPanel(new BorderLayout(UITheme.SPACE_8, 0));
        pnlDesc.setOpaque(false);
        pnlDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        pnlDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblDescTit = new JLabel("Desconto (R$):");
        lblDescTit.setFont(UITheme.FONT_BODY);
        pnlDesc.add(lblDescTit, BorderLayout.WEST);
        txtDescontoValor = new JTextField("0.00", 6);
        txtDescontoValor.setHorizontalAlignment(JTextField.RIGHT);
        txtDescontoValor.setFont(UITheme.FONT_BODY);
        txtDescontoValor.addActionListener(e -> recalcularTotais());
        txtDescontoValor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                recalcularTotais();
            }
        });
        pnlDesc.add(txtDescontoValor, BorderLayout.EAST);
        pnlDireita.add(pnlDesc);
        pnlDireita.add(Box.createVerticalStrut(UITheme.SPACE_20));

        // Card Total a Pagar
        lblTotalPagarValor = new JLabel("R$ 0,00");
        JPanel pnlCardTotal = UIComponents.criarCardMetrica("TOTAL A PAGAR", lblTotalPagarValor,
                UITheme.tokens().getSuccess());
        pnlCardTotal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        pnlCardTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDireita.add(pnlCardTotal);
        pnlDireita.add(Box.createVerticalStrut(UITheme.SPACE_20));

        // Botão de Destaque: FINALIZAR VENDA
        JButton btnFinalizarVenda = UIComponents.criarBotaoPrimario("FINALIZAR VENDA [F12]");
        btnFinalizarVenda.setFont(UITheme.FONT_TITLE);
        btnFinalizarVenda.setPreferredSize(new Dimension(280, 48));
        btnFinalizarVenda.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btnFinalizarVenda.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnFinalizarVenda.addActionListener(e -> abrirPagamentoVenda());
        pnlDireita.add(btnFinalizarVenda);
        pnlDireita.add(Box.createVerticalStrut(UITheme.SPACE_12));

        JButton btnComprovanteUltima = UIComponents.criarBotaoSecundario("Imprimir Último Comprovante");
        btnComprovanteUltima.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnComprovanteUltima.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnComprovanteUltima.addActionListener(e -> exibirUltimoComprovante());
        pnlDireita.add(btnComprovanteUltima);

        pnlDireita.add(Box.createVerticalGlue());

        pnlCentro.add(pnlDireita, BorderLayout.EAST);
        add(pnlCentro, BorderLayout.CENTER);

        // Listeners
        txtEntradaCodigoOuNome.addActionListener(e -> processarEntradaProduto());
        btnAdicionarItem.addActionListener(e -> processarEntradaProduto());
        btnBuscarEstoque.addActionListener(e -> abrirBuscaEstoqueDialog());
    }

    private void configurarAtalhosTeclado() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "finalizarVenda");
        am.put("finalizarVenda", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirPagamentoVenda();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "buscarEstoque");
        am.put("buscarEstoque", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirBuscaEstoqueDialog();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "limparCarrinho");
        am.put("limparCarrinho", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limparCarrinho();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "removerItem");
        am.put("removerItem", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removerItemSelecionado();
            }
        });
    }

    public void focarEntrada() {
        if (txtEntradaCodigoOuNome != null) {
            txtEntradaCodigoOuNome.requestFocusInWindow();
            txtEntradaCodigoOuNome.selectAll();
        }
    }

    private void adicionarBotaoAcessorioRapido(JPanel pnlVitrine, String rotulo, double precoPadrao) {
        JButton btn = UIComponents.criarBotaoSecundario(rotulo + " • R$ " + String.format("%.2f", precoPadrao));
        btn.setFont(UITheme.FONT_SMALL);
        btn.addActionListener(e -> {
            String termoLimpo = rotulo.replaceAll("[^a-zA-Z0-9 ]", "").trim();
            Produto p = produtoDAO.buscarPorCodigoOuId(termoLimpo);
            if (p != null) {
                adicionarAoCarrinho(p, 1);
            } else {
                ItemVenda item = new ItemVenda(0, rotulo, 1, precoPadrao);
                carrinho.add(item);
                atualizarTabelaCarrinho();
            }
        });
        pnlVitrine.add(btn);
    }

    private void processarEntradaProduto() {
        String texto = txtEntradaCodigoOuNome.getText().trim();
        if (texto.isEmpty()) {
            txtEntradaCodigoOuNome.requestFocus();
            return;
        }

        int qtd = (Integer) spQuantidade.getValue();
        if (qtd <= 0)
            qtd = 1;

        if (texto.contains("*")) {
            String[] partes = texto.split("\\*", 2);
            try {
                int qtdDigitada = Integer.parseInt(partes[0].trim());
                if (qtdDigitada > 0)
                    qtd = qtdDigitada;
                texto = partes[1].trim();
            } catch (Exception ignored) {
            }
        }

        Produto prod = produtoDAO.buscarPorCodigoOuId(texto);
        if (prod != null) {
            adicionarAoCarrinho(prod, qtd);
            txtEntradaCodigoOuNome.setText("");
            spQuantidade.setValue(1);
            txtEntradaCodigoOuNome.requestFocus();
        } else {
            int opt = JOptionPane.showConfirmDialog(this,
                    "Produto '" + texto + "' não localizado no estoque.\n\nDeseja realizar uma busca avançada?",
                    "Produto Não Encontrado", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (opt == JOptionPane.YES_OPTION) {
                abrirBuscaEstoqueDialog();
            } else {
                txtEntradaCodigoOuNome.requestFocus();
            }
        }
    }

    public void adicionarAoCarrinho(Produto p, int quantidade) {
        for (ItemVenda item : carrinho) {
            if (item.getProdutoId() > 0 && item.getProdutoId() == p.getId()) {
                item.setQuantidade(item.getQuantidade() + quantidade);
                atualizarTabelaCarrinho();
                return;
            }
        }

        ItemVenda novoItem = new ItemVenda(p.getId(), p.getNome(), quantidade, p.getPreco());
        carrinho.add(novoItem);
        atualizarTabelaCarrinho();
    }

    private void atualizarTabelaCarrinho() {
        modelCarrinho.setRowCount(0);
        int cont = 1;
        for (ItemVenda i : carrinho) {
            modelCarrinho.addRow(new Object[] {
                    cont++,
                    i.getProdutoId() > 0 ? "#" + i.getProdutoId() : "Avulso",
                    i.getNomeProduto(),
                    i.getQuantidade(),
                    String.format("R$ %.2f", i.getValorUnitario()),
                    String.format("R$ %.2f", i.getSubtotal())
            });
        }
        recalcularTotais();
    }

    private void recalcularTotais() {
        int totalItens = carrinho.stream().mapToInt(ItemVenda::getQuantidade).sum();
        double subtotal = carrinho.stream().mapToDouble(ItemVenda::getSubtotal).sum();

        double desconto = 0.0;
        try {
            desconto = Double.parseDouble(txtDescontoValor.getText().trim().replace(",", "."));
            if (desconto < 0)
                desconto = 0.0;
        } catch (Exception e) {
            desconto = 0.0;
        }

        double totalPagar = Math.max(0.0, subtotal - desconto);

        lblTotalItensQtd.setText("Total de Itens: " + totalItens);
        lblSubtotalValor.setText(String.format("R$ %.2f", subtotal));
        lblTotalPagarValor.setText(String.format("R$ %.2f", totalPagar));
    }

    private void removerItemSelecionado() {
        int row = tabelaCarrinho.getSelectedRow();
        if (row >= 0 && row < carrinho.size()) {
            carrinho.remove(row);
            atualizarTabelaCarrinho();
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um item no carrinho para remover!", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void limparCarrinho() {
        if (carrinho.isEmpty())
            return;
        int opt = JOptionPane.showConfirmDialog(this, "Deseja realmente limpar todos os itens do carrinho?",
                "Limpar Carrinho", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            carrinho.clear();
            txtDescontoValor.setText("0.00");
            atualizarTabelaCarrinho();
            txtEntradaCodigoOuNome.requestFocus();
        }
    }

    private void abrirBuscaEstoqueDialog() {
        List<Produto> todos = produtoDAO.buscarTodos();
        if (todos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum produto cadastrado no estoque.", "Estoque Vazio",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<Produto> cb = new JComboBox<>();
        for (Produto p : todos) {
            cb.addItem(p);
        }

        JSpinner sp = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        JPanel pnl = new JPanel(new GridLayout(2, 2, 8, 8));
        pnl.add(new JLabel("Selecione o Produto:"));
        pnl.add(cb);
        pnl.add(new JLabel("Quantidade:"));
        pnl.add(sp);

        int res = JOptionPane.showConfirmDialog(this, pnl, "Pesquisar Produto no Estoque", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            Produto selecionado = (Produto) cb.getSelectedItem();
            int qtd = (Integer) sp.getValue();
            if (selecionado != null) {
                adicionarAoCarrinho(selecionado, qtd);
            }
        }
    }

    public void atualizarStatusCaixa() {
        if (lblStatusCaixa == null)
            return;
        com.loja.model.CaixaSessao s = caixaDAO.obterSessaoAberta();
        if (s != null) {
            lblStatusCaixa.setText("● Caixa Aberto (Turno #" + s.getId() + " • " + s.getOperadorAbertura() + ")");
            lblStatusCaixa.setForeground(UITheme.tokens().getSuccess());
            lblStatusCaixa.setToolTipText("Turno aberto às " + s.getHoraAberturaFormatada());
            if (btnAcaoCaixa != null) {
                btnAcaoCaixa.setText("Fechar Caixa");
                UIComponents.estilizarBotaoSecundario(btnAcaoCaixa);
            }
        } else {
            lblStatusCaixa.setText("○ CAIXA FECHADO");
            lblStatusCaixa.setForeground(UITheme.tokens().getDanger());
            lblStatusCaixa.setToolTipText("O caixa está fechado. Clique para abrir o turno.");
            if (btnAcaoCaixa != null) {
                btnAcaoCaixa.setText("Abrir Caixa");
                UIComponents.estilizarBotaoPrimario(btnAcaoCaixa);
            }
        }
    }

    public void abrirAberturaCaixaDireto() {
        CaixaService service = new CaixaService();
        AberturaCaixaDialog dialog = new AberturaCaixaDialog(owner, service, caixaDAO);
        dialog.setVisible(true);
        if (dialog.isAbertaComSucesso()) {
            atualizarStatusCaixa();
        }
    }

    private void alternarAcaoCaixa() {
        com.loja.model.CaixaSessao s = caixaDAO.obterSessaoAberta();
        if (s == null) {
            abrirAberturaCaixaDireto();
        } else {
            int opt = JOptionPane.showConfirmDialog(this,
                    "O Caixa já está aberto (Turno #" + s.getId() + " • Operador: " + s.getOperadorAbertura() + ").\n\n"
                            + "Deseja realizar o Fechamento de Caixa (Conferência Cega) agora?",
                    "Fechar Caixa", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (opt == JOptionPane.YES_OPTION) {
                CaixaService service = new CaixaService();
                FechamentoCegoDialog dialog = new FechamentoCegoDialog(owner, service, caixaDAO, s);
                dialog.setVisible(true);
                if (dialog.isFechadoComSucesso()) {
                    atualizarStatusCaixa();
                }
            }
        }
    }

    public boolean verificarEAbrirCaixaSeNecessario() {
        com.loja.model.CaixaSessao s = caixaDAO.obterSessaoAberta();
        if (s != null) {
            atualizarStatusCaixa();
            return true;
        }

        int opt = JOptionPane.showConfirmDialog(this,
                "O CAIXA ESTÁ FECHADO!\n\nPara realizar vendas e movimentações financeiras, é necessário abrir o turno.\n"
                        +
                        "Deseja realizar a Abertura de Caixa agora?",
                "Caixa Fechado", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (opt == JOptionPane.YES_OPTION) {
            abrirAberturaCaixaDireto();
            return caixaDAO.obterSessaoAberta() != null;
        }
        return false;
    }

    private void abrirPagamentoVenda() {
        if (!verificarEAbrirCaixaSeNecessario()) {
            return;
        }

        if (carrinho.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "O carrinho está vazio! Adicione pelo menos um item para finalizar a venda.", "Carrinho Vazio",
                    JOptionPane.WARNING_MESSAGE);
            txtEntradaCodigoOuNome.requestFocus();
            return;
        }

        double subtotal = carrinho.stream().mapToDouble(ItemVenda::getSubtotal).sum();
        double desconto = 0.0;
        try {
            desconto = Double.parseDouble(txtDescontoValor.getText().trim().replace(",", "."));
            if (desconto < 0)
                desconto = 0.0;
        } catch (Exception e) {
            desconto = 0.0;
        }
        double totalPagar = Math.max(0.0, subtotal - desconto);

        PagamentoPDVDialog dialog = new PagamentoPDVDialog(owner, totalPagar, caixaDAO);
        dialog.setVisible(true);

        if (dialog.isConfirmado()) {
            VendaPDV venda = new VendaPDV();
            venda.setSubtotal(subtotal);
            venda.setDesconto(desconto);
            venda.setValorTotal(totalPagar);
            venda.setItens(new ArrayList<>(carrinho));
            venda.setPagamentos(dialog.getPagamentos());
            venda.recalcularTotais();

            boolean sucesso = vendaPDVDAO.finalizarVenda(venda);
            if (sucesso) {
                this.ultimaVendaConcluida = venda;
                JOptionPane.showMessageDialog(this,
                        "VENDA #" + venda.getId() + " CONCLUÍDA COM SUCESSO!\nValor Total: R$ "
                                + String.format("%.2f", totalPagar),
                        "Venda Finalizada", JOptionPane.INFORMATION_MESSAGE);

                exibirComprovanteVenda(venda);

                carrinho.clear();
                txtDescontoValor.setText("0.00");
                atualizarTabelaCarrinho();
                txtEntradaCodigoOuNome.requestFocus();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao gravar a venda no banco de dados.", "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exibirUltimoComprovante() {
        if (ultimaVendaConcluida == null) {
            JOptionPane.showMessageDialog(this, "Nenhuma venda foi realizada nesta sessão ainda.", "Aviso",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        exibirComprovanteVenda(ultimaVendaConcluida);
    }

    private void exibirComprovanteVenda(VendaPDV venda) {
        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append("                 CUPOM NÃO FISCAL - PDV                    \n");
        sb.append("            ASSISTÊNCIA TÉCNICA & ACESSÓRIOS                \n");
        sb.append("============================================================\n");
        sb.append("Venda Nº:           #").append(venda.getId()).append("\n");
        sb.append("Data / Hora:        ").append(venda.getDataHora().format(FORMATTER_HORA)).append("\n");
        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("%-28s %4s %9s %12s\n", "ITEM / PRODUTO", "QTD", "VL.UNIT", "SUBTOTAL"));
        sb.append("------------------------------------------------------------\n");

        for (ItemVenda item : venda.getItens()) {
            String nome = item.getNomeProduto();
            if (nome.length() > 27)
                nome = nome.substring(0, 24) + "...";
            sb.append(String.format("%-28s %4d %9.2f %12.2f\n", nome, item.getQuantidade(), item.getValorUnitario(),
                    item.getSubtotal()));
        }

        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("SUBTOTAL:                                     R$ %10.2f\n", venda.getSubtotal()));
        if (venda.getDesconto() > 0) {
            sb.append(String.format("DESCONTO CONCEDIDO:                          -R$ %10.2f\n", venda.getDesconto()));
        }
        sb.append(String.format("TOTAL A PAGAR:                                R$ %10.2f\n", venda.getValorTotal()));
        sb.append("------------------------------------------------------------\n");
        sb.append("[FORMAS DE PAGAMENTO UTILIZADAS]\n");
        for (PagamentoItem p : venda.getPagamentos()) {
            sb.append(String.format("  • %-32s R$ %10.2f\n", p.getModalidadeFormatada(), p.getValorBruto()));
            if (p.getTroco() > 0) {
                sb.append(String.format("    (Recebido: R$ %.2f | Troco: R$ %.2f)\n", p.getValorRecebidoCliente(),
                        p.getTroco()));
            }
        }
        sb.append("============================================================\n");
        sb.append("         Obrigado pela preferência! Volte sempre!           \n");
        sb.append("============================================================\n");

        JTextArea area = new JTextArea(sb.toString(), 22, 48);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);

        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Comprovante de Venda #" + venda.getId(),
                JOptionPane.PLAIN_MESSAGE);
    }
}
