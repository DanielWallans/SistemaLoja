package com.loja.view.theme;

import java.awt.Color;

public class UIThemeLight implements ThemeTokens {
    @Override public Color getBgApp() { return new Color(248, 250, 252); } // #f8fafc
    @Override public Color getBgSidebar() { return new Color(255, 255, 255); } // #ffffff
    @Override public Color getBgCard() { return new Color(255, 255, 255); } // #ffffff
    @Override public Color getBgCardHover() { return new Color(241, 245, 249); } // #f1f5f9
    @Override public Color getBorderSubtle() { return new Color(226, 232, 240); } // #e2e8f0
    @Override public Color getTextPrimary() { return new Color(15, 23, 42); } // #0f172a
    @Override public Color getTextSecondary() { return new Color(100, 116, 139); } // #64748b
    @Override public Color getPrimaryAccent() { return new Color(37, 99, 235); } // #2563eb
    @Override public Color getAccentHover() { return new Color(29, 78, 216); } // #1d4ed8
    @Override public Color getFocusRing() { return new Color(37, 99, 235, 102); }
    @Override public Color getSuccess() { return new Color(5, 150, 105); } // #059669
    @Override public Color getWarning() { return new Color(217, 119, 6); } // #d97706
    @Override public Color getDanger() { return new Color(220, 38, 38); } // #dc2626
    @Override public Color getInfo() { return new Color(71, 85, 105); } // #475569
    @Override public boolean isDark() { return false; }
}
