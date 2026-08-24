package com.loja.view;

import com.loja.model.Cliente;
import com.loja.model.Equipamento;
import com.loja.model.OrdemServico;
import com.loja.repository.ClienteDAO;
import com.loja.repository.EquipamentoDAO;
import com.loja.repository.OrdemServicoDAO;
import com.loja.repository.ProdutoDAO;
import com.loja.service.CaixaService;
import com.loja.view.dialogs.DetalhesOSDialog;
import com.loja.view.dialogs.MontarOrcamentoDialog;
import com.loja.view.dialogs.NovaOSDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrdemServicoPanel extends JPanel {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final OrdemServicoDAO osDAO;
    private final ClienteDAO clienteDAO;
    private final EquipamentoDAO equipDAO;
    private final ProdutoDAO produtoDAO;
    private final CaixaService caixaService;
    private final Frame owner;

    private JTable tabela;
    private DefaultTableModel tableModel;
    private JComboBox<String> cbFiltroStatus;
    private JLabel lblContador;

    public OrdemServicoPanel(Frame owner, OrdemServicoDAO osDAO, ClienteDAO clienteDAO, 
                             EquipamentoDAO equipDAO, ProdutoDAO produtoDAO, CaixaService caixaService) {
        this.owner = owner;
        this.osDAO = osDAO;
        this.clienteDAO = clienteDAO;
        this.equipDAO = equipDAO;
        this.produtoDAO = produtoDAO;
        this.caixaService = caixaService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        initComponents();
        recarregarTabela();
    }

    private void initComponents() {
        // 1. Top Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));

        JPanel pnlTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JLabel lblTitulo = new JLabel("Ordens de Serviço (OS)");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 18f));
        pnlTitulo.add(lblTitulo);

        JPanel pnlAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        cbFiltroStatus = new JComboBox<>(new String[]{
                "Todos os Status",
                "Aguardando Orçamento",
                "Aguardando Aprovação do Cliente",
                "Aprovado - Em Manutenção",
                "Aguardando Peça",
                "Pronto (Aguardando Retirada)",
                "Entregue (Finalizado)",
                "Orçamento Recusado / Cancelada"
        });
        cbFiltroStatus.addActionListener(e -> filtrarPorStatus());

        JButton btnNovaOS = new JButton("+ Abrir Nova OS");
        btnNovaOS.setFont(btnNovaOS.getFont().deriveFont(Font.BOLD));
        JButton btnMontarOrcamento = new JButton("🛠️ Montar Orçamento");
        JButton btnDetalhes = new JButton("Ver Detalhes");
        JButton btnAtualizar = new JButton("Atualizar");

        btnNovaOS.addActionListener(e -> abrirNovaOS());
        btnMontarOrcamento.addActionListener(e -> abrirMontarOrcamentoSelecionado());
        btnDetalhes.addActionListener(e -> abrirDetalhesOSSelecionada());
        btnAtualizar.addActionListener(e -> recarregarTabela());

        pnlAcoes.add(new JLabel("Status:"));
        pnlAcoes.add(cbFiltroStatus);
        pnlAcoes.add(btnNovaOS);
        pnlAcoes.add(btnMontarOrcamento);
        pnlAcoes.add(btnDetalhes);
        pnlAcoes.add(btnAtualizar);

        topPanel.add(pnlTitulo, BorderLayout.WEST);
        topPanel.add(pnlAcoes, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 2. Tabela de OS
        String[] colunas = {"OS #", "Entrada", "Cliente", "Aparelho / Equipamento", "Status", "Mão de Obra", "Total (R$)", "Saída"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(tableModel);
        tabela.setRowHeight(30);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(180);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(150);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(7).setPreferredWidth(120);

        // Custom Renderer para destacar a coluna Status
        tabela.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    String status = value.toString();
                    setHorizontalAlignment(CENTER);
                    setFont(getFont().deriveFont(Font.BOLD));
                    if (!isSelected) {
                        if (status.contains("Aguardando Orçamento")) {
                            setForeground(new Color(230, 126, 34));
                        } else if (status.contains("Aprovação")) {
                            setForeground(new Color(211, 84, 0));
                        } else if (status.contains("Manutenção") || status.contains("Andamento")) {
                            setForeground(new Color(41, 128, 185));
                        } else if (status.contains("Peça")) {
                            setForeground(new Color(155, 89, 182));
                        } else if (status.contains("Pronto")) {
                            setForeground(new Color(142, 68, 173));
                        } else if (status.contains("Entregue") || status.contains("Finalizado")) {
                            setForeground(new Color(39, 174, 96));
                        } else if (status.contains("Cancelada") || status.contains("Recusado")) {
                            setForeground(new Color(192, 57, 43));
                        } else {
                            setForeground(table.getForeground());
                        }
                    }
                }
                return c;
            }
        });

        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    abrirDetalhesOSSelecionada();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tabela);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Rodapé
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        lblContador = new JLabel("Total de Ordens de Serviço: 0");
        lblContador.setFont(lblContador.getFont().deriveFont(Font.ITALIC));
        bottomPanel.add(lblContador);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void recarregarTabela() {
        tableModel.setRowCount(0);
        List<OrdemServico> lista = osDAO.buscarTodos();
        String filtro = (String) cbFiltroStatus.getSelectedItem();

        int contagem = 0;
        for (OrdemServico os : lista) {
            if (filtro != null && !filtro.equals("Todos os Status") && !filtro.equalsIgnoreCase(os.getStatus())) {
                continue;
            }

            Cliente c = clienteDAO.buscarPorId(os.getClienteId());
            Equipamento eq = equipDAO.buscarPorId(os.getEquipamentoId());

            String nomeCliente = c != null ? c.getNome() : "Cliente #" + os.getClienteId();
            String descEquip = eq != null ? eq.getTipo() + " " + eq.getMarca() + " " + eq.getModelo() : "Equipamento #" + os.getEquipamentoId();

            tableModel.addRow(new Object[]{
                    os.getId(),
                    os.getDataEntrada() != null ? os.getDataEntrada().format(formatter) : "-",
                    nomeCliente,
                    descEquip,
                    os.getStatus(),
                    String.format("R$ %.2f", os.getValorServico()),
                    String.format("R$ %.2f", os.getValorTotal()),
                    os.getDataSaida() != null ? os.getDataSaida().format(formatter) : "Em aberto"
            });
            contagem++;
        }
        lblContador.setText("Ordens de Serviço listadas: " + contagem);
    }

    private void filtrarPorStatus() {
        recarregarTabela();
    }

    private void abrirNovaOS() {
        NovaOSDialog dialog = new NovaOSDialog(owner, clienteDAO, equipDAO, osDAO);
        dialog.setVisible(true);
        if (dialog.isSalvo()) {
            recarregarTabela();
        }
    }

    private void abrirMontarOrcamentoSelecionado() {
        int row = tabela.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma Ordem de Serviço na tabela para montar o orçamento!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int osId = (int) tableModel.getValueAt(row, 0);
        OrdemServico os = osDAO.buscarPorId(osId);
        if (os != null) {
            MontarOrcamentoDialog dialog = new MontarOrcamentoDialog(owner, os, osDAO, clienteDAO, equipDAO, produtoDAO);
            dialog.setVisible(true);
            if (dialog.isSalvo()) {
                recarregarTabela();
            }
        }
    }

    private void abrirDetalhesOSSelecionada() {
        int row = tabela.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma Ordem de Serviço na tabela para visualizar!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int osId = (int) tableModel.getValueAt(row, 0);
        OrdemServico os = osDAO.buscarPorId(osId);
        if (os != null) {
            DetalhesOSDialog dialog = new DetalhesOSDialog(owner, os, osDAO, clienteDAO, equipDAO, produtoDAO, caixaService);
            dialog.setVisible(true);
            if (dialog.isAlterado()) {
                recarregarTabela();
            }
        }
    }
}
