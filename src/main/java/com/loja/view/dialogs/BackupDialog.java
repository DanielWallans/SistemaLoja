package com.loja.view.dialogs;

import com.loja.service.BackupService;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class BackupDialog extends JDialog {
    private final Frame owner;

    private JTextField txtCaminhoDestino;
    private JLabel lblStatusOperacao;
    private JTextArea txtLog;
    private JButton btnFazerBackup;
    private JButton btnRestaurarBackup;

    public BackupDialog(Frame owner) {
        super(owner, "Segurança do Sistema • Backup & Restauração MySQL", true);
        this.owner = owner;

        setSize(640, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        initComponents();
    }

    private void initComponents() {
        // 1. Cabeçalho
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(44, 62, 80));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel lblTit = new JLabel("Backup e Restauração de Segurança (MySQL)");
        lblTit.setFont(lblTit.getFont().deriveFont(Font.BOLD, 16f));
        lblTit.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel(
                "Exporte cópias compactadas em .ZIP ou restaure o banco em caso de troca de computador.");
        lblSub.setForeground(new Color(189, 195, 199));

        pnlHeader.add(lblTit, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Corpo Central
        JPanel pnlCentro = new JPanel();
        pnlCentro.setLayout(new BoxLayout(pnlCentro, BoxLayout.Y_AXIS));
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        // Seção 1: Fazer Backup
        JPanel pnlBackupBox = new JPanel(new BorderLayout(8, 8));
        pnlBackupBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), " 1. Gerar Cópia de Segurança (Backup em .ZIP) ",
                0, 0, new Font("SansSerif", Font.BOLD, 12)));

        JPanel pnlDestino = new JPanel(new BorderLayout(6, 0));
        txtCaminhoDestino = new JTextField();
        File padrao = BackupService.gerarNomeArquivoPadrao(new File(System.getProperty("user.home"), "Documents"));
        txtCaminhoDestino.setText(padrao.getAbsolutePath());

        JButton btnProcurar = new JButton("Escolher Pasta...");
        btnProcurar.addActionListener(e -> escolherDestino());

        pnlDestino.add(txtCaminhoDestino, BorderLayout.CENTER);
        pnlDestino.add(btnProcurar, BorderLayout.EAST);
        pnlBackupBox.add(pnlDestino, BorderLayout.NORTH);

        JPanel pnlBtnBackup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        btnFazerBackup = new JButton("Criar Backup Completo Agora");
        btnFazerBackup.setFont(btnFazerBackup.getFont().deriveFont(Font.BOLD, 13f));
        btnFazerBackup.setBackground(new Color(39, 174, 96));
        btnFazerBackup.setForeground(Color.WHITE);
        btnFazerBackup.addActionListener(e -> executarBackup());
        pnlBtnBackup.add(btnFazerBackup);
        pnlBackupBox.add(pnlBtnBackup, BorderLayout.SOUTH);

        pnlCentro.add(pnlBackupBox);
        pnlCentro.add(Box.createVerticalStrut(12));

        // Seção 2: Restaurar Backup
        JPanel pnlRestaurarBox = new JPanel(new BorderLayout(8, 8));
        pnlRestaurarBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), " 2. Restaurar Banco de Dados a partir de um Backup ",
                0, 0, new Font("SansSerif", Font.BOLD, 12)));

        JLabel lblAvisoRest = new JLabel(
                "<html><font color='#c0392b'><b>ATENÇÃO:</b> A restauração substitui os dados atuais do banco pelos dados do arquivo selecionado.</font></html>");
        pnlRestaurarBox.add(lblAvisoRest, BorderLayout.CENTER);

        JPanel pnlBtnRestaurar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        btnRestaurarBackup = new JButton("Selecionar Arquivo .ZIP / .SQL e Restaurar");
        btnRestaurarBackup.setFont(btnRestaurarBackup.getFont().deriveFont(Font.BOLD, 12f));
        btnRestaurarBackup.setBackground(new Color(230, 126, 34));
        btnRestaurarBackup.setForeground(Color.WHITE);
        btnRestaurarBackup.addActionListener(e -> executarRestauracao());
        pnlBtnRestaurar.add(btnRestaurarBackup);
        pnlRestaurarBox.add(pnlBtnRestaurar, BorderLayout.SOUTH);

        pnlCentro.add(pnlRestaurarBox);
        pnlCentro.add(Box.createVerticalStrut(10));

        // Log / Histórico
        txtLog = new JTextArea(5, 40);
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtLog.setEditable(false);
        txtLog.setText("Pronto para operações de backup e restauração.\n");

        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder(" Detalhes da Execução "));
        pnlCentro.add(scrollLog);

        add(pnlCentro, BorderLayout.CENTER);

        // 3. Rodapé
        JPanel pnlRodape = new JPanel(new BorderLayout());
        pnlRodape.setBorder(BorderFactory.createEmptyBorder(6, 18, 12, 18));
        lblStatusOperacao = new JLabel(" ");
        lblStatusOperacao.setFont(lblStatusOperacao.getFont().deriveFont(Font.BOLD, 12f));

        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());

        pnlRodape.add(lblStatusOperacao, BorderLayout.WEST);
        pnlRodape.add(btnFechar, BorderLayout.EAST);
        add(pnlRodape, BorderLayout.SOUTH);
    }

    private void escolherDestino() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Salvar Backup Em");
        fc.setSelectedFile(new File(txtCaminhoDestino.getText().trim()));

        int opt = fc.showSaveDialog(this);
        if (opt == JFileChooser.APPROVE_OPTION) {
            File sel = fc.getSelectedFile();
            if (!sel.getName().toLowerCase().endsWith(".zip")) {
                sel = new File(sel.getAbsolutePath() + ".zip");
            }
            txtCaminhoDestino.setText(sel.getAbsolutePath());
        }
    }

    private void executarBackup() {
        File destino = new File(txtCaminhoDestino.getText().trim());
        btnFazerBackup.setEnabled(false);
        lblStatusOperacao.setText("⏳ Gerando backup...");

        SwingUtilities.invokeLater(() -> {
            BackupService.ResultadoBackup res = BackupService.realizarBackupZip(destino);
            btnFazerBackup.setEnabled(true);

            if (res.isSucesso()) {
                lblStatusOperacao.setText("Backup concluído com sucesso!");
                lblStatusOperacao.setForeground(new Color(39, 174, 96));

                String logMsg = String.format("=========================================\n" +
                        "BACKUP CONCLUÍDO COM SUCESSO!\n" +
                        "• Arquivo: %s\n" +
                        "• Tabelas Exportadas: %d\n" +
                        "• Total de Registros: %d\n" +
                        "• Tamanho do ZIP: %d KB\n" +
                        "• Tempo de Execução: %d ms\n" +
                        "=========================================\n",
                        res.getCaminhoArquivo(), res.getTotalTabelas(), res.getTotalRegistros(), res.getTamanhoKb(),
                        res.getTempoMs());
                txtLog.append(logMsg);
                JOptionPane.showMessageDialog(this, "Backup gerado com sucesso em:\n" + res.getCaminhoArquivo(),
                        "Backup Concluído", JOptionPane.INFORMATION_MESSAGE);
            } else {
                lblStatusOperacao.setText(res.getMensagem());
                lblStatusOperacao.setForeground(new Color(231, 76, 60));
                txtLog.append("ERRO NO BACKUP: " + res.getMensagem() + "\n");
                JOptionPane.showMessageDialog(this, res.getMensagem(), "Erro no Backup", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void executarRestauracao() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Selecionar Arquivo de Backup para Restaurar");

        int opt = fc.showOpenDialog(this);
        if (opt == JFileChooser.APPROVE_OPTION) {
            File arquivo = fc.getSelectedFile();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "ATENÇÃO: Deseja realmente restaurar o banco de dados?\n\n" +
                            "Arquivo: " + arquivo.getName() + "\n" +
                            "Todos os dados atuais serão substituídos pelos dados contidos neste backup.",
                    "Confirmar Restauração", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION)
                return;

            btnRestaurarBackup.setEnabled(false);
            lblStatusOperacao.setText("Restaurando banco de dados...");

            SwingUtilities.invokeLater(() -> {
                BackupService.ResultadoRestauracao res = BackupService.restaurarBackup(arquivo);
                btnRestaurarBackup.setEnabled(true);

                if (res.isSucesso()) {
                    lblStatusOperacao.setText(res.getMensagem());
                    lblStatusOperacao.setForeground(new Color(39, 174, 96));
                    txtLog.append("RESTAURAÇÃO: " + res.getMensagem() + "\n");
                    JOptionPane.showMessageDialog(this, res.getMensagem(), "Restauração Concluída",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    lblStatusOperacao.setText(res.getMensagem());
                    lblStatusOperacao.setForeground(new Color(231, 76, 60));
                    txtLog.append("ERRO RESTAURAÇÃO: " + res.getMensagem() + "\n");
                    JOptionPane.showMessageDialog(this, res.getMensagem(), "Erro na Restauração",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }
}
