package org.devup.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public final class UiStyle {

    public static final Color BG_MAIN = new Color(8, 22, 43);
    public static final Color BG_PANEL = new Color(17, 42, 80);
    public static final Color BG_TOP = new Color(11, 30, 58);
    public static final Color BG_BANNER = new Color(80, 22, 26);
    public static final Color BTN_BLUE = new Color(30, 110, 200);
    public static final Color BTN_DARK = new Color(22, 82, 155);
    public static final Color BTN_RED = new Color(170, 45, 55);
    public static final Color BTN_RED_DARK = new Color(125, 30, 40);
    public static final Color TEXT_MAIN = new Color(220, 238, 255);
    public static final Color TEXT_DIM = new Color(150, 190, 230);
    public static final Color STATUS_OK = new Color(70, 230, 150);
    public static final Color STATUS_BG_OK = new Color(12, 70, 48);
    public static final Color STATUS_OFF = new Color(140, 175, 210);
    public static final Color STATUS_BG_OFF = new Color(28, 48, 78);
    public static final Color STATUS_WARN = new Color(255, 200, 90);
    public static final Color STATUS_BG_WARN = new Color(80, 58, 18);
    public static final Color DOT_ON = new Color(70, 230, 150);
    public static final Color DOT_OFF = new Color(255, 90, 100);

    private UiStyle() {}

    public static JButton styledButton(String text, Color bg, Color hover) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        b.setPreferredSize(new Dimension(170, 44));
        b.setBorder(BorderFactory.createLineBorder(new Color(90, 170, 255), 1, true));
        b.addChangeListener(e -> b.setBackground(b.getModel().isRollover() ? hover : bg));
        return b;
    }

    public static JButton smallButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(BG_PANEL);
        b.setForeground(TEXT_DIM);
        b.setFocusPainted(false);
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 120, 190), 1, true),
                BorderFactory.createEmptyBorder(7, 18, 7, 18)));
        return b;
    }

    public static TitledBorder styledBorder(String title) {
        TitledBorder b = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(50, 120, 200), 1, true), title);
        b.setTitleColor(TEXT_MAIN);
        b.setTitleFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        return b;
    }

    public static void styleStatusPill(JLabel label, Color fg, Color bg) {
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        label.setForeground(fg);
        label.setOpaque(true);
        label.setBackground(bg);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg.darker(), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }
}
