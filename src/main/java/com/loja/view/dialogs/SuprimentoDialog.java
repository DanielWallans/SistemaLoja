package com.loja.view.dialogs;

import com.loja.service.CaixaService;

import javax.swing.*;
import java.awt.*;
import com.loja.view.theme.UIComponents;

public class SuprimentoDialog extends JDialog {
    private final CaixaService caixaService;
    private boolean realizada = false;

    private JTextField txtValor;
    private JTextField txtJustificativa;

    public SuprimentoDialog(Frame owner, CaixaService caixaService) {
        super(owner, "Registrar Suprimento (Entrada de Troco / Reforço)", true);
        this.caixaService = caixaService;

        initComponents();
        setSize(460, 260);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridLayout(2, 2, 10, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 15, 25));

        txtValor = new JTextField(10);
        txtJustificativa = new JTextField("Entrada de Troco Inicial");

        mainPanel.add(new JLabel("Valor a Entrar (R$) *:"));
        mainPanel.add(txtValor);

        mainPanel.add(new JLabel("Justificativa / Motivo *:"));
        mainPanel.add(txtJustificativa);

        add(mainPanel, BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnCancelar = new JButton("Cancelar");
        UIComponents.estilizarBotaoSecundario(btnCancelar);

        JButton btnConfirmar = new JButton("Confirmar Suprimento");
        UIComponents.estilizarBotaoPrimario(btnConfirmar);

        btnCancelar.addActionListener(e -> dispose());
        btnConfirmar.addActionListener(e -> confirmarSuprimento());

        pnlButtons.add(btnCancelar);
        pnlButtons.add(btnConfirmar);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    private void confirmarSuprimento() {
        String valStr = txtValor.getText().trim().replace(",", ".");
        String just = txtJustificativa.getText().trim();

        if (valStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o valor do suprimento!", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtValor.requestFocus();
            return;
        }

        if (just.isEmpty()) {
            JOptionPane.showMessageDialog(this, "A justificativa do suprimento é obrigatória!", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtJustificativa.requestFocus();
            return;
        }

        try {
            double valor = Double.parseDouble(valStr);
            if (valor <= 0) {
                JOptionPane.showMessageDialog(this, "O valor deve ser maior que zero!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = caixaService.realizarSuprimento(valor, just);
            if (ok) {
                this.realizada = true;
                JOptionPane.showMessageDialog(this, "Suprimento de R$ " + String.format("%.2f", valor) + " registrado com sucesso no caixa!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao registrar suprimento no banco de dados.", "Erro", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Informe um valor numérico válido!", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isRealizada() {
        return realizada;
    }
}
