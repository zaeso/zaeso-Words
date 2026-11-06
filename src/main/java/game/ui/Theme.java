package game.ui;

import game.config.ThemeChoice;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

/**
 * Единая палитра и оформление интерфейса. Цвета меняются через
 * {@link #apply(ThemeChoice)}; выбранная тема хранится в настройках.
 */
public final class Theme {
    public static Color RED = new Color(0xC62828);
    public static Color RED_DARK = new Color(0x8E1B1B);
    public static Color BLUE = new Color(0x1565C0);
    public static Color BLUE_DARK = new Color(0x0D47A1);
    public static Color GREEN = new Color(0x2E7D32);
    public static Color GREEN_LIGHT = new Color(0xC8E6C9);
    public static Color BACKGROUND = new Color(0xF4F7FB);
    public static Color PANEL = Color.WHITE;
    public static Color TEXT = new Color(0x1E2430);
    public static Color MUTED = new Color(0x5F6B7A);
    public static Color SUBTITLE = new Color(0xFFE0E0);
    public static Color BORDER = new Color(0xD8E1EC);
    public static Color CELL = Color.WHITE;
    public static Color CELL_TEXT = new Color(0x1E2430);

    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 34);
    public static final Font HEADING = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BUTTON = new Font("Segoe UI", Font.BOLD, 16);

    static {
        apply(ThemeChoice.CLASSIC);
    }

    private Theme() {
    }

    /** Применяет цветовую схему к компонентам, созданным после вызова. */
    public static void apply(ThemeChoice choice) {
        switch (choice == null ? ThemeChoice.CLASSIC : choice) {
            case OCEAN -> {
                RED = new Color(0x0E7490);
                RED_DARK = new Color(0x155E75);
                BLUE = new Color(0x1D4ED8);
                BLUE_DARK = new Color(0x0C2D6B);
                GREEN = new Color(0x0F766E);
                GREEN_LIGHT = new Color(0xA5F3FC);
                BACKGROUND = new Color(0xEEF7FA);
                PANEL = Color.WHITE;
                TEXT = new Color(0x0F172A);
                MUTED = new Color(0x526477);
                SUBTITLE = new Color(0xD5F3FA);
                BORDER = new Color(0xC9E4EC);
                CELL = Color.WHITE;
            }
            case FOREST -> {
                RED = new Color(0x2F6B3A);
                RED_DARK = new Color(0x1B4523);
                BLUE = new Color(0x8B5E34);
                BLUE_DARK = new Color(0x5C3D1F);
                GREEN = new Color(0x2E7D32);
                GREEN_LIGHT = new Color(0xDCEDC8);
                BACKGROUND = new Color(0xF5F8EF);
                PANEL = Color.WHITE;
                TEXT = new Color(0x1F2A1C);
                MUTED = new Color(0x5B6B57);
                SUBTITLE = new Color(0xE2F0D9);
                BORDER = new Color(0xD6E2CB);
                CELL = Color.WHITE;
            }
            case DARK -> {
                RED = new Color(0x7F1D1D);
                RED_DARK = new Color(0x4C0F0F);
                BLUE = new Color(0x2563EB);
                BLUE_DARK = new Color(0x0B1220);
                GREEN = new Color(0x15803D);
                GREEN_LIGHT = new Color(0x86EFAC);
                BACKGROUND = new Color(0x0F141C);
                PANEL = new Color(0x1A2230);
                TEXT = new Color(0xE8EDF5);
                MUTED = new Color(0xA6B1C2);
                SUBTITLE = new Color(0xE7C9C9);
                BORDER = new Color(0x2E3A4C);
                CELL = new Color(0xE8EDF5);
            }
            default -> {
                RED = new Color(0xC62828);
                RED_DARK = new Color(0x8E1B1B);
                BLUE = new Color(0x1565C0);
                BLUE_DARK = new Color(0x0D47A1);
                GREEN = new Color(0x2E7D32);
                GREEN_LIGHT = new Color(0xC8E6C9);
                BACKGROUND = new Color(0xF4F7FB);
                PANEL = Color.WHITE;
                TEXT = new Color(0x1E2430);
                MUTED = new Color(0x5F6B7A);
                SUBTITLE = new Color(0xFFE0E0);
                BORDER = new Color(0xD8E1EC);
                CELL = Color.WHITE;
            }
        }
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
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        );
    }

    public static void styleCard(JComponent component) {
        component.setBackground(PANEL);
        component.setBorder(cardBorder());
    }
}
