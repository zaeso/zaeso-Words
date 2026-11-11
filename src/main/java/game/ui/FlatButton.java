package game.ui;

import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Плоская закруглённая кнопка, которая полностью рисует себя сама: фон,
 * рамку и текст. Не зависит от установленного Look & Feel, поэтому кнопки
 * выглядят одинаково и всегда видны на любой Java и в любой теме.
 */
public final class FlatButton extends JButton {
    private static final int ARC = 14;

    private final Color base;
    private final Color border;
    private boolean hovered;
    private boolean pressed;

    public FlatButton(String text, Color background) {
        super(text);
        this.base = background;
        this.border = background.darker();
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(Color.WHITE);
        setFont(Theme.BUTTON);
        setBorder(new javax.swing.border.EmptyBorder(12, 26, 12, 26));
        setPreferredSize(new Dimension(getPreferredSize().width, 44));

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent event) {
                hovered = false;
                pressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent event) {
                if (isEnabled()) {
                    pressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                pressed = false;
                repaint();
            }
        };
        addMouseListener(mouse);
        addPropertyChangeListener("enabled", event -> repaint());
    }

    /** Крупная кнопка главного меню. */
    public void large() {
        setFont(new Font("Segoe UI", Font.BOLD, 18));
        setPreferredSize(new Dimension(300, 50));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = base;
            Color edge = border;
            if (!isEnabled()) {
                fill = mix(base, Theme.MUTED, 0.45);
                edge = mix(border, Theme.MUTED, 0.45);
            } else if (pressed) {
                fill = base.darker().darker();
                edge = fill;
            } else if (hovered) {
                fill = mix(base, Color.WHITE, 0.20);
                edge = fill.darker();
            }

            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g.setColor(edge);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);

            Font font = getFont();
            g.setFont(font);
            FontMetrics metrics = g.getFontMetrics(font);
            String text = getText();
            int textX = (getWidth() - metrics.stringWidth(text)) / 2;
            int textY = (getHeight() - metrics.getHeight()) / 2
                    + metrics.getAscent();
            g.setColor(isEnabled() ? getForeground() : mix(getForeground(), Theme.MUTED, 0.4));
            g.drawString(text, textX, textY);
        } finally {
            g.dispose();
        }
    }

    private static Color mix(Color color, Color other, double ratio) {
        return new Color(
                (int) Math.round(color.getRed() + (other.getRed() - color.getRed()) * ratio),
                (int) Math.round(color.getGreen() + (other.getGreen() - color.getGreen()) * ratio),
                (int) Math.round(color.getBlue() + (other.getBlue() - color.getBlue()) * ratio));
    }
}
