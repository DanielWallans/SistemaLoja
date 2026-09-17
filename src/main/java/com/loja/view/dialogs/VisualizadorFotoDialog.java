package com.loja.view.dialogs;

import com.loja.model.OSFoto;
import com.loja.repository.OSFotoDAO;
import com.loja.service.ImagemService;
import com.loja.view.theme.UIComponents;
import com.loja.view.theme.UITheme;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class VisualizadorFotoDialog extends JDialog {

    private final OSFoto fotoInfo;
    private final OSFotoDAO fotoDAO;
    private byte[] dadosCompletos;
    private BufferedImage imagemOriginal;
    private double zoomFactor = 1.0;
    private boolean alterado = false;
    private boolean excluido = false;

    private JLabel lblImagem;
    private JScrollPane scrollPane;
    private JLabel lblStatusInfo;
    private JLabel lblLegenda;

    public VisualizadorFotoDialog(Window owner, OSFoto fotoInfo, OSFotoDAO fotoDAO) {
        super(owner, "Visualizador de Evidência • " + fotoInfo.getNomeArquivo(), ModalityType.APPLICATION_MODAL);
        this.fotoInfo = fotoInfo;
        this.fotoDAO = fotoDAO;

        setSize(920, 720);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        carregarDadosImagem();
        initComponents();
    }

    private void carregarDadosImagem() {
        if (fotoInfo.getDados() != null && fotoInfo.getDados().length > 0) {
            this.dadosCompletos = fotoInfo.getDados();
        } else {
            this.dadosCompletos = fotoDAO.obterDadosCompletos(fotoInfo.getId());
        }

        if (dadosCompletos != null) {
            try {
                this.imagemOriginal = ImageIO.read(new ByteArrayInputStream(dadosCompletos));
            } catch (IOException e) {
                System.err.println("[ERRO] Falha ao carregar imagem para visualização: " + e.getMessage());
            }
        }
    }

    private void initComponents() {
        // 1. Cabeçalho com Legenda e Detalhes
        JPanel pnlHeader = new JPanel(new BorderLayout(12, 6));
        pnlHeader.setBackground(UITheme.tokens().getBgCard());
        pnlHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.tokens().getBorderSubtle()),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        lblLegenda = new JLabel(fotoInfo.getDescricao().isEmpty() ? "Sem legenda informada" : fotoInfo.getDescricao());
        lblLegenda.setFont(UITheme.FONT_TITLE);
        lblLegenda.setForeground(UITheme.tokens().getTextPrimary());

        String infoTexto = String.format("Arquivo: %s  •  Operador: %s  •  Data: %s  •  Tamanho: %s",
                fotoInfo.getNomeArquivo(),
                fotoInfo.getOperador(),
                fotoInfo.getDataUploadFormatada(),
                fotoInfo.getTamanhoFormatado());
        lblStatusInfo = new JLabel(infoTexto);
        lblStatusInfo.setFont(UITheme.FONT_CAPTION);
        lblStatusInfo.setForeground(UITheme.tokens().getTextSecondary());

        pnlHeader.add(lblLegenda, BorderLayout.NORTH);
        pnlHeader.add(lblStatusInfo, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Área Central com Imagem e Zoom
        lblImagem = new JLabel();
        lblImagem.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagem.setVerticalAlignment(SwingConstants.CENTER);

        ajustarZoomInicial();

        scrollPane = new JScrollPane(lblImagem);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Barra de Ferramentas / Ações
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(UITheme.tokens().getBgCard());
        pnlFooter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.tokens().getBorderSubtle()),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        // Controles de Zoom à Esquerda
        JPanel pnlZoom = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlZoom.setOpaque(false);
        JButton btnZoomIn = UIComponents.criarBotaoSecundario("Zoom +", () -> alterarZoom(1.25));
        JButton btnZoomOut = UIComponents.criarBotaoSecundario("Zoom -", () -> alterarZoom(0.8));
        JButton btnAjustar = UIComponents.criarBotaoSecundario("Ajustar à Janela", this::ajustarZoomInicial);
        JButton btnTamanhoReal = UIComponents.criarBotaoSecundario("100% Original", () -> {
            zoomFactor = 1.0;
            renderizarImagem();
        });

        pnlZoom.add(btnZoomIn);
        pnlZoom.add(btnZoomOut);
        pnlZoom.add(btnAjustar);
        pnlZoom.add(btnTamanhoReal);

        // Ações à Direita
        JPanel pnlAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlAcoes.setOpaque(false);

        JButton btnCopiar = UIComponents.criarBotaoSecundario("Copiar Imagem", this::copiarImagemParaClipboard);
        JButton btnSalvar = UIComponents.criarBotaoSecundario("Salvar Como...", this::salvarComo);
        JButton btnEditarLegenda = UIComponents.criarBotaoSecundario("Editar Legenda", this::editarLegenda);
        JButton btnExcluir = new JButton("Excluir Foto");
        btnExcluir.setFont(UITheme.FONT_BODY);
        btnExcluir.setBackground(new Color(231, 76, 60));
        btnExcluir.setForeground(Color.WHITE);
        btnExcluir.setFocusPainted(false);
        btnExcluir.putClientProperty("JButton.arc", 6);
        btnExcluir.addActionListener(e -> excluirFoto());

        JButton btnFechar = UIComponents.criarBotaoSecundario("Fechar", this::dispose);

        pnlAcoes.add(btnCopiar);
        pnlAcoes.add(btnSalvar);
        pnlAcoes.add(btnEditarLegenda);
        pnlAcoes.add(btnExcluir);
        pnlAcoes.add(btnFechar);

        pnlFooter.add(pnlZoom, BorderLayout.WEST);
        pnlFooter.add(pnlAcoes, BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private void ajustarZoomInicial() {
        if (imagemOriginal == null) {
            lblImagem.setText("Não foi possível carregar a imagem original.");
            return;
        }

        // Calcula tamanho disponível na viewport inicial aproximada
        int viewW = Math.max(200, getWidth() - 50);
        int viewH = Math.max(200, getHeight() - 170);

        double ratioW = (double) viewW / imagemOriginal.getWidth();
        double ratioH = (double) viewH / imagemOriginal.getHeight();
        zoomFactor = Math.min(ratioW, ratioH);
        if (zoomFactor > 1.0) zoomFactor = 1.0;

        renderizarImagem();
    }

    private void alterarZoom(double multiplicador) {
        zoomFactor *= multiplicador;
        if (zoomFactor < 0.1) zoomFactor = 0.1;
        if (zoomFactor > 5.0) zoomFactor = 5.0;
        renderizarImagem();
    }

    private void renderizarImagem() {
        if (imagemOriginal == null) return;

        int w = Math.max(1, (int) (imagemOriginal.getWidth() * zoomFactor));
        int h = Math.max(1, (int) (imagemOriginal.getHeight() * zoomFactor));

        Image scaled = imagemOriginal.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        lblImagem.setIcon(new ImageIcon(scaled));
        lblImagem.revalidate();
        lblImagem.repaint();
    }

    private void copiarImagemParaClipboard() {
        if (imagemOriginal == null) return;
        try {
            Transferable t = new Transferable() {
                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[] { DataFlavor.imageFlavor };
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return DataFlavor.imageFlavor.equals(flavor);
                }

                @Override
                public Object getTransferData(DataFlavor flavor) {
                    return imagemOriginal;
                }
            };
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(t, null);
            JOptionPane.showMessageDialog(this, "Foto copiada para a Área de Transferência com sucesso!\nVocê pode colar em qualquer conversa ou documento (Ctrl+V).",
                    "Imagem Copiada", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao copiar imagem: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salvarComo() {
        if (dadosCompletos == null) return;

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Salvar Imagem em Disco");
        fc.setSelectedFile(new File(fotoInfo.getNomeArquivo()));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File arq = fc.getSelectedFile();
                ImagemService.salvarEmArquivo(dadosCompletos, arq);
                JOptionPane.showMessageDialog(this, "Foto salva com sucesso em:\n" + arq.getAbsolutePath(),
                        "Salvo com Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar arquivo: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editarLegenda() {
        String nova = JOptionPane.showInputDialog(this, "Informe a nova legenda / descrição da evidência:",
                fotoInfo.getDescricao());
        if (nova != null) {
            if (fotoDAO.atualizarDescricao(fotoInfo.getId(), nova)) {
                fotoInfo.setDescricao(nova);
                lblLegenda.setText(nova.isEmpty() ? "Sem legenda informada" : nova);
                this.alterado = true;
            } else {
                JOptionPane.showMessageDialog(this, "Falha ao atualizar legenda no banco de dados.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void excluirFoto() {
        int opt = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir permanentemente esta foto da Ordem de Serviço?\nEsta ação não poderá ser desfeita.",
                "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (opt == JOptionPane.YES_OPTION) {
            if (fotoDAO.excluir(fotoInfo.getId())) {
                this.excluido = true;
                this.alterado = true;
                JOptionPane.showMessageDialog(this, "Foto excluída com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Falha ao excluir foto do banco de dados.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean isAlterado() { return alterado; }
    public boolean isExcluido() { return excluido; }
}
