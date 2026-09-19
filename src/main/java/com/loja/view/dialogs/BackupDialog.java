package com.loja.view.dialogs;

import com.loja.model.AutoBackupConfig;
import com.loja.service.AutoBackupService;
import com.loja.service.BackupService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupDialog extends JDialog {
    private final Frame owner;

    // --- Componentes Aba Backup Automático ---
    private JCheckBox chkAtivo;
    private JCheckBox chkPosOperacao;
    private JCheckBox chkAoFechar;
    private JCheckBox chkLembreteInicial;
    private JCheckBox chkPeriodico;
    private JSpinner spnIntervalo;
    private JTextField txtAutoPastaDestino;
    private JTextField txtAutoPastaSecundaria;
    private JSpinner spnRetencao;
    private JLabel lblStatusAuto;
    private JButton btnSalvarConfig;
    private JButton btnTestarAutoBackup;

    // --- Componentes Aba Backup Manual ---
    private JTextField txtCaminhoDestino;
    private JLabel lblStatusOperacao;
    private JTextArea txtLog;
    private JButton btnFazerBackup;
    private JButton btnRestaurarBackup;

    public BackupDialog(Frame owner) {
        super(owner, "Segurança do Sistema • Central de Backup & Restauração", true);
        this.owner = owner;

        setSize(780, 640);
        setMinimumSize(new Dimension(700, 560));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        carregarDadosConfiguracao();
    }

    private void initComponents() {
        // 1. Cabeçalho
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(33, 37, 41));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel lblTit = new JLabel("Central de Segurança • Backup & Restauração");
        lblTit.setFont(lblTit.getFont().deriveFont(Font.BOLD, 17f));
        lblTit.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel(
                "Proteção contínua contra quedas de energia, panes de disco e sincronização com Nuvem/Pendrive.");
        lblSub.setForeground(new Color(173, 181, 189));

        pnlHeader.add(lblTit, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Abas Centrais
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        tabbedPane.addTab("🛡️ Backup Automático (Anti-Apagão)", criarAbaBackupAutomatico());
        tabbedPane.addTab("📦 Backup Manual & Restauração", criarAbaBackupManual());

        add(tabbedPane, BorderLayout.CENTER);

        // 3. Rodapé
        JPanel pnlRodape = new JPanel(new BorderLayout());
        pnlRodape.setBorder(BorderFactory.createEmptyBorder(6, 20, 12, 20));

        lblStatusOperacao = new JLabel("Pronto.");
        lblStatusOperacao.setFont(lblStatusOperacao.getFont().deriveFont(Font.BOLD, 12f));

        JButton btnFechar = new JButton("Fechar");
        btnFechar.setPreferredSize(new Dimension(100, 32));
        btnFechar.addActionListener(e -> dispose());

        pnlRodape.add(lblStatusOperacao, BorderLayout.WEST);
        pnlRodape.add(btnFechar, BorderLayout.EAST);
        add(pnlRodape, BorderLayout.SOUTH);
    }

    private JPanel criarAbaBackupAutomatico() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Chave Geral
        JPanel pnlAtivacao = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pnlAtivacao.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), " Status do Serviço ",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12)));

        chkAtivo = new JCheckBox("Ativar Sistema de Backup Automático em Segundo Plano");
        chkAtivo.setFont(chkAtivo.getFont().deriveFont(Font.BOLD, 13f));
        chkAtivo.addActionListener(e -> atualizarHabilitacaoCampos());
        pnlAtivacao.add(chkAtivo);
        pnl.add(pnlAtivacao);
        pnl.add(Box.createVerticalStrut(8));

        // 2. Gatilhos de Proteção
        JPanel pnlGatilhos = new JPanel();
        pnlGatilhos.setLayout(new BoxLayout(pnlGatilhos, BoxLayout.Y_AXIS));
        pnlGatilhos.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), " Gatilhos de Proteção em Tempo Real ",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12)));

        chkPosOperacao = new JCheckBox("⚡ Proteção Anti-Apagão: Salvar automaticamente após Vendas e Ordens de Serviço");
        chkPosOperacao.setFont(chkPosOperacao.getFont().deriveFont(Font.BOLD, 12f));
        chkPosOperacao.setForeground(new Color(46, 204, 113));
        chkPosOperacao.setToolTipText("Gera um snapshot imediato após 25 segundos de cada venda ou alteração de OS.");

        JLabel lblDicaAntiApagao = new JLabel("   └ Garante que mesmo com queda brusca de energia, o último atendimento estará salvo no arquivo.");
        lblDicaAntiApagao.setForeground(new Color(149, 165, 166));
        lblDicaAntiApagao.setFont(lblDicaAntiApagao.getFont().deriveFont(11f));

        chkAoFechar = new JCheckBox("🔒 Salvar cópia de segurança completa ao fechar o sistema ou encerrar o caixa");
        chkAoFechar.setFont(chkAoFechar.getFont().deriveFont(Font.PLAIN, 12f));

        chkLembreteInicial = new JCheckBox("🔔 Lembrar de fazer backup ao iniciar o sistema (se não houver backup no dia)");
        chkLembreteInicial.setFont(chkLembreteInicial.getFont().deriveFont(Font.PLAIN, 12f));

        JPanel pnlPeriodicoLinha = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlPeriodicoLinha.setOpaque(false);
        chkPeriodico = new JCheckBox("⏱️ Backup periódico em segundo plano a cada:");
        spnIntervalo = new JSpinner(new SpinnerNumberModel(15, 1, 1440, 5));
        spnIntervalo.setPreferredSize(new Dimension(65, 26));
        JLabel lblMin = new JLabel("minutos");

        pnlPeriodicoLinha.add(chkPeriodico);
        pnlPeriodicoLinha.add(spnIntervalo);
        pnlPeriodicoLinha.add(lblMin);

        pnlGatilhos.add(chkPosOperacao);
        pnlGatilhos.add(lblDicaAntiApagao);
        pnlGatilhos.add(Box.createVerticalStrut(6));
        pnlGatilhos.add(chkAoFechar);
        pnlGatilhos.add(Box.createVerticalStrut(6));
        pnlGatilhos.add(chkLembreteInicial);
        pnlGatilhos.add(Box.createVerticalStrut(6));
        pnlGatilhos.add(pnlPeriodicoLinha);

        pnl.add(pnlGatilhos);
        pnl.add(Box.createVerticalStrut(8));

        // 3. Pastas de Armazenamento
        JPanel pnlPastas = new JPanel(new GridBagLayout());
        pnlPastas.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), " Locais de Armazenamento & Nuvem ",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Pasta Principal
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        pnlPastas.add(new JLabel("Pasta Principal:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtAutoPastaDestino = new JTextField();
        pnlPastas.add(txtAutoPastaDestino, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        JButton btnProcurarPrinc = new JButton("Procurar...");
        btnProcurarPrinc.addActionListener(e -> escolherPasta(txtAutoPastaDestino, "Escolher Pasta Principal de Backup"));
        pnlPastas.add(btnProcurarPrinc, gbc);

        // Pasta Secundária
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        pnlPastas.add(new JLabel("Pendrive / Nuvem (Opcional):"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        txtAutoPastaSecundaria = new JTextField();
        pnlPastas.add(txtAutoPastaSecundaria, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0;
        JButton btnProcurarSec = new JButton("Procurar...");
        btnProcurarSec.addActionListener(e -> escolherPasta(txtAutoPastaSecundaria, "Escolher Pasta Secundária / Pendrive"));
        pnlPastas.add(btnProcurarSec, gbc);

        // Dica
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        JLabel lblDicaNuvem = new JLabel("💡 Dica: Aponte a pasta para o Google Drive ou OneDrive para salvar na nuvem automaticamente.");
        lblDicaNuvem.setForeground(new Color(52, 152, 219));
        lblDicaNuvem.setFont(lblDicaNuvem.getFont().deriveFont(11f));
        pnlPastas.add(lblDicaNuvem, gbc);

        pnl.add(pnlPastas);
        pnl.add(Box.createVerticalStrut(8));

        // 4. Retenção e Rotação
        JPanel pnlRetencao = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        pnlRetencao.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), " Gerenciamento de Espaço em Disco ",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12)));

        pnlRetencao.add(new JLabel("Manter os últimos:"));
        spnRetencao = new JSpinner(new SpinnerNumberModel(20, 3, 365, 1));
        spnRetencao.setPreferredSize(new Dimension(65, 26));
        pnlRetencao.add(spnRetencao);
        pnlRetencao.add(new JLabel("arquivos de backup (os mais antigos serão excluídos automaticamente)."));

        pnl.add(pnlRetencao);
        pnl.add(Box.createVerticalStrut(8));

        // 5. Painel de Ações & Status
        JPanel pnlStatusCard = new JPanel(new BorderLayout(10, 6));
        pnlStatusCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        lblStatusAuto = new JLabel("Status: Carregando informações...");
        lblStatusAuto.setFont(lblStatusAuto.getFont().deriveFont(Font.BOLD, 12f));
        pnlStatusCard.add(lblStatusAuto, BorderLayout.CENTER);

        JPanel pnlBotoesAuto = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnTestarAutoBackup = new JButton("⚡ Executar Backup Agora");
        btnTestarAutoBackup.setBackground(new Color(41, 128, 185));
        btnTestarAutoBackup.setForeground(Color.WHITE);
        btnTestarAutoBackup.addActionListener(e -> executarBackupAutomaticoTeste());

        btnSalvarConfig = new JButton("💾 Salvar Configurações");
        btnSalvarConfig.setBackground(new Color(39, 174, 96));
        btnSalvarConfig.setForeground(Color.WHITE);
        btnSalvarConfig.setFont(btnSalvarConfig.getFont().deriveFont(Font.BOLD, 12f));
        btnSalvarConfig.addActionListener(e -> salvarConfiguracoesAutomaticas());

        pnlBotoesAuto.add(btnTestarAutoBackup);
        pnlBotoesAuto.add(btnSalvarConfig);
        pnlStatusCard.add(pnlBotoesAuto, BorderLayout.EAST);

        pnl.add(pnlStatusCard);

        return pnl;
    }

    private JPanel criarAbaBackupManual() {
        JPanel pnlCentro = new JPanel();
        pnlCentro.setLayout(new BoxLayout(pnlCentro, BoxLayout.Y_AXIS));
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Seção 1: Fazer Backup Manual
        JPanel pnlBackupBox = new JPanel(new BorderLayout(8, 8));
        pnlBackupBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), " 1. Gerar Cópia de Segurança Avulsa (.ZIP) ",
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

        JPanel pnlBtnBackup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        btnFazerBackup = new JButton("Criar Backup Completo Agora");
        btnFazerBackup.setFont(btnFazerBackup.getFont().deriveFont(Font.BOLD, 13f));
        btnFazerBackup.setBackground(new Color(39, 174, 96));
        btnFazerBackup.setForeground(Color.WHITE);
        btnFazerBackup.addActionListener(e -> executarBackup());
        pnlBtnBackup.add(btnFazerBackup);
        pnlBackupBox.add(pnlBtnBackup, BorderLayout.SOUTH);

        pnlCentro.add(pnlBackupBox);
        pnlCentro.add(Box.createVerticalStrut(10));

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
        txtLog = new JTextArea(6, 40);
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtLog.setEditable(false);
        txtLog.setText("Pronto para operações de backup e restauração.\n");

        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder(" Detalhes da Execução "));
        pnlCentro.add(scrollLog);

        return pnlCentro;
    }

    private void carregarDadosConfiguracao() {
        AutoBackupConfig config = AutoBackupService.getInstance().getConfig();

        chkAtivo.setSelected(config.isAtivo());
        chkPosOperacao.setSelected(config.isSalvarPosOperacaoCritica());
        chkAoFechar.setSelected(config.isSalvarAoFechar());
        chkLembreteInicial.setSelected(config.isLembreteInicial());
        chkPeriodico.setSelected(config.isSalvarPeriodico());
        spnIntervalo.setValue(config.getIntervaloMinutos());
        txtAutoPastaDestino.setText(config.getPastaDestino());
        txtAutoPastaSecundaria.setText(config.getPastaSecundaria());
        spnRetencao.setValue(config.getRetencaoMaxArquivos());

        atualizarLabelStatus(config);
        atualizarHabilitacaoCampos();
    }

    private void atualizarLabelStatus(AutoBackupConfig config) {
        String timestamp = config.getUltimoBackupTimestamp();
        String status = config.getUltimoBackupStatus();

        if (timestamp == null || timestamp.trim().isEmpty()) {
            lblStatusAuto.setText("<html>Status: <font color='#f39c12'>Nenhum backup automático realizado nesta sessão.</font></html>");
        } else {
            lblStatusAuto.setText(String.format("<html>Último Backup: <b>%s</b> &nbsp;|&nbsp; <font color='#2ecc71'>%s</font></html>",
                    timestamp, (status != null && !status.isEmpty()) ? status : "Concluído"));
        }
    }

    private void atualizarHabilitacaoCampos() {
        boolean ativo = chkAtivo.isSelected();
        chkPosOperacao.setEnabled(ativo);
        chkAoFechar.setEnabled(ativo);
        chkLembreteInicial.setEnabled(ativo);
        chkPeriodico.setEnabled(ativo);
        spnIntervalo.setEnabled(ativo && chkPeriodico.isSelected());
        txtAutoPastaDestino.setEnabled(ativo);
        txtAutoPastaSecundaria.setEnabled(ativo);
        spnRetencao.setEnabled(ativo);
        btnTestarAutoBackup.setEnabled(ativo);
    }

    private void escolherPasta(JTextField targetField, String titulo) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(titulo);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        String atual = targetField.getText().trim();
        if (!atual.isEmpty()) {
            File f = new File(atual);
            if (f.exists()) {
                fc.setSelectedFile(f);
            }
        }

        int opt = fc.showOpenDialog(this);
        if (opt == JFileChooser.APPROVE_OPTION) {
            File dir = fc.getSelectedFile();
            targetField.setText(dir.getAbsolutePath());
        }
    }

    private void salvarConfiguracoesAutomaticas() {
        String pasta = txtAutoPastaDestino.getText().trim();
        if (pasta.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione uma pasta principal de destino para o backup.",
                    "Pasta Obrigatória", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AutoBackupConfig config = AutoBackupService.getInstance().getConfig();
        config.setAtivo(chkAtivo.isSelected());
        config.setSalvarPosOperacaoCritica(chkPosOperacao.isSelected());
        config.setSalvarAoFechar(chkAoFechar.isSelected());
        config.setLembreteInicial(chkLembreteInicial.isSelected());
        config.setSalvarPeriodico(chkPeriodico.isSelected());
        config.setIntervaloMinutos((Integer) spnIntervalo.getValue());
        config.setPastaDestino(pasta);
        config.setPastaSecundaria(txtAutoPastaSecundaria.getText().trim());
        config.setRetencaoMaxArquivos((Integer) spnRetencao.getValue());

        boolean ok = AutoBackupService.getInstance().getConfigDAO().salvarConfiguracoes(config);
        AutoBackupService.getInstance().recarregarConfiguracoes();

        if (ok) {
            lblStatusOperacao.setText("Configurações de backup salvas com sucesso!");
            lblStatusOperacao.setForeground(new Color(39, 174, 96));
            JOptionPane.showMessageDialog(this, "Configurações de backup atualizadas com sucesso!",
                    "Configuração Salva", JOptionPane.INFORMATION_MESSAGE);
        } else {
            lblStatusOperacao.setText("Erro ao salvar configurações.");
            lblStatusOperacao.setForeground(new Color(231, 76, 60));
        }
    }

    private void executarBackupAutomaticoTeste() {
        btnTestarAutoBackup.setEnabled(false);
        lblStatusOperacao.setText("⏳ Gerando snapshot do backup automático...");

        new Thread(() -> {
            AutoBackupService.getInstance().executarBackupSilencioso("Teste Manual do Usuário", false);
            SwingUtilities.invokeLater(() -> {
                btnTestarAutoBackup.setEnabled(true);
                lblStatusOperacao.setText("Backup concluído com sucesso!");
                lblStatusOperacao.setForeground(new Color(39, 174, 96));
                atualizarLabelStatus(AutoBackupService.getInstance().getConfig());
                JOptionPane.showMessageDialog(this, "Backup de teste gerado com sucesso!\nVerifique a pasta de destino configurada.",
                        "Backup Concluído", JOptionPane.INFORMATION_MESSAGE);
            });
        }).start();
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
