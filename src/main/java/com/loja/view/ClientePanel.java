package com.loja.view;

import com.loja.model.Cliente;
import com.loja.repository.ClienteDAO;
import com.loja.repository.EquipamentoDAO;
import com.loja.view.dialogs.ClienteDialog;
import com.loja.view.dialogs.EquipamentoDialog;

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

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        initComponents();
        recarregarTabela();
    }

    private void initComponents() {
        // 1. Barra Superior (Título e Ações)
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        
        JPanel pnlTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JLabel lblTitulo = new JLabel("Gestão de Clientes");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 18f));
        pnlTitulo.add(lblTitulo);

        JPanel pnlBuscaEAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        txtBusca = new JTextField(18);
        txtBusca.putClientProperty("JTextField.placeholderText", "Buscar por nome ou CPF...");
        JButton btnBuscar = new JButton("Buscar");
        JButton btnNovo = new JButton("+ Novo Cliente");
        btnNovo.setFont(btnNovo.getFont().deriveFont(Font.BOLD));
        JButton btnEditar = new JButton("Editar");
        JButton btnNovoEquip = new JButton("+ Adicionar Equipamento");
        JButton btnAtualizar = new JButton("Atualizar");

        btnBuscar.addActionListener(e -> filtrarClientes());
        txtBusca.addActionListener(e -> filtrarClientes());
        btnNovo.addActionListener(e -> abrirNovoCliente());
        btnEditar.addActionListener(e -> editarClienteSelecionado());
        btnNovoEquip.addActionListener(e -> abrirEquipamentoDoCliente());
        btnAtualizar.addActionListener(e -> {
            txtBusca.setText("");
            recarregarTabela();
        });

        pnlBuscaEAcoes.add(new JLabel("Pesquisar:"));
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
        String[] colunas = {"ID", "Nome Completo", "CPF / CNPJ", "Telefone", "E-mail", "Endereço"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(tableModel);
        tabela.setRowHeight(28);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(180);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(140);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(200);

        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarClienteSelecionado();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tabela);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Rodapé com contagem
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        lblContador = new JLabel("Total de clientes: 0");
        lblContador.setFont(lblContador.getFont().deriveFont(Font.ITALIC));
        bottomPanel.add(lblContador);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void recarregarTabela() {
        tableModel.setRowCount(0);
        List<Cliente> clientes = clienteDAO.buscarTodos();
        for (Cliente c : clientes) {
            tableModel.addRow(new Object[]{
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
            tableModel.addRow(new Object[]{
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
            JOptionPane.showMessageDialog(this, "Selecione um cliente na tabela para editar!", "Aviso", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Selecione um cliente na tabela para vincular um equipamento!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int clienteId = (int) tableModel.getValueAt(row, 0);
        EquipamentoDialog dialog = new EquipamentoDialog(owner, equipDAO, clienteDAO, null, clienteId);
        dialog.setVisible(true);
    }
}
