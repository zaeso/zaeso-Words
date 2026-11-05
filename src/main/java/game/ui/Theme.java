package game.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

/** Единая палитра и оформление интерфейса. */
public final class Theme {
    public static final Color RED = new Color(0xC62828);
    public static final Color RED_DARK = new Color(0x8E1B1B);
    public static final Color BLUE = new Color(0x1565C0);
    public static final Color BLUE_DARK = new Color(0x0D47A1);
    public static final Color GREEN = new Color(0x2E7D32);
    public static final Color GREEN_LIGHT = new Color(0xC8E6C9);
    public static final Color BACKGROUND = new Color(0xF4F7FB);
    public static final Color PANEL = Color.WHITE;
    public static final Color TEXT = new Color(0x1E2430);
    public static final Color MUTED = new Color(0x5F6B7A);

    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 34);
    public static final Font HEADING = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BUTTON = new Font("Segoe UI", Font.BOLD, 16);

    private Theme() {
    }

    public static JButton button(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(BUTTON);
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(12, 22, 12, 22));
        return button;
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD8E1EC), 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        );
    }

    public static void styleCard(JComponent component) {
        component.setBackground(PANEL);
        component.setBorder(cardBorder());
    }
}
