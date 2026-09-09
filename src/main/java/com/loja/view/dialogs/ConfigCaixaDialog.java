package com.loja.view.dialogs;

import com.loja.model.SessaoUsuario;
import com.loja.model.Usuario;
import com.loja.repository.CaixaDAO;
import com.loja.view.theme.UIComponents;
import com.loja.view.theme.UITheme;

import javax.swing.*;
import java.awt.*;

public class ConfigCaixaDialog extends JDialog {
    private final CaixaDAO caixaDAO;

    // Aba 1: Dados da Empresa (Orçamento PDF)
    private JTextField txtNomeEmpresa;
    private JTextField txtCnpj;
    private JTextField txtTelefone;
    private JTextField txtEmail;
    private JTextField txtEndereco;
    private JTextArea txtTermosGarantia;

    // Aba 2: Caixa & Impressão
    private JTextField txtWhatsAppDono;
    private JTextField txtFundoTrocoPadrao;
    private JComboBox<String> cbLarguraCupom;

    private JButton btnSalvar;
    private boolean salvo = false;
    private boolean ehAdmin = true;

    public ConfigCaixaDialog(Frame owner, CaixaDAO caixaDAO) {
        super(owner, "Preferências da Empresa & Sistema", true);
        this.caixaDAO = caixaDAO;

        Usuario user = SessaoUsuario.getInstancia().getUsuarioLogado();
        this.ehAdmin = (user == null || user.isAdmin());

        setSize(580, 560);
        setLocationRelativeTo(owner);
        setResizable(false);
        setLayout(new BorderLayout());

        initComponents();
        carregarConfiguracoes();

        if (!ehAdmin) {
            desabilitarEdicaoNaoAdmin();
        }
    }

