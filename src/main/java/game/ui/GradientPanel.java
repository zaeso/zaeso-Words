package game.ui;

import javax.swing.JPanel;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Панель с вертикальным градиентом текущей темы. Используется для главного
 * меню и шапки игры, чтобы фон выглядел аккуратно на любой Java.
 */
public final class GradientPanel extends JPanel {
    private final java.awt.Color top;
    private final java.awt.Color bottom;

    public GradientPanel(java.awt.Color top, java.awt.Color bottom) {
        this.top = top;
        this.bottom = bottom;
        setOpaque(false);
    }

    /** Градиент по цветам шапки текущей темы. */
    public static GradientPanel header() {
        return new GradientPanel(Theme.RED, Theme.RED_DARK);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_SPEED);
            g.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g.fillRect(0, 0, getWidth(), getHeight());
        } finally {
            g.dispose();
        }
    }
}
