package game.ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Панель с вертикальным градиентом текущей темы. Используется для главного
 * меню и шапок экранов, чтобы фон выглядел аккуратно на любой Java.
 * Декоративный вариант добавляет мягкие круги и затемнение снизу.
 */
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

    /** Фон главного меню: градиент с декоративными кругами. */
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

        g.setColor(new Color(255, 255, 255, 20));
        g.fillOval(-width / 4, -height / 5, width / 2, height / 2);
        g.fillOval(width * 3 / 4, height / 8, width / 3, height / 3);

        g.setColor(new Color(255, 255, 255, 12));
        g.fillOval(-width / 10, height * 2 / 3, width / 2, height / 2);
        g.fillOval(width * 2 / 3, -height / 8, width / 4, height / 4);

        g.setPaint(new GradientPaint(0, height * 3 / 4, new Color(0, 0, 0, 0),
                0, height, new Color(0, 0, 0, 42)));
        g.fillRect(0, 0, width, height);
    }
}
