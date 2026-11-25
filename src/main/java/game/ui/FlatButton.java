package game.ui;

import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Кнопка с собственными состояниями наведения, нажатия и фокуса. */
public final class FlatButton extends JButton {
    private static final int ARC = 16;

    private final Color base;

    private boolean hovered;
    private boolean pressed;

    public FlatButton(String text, Color background) {
        super(text);
        this.base = background;

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

            Color fill = base;
            if (!active) {
                fill = mix(base, Theme.MUTED, 0.58);
            } else if (pressed || getModel().isPressed()) {
                fill = mix(base, Color.BLACK, 0.18);
            } else if (hovered) {
                fill = mix(base, Color.WHITE, 0.10);
            }

            g.setColor(fill);
            g.fillRoundRect(0, 0, width, height, ARC, ARC);
            if (isFocusOwner()) {
                g.setColor(getForeground());
                g.drawRoundRect(3, 3, width - 7, height - 7, ARC - 4, ARC - 4);
            }

            g.setFont(getFont());
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            FontMetrics metrics = g.getFontMetrics();
            String text = getText();
            int textX = (width - metrics.stringWidth(text)) / 2;
            int textY = (height - metrics.getHeight()) / 2 + metrics.getAscent();
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
