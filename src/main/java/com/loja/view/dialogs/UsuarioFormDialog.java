package com.loja.view.dialogs;

import com.loja.model.PerfilUsuario;
import com.loja.model.Usuario;
import com.loja.repository.UsuarioDAO;

import javax.swing.*;
import java.awt.*;
import com.loja.view.theme.UITheme;

public class UsuarioFormDialog extends JDialog {
    private final UsuarioDAO usuarioDAO;
    private final Usuario usuarioEdicao;

    private JTextField txtNome;
    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JComboBox<PerfilUsuario> cbPerfil;
    private JCheckBox chkAtivo;

    private boolean salvo = false;

    public UsuarioFormDialog(Dialog owner, UsuarioDAO usuarioDAO, Usuario usuarioEdicao) {
        super(owner, usuarioEdicao == null ? "Novo Usuário" : "Editar Usuário #" + usuarioEdicao.getId(), true);
        this.usuarioDAO = usuarioDAO;
        this.usuarioEdicao = usuarioEdicao;

        setSize(460, 420);
        setLocationRelativeTo(owner);
        setResizable(false);
        setLayout(new BorderLayout());

        initComponents();
        preencherDadosSeEdicao();
    }

    private void initComponents() {
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(UITheme.tokens().getBgSidebar());
        pnlHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.tokens().getBorderSubtle()),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        JLabel lblTit = new JLabel(usuarioEdicao == null ? "Cadastro de Novo Usuário" : "Editar Usuário");
        lblTit.setFont(lblTit.getFont().deriveFont(Font.BOLD, 15f));
        lblTit.setForeground(UITheme.tokens().getTextPrimary());

        JLabel lblSub = new JLabel("Defina o perfil de acesso e credenciais de login.");
        lblSub.setForeground(UITheme.tokens().getTextSecondary());

        pnlHeader.add(lblTit, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nome
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        pnlForm.add(new JLabel("Nome Completo:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtNome = new JTextField();
        pnlForm.add(txtNome, gbc);

        // Login
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        pnlForm.add(new JLabel("Usuário / Login:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtLogin = new JTextField();
        pnlForm.add(txtLogin, gbc);

        // Senha
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.35;
        JLabel lblSenha = new JLabel(usuarioEdicao == null ? "Senha de Acesso:" : "Nova Senha (Opcional):");
        pnlForm.add(lblSenha, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtSenha = new JPasswordField();
        pnlForm.add(txtSenha, gbc);

        // Perfil
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.35;
        pnlForm.add(new JLabel("Perfil de Acesso:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        cbPerfil = new JComboBox<>(PerfilUsuario.values());
        pnlForm.add(cbPerfil, gbc);

        // Ativo
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.35;
        pnlForm.add(new JLabel("Status da Conta:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        chkAtivo = new JCheckBox("Usuário Ativo (Pode fazer login)", true);
        pnlForm.add(chkAtivo, gbc);

        // Descrição do perfil
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JLabel lblDescPerfil = new JLabel(" ");
        lblDescPerfil.setFont(lblDescPerfil.getFont().deriveFont(Font.ITALIC, 11f));
        lblDescPerfil.setForeground(new Color(127, 140, 141));
        pnlForm.add(lblDescPerfil, gbc);

        cbPerfil.addActionListener(e -> {
            PerfilUsuario p = (PerfilUsuario) cbPerfil.getSelectedItem();
            if (p != null) lblDescPerfil.setText(p.getDescricao());
        });
        lblDescPerfil.setText(((PerfilUsuario) cbPerfil.getSelectedItem()).getDescricao());

        add(pnlForm, BorderLayout.CENTER);

        JPanel pnlBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnSalvar = new JButton("Salvar Usuário");
        btnSalvar.setFont(btnSalvar.getFont().deriveFont(Font.BOLD, 12f));
        btnSalvar.setBackground(UITheme.tokens().getPrimaryAccent());
        btnSalvar.setForeground(Color.WHITE);

        btnCancelar.addActionListener(e -> dispose());
        btnSalvar.addActionListener(e -> salvar());

        pnlBotoes.add(btnCancelar);
        pnlBotoes.add(btnSalvar);
        add(pnlBotoes, BorderLayout.SOUTH);
    }

    private void preencherDadosSeEdicao() {
        if (usuarioEdicao != null) {
            txtNome.setText(usuarioEdicao.getNome());
            txtLogin.setText(usuarioEdicao.getLogin());
            cbPerfil.setSelectedItem(usuarioEdicao.getPerfil());
            chkAtivo.setSelected(usuarioEdicao.isAtivo());
        }
    }

    private void salvar() {
        String nome = txtNome.getText().trim();
        String login = txtLogin.getText().trim().toLowerCase();
        String senha = new String(txtSenha.getPassword());
        PerfilUsuario perfil = (PerfilUsuario) cbPerfil.getSelectedItem();
        boolean ativo = chkAtivo.isSelected();

        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do usuário!", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtNome.requestFocus();
            return;
        }

        if (login.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o login do usuário!", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtLogin.requestFocus();
            return;
        }

        if (usuarioEdicao == null && senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe a senha para o novo usuário!", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtSenha.requestFocus();
            return;
        }

        if (usuarioEdicao == null) {
            Usuario novo = new Usuario(0, nome, login, "", perfil, ativo, null);
            boolean ok = usuarioDAO.salvar(novo, senha);
            if (ok) {
                this.salvo = true;
                JOptionPane.showMessageDialog(this, "Usuário cadastrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao cadastrar usuário (login já em uso ou falha no banco).", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            usuarioEdicao.setNome(nome);
            usuarioEdicao.setLogin(login);
            usuarioEdicao.setPerfil(perfil);
            usuarioEdicao.setAtivo(ativo);

            boolean ok = usuarioDAO.atualizar(usuarioEdicao);
            if (!senha.isEmpty()) {
                usuarioDAO.alterarSenha(usuarioEdicao.getId(), senha);
            }

            if (ok) {
                this.salvo = true;
                JOptionPane.showMessageDialog(this, "Usuário atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao atualizar dados do usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean isSalvo() {
        return salvo;
    }
}
