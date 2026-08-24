package com.loja.view.dialogs;

import com.loja.model.Cliente;
import com.loja.model.Equipamento;
import com.loja.repository.ClienteDAO;
import com.loja.repository.EquipamentoDAO;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class EquipamentoDialog extends JDialog {
    private final EquipamentoDAO equipamentoDAO;
    private final ClienteDAO clienteDAO;
    private final Equipamento equipamentoEdicao;
    private int preSelectedClienteId = -1;
    private boolean salvo = false;
    private Equipamento equipamentoCriado = null;

    private JComboBox<ClienteComboItem> cbCliente;
    private JComboBox<String> cbTipo;
    private JTextField txtMarca;
    private JTextField txtModelo;
    private JTextField txtNumeroSerie;
    private JTextField txtCor;
    private JTextField txtAvarias;
    private JTextField txtPatrimonio;
    private JTextField txtSenha;
    private JTextField txtAcessorios;

    public static class ClienteComboItem {
        public final int id;
        public final String nome;

        public ClienteComboItem(int id, String nome) {
            this.id = id;
            this.nome = nome;
        }

        @Override
        public String toString() {
            return "#" + id + " - " + nome;
        }
    }

    public EquipamentoDialog(Frame owner, EquipamentoDAO equipamentoDAO, ClienteDAO clienteDAO, Equipamento equipamentoEdicao, int preSelectedClienteId) {
        super(owner, equipamentoEdicao == null ? "Cadastrar Novo Equipamento" : "Editar Equipamento", true);
        this.equipamentoDAO = equipamentoDAO;
        this.clienteDAO = clienteDAO;
        this.equipamentoEdicao = equipamentoEdicao;
        this.preSelectedClienteId = preSelectedClienteId;

        initComponents();
        carregarClientes();
        if (equipamentoEdicao != null) {
            preencherCampos(equipamentoEdicao);
        }
        setSize(560, 520);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        cbCliente = new JComboBox<>();
        cbTipo = new JComboBox<>(new String[]{"Computador (Desktop)", "Notebook", "Impressora", "Celular / Smartphone", "Tablet", "Monitor", "Console de Vídeo Game", "Outro"});
        cbTipo.setEditable(true);

        txtMarca = new JTextField(25);
        txtModelo = new JTextField(25);
        txtNumeroSerie = new JTextField(25);
        txtCor = new JTextField(25);
        txtAvarias = new JTextField(25);
        txtPatrimonio = new JTextField(25);
        txtSenha = new JTextField(25);
        txtAcessorios = new JTextField(25);

        int linha = 0;
        adicionarCampo(formPanel, gbc, linha++, "Cliente Proprietário *:", cbCliente);
        adicionarCampo(formPanel, gbc, linha++, "Tipo do Equipamento *:", cbTipo);
        adicionarCampo(formPanel, gbc, linha++, "Marca (ex: Dell, Samsung):", txtMarca);
        adicionarCampo(formPanel, gbc, linha++, "Modelo (ex: Inspiron 15):", txtModelo);
        adicionarCampo(formPanel, gbc, linha++, "Nº de Série / S/N:", txtNumeroSerie);
        adicionarCampo(formPanel, gbc, linha++, "Cor do Aparelho:", txtCor);
        adicionarCampo(formPanel, gbc, linha++, "Avarias / Riscos / Trincas:", txtAvarias);
        adicionarCampo(formPanel, gbc, linha++, "Nº Patrimônio (Opcional):", txtPatrimonio);
        adicionarCampo(formPanel, gbc, linha++, "Senha / PIN de Teste:", txtSenha);
        adicionarCampo(formPanel, gbc, linha++, "Acessórios Deixados:", txtAcessorios);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnSalvar = new JButton("Salvar Equipamento");
        btnSalvar.setFont(btnSalvar.getFont().deriveFont(Font.BOLD));

        btnCancelar.addActionListener(e -> dispose());
        btnSalvar.addActionListener(e -> salvar());

        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnSalvar);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void adicionarCampo(JPanel panel, GridBagConstraints gbc, int linha, String rotulo, JComponent campo) {
        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(rotulo);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(campo, gbc);
    }

    private void carregarClientes() {
        cbCliente.removeAllItems();
        List<Cliente> clientes = clienteDAO.buscarTodos();
        ClienteComboItem selecionado = null;
        for (Cliente c : clientes) {
            ClienteComboItem item = new ClienteComboItem(c.getId(), c.getNome());
            cbCliente.addItem(item);
            if (c.getId() == preSelectedClienteId) {
                selecionado = item;
            }
        }
        if (selecionado != null) {
            cbCliente.setSelectedItem(selecionado);
            if (preSelectedClienteId > 0 && equipamentoEdicao == null) {
                cbCliente.setEnabled(false);
            }
        }
    }

    private void preencherCampos(Equipamento eq) {
        cbTipo.setSelectedItem(eq.getTipo());
        txtMarca.setText(eq.getMarca());
        txtModelo.setText(eq.getModelo());
        txtNumeroSerie.setText(eq.getNumeroSerie());
        txtCor.setText(eq.getCor());
        txtAvarias.setText(eq.getAvarias());
        txtPatrimonio.setText(eq.getPatrimonio());
        txtSenha.setText(eq.getSenhaAcesso());
        txtAcessorios.setText(eq.getAcessorios());
    }

    private void salvar() {
        ClienteComboItem clienteItem = (ClienteComboItem) cbCliente.getSelectedItem();
        if (clienteItem == null) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente proprietário!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tipo = cbTipo.getSelectedItem() != null ? cbTipo.getSelectedItem().toString().trim() : "Outro";
        if (tipo.isEmpty()) tipo = "Outro";

        String marca = txtMarca.getText().trim();
        if (marca.isEmpty()) marca = "Genérica / Não informada";

        String modelo = txtModelo.getText().trim();
        if (modelo.isEmpty()) modelo = "Não informado";

        String ns = txtNumeroSerie.getText().trim();
        if (ns.isEmpty()) ns = "N/A";

        String cor = txtCor.getText().trim();
        if (cor.isEmpty()) cor = "Não informada";

        String avarias = txtAvarias.getText().trim();
        if (avarias.isEmpty()) avarias = "Sem avarias visíveis constatadas";

        String patrimonio = txtPatrimonio.getText().trim();
        if (patrimonio.isEmpty()) patrimonio = "N/A";

        String senha = txtSenha.getText().trim();
        if (senha.isEmpty()) senha = "Não informada";

        String acessorios = txtAcessorios.getText().trim();
        if (acessorios.isEmpty()) acessorios = "Nenhum";

        if (equipamentoEdicao == null) {
            Equipamento novo = new Equipamento(clienteItem.id, tipo, marca, modelo, ns, cor, avarias, patrimonio, senha, acessorios);
            if (equipamentoDAO.salvar(novo)) {
                this.salvo = true;
                this.equipamentoCriado = novo;
                JOptionPane.showMessageDialog(this, "Equipamento registrado com sucesso! ID: #" + novo.getId(), "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Falha ao gravar equipamento no banco de dados!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            equipamentoEdicao.setClienteId(clienteItem.id);
            equipamentoEdicao.setTipo(tipo);
            equipamentoEdicao.setMarca(marca);
            equipamentoEdicao.setModelo(modelo);
            equipamentoEdicao.setNumeroSerie(ns);
            equipamentoEdicao.setCor(cor);
            equipamentoEdicao.setAvarias(avarias);
            equipamentoEdicao.setPatrimonio(patrimonio);
            equipamentoEdicao.setSenhaAcesso(senha);
            equipamentoEdicao.setAcessorios(acessorios);

            if (equipamentoDAO.salvar(equipamentoEdicao)) {
                this.salvo = true;
                this.equipamentoCriado = equipamentoEdicao;
                JOptionPane.showMessageDialog(this, "Equipamento atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Falha ao atualizar equipamento!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean isSalvo() {
        return salvo;
    }

    public Equipamento getEquipamentoCriado() {
        return equipamentoCriado;
    }
}
