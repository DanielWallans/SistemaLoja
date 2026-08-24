package com.loja.view;

import com.loja.repository.CaixaDAO;
import com.loja.service.CaixaService;
import com.loja.view.dialogs.SangriaDialog;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CaixaPanel extends JPanel {
    private final CaixaService caixaService;
    private final CaixaDAO caixaDAO;
    private final Frame owner;

    private JLabel lblSaldoValor;
    private JTable tabelaHistorico;
    private DefaultTableModel tableModel;

    public CaixaPanel(Frame owner, CaixaService caixaService, CaixaDAO caixaDAO) {
        this.owner = owner;
        this.caixaService = caixaService;
        this.caixaDAO = caixaDAO;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        initComponents();
        recarregarDados();
    }

    private void initComponents() {
        // 1. Painel Superior: Card de Saldo e Botões
        JPanel topPanel = new JPanel(new BorderLayout(15, 15));

        // Card do Saldo
        JPanel pnlSaldoCard = new JPanel(new BorderLayout(5, 5));
        pnlSaldoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(39, 174, 96), 2),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        pnlSaldoCard.setBackground(new Color(39, 174, 96, 20));

        JLabel lblSaldoTitulo = new JLabel("SALDO DISPONÍVEL NO CAIXA");
        lblSaldoTitulo.setFont(lblSaldoTitulo.getFont().deriveFont(Font.BOLD, 12f));

        lblSaldoValor = new JLabel("R$ 0,00");
        lblSaldoValor.setFont(lblSaldoValor.getFont().deriveFont(Font.BOLD, 28f));
        lblSaldoValor.setForeground(new Color(39, 174, 96));

        pnlSaldoCard.add(lblSaldoTitulo, BorderLayout.NORTH);
        pnlSaldoCard.add(lblSaldoValor, BorderLayout.CENTER);

        // Painel de Ações Rápidas
        JPanel pnlAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        JButton btnSangria = new JButton("Registrar Sangria (Retirada)");
        btnSangria.setFont(btnSangria.getFont().deriveFont(Font.BOLD, 13f));
        JButton btnFechamento = new JButton("Visualizar Relatório do Dia");
        JButton btnAtualizar = new JButton("Atualizar Saldo");

        btnSangria.addActionListener(e -> abrirSangria());
        btnFechamento.addActionListener(e -> exibirFechamento());
        btnAtualizar.addActionListener(e -> recarregarDados());

        pnlAcoes.add(btnSangria);
        pnlAcoes.add(btnFechamento);
        pnlAcoes.add(btnAtualizar);

        topPanel.add(pnlSaldoCard, BorderLayout.WEST);
        topPanel.add(pnlAcoes, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 2. Histórico de Retiradas e Movimentações
        JPanel pnlHistorico = new JPanel(new BorderLayout(10, 10));
        pnlHistorico.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), " Histórico de Retiradas (Sangrias Registradas no Banco) ",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 13)
        ));

        String[] colunas = {"Data e Hora da Retirada", "Valor Retirado (R$)", "Tipo de Operação"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaHistorico = new JTable(tableModel);
        tabelaHistorico.setRowHeight(28);
        tabelaHistorico.getColumnModel().getColumn(0).setPreferredWidth(250);
        tabelaHistorico.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabelaHistorico.getColumnModel().getColumn(2).setPreferredWidth(200);

        JScrollPane scroll = new JScrollPane(tabelaHistorico);
        pnlHistorico.add(scroll, BorderLayout.CENTER);

        add(pnlHistorico, BorderLayout.CENTER);
    }

    public void recarregarDados() {
        double saldo = caixaDAO.obterSaldo();
        lblSaldoValor.setText(String.format("R$ %.2f", saldo));

        tableModel.setRowCount(0);
        List<String> sangrias = caixaDAO.obterHistoricoSangrias();
        for (String s : sangrias) {
            // Formato esperado: "Retirada: R$ XX.XX em YYYY-MM-DD HH:MM:SS"
            tableModel.addRow(new Object[]{
                    s.contains(" em ") ? s.substring(s.indexOf(" em ") + 4) : "-",
                    s.contains(" em ") ? s.substring(s.indexOf("R$"), s.indexOf(" em ")) : s,
                    "Retirada / Sangria de Caixa"
            });
        }
    }

    private void abrirSangria() {
        double saldo = caixaDAO.obterSaldo();
        SangriaDialog dialog = new SangriaDialog(owner, caixaService, saldo);
        dialog.setVisible(true);
        if (dialog.isRealizada()) {
            recarregarDados();
        }
    }

    private void exibirFechamento() {
        double saldo = caixaDAO.obterSaldo();
        List<String> sangrias = caixaDAO.obterHistoricoSangrias();

        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append("              RELATÓRIO FINANCEIRO DO CAIXA                 \n");
        sb.append("============================================================\n");
        sb.append(" SALDO ATUAL EM CAIXA:  R$ ").append(String.format("%.2f", saldo)).append("\n");
        sb.append(" TOTAL DE SANGRIAS:     ").append(sangrias.size()).append("\n");
        sb.append("------------------------------------------------------------\n");
        sb.append(" DETALHAMENTO DE RETIRADAS REGISTRADAS:\n");
        if (sangrias.isEmpty()) {
            sb.append("   [!] Nenhuma retirada registrada no banco.\n");
        } else {
            sangrias.forEach(sg -> sb.append("   > ").append(sg).append("\n"));
        }
        sb.append("============================================================\n");

        JTextArea txt = new JTextArea(sb.toString(), 15, 45);
        txt.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txt.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(txt), "Fechamento de Caixa", JOptionPane.PLAIN_MESSAGE);
    }
}