    private void initComponents() {
        // 1. Cabeçalho
        JPanel pnlHeader = new JPanel(new BorderLayout(UITheme.SPACE_12, UITheme.SPACE_4));
        pnlHeader.setBackground(UITheme.tokens().getBgCard());
        pnlHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.tokens().getBorderSubtle()),
                BorderFactory.createEmptyBorder(UITheme.SPACE_16, UITheme.SPACE_20, UITheme.SPACE_16, UITheme.SPACE_20)
        ));

        JLabel lblTit = new JLabel("🏢 Preferências da Empresa & Sistema");
        lblTit.setFont(UITheme.FONT_TITLE);
        lblTit.setForeground(UITheme.tokens().getTextPrimary());

        JLabel lblSub = new JLabel("Defina os dados da empresa para os Orçamentos em PDF e parâmetros do Caixa.");
        lblSub.setFont(UITheme.FONT_CAPTION);
        lblSub.setForeground(UITheme.tokens().getTextSecondary());

        pnlHeader.add(lblTit, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);

        if (!ehAdmin) {
            JLabel lblAviso = new JLabel("🔒 Modo Leitura: Apenas administradores podem salvar alterações nestas configurações.");
            lblAviso.setFont(UITheme.FONT_SMALL);
            lblAviso.setForeground(UITheme.tokens().getWarning());
            lblAviso.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACE_8, 0, 0, 0));
            pnlHeader.add(lblAviso, BorderLayout.PAGE_END);
        }

        add(pnlHeader, BorderLayout.NORTH);

        // 2. Abas de Configuração
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BODY);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACE_8, UITheme.SPACE_16, UITheme.SPACE_8, UITheme.SPACE_16));

        tabbedPane.addTab("📄 Dados da Empresa (PDF)", criarAbaEmpresa());
        tabbedPane.addTab("💰 Caixa & Impressão", criarAbaCaixa());

        add(tabbedPane, BorderLayout.CENTER);

        // 3. Rodapé com Ações
        JPanel pnlBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.SPACE_12, UITheme.SPACE_12));
        pnlBotoes.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.tokens().getBorderSubtle()));

        JButton btnCancelar = UIComponents.criarBotaoSecundario("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        btnSalvar = UIComponents.criarBotaoPrimario("💾 Salvar Alterações");
        btnSalvar.addActionListener(e -> salvar());

        pnlBotoes.add(btnCancelar);
        pnlBotoes.add(btnSalvar);
        add(pnlBotoes, BorderLayout.SOUTH);
    }

    private JPanel criarAbaEmpresa() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACE_12, UITheme.SPACE_8, UITheme.SPACE_12, UITheme.SPACE_8));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Nome da Empresa
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        pnl.add(criarLabelCampo("Nome / Razão Social:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        txtNomeEmpresa = new JTextField();
        txtNomeEmpresa.putClientProperty("JTextField.placeholderText", "Ex: TechAssist Manutenção & Eletrônica");
        pnl.add(txtNomeEmpresa, gbc);

        // CNPJ / CPF
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        pnl.add(criarLabelCampo("CNPJ / CPF:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        txtCnpj = new JTextField();
        txtCnpj.putClientProperty("JTextField.placeholderText", "Ex: 12.345.678/0001-90");
        pnl.add(txtCnpj, gbc);

        // Telefone / WhatsApp
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        pnl.add(criarLabelCampo("Telefone / WhatsApp Comercial:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        txtTelefone = new JTextField();
        txtTelefone.putClientProperty("JTextField.placeholderText", "Ex: (11) 98765-4321");
        pnl.add(txtTelefone, gbc);

        // E-mail
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        pnl.add(criarLabelCampo("E-mail de Contato:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        txtEmail = new JTextField();
        txtEmail.putClientProperty("JTextField.placeholderText", "Ex: contato@sualoja.com.br");
        pnl.add(txtEmail, gbc);

        // Endereço Completo
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        pnl.add(criarLabelCampo("Endereço Completo:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        txtEndereco = new JTextField();
        txtEndereco.putClientProperty("JTextField.placeholderText", "Ex: Av. Paulista, 1000, Bela Vista - São Paulo / SP");
        pnl.add(txtEndereco, gbc);

        // Termos de Garantia do PDF
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        pnl.add(criarLabelCampo("Termos de Garantia (PDF):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        txtTermosGarantia = new JTextArea(4, 20);
        txtTermosGarantia.setLineWrap(true);
        txtTermosGarantia.setWrapStyleWord(true);
        txtTermosGarantia.setFont(UITheme.FONT_BODY);
        JScrollPane scrollTermos = new JScrollPane(txtTermosGarantia);
        pnl.add(scrollTermos, gbc);

        return pnl;
    }

    private JPanel criarAbaCaixa() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACE_16, UITheme.SPACE_8, UITheme.SPACE_16, UITheme.SPACE_8));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // WhatsApp do Dono
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        pnl.add(criarLabelCampo("WhatsApp do Dono (com DDD):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtWhatsAppDono = new JTextField();
        txtWhatsAppDono.putClientProperty("JTextField.placeholderText", "Ex: 11999998888 (recebe resumo do fechamento)");
        pnl.add(txtWhatsAppDono, gbc);

        // Fundo de Troco Padrão
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        pnl.add(criarLabelCampo("Fundo de Troco Sugerido (R$):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtFundoTrocoPadrao = new JTextField("100.00");
        pnl.add(txtFundoTrocoPadrao, gbc);

        // Largura do Cupom
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        pnl.add(criarLabelCampo("Formato do Cupom Térmico:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        cbLarguraCupom = new JComboBox<>(new String[]{
                "80mm (Padrão Comercial - 48 Colunas)",
                "58mm (Mini Impressora Térmica - 32 Colunas)"
        });
        pnl.add(cbLarguraCupom, gbc);

        // Espaço vazio para manter o topo alinhado
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        pnl.add(Box.createGlue(), gbc);

        return pnl;
    }

    private JLabel criarLabelCampo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.tokens().getTextPrimary());
        return lbl;
    }

    private void carregarConfiguracoes() {
        txtNomeEmpresa.setText(caixaDAO.obterConfig("nome_loja", "Assistência Técnica & Gestão Pro"));
        txtCnpj.setText(caixaDAO.obterConfig("cnpj_loja", ""));
        txtTelefone.setText(caixaDAO.obterConfig("telefone_loja", ""));
        txtEmail.setText(caixaDAO.obterConfig("email_loja", ""));
        txtEndereco.setText(caixaDAO.obterConfig("endereco_loja", ""));
        txtTermosGarantia.setText(caixaDAO.obterConfig("termos_garantia_pdf",
                "Garantia legal de 90 dias conforme Art. 26 do CDC sobre peças trocadas e serviços executados. " +
                "Orçamento válido por 10 dias úteis."));

        txtWhatsAppDono.setText(caixaDAO.obterConfig("whatsapp_proprietario", ""));
        txtFundoTrocoPadrao.setText(caixaDAO.obterConfig("fundo_troco_padrao", "100.00"));
        String larg = caixaDAO.obterConfig("largura_cupom_mm", "80");
        if ("58".equals(larg)) {
            cbLarguraCupom.setSelectedIndex(1);
        } else {
            cbLarguraCupom.setSelectedIndex(0);
        }
    }

    private void desabilitarEdicaoNaoAdmin() {
        txtNomeEmpresa.setEditable(false);
        txtCnpj.setEditable(false);
        txtTelefone.setEditable(false);
        txtEmail.setEditable(false);
        txtEndereco.setEditable(false);
        txtTermosGarantia.setEditable(false);

        txtWhatsAppDono.setEditable(false);
        txtFundoTrocoPadrao.setEditable(false);
        cbLarguraCupom.setEnabled(false);

        btnSalvar.setEnabled(false);
        btnSalvar.setToolTipText("Apenas usuários com perfil Administrador podem alterar as configurações.");
    }

    private void salvar() {
        if (!ehAdmin) {
            JOptionPane.showMessageDialog(this,
                    "Apenas usuários Administradores têm permissão para alterar as preferências da empresa.",
                    "Acesso Negado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nome = txtNomeEmpresa.getText().trim();
        String cnpj = txtCnpj.getText().trim();
        String tel = txtTelefone.getText().trim();
        String email = txtEmail.getText().trim();
        String endereco = txtEndereco.getText().trim();
        String termos = txtTermosGarantia.getText().trim();

        String whats = txtWhatsAppDono.getText().replaceAll("[^0-9]", "").trim();
        String troco = txtFundoTrocoPadrao.getText().trim().replace(",", ".");
        String larg = cbLarguraCupom.getSelectedIndex() == 1 ? "58" : "80";

        // Salvar preferências no banco
        caixaDAO.salvarConfig("nome_loja", nome.isEmpty() ? "Assistência Técnica Pro" : nome);
        caixaDAO.salvarConfig("cnpj_loja", cnpj);
        caixaDAO.salvarConfig("telefone_loja", tel);
        caixaDAO.salvarConfig("email_loja", email);
        caixaDAO.salvarConfig("endereco_loja", endereco);
        caixaDAO.salvarConfig("termos_garantia_pdf", termos);

        caixaDAO.salvarConfig("whatsapp_proprietario", whats);
        caixaDAO.salvarConfig("fundo_troco_padrao", troco.isEmpty() ? "100.00" : troco);
        caixaDAO.salvarConfig("largura_cupom_mm", larg);

        this.salvo = true;
        JOptionPane.showMessageDialog(this,
                "Preferências da empresa e do sistema salvas com sucesso!\n" +
                "Os novos orçamentos em PDF serão gerados com estas informações.",
                "Configurações Salvas", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    public boolean isSalvo() {
        return salvo;
    }
}
