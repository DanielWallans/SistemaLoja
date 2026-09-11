package com.loja.view.theme;

import java.awt.Color;

public class UIThemeLight implements ThemeTokens {
    @Override
    public Color getBgApp() {
        return new Color(248, 250, 252);
    } // #f8fafc

    @Override
    public Color getBgSidebar() {
        return new Color(255, 255, 255);
    } // #ffffff

    @Override
    public Color getBgCard() {
        return new Color(255, 255, 255);
    } // #ffffff

    @Override
    public Color getBgCardHover() {
        return new Color(241, 245, 249);
    } // #f1f5f9

    @Override
    public Color getBorderSubtle() {
        return new Color(226, 232, 240);
    } // #e2e8f0

    @Override
    public Color getTextPrimary() {
        return new Color(15, 23, 42);
    } // #0f172a - Preto sólido e nítido

    @Override
    public Color getTextSecondary() {
        return new Color(51, 65, 85);
    } // #334155 - Chumbo escuro legível

    @Override
    public Color getPrimaryAccent() {
        return new Color(51, 65, 85);
    } // #334155 - Cinza executivo sólido (substitui o azul)

    @Override
    public Color getAccentHover() {
        return new Color(15, 23, 42);
    } // #0f172a - Grafite profundo

    @Override
    public Color getFocusRing() {
        return new Color(51, 65, 85, 90);
    }

    @Override
    public Color getSuccess() {
        return new Color(22, 101, 52);
    } // #166534 - Verde sólido

    @Override
    public Color getWarning() {
        return new Color(180, 83, 9);
    } // #b45309 - Âmbar sólido

    @Override
    public Color getDanger() {
        return new Color(185, 28, 28);
    } // #b91c1c - Vermelho sólido

    @Override
    public Color getInfo() {
        return new Color(71, 85, 105);
    } // #475569 - Cinza neutro sólido

    @Override
    public boolean isDark() {
        return false;
    }
}
