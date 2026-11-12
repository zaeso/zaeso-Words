package game.ui;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Ярлык-плашка с закруглённым полупрозрачным фоном для шапки игры. */
public final class BadgeLabel extends JLabel {
    public BadgeLabel(String text) {
        super(text);
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.CENTER);
        setForeground(Color.WHITE);
        setFont(Theme.BUTTON);
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics metrics = getFontMetrics(getFont());
        return new Dimension(metrics.stringWidth(getText()) + 30,
                metrics.getHeight() + 14);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(255, 255, 255, 38));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g.setColor(new Color(255, 255, 255, 60));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        } finally {
            g.dispose();
        }
        super.paintComponent(graphics);
    }
}
