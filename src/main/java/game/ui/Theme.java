package game.ui;

import game.config.ThemeChoice;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseEvent;

/**
 * Единая палитра и оформление интерфейса. Цвета меняются через
 * {@link #apply(ThemeChoice)}; выбранная тема хранится в настройках.
 * Кнопки подсвечиваются при наведении и мягко «проваливаются» при нажатии.
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
        ThemeChoice theme = choice == null ? ThemeChoice.CLASSIC : choice;
        switch (theme) {
            case OCEAN:
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
                break;
            case FOREST:
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
                break;
            case DARK:
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
                break;
            default:
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
                break;
        }
    }

    /** Окрашенная кнопка с закруглёнными краями и реакцией на наведение. */
    public static JButton button(String text, Color background) {
        final JButton button = new JButton(text);
        button.setFont(BUTTON);
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background, 1, true),
                BorderFactory.createEmptyBorder(12, 26, 12, 26)));

        final Color base = background;
        MouseInputAdapter hover = new MouseInputAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                if (button.isEnabled()) {
                    button.setBackground(brighter(base));
                    button.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(brighter(base), 1, true),
                            BorderFactory.createEmptyBorder(12, 26, 12, 26)));
                }
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(base);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(base, 1, true),
                        BorderFactory.createEmptyBorder(12, 26, 12, 26)));
            }

            @Override
            public void mousePressed(MouseEvent event) {
                if (button.isEnabled()) {
                    button.setBackground(darker(base));
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                button.setBackground(button.contains(event.getPoint())
                        ? brighter(base) : base);
            }
        };
        button.addMouseListener(hover);
        button.addPropertyChangeListener("enabled", event -> {
            if (!button.isEnabled()) {
                button.setBackground(darker(base));
            } else {
                button.setBackground(base);
            }
        });
        return button;
    }

    private static Color brighter(Color color) {
        return mix(color, Color.WHITE, 0.18);
    }

    private static Color darker(Color color) {
        return mix(color, Color.BLACK, 0.18);
    }

    private static Color mix(Color color, Color other, double ratio) {
        return new Color(
                (int) Math.round(color.getRed() + (other.getRed() - color.getRed()) * ratio),
                (int) Math.round(color.getGreen() + (other.getGreen() - color.getGreen()) * ratio),
                (int) Math.round(color.getBlue() + (other.getBlue() - color.getBlue()) * ratio));
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
