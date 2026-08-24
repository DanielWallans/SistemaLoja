package com.loja.view;

import com.loja.model.Cliente;
import com.loja.model.Equipamento;
import com.loja.repository.ClienteDAO;
import com.loja.repository.EquipamentoDAO;
import com.loja.view.dialogs.EquipamentoDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class EquipamentoPanel extends JPanel {
    private final EquipamentoDAO equipDAO;
    private final ClienteDAO clienteDAO;
    private final Frame owner;

    private JTable tabela;
    private DefaultTableModel tableModel;
    private JComboBox<EquipamentoDialog.ClienteComboItem> cbFiltroCliente;
    private JLabel lblContador;

    public EquipamentoPanel(Frame owner, EquipamentoDAO equipDAO, ClienteDAO clienteDAO) {
        this.owner = owner;
        this.equipDAO = equipDAO;
        this.clienteDAO = clienteDAO;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        initComponents();
        recarregarFiltroClientes();
        recarregarTabela();
    }

    private void initComponents() {
        // 1. Top Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));

        JPanel pnlTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JLabel lblTitulo = new JLabel("Gestão de Equipamentos");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 18f));
        pnlTitulo.add(lblTitulo);

        JPanel pnlAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        cbFiltroCliente = new JComboBox<>();
        cbFiltroCliente.addActionListener(e -> filtrarPorCliente());

        JButton btnNovo = new JButton("+ Novo Equipamento");
        btnNovo.setFont(btnNovo.getFont().deriveFont(Font.BOLD));
        JButton btnEditar = new JButton("Editar");
        JButton btnAtualizar = new JButton("Atualizar");

        btnNovo.addActionListener(e -> abrirNovoEquipamento());
        btnEditar.addActionListener(e -> editarEquipamentoSelecionado());
        btnAtualizar.addActionListener(e -> {
            recarregarFiltroClientes();
            recarregarTabela();
        });

        pnlAcoes.add(new JLabel("Filtrar por Cliente:"));
        pnlAcoes.add(cbFiltroCliente);
        pnlAcoes.add(btnNovo);
        pnlAcoes.add(btnEditar);
        pnlAcoes.add(btnAtualizar);

        topPanel.add(pnlTitulo, BorderLayout.WEST);
        topPanel.add(pnlAcoes, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 2. Tabela de Equipamentos
        String[] colunas = {"ID", "Cliente", "Tipo", "Marca / Modelo", "Nº Série", "Cor", "Avarias", "Senha", "Acessórios"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(tableModel);
        tabela.setRowHeight(28);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(45);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(140);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(140);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(70);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(7).setPreferredWidth(80);
        tabela.getColumnModel().getColumn(8).setPreferredWidth(140);

        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editarEquipamentoSelecionado();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tabela);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Rodapé
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        lblContador = new JLabel("Total de equipamentos: 0");
        lblContador.setFont(lblContador.getFont().deriveFont(Font.ITALIC));
        bottomPanel.add(lblContador);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void recarregarFiltroClientes() {
        cbFiltroCliente.removeAllItems();
        cbFiltroCliente.addItem(new EquipamentoDialog.ClienteComboItem(-1, "Todos os Clientes"));
        List<Cliente> clientes = clienteDAO.buscarTodos();
        for (Cliente c : clientes) {
            cbFiltroCliente.addItem(new EquipamentoDialog.ClienteComboItem(c.getId(), c.getNome()));
        }
    }

    public void recarregarTabela() {
        tableModel.setRowCount(0);
        EquipamentoDialog.ClienteComboItem item = (EquipamentoDialog.ClienteComboItem) cbFiltroCliente.getSelectedItem();
        List<Cliente> todosClientes = clienteDAO.buscarTodos();

        int contagem = 0;
        if (item == null || item.id == -1) {
            for (Cliente c : todosClientes) {
                List<Equipamento> equips = equipDAO.buscarPorCliente(c.getId());
                for (Equipamento eq : equips) {
                    adicionarLinhaTabela(eq, c.getNome());
                    contagem++;
                }
            }
        } else {
            List<Equipamento> equips = equipDAO.buscarPorCliente(item.id);
            for (Equipamento eq : equips) {
                adicionarLinhaTabela(eq, item.nome);
                contagem++;
            }
        }
        lblContador.setText("Total de equipamentos exibidos: " + contagem);
    }

    private void adicionarLinhaTabela(Equipamento eq, String nomeCliente) {
        tableModel.addRow(new Object[]{
                eq.getId(),
                nomeCliente,
                eq.getTipo(),
                eq.getMarca() + " " + eq.getModelo(),
                eq.getNumeroSerie() != null ? eq.getNumeroSerie() : "-",
                eq.getCor() != null ? eq.getCor() : "-",
                eq.getAvarias() != null ? eq.getAvarias() : "-",
                eq.getSenhaAcesso() != null ? eq.getSenhaAcesso() : "-",
                eq.getAcessorios() != null ? eq.getAcessorios() : "-"
        });
    }

    private void filtrarPorCliente() {
        recarregarTabela();
    }

    private void abrirNovoEquipamento() {
        EquipamentoDialog.ClienteComboItem item = (EquipamentoDialog.ClienteComboItem) cbFiltroCliente.getSelectedItem();
        int clienteId = (item != null && item.id > 0) ? item.id : -1;
        EquipamentoDialog dialog = new EquipamentoDialog(owner, equipDAO, clienteDAO, null, clienteId);
        dialog.setVisible(true);
        if (dialog.isSalvo()) {
            recarregarTabela();
        }
    }

    private void editarEquipamentoSelecionado() {
        int row = tabela.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um equipamento na tabela para editar!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        Equipamento eq = equipDAO.buscarPorId(id);
        if (eq != null) {
            EquipamentoDialog dialog = new EquipamentoDialog(owner, equipDAO, clienteDAO, eq, eq.getClienteId());
            dialog.setVisible(true);
            if (dialog.isSalvo()) {
                recarregarTabela();
            }
        }
    }
}
