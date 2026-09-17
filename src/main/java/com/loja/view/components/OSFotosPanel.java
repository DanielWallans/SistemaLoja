package com.loja.view.components;

import com.loja.model.OSFoto;
import com.loja.model.SessaoUsuario;
import com.loja.model.Usuario;
import com.loja.repository.OSFotoDAO;
import com.loja.service.ImagemService;
import com.loja.view.dialogs.VisualizadorFotoDialog;
import com.loja.view.theme.ThemeTokens;
import com.loja.view.theme.UIComponents;
import com.loja.view.theme.UITheme;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

public class OSFotosPanel extends JPanel {

    private final Window owner;
    private final int osId;
    private final OSFotoDAO fotoDAO;

    private JPanel pnlGaleria;
    private JLabel lblContador;
    private JButton btnColarClipboard;

    public OSFotosPanel(Window owner, int osId, OSFotoDAO fotoDAO) {
        this.owner = owner;
        this.osId = osId;
        this.fotoDAO = fotoDAO;

        setLayout(new BorderLayout(8, 8));
        setOpaque(false);

        initComponents();
        recarregarFotos();
    }

    private void initComponents() {
        ThemeTokens t = UITheme.tokens();

        // 1. Barra de Ações Superior do Painel de Fotos
        JPanel pnlTopo = new JPanel(new BorderLayout(8, 0));
        pnlTopo.setOpaque(false);
        pnlTopo.setBorder(BorderFactory.createEmptyBorder(2, 4, 4, 4));

        JPanel pnlTitEContador = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTitEContador.setOpaque(false);

        JLabel lblTit = new JLabel("Fotos & Evidências Técnicas (Entrada, Avarias, Laudo)");
        lblTit.setFont(UITheme.FONT_SUBTITLE);
        lblTit.setForeground(t.getTextPrimary());

        lblContador = new JLabel("0 fotos");
        lblContador.setFont(UITheme.FONT_CAPTION);
        lblContador.setForeground(t.getTextSecondary());

        pnlTitEContador.add(lblTit);
        pnlTitEContador.add(lblContador);

        JPanel pnlBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlBotoes.setOpaque(false);

        JButton btnAnexarArquivo = UIComponents.criarBotaoPrimario("+ Anexar Foto(s)", this::anexarFotosArquivo);
        btnColarClipboard = UIComponents.criarBotaoSecundario("Colar Foto [Ctrl+V]", this::colarFotoClipboard);
        JButton btnAtualizar = UIComponents.criarBotaoSecundario("Atualizar", this::recarregarFotos);

        pnlBotoes.add(btnAnexarArquivo);
        pnlBotoes.add(btnColarClipboard);
        pnlBotoes.add(btnAtualizar);

        pnlTopo.add(pnlTitEContador, BorderLayout.WEST);
        pnlTopo.add(pnlBotoes, BorderLayout.EAST);
        add(pnlTopo, BorderLayout.NORTH);

        // 2. Container da Galeria com rolagem
        pnlGaleria = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        pnlGaleria.setOpaque(false);

        JScrollPane scroll = new JScrollPane(pnlGaleria);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(t.getBorderSubtle(), 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));
        scroll.setPreferredSize(new Dimension(800, 185));
        scroll.getHorizontalScrollBar().setUnitIncrement(20);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        add(scroll, BorderLayout.CENTER);
    }

    public void recarregarFotos() {
        pnlGaleria.removeAll();

        if (btnColarClipboard != null) {
            btnColarClipboard.setEnabled(true);
        }

        List<OSFoto> fotos = fotoDAO.listarMiniaturasPorOS(osId);
        lblContador.setText("(" + fotos.size() + " " + (fotos.size() == 1 ? "foto anexada" : "fotos anexadas") + ")");

        if (fotos.isEmpty()) {
            JPanel pnlVazio = new JPanel(new BorderLayout());
            pnlVazio.setOpaque(false);
            pnlVazio.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

            JLabel lblVazio = new JLabel("<html><center><font color='#7f8c8d'>Nenhuma foto anexada a esta OS ainda.<br>" +
                    "Tire uma foto de avarias (ex: dobradiça quebrada, carcaça ou tela) e clique em <b>[+ Anexar Foto(s)]</b> ou copie e clique em <b>[Colar Foto]</b>.</font></center></html>");
            lblVazio.setFont(UITheme.FONT_BODY);
            lblVazio.setHorizontalAlignment(SwingConstants.CENTER);
            pnlVazio.add(lblVazio, BorderLayout.CENTER);
            pnlGaleria.add(pnlVazio);
        } else {
            for (OSFoto f : fotos) {
                pnlGaleria.add(criarCardFoto(f));
            }
        }

        pnlGaleria.revalidate();
        pnlGaleria.repaint();
    }

    private JPanel criarCardFoto(OSFoto foto) {
        ThemeTokens t = UITheme.tokens();

        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(t.getBgCard());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(t.getBorderSubtle());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(150, 155));
        card.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Miniatura
        JLabel lblThumb = new JLabel();
        lblThumb.setHorizontalAlignment(SwingConstants.CENTER);
        lblThumb.setPreferredSize(new Dimension(138, 95));

        if (foto.getMiniatura() != null && foto.getMiniatura().length > 0) {
            ImageIcon icon = new ImageIcon(foto.getMiniatura());
            lblThumb.setIcon(icon);
        } else {
            lblThumb.setText("[Sem Miniatura]");
            lblThumb.setFont(UITheme.FONT_CAPTION);
        }
        card.add(lblThumb, BorderLayout.CENTER);

        // Informações abaixo da miniatura
        JPanel pnlInfo = new JPanel(new BorderLayout(0, 2));
        pnlInfo.setOpaque(false);

        String desc = foto.getDescricao();
        if (desc == null || desc.trim().isEmpty()) {
            desc = foto.getNomeArquivo();
        }
        JLabel lblTitulo = new JLabel(desc);
        lblTitulo.setFont(UITheme.FONT_CAPTION);
        lblTitulo.setForeground(t.getTextPrimary());
        lblTitulo.setToolTipText(desc);

        JLabel lblSub = new JLabel(foto.getDataUploadFormatada());
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblSub.setForeground(t.getTextSecondary());

        pnlInfo.add(lblTitulo, BorderLayout.NORTH);
        pnlInfo.add(lblSub, BorderLayout.SOUTH);
        card.add(pnlInfo, BorderLayout.SOUTH);

        // Clique para abrir visualizador
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirVisualizador(foto);
            }
        });

        return card;
    }

    private void abrirVisualizador(OSFoto foto) {
        VisualizadorFotoDialog dialog = new VisualizadorFotoDialog(owner, foto, fotoDAO);
        dialog.setVisible(true);
        if (dialog.isAlterado()) {
            recarregarFotos();
        }
    }

    private void anexarFotosArquivo() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Selecione a(s) Foto(s) da Máquina / Avaria");
        fc.setMultiSelectionEnabled(true);
        fc.setFileFilter(new FileNameExtensionFilter("Imagens (JPG, PNG, JPEG, WEBP, BMP)", "jpg", "jpeg", "png", "webp", "bmp"));

        if (fc.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION) {
            File[] arquivos = fc.getSelectedFiles();
            if (arquivos == null || arquivos.length == 0) {
                File sel = fc.getSelectedFile();
                if (sel != null) {
                    arquivos = new File[] { sel };
                }
            }

            if (arquivos != null && arquivos.length > 0) {
                String legendaGeral = JOptionPane.showInputDialog(owner,
                        "Informe uma legenda / observação para a(s) foto(s) (Opcional):\nEx: Dobradiça quebrada, Carcaça amassada, Placa com oxidação",
                        "Legenda da Foto", JOptionPane.QUESTION_MESSAGE);
                if (legendaGeral == null) legendaGeral = "";

                Usuario u = SessaoUsuario.getInstancia().getUsuarioLogado();
                String operador = (u != null && u.getNome() != null && !u.getNome().isEmpty()) ? u.getNome() : "Técnico";

                int sucesso = 0;
                for (File arq : arquivos) {
                    try {
                        byte[] bytesOriginal = ImagemService.lerBytesArquivo(arq);
                        byte[] bytesOtimizados = ImagemService.otimizarFoto(bytesOriginal);
                        byte[] thumb = ImagemService.gerarMiniatura(bytesOtimizados);

                        OSFoto f = new OSFoto(
                                osId,
                                arq.getName(),
                                legendaGeral.trim(),
                                bytesOtimizados,
                                thumb,
                                bytesOtimizados.length,
                                operador
                        );

                        if (fotoDAO.salvar(f)) {
                            sucesso++;
                        }
                    } catch (Exception e) {
                        System.err.println("[ERRO] Falha ao processar foto " + arq.getName() + ": " + e.getMessage());
                    }
                }

                if (sucesso > 0) {
                    JOptionPane.showMessageDialog(owner, sucesso + " foto(s) anexada(s) com sucesso à OS #" + osId + "!",
                            "Fotos Anexadas", JOptionPane.INFORMATION_MESSAGE);
                    recarregarFotos();
                } else {
                    JOptionPane.showMessageDialog(owner, "Falha ao gravar as fotos no banco de dados.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void colarFotoClipboard() {
        if (!ImagemService.temImagemNaAreaTransferencia()) {
            JOptionPane.showMessageDialog(owner,
                    "Nenhuma imagem encontrada na Área de Transferência!\n\n" +
                    "Para colar uma foto:\n" +
                    "1. Tire um PrintScreen ou clique com botão direito em uma foto (ex: WhatsApp Web) e escolha 'Copiar Imagem'.\n" +
                    "2. Em seguida, clique novamente neste botão.",
                    "Área de Transferência Vazia", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        byte[] bytesImagem = ImagemService.obterImagemDaAreaTransferencia();
        if (bytesImagem == null || bytesImagem.length == 0) {
            JOptionPane.showMessageDialog(owner, "Não foi possível extrair a imagem copiada.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String legenda = JOptionPane.showInputDialog(owner,
                "Imagem identificada na Área de Transferência!\nInforme uma legenda / avaria (ex: Dobradiça esquerda quebrada):",
                "Colar Imagem na OS #" + osId, JOptionPane.QUESTION_MESSAGE);
        if (legenda == null) {
            return; // Cancelado
        }

        Usuario u = SessaoUsuario.getInstancia().getUsuarioLogado();
        String operador = (u != null && u.getNome() != null && !u.getNome().isEmpty()) ? u.getNome() : "Técnico";

        try {
            byte[] bytesOtimizados = ImagemService.otimizarFoto(bytesImagem);
            byte[] thumb = ImagemService.gerarMiniatura(bytesOtimizados);

            String nomeArquivo = "captura_" + System.currentTimeMillis() + ".jpg";
            OSFoto f = new OSFoto(
                    osId,
                    nomeArquivo,
                    legenda.trim(),
                    bytesOtimizados,
                    thumb,
                    bytesOtimizados.length,
                    operador
            );

            if (fotoDAO.salvar(f)) {
                JOptionPane.showMessageDialog(owner, "Imagem colada e anexada com sucesso à OS #" + osId + "!",
                        "Foto Anexada", JOptionPane.INFORMATION_MESSAGE);
                recarregarFotos();
            } else {
                JOptionPane.showMessageDialog(owner, "Falha ao salvar a imagem colada no banco MySQL.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(owner, "Erro ao processar imagem: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
