package com.loja.view.dialogs;

import com.loja.repository.CaixaDAO;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigTaxasDialog extends JDialog {
    private final CaixaDAO caixaDAO;
    private boolean salvo = false;

    private JTextField txtDebito;
    private JTextField txtCredito1x;
    private JTextField txtCredito2x6x;
    private JTextField txtCredito7x12x;

    public ConfigTaxasDialog(Frame owner, CaixaDAO caixaDAO) {
        super(owner, "Configuração de Taxas da Maquininha de Cartão", true);
        this.caixaDAO = caixaDAO;

        initComponents();
        carregarTaxas();
        setSize(480, 360);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblInfo = new JLabel("<html><b>Defina as taxas descontadas pela sua operadora de cartão.</b><br>Essas taxas são usadas para calcular automaticamente o valor líquido real recebido pela loja.</html>");
        lblInfo.setFont(lblInfo.getFont().deriveFont(12f));
        mainPanel.add(lblInfo);
        mainPanel.add(Box.createVerticalStrut(15));

        JPanel pnlForm = new JPanel(new GridLayout(4, 2, 10, 10));
        pnlForm.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), " Taxas por Modalidade (%) ",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12)
        ));

        txtDebito = new JTextField(8);
        txtCredito1x = new JTextField(8);
        txtCredito2x6x = new JTextField(8);
        txtCredito7x12x = new JTextField(8);

        pnlForm.add(new JLabel("Cartão de Débito (%):"));
        pnlForm.add(txtDebito);

        pnlForm.add(new JLabel("Crédito à Vista 1x (%):"));
        pnlForm.add(txtCredito1x);

        pnlForm.add(new JLabel("Crédito Parcelado 2x a 6x (%):"));
        pnlForm.add(txtCredito2x6x);

        pnlForm.add(new JLabel("Crédito Parcelado 7x a 12x (%):"));
        pnlForm.add(txtCredito7x12x);

        mainPanel.add(pnlForm);
        add(mainPanel, BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnSalvar = new JButton("💾 Salvar Taxas");
        btnSalvar.setFont(btnSalvar.getFont().deriveFont(Font.BOLD));

        btnCancelar.addActionListener(e -> dispose());
        btnSalvar.addActionListener(e -> salvarTaxas());

        pnlButtons.add(btnCancelar);
        pnlButtons.add(btnSalvar);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    private void carregarTaxas() {
        Map<String, Double> taxas = caixaDAO.obterTaxasMaquininha();
        txtDebito.setText(String.format("%.2f", taxas.getOrDefault("DEBITO", 1.50)).replace(",", "."));
        txtCredito1x.setText(String.format("%.2f", taxas.getOrDefault("CREDITO_1X", 3.20)).replace(",", "."));
        txtCredito2x6x.setText(String.format("%.2f", taxas.getOrDefault("CREDITO_PARCELADO_2X_6X", 5.50)).replace(",", "."));
        txtCredito7x12x.setText(String.format("%.2f", taxas.getOrDefault("CREDITO_PARCELADO_7X_12X", 9.80)).replace(",", "."));
    }

    private void salvarTaxas() {
        try {
            double debito = Double.parseDouble(txtDebito.getText().trim().replace(",", "."));
            double cred1x = Double.parseDouble(txtCredito1x.getText().trim().replace(",", "."));
            double cred2x6x = Double.parseDouble(txtCredito2x6x.getText().trim().replace(",", "."));
            double cred7x12x = Double.parseDouble(txtCredito7x12x.getText().trim().replace(",", "."));

            if (debito < 0 || cred1x < 0 || cred2x6x < 0 || cred7x12x < 0) {
                JOptionPane.showMessageDialog(this, "As taxas não podem ser negativas!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Map<String, Double> taxas = new HashMap<>();
            taxas.put("DINHEIRO", 0.0);
            taxas.put("PIX", 0.0);
            taxas.put("DEBITO", debito);
            taxas.put("CREDITO_1X", cred1x);
            taxas.put("CREDITO_PARCELADO_2X_6X", cred2x6x);
            taxas.put("CREDITO_PARCELADO_7X_12X", cred7x12x);

            boolean ok = caixaDAO.salvarTaxasMaquininha(taxas);
            if (ok) {
                this.salvo = true;
                JOptionPane.showMessageDialog(this, "Taxas da maquininha atualizadas com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Falha ao gravar taxas no banco de dados.", "Erro", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Informe valores numéricos válidos (ex: 3.50)!", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isSalvo() {
        return salvo;
    }
}
