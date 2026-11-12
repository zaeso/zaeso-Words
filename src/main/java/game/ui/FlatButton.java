package game.ui;

import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Плоская закруглённая кнопка, которая полностью рисует себя сама: градиент,
 * тень, блик, рамку и текст. Не зависит от установленного Look & Feel,
 * поэтому кнопки выглядят одинаково и всегда видны на любой Java.
 */
public final class FlatButton extends JButton {
    private static final int ARC = 16;

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
        setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        setPreferredSize(new Dimension(300, 52));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            boolean active = isEnabled();

            Color fillTop = mix(base, Color.WHITE, 0.26);
            Color fillBottom = base;
            Color edge = border;

            if (!active) {
                fillTop = mix(base, Theme.MUTED, 0.42);
                fillBottom = mix(base, Theme.MUTED, 0.55);
                edge = mix(border, Theme.MUTED, 0.45);
            } else if (pressed) {
                fillTop = mix(base, Color.BLACK, 0.10);
                fillBottom = base.darker();
                edge = fillBottom.darker();
            } else if (hovered) {
                fillTop = mix(base, Color.WHITE, 0.44);
                fillBottom = mix(base, Color.WHITE, 0.10);
                edge = fillBottom.darker();
            }

            if (active && !pressed) {
                g.setColor(new Color(0, 0, 0, hovered ? 60 : 34));
                g.fillRoundRect(2, 4, width - 4, height - 4, ARC, ARC);
            }

            g.setPaint(new GradientPaint(0, 1, fillTop, 0, height - 1, fillBottom));
            g.fillRoundRect(0, 0, width - 1, height - 1, ARC, ARC);
            g.setColor(edge);
            g.drawRoundRect(0, 0, width - 1, height - 1, ARC, ARC);

            if (active) {
                g.setColor(new Color(255, 255, 255, 70));
                g.drawLine(ARC, 1, width - 1 - ARC, 1);
            }

            FontMetrics metrics = g.getFontMetrics();
            String text = getText();
            int textX = (width - metrics.stringWidth(text)) / 2;
            int textY = (height - metrics.getHeight()) / 2 + metrics.getAscent();
            if (active) {
                g.setColor(new Color(0, 0, 0, 60));
                g.drawString(text, textX, textY + 1);
            }
            g.setColor(active ? getForeground() : mix(getForeground(), Theme.MUTED, 0.4));
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
