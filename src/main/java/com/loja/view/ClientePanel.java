package com.loja.view;

import com.loja.model.Cliente;
import com.loja.repository.ClienteDAO;
import com.loja.repository.EquipamentoDAO;
import com.loja.view.dialogs.ClienteDialog;
import com.loja.view.dialogs.EquipamentoDialog;
import com.loja.view.theme.ThemeTokens;
import com.loja.view.theme.UIComponents;
import com.loja.view.theme.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ClientePanel extends JPanel {
    private final ClienteDAO clienteDAO;
    private final EquipamentoDAO equipDAO;
    private final Frame owner;

    private JTable tabela;
    private DefaultTableModel tableModel;
    private JTextField txtBusca;
    private JLabel lblContador;

    public ClientePanel(Frame owner, ClienteDAO clienteDAO, EquipamentoDAO equipDAO) {
        this.owner = owner;
        this.clienteDAO = clienteDAO;
        this.equipDAO = equipDAO;

        setLayout(new BorderLayout(0, UITheme.SPACE_16));
        setBorder(BorderFactory.createEmptyBorder(UITheme.SPACE_20, UITheme.SPACE_24, UITheme.SPACE_20,
                UITheme.SPACE_24));

        initComponents();
        recarregarTabela();
    }

    private void initComponents() {
        ThemeTokens t = UITheme.tokens();

        // 1. Barra Superior (Título e Ações)
        JPanel topPanel = new JPanel(new BorderLayout(UITheme.SPACE_16, 0));
        topPanel.setOpaque(false);

        JPanel pnlTitulo = new JPanel(new BorderLayout(0, UITheme.SPACE_4));
        pnlTitulo.setOpaque(false);
        JLabel lblTitulo = new JLabel("Gestão de Clientes");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(t.getTextPrimary());
        JLabel lblSub = new JLabel("Base cadastral de clientes, contatos e aparelhos vinculados.");
        lblSub.setFont(UITheme.FONT_SUBTITLE);
        lblSub.setForeground(t.getTextSecondary());
        pnlTitulo.add(lblTitulo, BorderLayout.NORTH);
        pnlTitulo.add(lblSub, BorderLayout.SOUTH);

        JPanel pnlBuscaEAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.SPACE_8, 0));
        pnlBuscaEAcoes.setOpaque(false);

        txtBusca = new JTextField(20);
        txtBusca.setFont(UITheme.FONT_BODY);
        txtBusca.putClientProperty("JTextField.placeholderText", "Buscar por nome ou CPF [F3]...");

        JButton btnBuscar = UIComponents.criarBotaoSecundario("Buscar", this::filtrarClientes);
        JButton btnNovo = UIComponents.criarBotaoPrimario("+ Novo Cliente", this::abrirNovoCliente);
        JButton btnEditar = UIComponents.criarBotaoSecundario("Editar", this::editarClienteSelecionado);
        JButton btnNovoEquip = UIComponents.criarBotaoSecundario("+ Vincular Aparelho", this::abrirEquipamentoDoCliente);
        JButton btnAtualizar = UIComponents.criarBotaoSecundario("Atualizar [F5]", () -> {
            txtBusca.setText("");
            recarregarTabela();
        });

        txtBusca.addActionListener(e -> filtrarClientes());

        pnlBuscaEAcoes.add(txtBusca);
        pnlBuscaEAcoes.add(btnBuscar);
        pnlBuscaEAcoes.add(btnNovo);
        pnlBuscaEAcoes.add(btnEditar);
        pnlBuscaEAcoes.add(btnNovoEquip);
        pnlBuscaEAcoes.add(btnAtualizar);

        topPanel.add(pnlTitulo, BorderLayout.WEST);
        topPanel.add(pnlBuscaEAcoes, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 2. Tabela de Clientes
        String[] colunas = { "ID", "Nome Completo", "CPF / CNPJ", "Telefone", "E-mail", "Endereço" };
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(tableModel);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(60);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(190);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(150);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(210);

        UIComponents.formatarTabelaModerna(tabela, -1);

        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarClienteSelecionado();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tabela);
        scrollPane.setBorder(BorderFactory.createLineBorder(t.getBorderSubtle(), 1));
        add(scrollPane, BorderLayout.CENTER);

        // 3. Rodapé com contagem
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottomPanel.setOpaque(false);
        lblContador = new JLabel("Total de clientes: 0");
        lblContador.setFont(UITheme.FONT_SMALL);
        lblContador.setForeground(t.getTextSecondary());
        bottomPanel.add(lblContador);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void recarregarTabela() {
        UIComponents.formatarTabelaModerna(tabela, -1);
        tableModel.setRowCount(0);
        List<Cliente> clientes = clienteDAO.buscarTodos();
        for (Cliente c : clientes) {
            tableModel.addRow(new Object[] {
                    c.getId(),
                    c.getNome(),
                    c.getCpfCnpj() != null ? c.getCpfCnpj() : "-",
                    c.getTelefone() != null ? c.getTelefone() : "-",
                    c.getEmail() != null ? c.getEmail() : "-",
                    c.getEndereco() != null ? c.getEndereco() : "-"
            });
        }
        lblContador.setText("Total de clientes cadastrados: " + clientes.size());
    }

    private void filtrarClientes() {
        String termo = txtBusca.getText().trim();
        if (termo.isEmpty()) {
            recarregarTabela();
            return;
        }
        tableModel.setRowCount(0);
        List<Cliente> clientes = clienteDAO.buscarPorNome(termo);
        for (Cliente c : clientes) {
            tableModel.addRow(new Object[] {
                    c.getId(),
                    c.getNome(),
                    c.getCpfCnpj() != null ? c.getCpfCnpj() : "-",
                    c.getTelefone() != null ? c.getTelefone() : "-",
                    c.getEmail() != null ? c.getEmail() : "-",
                    c.getEndereco() != null ? c.getEndereco() : "-"
            });
        }
        lblContador.setText("Resultados encontrados: " + clientes.size());
    }

    private void abrirNovoCliente() {
        ClienteDialog dialog = new ClienteDialog(owner, clienteDAO, null);
        dialog.setVisible(true);
        if (dialog.isSalvo()) {
            recarregarTabela();
        }
    }

    private void editarClienteSelecionado() {
        int row = tabela.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente na tabela para editar!", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        Cliente cliente = clienteDAO.buscarPorId(id);
        if (cliente != null) {
            ClienteDialog dialog = new ClienteDialog(owner, clienteDAO, cliente);
            dialog.setVisible(true);
            if (dialog.isSalvo()) {
                recarregarTabela();
            }
        }
    }

    private void abrirEquipamentoDoCliente() {
        int row = tabela.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente na tabela para vincular um equipamento!", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int clienteId = (int) tableModel.getValueAt(row, 0);
        EquipamentoDialog dialog = new EquipamentoDialog(owner, equipDAO, clienteDAO, null, clienteId);
        dialog.setVisible(true);
    }

    public void focarBusca() {
        if (txtBusca != null) {
            txtBusca.requestFocusInWindow();
            txtBusca.selectAll();
        }
    }
}
