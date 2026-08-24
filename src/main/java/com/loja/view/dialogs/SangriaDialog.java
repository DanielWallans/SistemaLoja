package com.loja.view.dialogs;

import com.loja.service.CaixaService;

import javax.swing.*;
import java.awt.*;

public class SangriaDialog extends JDialog {
    private final CaixaService caixaService;
    private final double saldoAtual;
    private boolean realizada = false;

    private JTextField txtValor;

    public SangriaDialog(Frame owner, CaixaService caixaService, double saldoAtual) {
        super(owner, "Registrar Retirada do Caixa (Sangria)", true);
        this.caixaService = caixaService;
        this.saldoAtual = saldoAtual;

        initComponents();
        setSize(420, 220);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        txtValor = new JTextField(12);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4;
        formPanel.add(new JLabel("Saldo Disponível:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6;
        JLabel lblSaldo = new JLabel(String.format("R$ %.2f", saldoAtual));
        lblSaldo.setFont(lblSaldo.getFont().deriveFont(Font.BOLD, 13f));
        lblSaldo.setForeground(new Color(39, 174, 96));
        formPanel.add(lblSaldo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Valor da Retirada (R$):"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtValor, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnConfirmar = new JButton("Confirmar Retirada");
        btnConfirmar.setFont(btnConfirmar.getFont().deriveFont(Font.BOLD));

        btnCancelar.addActionListener(e -> dispose());
        btnConfirmar.addActionListener(e -> efetuarSangria());

        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnConfirmar);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void efetuarSangria() {
        double valor;
        try {
            valor = Double.parseDouble(txtValor.getText().trim().replace(",", "."));
            if (valor <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Informe um valor positivo válido!", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtValor.requestFocus();
            return;
        }

        if (valor > saldoAtual) {
            JOptionPane.showMessageDialog(this, "Saldo insuficiente para retirar R$ " + String.format("%.2f", valor) + "!\nSaldo atual: R$ " + String.format("%.2f", saldoAtual), "Saldo Insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        caixaService.realizarSangria(valor);
        this.realizada = true;
        JOptionPane.showMessageDialog(this, "Retirada de R$ " + String.format("%.2f", valor) + " realizada com sucesso!", "Sangria Concluída", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    public boolean isRealizada() {
        return realizada;
    }
}
