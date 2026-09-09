package com.loja.view.theme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public final class UIComponents {

    private UIComponents() {}

    /**
     * Cria um Card de Métrica moderno, minimalista e limpo (estilo SaaS).
     */
    public static JPanel criarCardMetrica(String titulo, JLabel lblValor, String icone, Color acento) {
        ThemeTokens t = UITheme.tokens();

        JPanel card = new JPanel(new BorderLayout(0, UITheme.SPACE_8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(t.getBgCard());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(t.getBorderSubtle());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACE_16, UITheme.SPACE_16, UITheme.SPACE_16, UITheme.SPACE_16));

        // Topo: Rótulo + Ícone Discreto
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(UITheme.FONT_CAPTION);
        lblTit.setForeground(t.getTextSecondary());

        JLabel lblIco = new JLabel(icone);
        lblIco.setFont(lblIco.getFont().deriveFont(16f));
        if (acento != null) {
            lblIco.setForeground(acento);
        }

        pnlTop.add(lblTit, BorderLayout.WEST);
        pnlTop.add(lblIco, BorderLayout.EAST);

        // Centro: Valor da Métrica
        lblValor.setFont(UITheme.FONT_METRIC);
        lblValor.setForeground(t.getTextPrimary());

        card.add(pnlTop, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);

        return card;
    }

    public static JPanel criarCardMetrica(String titulo, JLabel lblValor, Color acento) {
        return criarCardMetrica(titulo, lblValor, "", acento);
    }

    /**
     * Botão com estilo primário moderno e cantos arredondados.
     */
    public static JButton criarBotaoPrimario(String texto) {
        return criarBotaoPrimario(texto, null);
    }

    public static JButton criarBotaoPrimario(String texto, Runnable acao) {
        ThemeTokens t = UITheme.tokens();
        JButton btn = new JButton(texto);
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setBackground(t.getPrimaryAccent());
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        if (acao != null) {
            btn.addActionListener(e -> acao.run());
        }
        return btn;
    }

    /**
     * Botão secundário com estilo outline sutil.
     */
    public static JButton criarBotaoSecundario(String texto) {
        return criarBotaoSecundario(texto, null);
    }

    public static JButton criarBotaoSecundario(String texto, Runnable acao) {
        ThemeTokens t = UITheme.tokens();
        JButton btn = new JButton(texto);
        btn.setFont(UITheme.FONT_BODY);
        btn.setBackground(t.getBgCard());
        btn.setForeground(t.getTextPrimary());
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(t.getBorderSubtle(), 1),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)
        ));
        if (acao != null) {
            btn.addActionListener(e -> acao.run());
        }
        return btn;
    }

    /**
     * Retorna a cor semântica apropriada para cada status do sistema.
     */
    public static Color obterCorStatus(String status) {
        ThemeTokens t = UITheme.tokens();
        if (status == null) return t.getInfo();
        String s = status.toLowerCase();
        if (s.contains("aguardando") || s.contains("aprovação") || s.contains("orçamento")) {
            return t.getWarning();
        } else if (s.contains("manutenção") || s.contains("andamento") || s.contains("peça")) {
            return t.getPrimaryAccent();
        } else if (s.contains("pronto") || s.contains("entregue") || s.contains("finalizado") || s.contains("ativo") || s.contains("aberto")) {
            return t.getSuccess();
        } else if (s.contains("cancelad") || s.contains("recusad") || s.contains("inativo") || s.contains("fechado")) {
            return t.getDanger();
        }
        return t.getInfo();
    }

    /**
     * Formata uma JTable para o padrão visual moderno (SaaS/Linear), com badges para a coluna de status.
     */
    public static void formatarTabelaModerna(JTable tabela, int colunaStatus) {
        ThemeTokens t = UITheme.tokens();
        tabela.setRowHeight(34);
        tabela.setShowHorizontalLines(true);
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(t.getBorderSubtle());
        tabela.setFont(UITheme.FONT_BODY);
        tabela.setSelectionBackground(UITheme.withAlpha(t.getPrimaryAccent(), 0.25f));
        tabela.setSelectionForeground(t.getTextPrimary());

        tabela.getTableHeader().setFont(UITheme.FONT_CAPTION);
        tabela.getTableHeader().setBackground(t.getBgSidebar());
        tabela.getTableHeader().setForeground(t.getTextSecondary());
        tabela.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, t.getBorderSubtle()));
        tabela.getTableHeader().setPreferredSize(new Dimension(0, 36));

        // Renderizador padrão com zebra suave
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? t.getBgCard() : t.getBgCardHover());
                    c.setForeground(t.getTextPrimary());
                }
                if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(0, UITheme.SPACE_8, 0, UITheme.SPACE_8));
                }
                return c;
            }
        });

        // Se houver coluna de status, aplicar renderizador de Pill Badge com fundo translúcido
        if (colunaStatus >= 0 && colunaStatus < tabela.getColumnCount()) {
            tabela.getColumnModel().getColumn(colunaStatus).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    String texto = value != null ? value.toString() : "";
                    Color corStatus = obterCorStatus(texto);

                    JPanel pnlPill = new JPanel(new GridBagLayout()) {
                        @Override
                        protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? t.getBgCard() : t.getBgCardHover()));
                            g2.fillRect(0, 0, getWidth(), getHeight());

                            // Fundo translúcido do pill badge (~15% opacidade)
                            g2.setColor(UITheme.withAlpha(corStatus, 0.15f));
                            int pillW = Math.min(getWidth() - 16, 170);
                            int pillH = getHeight() - 10;
                            int pillX = (getWidth() - pillW) / 2;
                            int pillY = (getHeight() - pillH) / 2;
                            g2.fillRoundRect(pillX, pillY, pillW, pillH, pillH, pillH);

                            // Borda ultra-sutil do pill
                            g2.setColor(UITheme.withAlpha(corStatus, 0.35f));
                            g2.drawRoundRect(pillX, pillY, pillW - 1, pillH - 1, pillH, pillH);
                            g2.dispose();
                        }
                    };

                    JLabel lbl = new JLabel(texto);
                    lbl.setFont(UITheme.FONT_CAPTION);
                    lbl.setForeground(corStatus);
                    pnlPill.add(lbl);

                    return pnlPill;
                }
            });
        }
    }

    /**
     * Cria um badge interativo de tecla de atalho [F2, F3, etc.] com estilo keyboard key (kbd).
     */
    public static JButton criarBadgeAtalho(String tecla, String rotulo, Runnable acao) {
        ThemeTokens t = UITheme.tokens();
        JButton btn = new JButton(String.format("<html><b><font color='%s'>[%s]</font></b> <font color='%s'>%s</font></html>",
                String.format("#%02x%02x%02x", t.getPrimaryAccent().getRed(), t.getPrimaryAccent().getGreen(), t.getPrimaryAccent().getBlue()),
                tecla,
                String.format("#%02x%02x%02x", t.getTextSecondary().getRed(), t.getTextSecondary().getGreen(), t.getTextSecondary().getBlue()),
                rotulo));
        btn.setFont(UITheme.FONT_CAPTION);
        btn.setBackground(t.getBgCard());
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusable(false);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(t.getBorderSubtle(), 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        btn.setToolTipText("Pressione " + tecla + " ou clique aqui");
        if (acao != null) {
            btn.addActionListener(e -> acao.run());
        }
        return btn;
    }
}
