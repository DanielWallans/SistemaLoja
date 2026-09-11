package com.loja.view.theme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public final class UIComponents {

    private UIComponents() {
    }

    /**
     * Cria um Card de Métrica moderno, minimalista e limpo (estilo SaaS).
     */
    public static JPanel criarCardMetrica(String titulo, JLabel lblValor, String icone, Color acento) {
        ThemeTokens t = UITheme.tokens();

        JPanel card = new JPanel(new BorderLayout(0, UITheme.SPACE_8)) {
            @Override
            protected void paintComponent(Graphics g) {
                ThemeTokens currentT = UITheme.tokens();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(currentT.getBgCard());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(currentT.getBorderSubtle());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACE_16, UITheme.SPACE_16, UITheme.SPACE_16,
                UITheme.SPACE_16));

        // Topo: Rótulo + Ícone Discreto
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(UITheme.FONT_CAPTION);
        lblTit.setForeground(t.getTextSecondary());

        JLabel lblIco = new JLabel(icone);
        lblIco.setFont(UITheme.FONT_CAPTION);
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
     * Botão com estilo primário moderno e contraste limpo (estilo SaaS).
     */
    public static JButton criarBotaoPrimario(String texto) {
        return criarBotaoPrimario(texto, null);
    }

    public static JButton criarBotaoPrimario(String texto, Runnable acao) {
        ThemeTokens t = UITheme.tokens();
        JButton btn = new JButton(texto);
        btn.setFont(UITheme.FONT_BODY_BOLD);
        if (t.isDark()) {
            btn.setBackground(new Color(237, 237, 237)); // #EDEDED
            btn.setForeground(new Color(15, 15, 16));     // #0F0F10 para contraste nítido
        } else {
            btn.setBackground(new Color(15, 23, 42));     // Grafite escuro elegante
            btn.setForeground(new Color(255, 255, 255));  // Branco nítido
        }
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.arc", 6);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        if (acao != null) {
            btn.addActionListener(e -> acao.run());
        }
        return btn;
    }

    /**
     * Botão secundário com estilo outline sutil e cantos de 6px.
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
        btn.putClientProperty("JButton.arc", 6);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(t.getBorderSubtle(), 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        if (acao != null) {
            btn.addActionListener(e -> acao.run());
        }
        return btn;
    }

    /**
     * Retorna a cor semântica sóbria apropriada para cada status do sistema.
     */
    public static Color obterCorStatus(String status) {
        ThemeTokens t = UITheme.tokens();
        if (status == null)
            return t.getInfo();
        String s = status.toLowerCase();

        if (s.contains("orçamento") || s.contains("aguardando aprovação") || s.contains("aguardando")) {
            return new Color(217, 119, 6); // Âmbar sóbrio
        } else if (s.contains("aprovad") || s.contains("manutenção") || s.contains("andamento")) {
            return t.isDark() ? new Color(168, 168, 168) : new Color(71, 85, 105);
        } else if (s.contains("peça")) {
            return t.isDark() ? new Color(156, 163, 175) : new Color(100, 116, 139);
        } else if (s.contains("pronto") || s.contains("retirada")) {
            return t.isDark() ? new Color(34, 197, 94) : new Color(22, 101, 52);
        } else if (s.contains("entregue") || s.contains("finalizado") || s.contains("pago")) {
            return t.isDark() ? new Color(34, 197, 94) : new Color(22, 101, 52);
        } else if (s.contains("cancelad") || s.contains("recusad") || s.contains("inativ")) {
            return t.isDark() ? new Color(239, 68, 68) : new Color(185, 28, 28);
        } else if (s.contains("aberto") || s.contains("aberta")) {
            return new Color(217, 119, 6); // Âmbar sóbrio
        }
        return t.getInfo();
    }

    /**
     * Formata uma JTable para o padrão visual moderno (SaaS/Linear), com badges
     * discretos de 4px para a coluna de status.
     */
    public static void formatarTabelaModerna(JTable tabela, int colunaStatus) {
        ThemeTokens t = UITheme.tokens();
        tabela.setBackground(t.getBgCard());
        tabela.setForeground(t.getTextPrimary());
        tabela.setFillsViewportHeight(true);
        tabela.setRowHeight(34);
        tabela.setShowHorizontalLines(true);
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(t.getBorderSubtle());
        tabela.setFont(UITheme.FONT_BODY);
        tabela.setSelectionBackground(t.isDark() ? new Color(41, 42, 45) : new Color(226, 232, 240));
        tabela.setSelectionForeground(t.getTextPrimary());

        tabela.getTableHeader().setFont(UITheme.FONT_CAPTION);
        tabela.getTableHeader().setBackground(t.getBgSidebar());
        tabela.getTableHeader().setForeground(t.getTextSecondary());
        tabela.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, t.getBorderSubtle()));
        tabela.getTableHeader().setPreferredSize(new Dimension(0, 36));

        // Ajusta o fundo do viewport pai caso já esteja anexado
        if (tabela.getParent() instanceof JViewport) {
            JViewport vp = (JViewport) tabela.getParent();
            vp.setBackground(t.getBgCard());
            vp.setOpaque(true);
            if (vp.getParent() instanceof JScrollPane) {
                vp.getParent().setBackground(t.getBgCard());
            }
        }

        // Garante que quando for adicionado ou o tema mudar, o viewport acompanhe o tema ativo
        tabela.addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                ThemeTokens curT = UITheme.tokens();
                tabela.setBackground(curT.getBgCard());
                if (tabela.getParent() instanceof JViewport) {
                    JViewport vp = (JViewport) tabela.getParent();
                    vp.setBackground(curT.getBgCard());
                    vp.setOpaque(true);
                    if (vp.getParent() instanceof JScrollPane) {
                        vp.getParent().setBackground(curT.getBgCard());
                    }
                }
            }
            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {}
            @Override
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {}
        });

        // Renderizador padrão com zebra sutil
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                ThemeTokens currentT = UITheme.tokens();
                if (!isSelected) {
                    if (currentT.isDark()) {
                        c.setBackground(row % 2 == 0 ? new Color(26, 27, 29) : new Color(32, 33, 36));
                    } else {
                        c.setBackground(row % 2 == 0 ? new Color(255, 255, 255) : new Color(248, 250, 252));
                    }
                    c.setForeground(currentT.getTextPrimary());
                } else {
                    c.setBackground(currentT.isDark() ? new Color(41, 42, 45) : new Color(226, 232, 240));
                    c.setForeground(currentT.getTextPrimary());
                }
                if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(0, UITheme.SPACE_8, 0, UITheme.SPACE_8));
                }
                return c;
            }
        });

        // Se houver coluna de status, aplicar renderizador de Badge sóbrio (4px radius)
        if (colunaStatus >= 0 && colunaStatus < tabela.getColumnCount()) {
            tabela.getColumnModel().getColumn(colunaStatus).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    String texto = value != null ? value.toString() : "";
                    Color corStatus = obterCorStatus(texto);

                    JPanel pnlBadge = new JPanel(new GridBagLayout()) {
                        @Override
                        protected void paintComponent(Graphics g) {
                            ThemeTokens currentT = UITheme.tokens();
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            Color bgRow;
                            if (isSelected) {
                                bgRow = currentT.isDark() ? new Color(41, 42, 45) : new Color(226, 232, 240);
                            } else if (currentT.isDark()) {
                                bgRow = (row % 2 == 0 ? new Color(26, 27, 29) : new Color(32, 33, 36));
                            } else {
                                bgRow = (row % 2 == 0 ? new Color(255, 255, 255) : new Color(248, 250, 252));
                            }
                            g2.setColor(bgRow);
                            g2.fillRect(0, 0, getWidth(), getHeight());

                            // Fundo sóbrio do badge (4px radius)
                            g2.setColor(UITheme.withAlpha(corStatus, currentT.isDark() ? 0.15f : 0.12f));
                            int badgeW = Math.min(getWidth() - 16, 170);
                            int badgeH = getHeight() - 10;
                            int badgeX = (getWidth() - badgeW) / 2;
                            int badgeY = (getHeight() - badgeH) / 2;
                            g2.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 4, 4);

                            // Borda discreta do badge
                            g2.setColor(UITheme.withAlpha(corStatus, currentT.isDark() ? 0.35f : 0.40f));
                            g2.drawRoundRect(badgeX, badgeY, badgeW - 1, badgeH - 1, 4, 4);
                            g2.dispose();
                        }
                    };

                    JLabel lbl = new JLabel(texto);
                    lbl.setFont(UITheme.FONT_CAPTION);
                    lbl.setForeground(corStatus);
                    pnlBadge.add(lbl);

                    return pnlBadge;
                }
            });
        }
    }

    /**
     * Cria um badge interativo de tecla de atalho [F2, F3, etc.] com estilo
     * keyboard key (kbd).
     */
    public static JButton criarBadgeAtalho(String tecla, String rotulo, Runnable acao) {
        ThemeTokens t = UITheme.tokens();
        JButton btn = new JButton(
                String.format("<html><b><font color='%s'>[%s]</font></b> <font color='%s'>%s</font></html>",
                        String.format("#%02x%02x%02x", t.getPrimaryAccent().getRed(), t.getPrimaryAccent().getGreen(),
                                t.getPrimaryAccent().getBlue()),
                        tecla,
                        String.format("#%02x%02x%02x", t.getTextSecondary().getRed(), t.getTextSecondary().getGreen(),
                                t.getTextSecondary().getBlue()),
                        rotulo));
        btn.setFont(UITheme.FONT_CAPTION);
        btn.setBackground(t.getBgCard());
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusable(false);
        btn.putClientProperty("JButton.arc", 6);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(t.getBorderSubtle(), 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        btn.setToolTipText("Pressione " + tecla + " ou clique aqui");
        if (acao != null) {
            btn.addActionListener(e -> acao.run());
        }
        return btn;
    }
}
