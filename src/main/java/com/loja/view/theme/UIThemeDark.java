package com.loja.view.theme;

import java.awt.Color;

public class UIThemeDark implements ThemeTokens {
    // Paleta Dark Grey / Graphite SaaS Profissional
    // #0F0F10 (Background principal)
    // #141516 (Sidebar & Header)
    // #1A1B1D (Cards e painéis)
    // #222326 (Hover e seleção ativa)
    // #2A2C2F (Bordas e divisores discretos)
    // #F1F1F1 (Texto primário de alto contraste)
    // #A8A8A8 (Texto secundário e legendas)
    // #EDEDED (Botão primário estilo SaaS com contraste limpo)

    @Override
    public Color getBgApp() {
        return new Color(15, 15, 16); // #0F0F10
    }

    @Override
    public Color getBgSidebar() {
        return new Color(20, 21, 22); // #141516
    }

    @Override
    public Color getBgCard() {
        return new Color(26, 27, 29); // #1A1B1D
    }

    @Override
    public Color getBgCardHover() {
        return new Color(34, 35, 38); // #222326
    }

    @Override
    public Color getBorderSubtle() {
        return new Color(42, 44, 47); // #2A2C2F
    }

    @Override
    public Color getTextPrimary() {
        return new Color(241, 241, 241); // #F1F1F1
    }

    @Override
    public Color getTextSecondary() {
        return new Color(168, 168, 168); // #A8A8A8
    }

    @Override
    public Color getPrimaryAccent() {
        return new Color(237, 237, 237); // #EDEDED - Branco/cinza claro para botão primário de contraste limpo
    }

    @Override
    public Color getAccentHover() {
        return new Color(255, 255, 255); // Branco no hover primário
    }

    @Override
    public Color getFocusRing() {
        return new Color(74, 76, 80); // #4A4C50
    }

    @Override
    public Color getSuccess() {
        return new Color(34, 197, 94); // #22C55E - Verde semântico sóbrio
    }

    @Override
    public Color getWarning() {
        return new Color(217, 119, 6); // #D97706 - Âmbar sóbrio
    }

    @Override
    public Color getDanger() {
        return new Color(239, 68, 68); // #EF4444 - Vermelho sóbrio
    }

    @Override
    public Color getInfo() {
        return new Color(156, 163, 175); // #9CA3AF - Neutro informativo
    }

    @Override
    public boolean isDark() {
        return true;
    }
}
