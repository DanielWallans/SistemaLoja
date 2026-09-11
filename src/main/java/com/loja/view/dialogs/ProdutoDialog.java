package com.loja.view.dialogs;

import com.loja.model.Produto;
import com.loja.repository.ProdutoDAO;

import javax.swing.*;
import java.awt.*;
import com.loja.view.theme.UIComponents;

public class ProdutoDialog extends JDialog {
    private final ProdutoDAO produtoDAO;
    private final Produto produtoEdicao;
    private boolean salvo = false;
    private Produto produtoCriado = null;

    private JTextField txtId;
    private JTextField txtCodigoBarras;
    private JTextField txtNome;
    private JTextField txtPreco;
    private JSpinner spEstoque;

    public ProdutoDialog(Frame owner, ProdutoDAO produtoDAO, Produto produtoEdicao) {
        super(owner, produtoEdicao == null ? "Cadastrar Peça / Produto / Acessório" : "Editar Peça / Produto", true);
        this.produtoDAO = produtoDAO;
        this.produtoEdicao = produtoEdicao;

        initComponents();
        if (produtoEdicao != null) {
            preencherCampos(produtoEdicao);
        } else {
            txtId.setText(String.valueOf(produtoDAO.obterProximoId()));
        }
        setSize(480, 340);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        txtId = new JTextField(10);
        txtCodigoBarras = new JTextField(15);
        txtNome = new JTextField(20);
        txtPreco = new JTextField(10);
        spEstoque = new JSpinner(new SpinnerNumberModel(1, 0, 9999, 1));

        adicionarCampo(formPanel, gbc, 0, "Código / ID:", txtId);
        adicionarCampo(formPanel, gbc, 1, "Código de Barras (EAN):", txtCodigoBarras);
        adicionarCampo(formPanel, gbc, 2, "Nome / Descrição *:", txtNome);
        adicionarCampo(formPanel, gbc, 3, "Preço Unitário (R$) *:", txtPreco);
        adicionarCampo(formPanel, gbc, 4, "Quantidade em Estoque:", spEstoque);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnCancelar = new JButton("Cancelar");
        UIComponents.estilizarBotaoSecundario(btnCancelar);

        JButton btnSalvar = new JButton("Salvar no Estoque");
        UIComponents.estilizarBotaoPrimario(btnSalvar);

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

    private void preencherCampos(Produto p) {
        txtId.setText(String.valueOf(p.getId()));
        txtId.setEnabled(false);
        txtCodigoBarras.setText(p.getCodigoBarras() != null ? p.getCodigoBarras() : "");
        txtNome.setText(p.getNome());
        txtPreco.setText(String.format("%.2f", p.getPreco()).replace(",", "."));
        spEstoque.setValue(p.getEstoque());
    }

    private void salvar() {
        int id;
        try {
            id = Integer.parseInt(txtId.getText().trim());
            if (id <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Informe um código ID numérico válido maior que zero!", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtId.requestFocus();
            return;
        }

        String codBarras = txtCodigoBarras.getText().trim();
        String nome = txtNome.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O nome do produto/peça é obrigatório!", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtNome.requestFocus();
            return;
        }

        double preco;
        try {
            preco = Double.parseDouble(txtPreco.getText().trim().replace(",", "."));
            if (preco < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Informe um preço válido (ex: 45.00)!", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtPreco.requestFocus();
            return;
        }

        int estoque = (Integer) spEstoque.getValue();

        Produto p = new Produto(id, codBarras, nome, preco, estoque);
        if (produtoDAO.salvar(p)) {
            this.salvo = true;
            this.produtoCriado = p;
            JOptionPane.showMessageDialog(this, "Produto/Peça salvo com sucesso no estoque!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Falha ao gravar produto no banco MySQL!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSalvo() {
        return salvo;
    }

    public Produto getProdutoCriado() {
        return produtoCriado;
    }
}
