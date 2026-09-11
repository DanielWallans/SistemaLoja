package com.loja.view.dialogs;

import com.loja.model.PagamentoItem;
import com.loja.repository.CaixaDAO;
import com.loja.view.theme.ThemeTokens;
import com.loja.view.theme.UIComponents;
import com.loja.view.theme.UITheme;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PagamentoPDVDialog extends JDialog {
    private final double totalPagarOriginal;
    private final CaixaDAO caixaDAO;
    private final List<PagamentoItem> pagamentos = new ArrayList<>();
    private boolean confirmado = false;

    // Componentes de Totais
    private JLabel lblTotalCompra;
    private JLabel lblTotalPago;
    private JLabel lblRestante;
    private JLabel lblTroco;

    // Componentes de Entrada de Pagamento
    private JComboBox<String> cbModalidade;
    private JComboBox<String> cbParcelas;
    private JTextField txtValorAdicionar;
    private JTextField txtValorEntregueDinheiro;
    private JCheckBox chkRepassarTaxa;
    private JLabel lblTaxaInfo;
    private JLabel lblValorLiquidoInfo;

    // Tabela de Pagamentos
    private DefaultTableModel tableModel;
    private JTable tabela;
    private JButton btnFinalizar;

    private Map<String, Double> taxasMaquininha;

    public PagamentoPDVDialog(Frame owner, double totalPagarOriginal, CaixaDAO caixaDAO) {
        this((Window) owner, totalPagarOriginal, caixaDAO);
    }

    public PagamentoPDVDialog(Dialog owner, double totalPagarOriginal, CaixaDAO caixaDAO) {
        this((Window) owner, totalPagarOriginal, caixaDAO);
    }

    public PagamentoPDVDialog(Window owner, double totalPagarOriginal, CaixaDAO caixaDAO) {
        super(owner, "Pagamento & Fechamento de Venda", ModalityType.APPLICATION_MODAL);
        this.totalPagarOriginal = totalPagarOriginal;
        this.caixaDAO = caixaDAO;
        this.taxasMaquininha = caixaDAO.obterTaxasMaquininha();

        initComponents();
        atualizarTotaisERestante();
        setSize(880, 690);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        ThemeTokens t = UITheme.tokens();
        getContentPane().setBackground(t.getBgApp());
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(t.getBgApp());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // 1. Painel Superior: Indicadores de Totais (4 Cards Sólidos e Sóbrios)
        JPanel pnlTotais = new JPanel(new GridLayout(1, 4, 12, 0));
        pnlTotais.setOpaque(false);

        lblTotalCompra = new JLabel("R$ " + String.format("%.2f", totalPagarOriginal));
        lblTotalPago = new JLabel("R$ 0,00");
        lblRestante = new JLabel("R$ " + String.format("%.2f", totalPagarOriginal));
        lblTroco = new JLabel("R$ 0,00");

        pnlTotais.add(criarCardTotalizador("TOTAL DA VENDA", lblTotalCompra));
        pnlTotais.add(criarCardTotalizador("TOTAL PAGO", lblTotalPago));
        pnlTotais.add(criarCardTotalizador("VALOR RESTANTE", lblRestante));
        pnlTotais.add(criarCardTotalizador("TROCO", lblTroco));

        mainPanel.add(pnlTotais);
        mainPanel.add(Box.createVerticalStrut(12));

        // 2. Painel de Adição de Forma de Pagamento
        JPanel pnlAddPagamento = new JPanel(new GridBagLayout());
        pnlAddPagamento.setOpaque(false);
        pnlAddPagamento.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(t.getBorderSubtle(), 1),
                        " Adicionar Forma de Pagamento ",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        UITheme.FONT_BODY_BOLD, t.getTextPrimary()),
                BorderFactory.createEmptyBorder(10, 14, 12, 14)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 8, 5, 8);

        cbModalidade = new JComboBox<>(new String[] {
                "Dinheiro",
                "PIX",
                "Cartão de Débito",
                "Cartão de Crédito"
        });
        cbModalidade.setFont(UITheme.FONT_BODY);

        cbParcelas = new JComboBox<>(new String[] {
                "1x (À vista)",
                "2x sem juros",
                "3x sem juros",
                "4x sem juros",
                "5x sem juros",
                "6x sem juros",
                "7x sem juros",
                "8x sem juros",
                "9x sem juros",
                "10x sem juros",
                "11x sem juros",
                "12x sem juros"
        });
        cbParcelas.setFont(UITheme.FONT_BODY);
        cbParcelas.setEnabled(false);

        txtValorAdicionar = new JTextField(String.format("%.2f", totalPagarOriginal).replace(",", "."), 10);
        txtValorAdicionar.setFont(UITheme.FONT_BODY);

        txtValorEntregueDinheiro = new JTextField(10);
        txtValorEntregueDinheiro.setFont(UITheme.FONT_BODY);

        chkRepassarTaxa = new JCheckBox("Repassar taxa da maquininha ao cliente");
        chkRepassarTaxa.setFont(UITheme.FONT_BODY);
        chkRepassarTaxa.setForeground(t.getTextPrimary());
        chkRepassarTaxa.setOpaque(false);

        lblTaxaInfo = new JLabel("Taxa da Maquininha: 0.00% (R$ 0,00)");
        lblTaxaInfo.setFont(UITheme.FONT_SMALL);
        lblTaxaInfo.setForeground(t.getTextSecondary());

        lblValorLiquidoInfo = new JLabel("Valor Líquido da Loja: R$ " + String.format("%.2f", totalPagarOriginal));
        lblValorLiquidoInfo.setFont(UITheme.FONT_BODY_BOLD);
        lblValorLiquidoInfo.setForeground(t.getTextPrimary());

        JButton btnAdicionarPag = UIComponents.criarBotaoPrimario("Adicionar Pagamento", this::adicionarPagamento);

        // Listeners de atualização dinâmica de taxas e parcelas
        cbModalidade.addActionListener(e -> atualizarCamposModalidade());
        cbParcelas.addActionListener(e -> recalcularPreviaTaxas());
        chkRepassarTaxa.addActionListener(e -> recalcularPreviaTaxas());
        txtValorAdicionar.addActionListener(e -> recalcularPreviaTaxas());

        // Linha 0: Modalidade e Parcelas
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        JLabel lblMod = new JLabel("Forma de Pagamento:");
        lblMod.setFont(UITheme.FONT_BODY);
        lblMod.setForeground(t.getTextPrimary());
        pnlAddPagamento.add(lblMod, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3;
        pnlAddPagamento.add(cbModalidade, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        JLabel lblParc = new JLabel("Parcelas:");
        lblParc.setFont(UITheme.FONT_BODY);
        lblParc.setForeground(t.getTextPrimary());
        pnlAddPagamento.add(lblParc, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.3;
        pnlAddPagamento.add(cbParcelas, gbc);

        // Linha 1: Valor a pagar e Valor Entregue
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        JLabel lblVal = new JLabel("Valor a Lançar (R$):");
        lblVal.setFont(UITheme.FONT_BODY);
        lblVal.setForeground(t.getTextPrimary());
        pnlAddPagamento.add(lblVal, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3;
        pnlAddPagamento.add(txtValorAdicionar, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        JLabel lblDin = new JLabel("Dinheiro Entregue (R$):");
        lblDin.setFont(UITheme.FONT_BODY);
        lblDin.setForeground(t.getTextPrimary());
        pnlAddPagamento.add(lblDin, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.3;
        pnlAddPagamento.add(txtValorEntregueDinheiro, gbc);

        // Linha 2: Repassar Taxa e Informações de Taxa / Líquido
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        pnlAddPagamento.add(chkRepassarTaxa, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 2;
        pnlAddPagamento.add(lblTaxaInfo, gbc);

        // Linha 3: Líquido e Botão Adicionar
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        pnlAddPagamento.add(lblValorLiquidoInfo, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 2;
        pnlAddPagamento.add(btnAdicionarPag, gbc);

        mainPanel.add(pnlAddPagamento);
        mainPanel.add(Box.createVerticalStrut(12));

        // 3. Tabela de Pagamentos Lançados
        JPanel pnlTabela = new JPanel(new BorderLayout(8, 8));
        pnlTabela.setOpaque(false);
        pnlTabela.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(t.getBorderSubtle(), 1),
                        " Pagamentos Vinculados a Esta Venda ",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        UITheme.FONT_BODY_BOLD, t.getTextPrimary()),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        String[] colunas = { "Modalidade", "Parcelas", "Valor Bruto (R$)", "Taxa (%)", "Taxa (R$)",
                "Valor Líquido (R$)", "Troco (R$)" };
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(tableModel);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(80);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(80);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(100);

        UIComponents.formatarTabelaModerna(tabela, -1);

        JPanel pnlBotoesTabela = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlBotoesTabela.setOpaque(false);
        JButton btnRemoverItem = UIComponents.criarBotaoSecundario("Remover Pagamento Selecionado",
                this::removerPagamento);
        pnlBotoesTabela.add(btnRemoverItem);

        pnlTabela.add(new JScrollPane(tabela), BorderLayout.CENTER);
        pnlTabela.add(pnlBotoesTabela, BorderLayout.SOUTH);
        pnlTabela.setPreferredSize(new Dimension(800, 180));

        mainPanel.add(pnlTabela);
        add(mainPanel, BorderLayout.CENTER);

        // Barra inferior: Ações principais
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottomPanel.setBackground(t.getBgApp());
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, t.getBorderSubtle()));

        JButton btnCancelar = UIComponents.criarBotaoSecundario("Cancelar", this::dispose);
        btnFinalizar = UIComponents.criarBotaoPrimario("Confirmar Pagamento & Concluir", this::confirmarFechamento);
        btnFinalizar.setEnabled(false);

        bottomPanel.add(btnCancelar);
        bottomPanel.add(btnFinalizar);
        add(bottomPanel, BorderLayout.SOUTH);

        atualizarCamposModalidade();
    }

    private JPanel criarCardTotalizador(String titulo, JLabel lblValor) {
        ThemeTokens t = UITheme.tokens();
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(t.getBgCard());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(t.getBorderSubtle());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(UITheme.FONT_CAPTION);
        lblTit.setForeground(t.getTextSecondary());

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblValor.setForeground(t.getTextPrimary());

        card.add(lblTit, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        return card;
    }

    private void atualizarCamposModalidade() {
        String mod = (String) cbModalidade.getSelectedItem();
        boolean isCredito = mod != null && mod.contains("Crédito");
        boolean isDinheiro = mod != null && mod.contains("Dinheiro");

        cbParcelas.setEnabled(isCredito);
        if (!isCredito) {
            cbParcelas.setSelectedIndex(0);
        }

        txtValorEntregueDinheiro.setEnabled(isDinheiro);
        if (!isDinheiro) {
            txtValorEntregueDinheiro.setText("");
        }

        recalcularPreviaTaxas();
    }

    private void recalcularPreviaTaxas() {
        double valor;
        try {
            valor = Double.parseDouble(txtValorAdicionar.getText().trim().replace(",", "."));
            if (valor <= 0)
                return;
        } catch (Exception e) {
            return;
        }

        String mod = (String) cbModalidade.getSelectedItem();
        int parcelas = cbParcelas.getSelectedIndex() + 1;
        double taxaPct = obterTaxaPercentual(mod, parcelas);

        double valorBruto = valor;
        double valorTaxa;
        double valorLiquido;

        if (chkRepassarTaxa.isSelected() && taxaPct > 0 && taxaPct < 100) {
            valorBruto = valor / (1.0 - (taxaPct / 100.0));
            valorTaxa = valorBruto - valor;
            valorLiquido = valor;
        } else {
            valorTaxa = (valorBruto * taxaPct) / 100.0;
            valorLiquido = valorBruto - valorTaxa;
        }

        lblTaxaInfo.setText(String.format("Taxa da Maquininha: %.2f%% (R$ %.2f)", taxaPct, valorTaxa));
        lblValorLiquidoInfo.setText(String.format("Valor Líquido da Loja: R$ %.2f", valorLiquido));
    }

    private double obterTaxaPercentual(String modalidade, int parcelas) {
        if (modalidade == null)
            return 0.0;
        if (modalidade.contains("Dinheiro"))
            return taxasMaquininha.getOrDefault("DINHEIRO", 0.0);
        if (modalidade.contains("PIX"))
            return taxasMaquininha.getOrDefault("PIX", 0.0);
        if (modalidade.contains("Débito"))
            return taxasMaquininha.getOrDefault("DEBITO", 1.50);
        if (modalidade.contains("Crédito")) {
            if (parcelas <= 1)
                return taxasMaquininha.getOrDefault("CREDITO_1X", 3.20);
            if (parcelas <= 6)
                return taxasMaquininha.getOrDefault("CREDITO_PARCELADO_2X_6X", 5.50);
            return taxasMaquininha.getOrDefault("CREDITO_PARCELADO_7X_12X", 9.80);
        }
        return 0.0;
    }

    private void adicionarPagamento() {
        double valor;
        try {
            valor = Double.parseDouble(txtValorAdicionar.getText().trim().replace(",", "."));
            if (valor <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Informe um valor numérico válido maior que zero!", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            txtValorAdicionar.requestFocus();
            return;
        }

        String modStr = (String) cbModalidade.getSelectedItem();
        String modalidadeCodigo = "DINHEIRO";
        if (modStr.contains("PIX"))
            modalidadeCodigo = "PIX";
        else if (modStr.contains("Débito"))
            modalidadeCodigo = "DEBITO";
        else if (modStr.contains("Crédito")) {
            int p = cbParcelas.getSelectedIndex() + 1;
            modalidadeCodigo = p > 1 ? "CREDITO_PARCELADO" : "CREDITO_1X";
        }

        int parcelas = cbParcelas.getSelectedIndex() + 1;
        double taxaPct = obterTaxaPercentual(modStr, parcelas);

        double valorBruto = valor;
        if (chkRepassarTaxa.isSelected() && taxaPct > 0 && taxaPct < 100) {
            valorBruto = valor / (1.0 - (taxaPct / 100.0));
        }

        PagamentoItem item = new PagamentoItem(modalidadeCodigo, valorBruto, taxaPct, parcelas);

        if ("DINHEIRO".equalsIgnoreCase(modalidadeCodigo)) {
            String entregueStr = txtValorEntregueDinheiro.getText().trim().replace(",", ".");
            if (!entregueStr.isEmpty()) {
                try {
                    double entregue = Double.parseDouble(entregueStr);
                    if (entregue < valorBruto) {
                        JOptionPane.showMessageDialog(this,
                                "O valor entregue em dinheiro não pode ser menor que o valor a lançar!", "Aviso",
                                JOptionPane.WARNING_MESSAGE);
                        txtValorEntregueDinheiro.requestFocus();
                        return;
                    }
                    item.setValorRecebidoCliente(entregue);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        pagamentos.add(item);
        atualizarTotaisERestante();
    }

    private void removerPagamento() {
        int row = tabela.getSelectedRow();
        if (row >= 0 && row < pagamentos.size()) {
            pagamentos.remove(row);
            atualizarTotaisERestante();
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um pagamento na tabela para remover!", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void atualizarTotaisERestante() {
        tableModel.setRowCount(0);
        double totalPago = 0;
        double totalTroco = 0;

        for (PagamentoItem p : pagamentos) {
            tableModel.addRow(new Object[] {
                    p.getModalidadeFormatada(),
                    p.getParcelas() + "x",
                    String.format("R$ %.2f", p.getValorBruto()),
                    String.format("%.2f%%", p.getTaxaPercentual()),
                    String.format("R$ %.2f", p.getValorTaxa()),
                    String.format("R$ %.2f", p.getValorLiquido()),
                    String.format("R$ %.2f", p.getTroco())
            });
            totalPago += p.getValorBruto();
            totalTroco += p.getTroco();
        }

        double restante = Math.max(0.0, totalPagarOriginal - totalPago);

        lblTotalPago.setText("R$ " + String.format("%.2f", totalPago));
        lblRestante.setText("R$ " + String.format("%.2f", restante));
        lblTroco.setText("R$ " + String.format("%.2f", totalTroco));

        ThemeTokens t = UITheme.tokens();
        if (restante > 0.009) {
            lblRestante.setForeground(t.getTextPrimary());
            txtValorAdicionar.setText(String.format("%.2f", restante).replace(",", "."));
            btnFinalizar.setEnabled(false);
        } else {
            lblRestante.setForeground(t.getTextSecondary());
            txtValorAdicionar.setText("0.00");
            btnFinalizar.setEnabled(true);
        }

        recalcularPreviaTaxas();
    }

    private void confirmarFechamento() {
        double totalPago = pagamentos.stream().mapToDouble(PagamentoItem::getValorBruto).sum();
        if (totalPago < totalPagarOriginal - 0.01) {
            JOptionPane.showMessageDialog(this, "O valor total pago ainda é menor que o valor da venda!", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        this.confirmado = true;
        dispose();
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public List<PagamentoItem> getPagamentos() {
        return pagamentos;
    }
}
