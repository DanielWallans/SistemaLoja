package com.loja.view.theme;

import java.awt.Color;
import java.awt.Font;

public final class UITheme {
    private static ThemeTokens currentTokens = new UIThemeDark();

    // Spacing Scale (8-point grid with 4px sub-step)
    public static final int SPACE_4 = 4;
    public static final int SPACE_8 = 8;
    public static final int SPACE_12 = 12;
    public static final int SPACE_16 = 16;
    public static final int SPACE_20 = 20;
    public static final int SPACE_24 = 24;
    public static final int SPACE_32 = 32;

    // Typographic Scale
    public static final Font FONT_METRIC = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_CAPTION = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private UITheme() {}

    public static ThemeTokens tokens() {
        return currentTokens;
    }

    public static void setDark(boolean isDark) {
        currentTokens = isDark ? new UIThemeDark() : new UIThemeLight();
    }

    public static boolean isDark() {
        return currentTokens.isDark();
    }

    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

    public static Color withAlpha(Color color, float ratio) {
        int alpha = Math.round(ratio * 255);
        return withAlpha(color, alpha);
    }
}
