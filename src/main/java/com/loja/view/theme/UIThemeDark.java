package com.loja.view.theme;

import java.awt.Color;

public class UIThemeDark implements ThemeTokens {
    @Override public Color getBgApp() { return new Color(18, 21, 27); } // #12151b
    @Override public Color getBgSidebar() { return new Color(24, 29, 38); } // #181d26
    @Override public Color getBgCard() { return new Color(32, 38, 52); } // #202634
    @Override public Color getBgCardHover() { return new Color(39, 46, 63); } // #272e3f
    @Override public Color getBorderSubtle() { return new Color(45, 53, 70); } // #2d3546
    @Override public Color getTextPrimary() { return new Color(248, 250, 252); } // #f8fafc
    @Override public Color getTextSecondary() { return new Color(148, 163, 184); } // #94a3b8
    @Override public Color getPrimaryAccent() { return new Color(59, 130, 246); } // #3b82f6
    @Override public Color getAccentHover() { return new Color(37, 99, 235); } // #2563eb
    @Override public Color getFocusRing() { return new Color(59, 130, 246, 102); }
    @Override public Color getSuccess() { return new Color(16, 185, 129); } // #10b981
    @Override public Color getWarning() { return new Color(245, 158, 11); } // #f59e0b
    @Override public Color getDanger() { return new Color(239, 68, 68); } // #ef4444
    @Override public Color getInfo() { return new Color(100, 116, 139); } // #64748b
    @Override public boolean isDark() { return true; }
}
