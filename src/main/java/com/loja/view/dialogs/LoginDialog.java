package com.loja.view.dialogs;

import com.loja.model.SessaoUsuario;
import com.loja.model.Usuario;
import com.loja.repository.UsuarioDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginDialog extends JDialog {
    private final UsuarioDAO usuarioDAO;
    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JLabel lblMensagemErro;
    private boolean autenticado = false;

    public LoginDialog(Frame owner, UsuarioDAO usuarioDAO) {
        super(owner, "Acesso ao Sistema • Autenticação de Usuário", true);
        this.usuarioDAO = usuarioDAO;

        setSize(430, 480);
        setLocationRelativeTo(owner);
        setResizable(false);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
    }

    private void initComponents() {
        // 1. Cabeçalho
        JPanel pnlHeader = new JPanel(new BorderLayout(8, 6));
        pnlHeader.setBackground(new Color(24, 28, 36));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel pnlLogo = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pnlLogo.setOpaque(false);
        JLabel lblIcone = new JLabel("🛠️");
        lblIcone.setFont(lblIcone.getFont().deriveFont(26f));

        JLabel lblTit = new JLabel("ASSISTÊNCIA PRO");
        lblTit.setFont(lblTit.getFont().deriveFont(Font.BOLD, 19f));
        lblTit.setForeground(Color.WHITE);

        pnlLogo.add(lblIcone);
        pnlLogo.add(lblTit);

        JLabel lblSub = new JLabel("Controle de Acesso • Digite suas credenciais", SwingConstants.CENTER);
        lblSub.setFont(lblSub.getFont().deriveFont(12f));
        lblSub.setForeground(new Color(170, 178, 190));

        pnlHeader.add(pnlLogo, BorderLayout.CENTER);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Formulário Central
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createEmptyBorder(20, 30, 15, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Login
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        JLabel lblLoginTit = new JLabel("Usuário / Login:");
        lblLoginTit.setFont(lblLoginTit.getFont().deriveFont(Font.BOLD, 12f));
        pnlForm.add(lblLoginTit, gbc);

        gbc.gridy = 1;
        txtLogin = new JTextField("admin");
        txtLogin.setFont(txtLogin.getFont().deriveFont(14f));
        txtLogin.putClientProperty("JTextField.placeholderText", "Digite seu login...");
        pnlForm.add(txtLogin, gbc);

        // Senha
        gbc.gridy = 2;
        JLabel lblSenhaTit = new JLabel("Senha:");
        lblSenhaTit.setFont(lblSenhaTit.getFont().deriveFont(Font.BOLD, 12f));
        pnlForm.add(lblSenhaTit, gbc);

        gbc.gridy = 3;
        txtSenha = new JPasswordField();
        txtSenha.setFont(txtSenha.getFont().deriveFont(14f));
        txtSenha.putClientProperty("JTextField.placeholderText", "Digite sua senha...");
        pnlForm.add(txtSenha, gbc);

        // Mensagem de Erro
        gbc.gridy = 4;
        lblMensagemErro = new JLabel(" ");
        lblMensagemErro.setForeground(new Color(231, 76, 60));
        lblMensagemErro.setFont(lblMensagemErro.getFont().deriveFont(Font.BOLD, 11f));
        pnlForm.add(lblMensagemErro, gbc);

        // Botão Entrar
        gbc.gridy = 5;
        JButton btnEntrar = new JButton("🔐 Entrar no Sistema");
        btnEntrar.setFont(btnEntrar.getFont().deriveFont(Font.BOLD, 14f));
        btnEntrar.setBackground(new Color(39, 174, 96));
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setPreferredSize(new Dimension(280, 42));
        btnEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEntrar.addActionListener(e -> realizarLogin());
        pnlForm.add(btnEntrar, gbc);

        // Dica de Acesso Padrão
        gbc.gridy = 6;
        JLabel lblDica = new JLabel("<html><center><font color='#7f8c8d'>Usuário padrão inicial: <b>admin</b> | Senha: <b>admin</b></font></center></html>", SwingConstants.CENTER);
        lblDica.setFont(lblDica.getFont().deriveFont(10.5f));
        pnlForm.add(lblDica, gbc);

        // Botão Configurar Servidor / Banco de Dados
        gbc.gridy = 7;
        JButton btnConfigDb = new JButton("⚙️ Configurar Servidor / Banco de Dados");
        btnConfigDb.setFont(btnConfigDb.getFont().deriveFont(11f));
        btnConfigDb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfigDb.setContentAreaFilled(false);
        btnConfigDb.setBorderPainted(false);
        btnConfigDb.setForeground(new Color(140, 150, 165));
        btnConfigDb.addActionListener(e -> {
            ConfigBancoDialog dlg = new ConfigBancoDialog(this);
            dlg.setVisible(true);
        });
        pnlForm.add(btnConfigDb, gbc);

        add(pnlForm, BorderLayout.CENTER);

        // Listeners de Enter
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    realizarLogin();
                }
            }
        };

        txtLogin.addKeyListener(enterListener);
        txtSenha.addKeyListener(enterListener);
    }

    private void realizarLogin() {
        String login = txtLogin.getText().trim();
        String senha = new String(txtSenha.getPassword());

        if (login.isEmpty()) {
            lblMensagemErro.setText("Informe o nome de usuário!");
            txtLogin.requestFocus();
            return;
        }

        Usuario usuario = usuarioDAO.autenticar(login, senha);
        if (usuario != null) {
            SessaoUsuario.getInstancia().setUsuarioLogado(usuario);
            this.autenticado = true;
            dispose();
        } else {
            lblMensagemErro.setText("❌ Usuário ou senha incorretos / inativo!");
            txtSenha.setText("");
            txtSenha.requestFocus();
        }
    }

    public boolean isAutenticado() {
        return autenticado;
    }
}
