package com.loja.view.dialogs;

import com.loja.model.AutoBackupConfig;
import com.loja.service.AutoBackupService;
import com.loja.service.BackupService;

import javax.swing.*;
import java.awt.*;

public class LembreteBackupDialog extends JDialog {

    private final Frame owner;
    private JCheckBox chkLembrarSempre;
    private JLabel lblStatusExec;
    private JButton btnFazerAgora;
    private JButton btnAbrirCentral;
    private JButton btnContinuar;

    public LembreteBackupDialog(Frame owner) {
        super(owner, "Segurança do Sistema • Lembrete de Backup", true);
        this.owner = owner;

        setSize(560, 360);
        setMinimumSize(new Dimension(520, 340));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        initComponents();
    }

    private void initComponents() {
        AutoBackupConfig config = AutoBackupService.getInstance().getConfig();

        // 1. Cabeçalho
        JPanel pnlHeader = new JPanel(new BorderLayout(12, 0));
        pnlHeader.setBackground(new Color(33, 37, 41));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel lblIcone = new JLabel("🛡️");
        lblIcone.setFont(lblIcone.getFont().deriveFont(28f));
        pnlHeader.add(lblIcone, BorderLayout.WEST);

        JPanel pnlTitulos = new JPanel(new GridLayout(2, 1, 2, 2));
        pnlTitulos.setOpaque(false);

        JLabel lblTit = new JLabel("Lembrete de Segurança • Backup do Sistema");
        lblTit.setFont(lblTit.getFont().deriveFont(Font.BOLD, 15f));
        lblTit.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Mantenha as cópias de segurança em dia para proteger suas vendas e OS.");
        lblSub.setForeground(new Color(173, 181, 189));
        lblSub.setFont(lblSub.getFont().deriveFont(12f));

        pnlTitulos.add(lblTit);
        pnlTitulos.add(lblSub);
        pnlHeader.add(pnlTitulos, BorderLayout.CENTER);

        add(pnlHeader, BorderLayout.NORTH);

        // 2. Corpo Central
        JPanel pnlCentro = new JPanel();
        pnlCentro.setLayout(new BoxLayout(pnlCentro, BoxLayout.Y_AXIS));
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));

        // Card de Informações do Último Backup
        JPanel pnlInfoCard = new JPanel(new GridLayout(2, 1, 6, 6));
        pnlInfoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        String ultimo = config.getUltimoBackupTimestamp();
        boolean temBackupRecente = ultimo != null && !ultimo.trim().isEmpty();

        String textoUltimo = temBackupRecente
                ? "<html>Último backup registrado: <b>" + ultimo + "</b> (" + config.getUltimoBackupStatus() + ")</html>"
                : "<html><font color='#e67e22'>⚠️ Nenhum backup recente foi localizado neste computador.</font></html>";

        JLabel lblUltimo = new JLabel(textoUltimo);
        lblUltimo.setFont(lblUltimo.getFont().deriveFont(12f));

        String pasta = config.getPastaDestino();
        if (pasta.length() > 48) {
            pasta = "..." + pasta.substring(pasta.length() - 45);
        }
        JLabel lblDestino = new JLabel("📁 Pasta de Destino: " + pasta);
        lblDestino.setForeground(new Color(149, 165, 166));
        lblDestino.setFont(lblDestino.getFont().deriveFont(11f));

        pnlInfoCard.add(lblUltimo);
        pnlInfoCard.add(lblDestino);
        pnlCentro.add(pnlInfoCard);

        pnlCentro.add(Box.createVerticalStrut(12));

        // Status de Execução Dinâmico
        lblStatusExec = new JLabel("Recomendamos gerar uma cópia de segurança antes de iniciar o atendimento do dia.");
        lblStatusExec.setFont(lblStatusExec.getFont().deriveFont(Font.PLAIN, 12f));
        pnlCentro.add(lblStatusExec);

        pnlCentro.add(Box.createVerticalGlue());

        // Checkbox de Preferência
        chkLembrarSempre = new JCheckBox("Lembrar ao iniciar o sistema se não houver backup no dia", config.isLembreteInicial());
        chkLembrarSempre.setFont(chkLembrarSempre.getFont().deriveFont(11f));
        chkLembrarSempre.addActionListener(e -> {
            config.setLembreteInicial(chkLembrarSempre.isSelected());
            AutoBackupService.getInstance().getConfigDAO().salvarConfiguracoes(config);
        });
        pnlCentro.add(chkLembrarSempre);

        add(pnlCentro, BorderLayout.CENTER);

        // 3. Rodapé com Botões
        JPanel pnlRodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        pnlRodape.setBorder(BorderFactory.createEmptyBorder(0, 14, 6, 14));

        btnContinuar = new JButton("Continuar para o Sistema");
        btnContinuar.addActionListener(e -> dispose());

        btnAbrirCentral = new JButton("Central de Backup");
        btnAbrirCentral.addActionListener(e -> {
            dispose();
            BackupDialog dlg = new BackupDialog(owner);
            dlg.setVisible(true);
        });

        btnFazerAgora = new JButton("⚡ Fazer Backup Agora");
        btnFazerAgora.setBackground(new Color(39, 174, 96));
        btnFazerAgora.setForeground(Color.WHITE);
        btnFazerAgora.setFont(btnFazerAgora.getFont().deriveFont(Font.BOLD, 12f));
        btnFazerAgora.addActionListener(e -> executarBackupRapido());

        pnlRodape.add(btnContinuar);
        pnlRodape.add(btnAbrirCentral);
        pnlRodape.add(btnFazerAgora);

        add(pnlRodape, BorderLayout.SOUTH);
    }

    private void executarBackupRapido() {
        btnFazerAgora.setEnabled(false);
        btnContinuar.setEnabled(false);
        btnAbrirCentral.setEnabled(false);
        lblStatusExec.setText("⏳ Gerando cópia de segurança rápida...");
        lblStatusExec.setForeground(new Color(52, 152, 219));

        new Thread(() -> {
            AutoBackupService.getInstance().executarBackupSilencioso("Lembrete de Inicialização", false);
            SwingUtilities.invokeLater(() -> {
                lblStatusExec.setText("✅ Backup realizado com sucesso!");
                lblStatusExec.setForeground(new Color(39, 174, 96));

                Timer t = new Timer(900, evt -> dispose());
                t.setRepeats(false);
                t.start();
            });
        }).start();
    }
}
