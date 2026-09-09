package com.loja.view;

import com.loja.model.Produto;
import com.loja.repository.ProdutoDAO;
import com.loja.view.dialogs.ProdutoDialog;
import com.loja.view.theme.UIComponents;
import com.loja.view.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class EstoquePanel extends JPanel {
    private final ProdutoDAO produtoDAO;
    private final Frame owner;

    private JTable tabela;
    private DefaultTableModel tableModel;
    private JTextField txtBusca;
    private JLabel lblContador;

    public EstoquePanel(Frame owner, ProdutoDAO produtoDAO) {
        this.owner = owner;
        this.produtoDAO = produtoDAO;

        setLayout(new BorderLayout(UITheme.SPACE_16, UITheme.SPACE_16));
        setBorder(BorderFactory.createEmptyBorder(UITheme.SPACE_16, UITheme.SPACE_20, UITheme.SPACE_16, UITheme.SPACE_20));

        initComponents();
        recarregarTabela();
    }

    private void initComponents() {
        // 1. Top Panel
        JPanel topPanel = new JPanel(new BorderLayout(UITheme.SPACE_16, UITheme.SPACE_8));
        topPanel.setOpaque(false);

        JPanel pnlTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTitulo.setOpaque(false);
        JLabel lblTitulo = new JLabel("Estoque de Peças e Componentes");
        lblTitulo.setFont(UITheme.FONT_TITLE);
        pnlTitulo.add(lblTitulo);

        JPanel pnlAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.SPACE_8, 0));
        pnlAcoes.setOpaque(false);
        txtBusca = new JTextField(18);
        txtBusca.putClientProperty("JTextField.placeholderText", "Filtrar peças...");
        JButton btnNovo = UIComponents.criarBotaoPrimario("+ Cadastrar Peça");
        JButton btnEditar = UIComponents.criarBotaoSecundario("Editar");
        JButton btnAtualizar = UIComponents.criarBotaoSecundario("Atualizar");

        txtBusca.addActionListener(e -> filtrarPecas());
        btnNovo.addActionListener(e -> abrirNovoProduto());
        btnEditar.addActionListener(e -> editarProdutoSelecionado());
        btnAtualizar.addActionListener(e -> {
            txtBusca.setText("");
            recarregarTabela();
        });

        pnlAcoes.add(txtBusca);
        pnlAcoes.add(btnNovo);
        pnlAcoes.add(btnEditar);
        pnlAcoes.add(btnAtualizar);

        topPanel.add(pnlTitulo, BorderLayout.WEST);
        topPanel.add(pnlAcoes, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 2. Tabela de Peças
        String[] colunas = {"Código", "Componente / Descrição", "Preço Unitário (R$)", "Quantidade em Estoque", "Status do Estoque"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(tableModel);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(60);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(280);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(130);

        UIComponents.formatarTabelaModerna(tabela, 4);

        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarProdutoSelecionado();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tabela);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Rodapé
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottomPanel.setOpaque(false);
        lblContador = new JLabel("Total de itens em estoque: 0");
        lblContador.setFont(UITheme.FONT_CAPTION);
        lblContador.setForeground(UITheme.tokens().getTextSecondary());
        bottomPanel.add(lblContador);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void recarregarTabela() {
        tableModel.setRowCount(0);
        List<Produto> lista = produtoDAO.buscarTodos();
        for (Produto p : lista) {
            String statusEstoque;
            if (p.getEstoque() <= 0) {
                statusEstoque = "ZERADO";
            } else if (p.getEstoque() <= 3) {
                statusEstoque = "ESTOQUE BAIXO";
            } else {
                statusEstoque = "DISPONÍVEL";
            }

            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getNome(),
                    String.format("R$ %.2f", p.getPreco()),
                    p.getEstoque(),
                    statusEstoque
            });
        }
        lblContador.setText("Total de itens cadastrados no estoque: " + lista.size());
    }

    private void filtrarPecas() {
        String termo = txtBusca.getText().trim().toLowerCase();
        if (termo.isEmpty()) {
            recarregarTabela();
            return;
        }
        tableModel.setRowCount(0);
        List<Produto> lista = produtoDAO.buscarTodos();
        int cont = 0;
        for (Produto p : lista) {
            if (p.getNome().toLowerCase().contains(termo) || String.valueOf(p.getId()).equals(termo)) {
                String statusEstoque = p.getEstoque() <= 0 ? "ZERADO" : (p.getEstoque() <= 3 ? "ESTOQUE BAIXO" : "DISPONÍVEL");
                tableModel.addRow(new Object[]{
                        p.getId(),
                        p.getNome(),
                        String.format("R$ %.2f", p.getPreco()),
                        p.getEstoque(),
                        statusEstoque
                });
                cont++;
            }
        }
        lblContador.setText("Resultados encontrados: " + cont);
    }

    private void abrirNovoProduto() {
        ProdutoDialog dialog = new ProdutoDialog(owner, produtoDAO, null);
        dialog.setVisible(true);
        if (dialog.isSalvo()) {
            recarregarTabela();
        }
    }

    private void editarProdutoSelecionado() {
        int row = tabela.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma peça na tabela para editar!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        List<Produto> lista = produtoDAO.buscarTodos();
        for (Produto p : lista) {
            if (p.getId() == id) {
                ProdutoDialog dialog = new ProdutoDialog(owner, produtoDAO, p);
                dialog.setVisible(true);
                if (dialog.isSalvo()) {
                    recarregarTabela();
                }
                break;
            }
        }
    }
}
