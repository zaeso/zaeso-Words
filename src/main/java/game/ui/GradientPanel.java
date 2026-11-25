package game.ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Фон экранов и шапок в цветах активной темы. */
public final class GradientPanel extends JPanel {
    private final Color top;
    private final Color bottom;
    private boolean decorated;

    public GradientPanel(Color top, Color bottom) {
        this.top = top;
        this.bottom = bottom;
        setOpaque(false);
    }

    /** Градиент по цветам шапки текущей темы. */
    public static GradientPanel header() {
        return new GradientPanel(Theme.RED, Theme.RED_DARK);
    }

    /** Фон главного меню с узором из клеток. */
    public static GradientPanel menu() {
        GradientPanel panel = new GradientPanel(Theme.RED, Theme.RED_DARK);
        panel.decorated = true;
        return panel;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g.fillRect(0, 0, getWidth(), getHeight());

            if (decorated) {
                paintDecor(g);
            }
        } finally {
            g.dispose();
        }
    }

    private void paintDecor(Graphics2D g) {
        int width = getWidth();
        int height = getHeight();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(255, 255, 255, 11));
        int cell = 44;
        for (int x = 14; x < width; x += cell) {
            g.drawLine(x, 0, x, height);
        }
        for (int y = 14; y < height; y += cell) {
            g.drawLine(0, y, width, y);
        }
    }
}
