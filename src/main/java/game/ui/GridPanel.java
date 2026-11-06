package game.ui;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class GridPanel extends JPanel {
    private static final int GAP = 4;
    private static final int MARGIN = 14;
    private static final int HIGHLIGHT_MILLIS = 700;

    private final Consumer<String> onWordSelected;
    private final Timer animationTimer;
    private char[][] grid = new char[0][0];
    private final Set<Cell> foundCells = new HashSet<>();
    private final Set<Cell> animatedCells = new HashSet<>();
    private final List<Cell> selection = new ArrayList<>();
    private boolean animationsEnabled = true;
    private long animationStart;
    private Cell anchor;

    public GridPanel(Consumer<String> onWordSelected) {
        this.onWordSelected = onWordSelected;
        this.animationTimer = new Timer(40, e -> animateHighlight());
        setOpaque(true);
        setBackground(Theme.BLUE_DARK);
        setPreferredSize(new Dimension(520, 460));
        setFocusable(true);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                anchor = cellAt(e.getPoint());
                selection.clear();
                if (anchor != null) selection.add(anchor);
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (anchor == null) return;
                Cell current = cellAt(e.getPoint());
                if (current == null) return;
                selection.clear();
                selection.addAll(line(anchor, current));
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (selection.size() >= 2) {
                    StringBuilder word = new StringBuilder();
                    for (Cell cell : selection) {
                        word.append(grid[cell.row][cell.col]);
                    }
                    onWordSelected.accept(word.toString());
                }
                selection.clear();
                anchor = null;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (anchor == null) {
                    selection.clear();
                    repaint();
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void setGrid(char[][] grid) {
        this.grid = grid;
        this.foundCells.clear();
        this.animatedCells.clear();
        this.selection.clear();
        this.animationTimer.stop();
        setBackground(Theme.BLUE_DARK);
        repaint();
    }

    public void setFoundCells(Set<Cell> cells) {
        Set<Cell> added = new HashSet<>(cells);
        added.removeAll(foundCells);

        foundCells.clear();
        foundCells.addAll(cells);

        if (animationsEnabled && !added.isEmpty() && !animationTimer.isRunning()) {
            animatedCells.clear();
            animatedCells.addAll(added);
            animationStart = System.currentTimeMillis();
            animationTimer.restart();
        } else if (!animationsEnabled) {
            animatedCells.clear();
            animationTimer.stop();
        }
        repaint();
    }

    /** Включает или выключает плавную подсветку найденных букв. */
    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
        if (!enabled) {
            animatedCells.clear();
            animationTimer.stop();
            repaint();
        }
    }

    private void animateHighlight() {
        if (System.currentTimeMillis() - animationStart >= HIGHLIGHT_MILLIS) {
            animationTimer.stop();
            animatedCells.clear();
        }
        repaint();
    }

    private Cell cellAt(Point point) {
        if (grid.length == 0) return null;
        Metrics metrics = metrics();
        int col = (point.x - metrics.originX) / (metrics.cell + GAP);
        int row = (point.y - metrics.originY) / (metrics.cell + GAP);
        if (row < 0 || col < 0 || row >= grid.length || col >= grid[0].length) return null;

        int cellX = metrics.originX + col * (metrics.cell + GAP);
        int cellY = metrics.originY + row * (metrics.cell + GAP);
        if (point.x > cellX + metrics.cell || point.y > cellY + metrics.cell) return null;
        return new Cell(row, col);
    }

    private List<Cell> line(Cell from, Cell to) {
        int rowDelta = to.row - from.row;
        int colDelta = to.col - from.col;
        boolean straight = rowDelta == 0 || colDelta == 0
                || Math.abs(rowDelta) == Math.abs(colDelta);
        if (!straight) return List.of(from);

        int rowStep = Integer.signum(rowDelta);
        int colStep = Integer.signum(colDelta);
        int steps = Math.max(Math.abs(rowDelta), Math.abs(colDelta));
        List<Cell> cells = new ArrayList<>(steps + 1);
        for (int i = 0; i <= steps; i++) {
            cells.add(new Cell(from.row + rowStep * i, from.col + colStep * i));
        }
        return cells;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (grid.length == 0) return;

            Metrics metrics = metrics();
            int fontSize = Math.max(14, (int) (metrics.cell * 0.46));
            g.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
            FontMetrics fontMetrics = g.getFontMetrics();

            for (int row = 0; row < grid.length; row++) {
                for (int col = 0; col < grid[row].length; col++) {
                    Cell cell = new Cell(row, col);
                    int x = metrics.originX + col * (metrics.cell + GAP);
                    int y = metrics.originY + row * (metrics.cell + GAP);

                    if (foundCells.contains(cell)) {
                        g.setColor(foundColor(cell));
                    } else if (selection.contains(cell)) {
                        g.setColor(Theme.BLUE);
                    } else {
                        g.setColor(Theme.CELL);
                    }
                    g.fillRoundRect(x, y, metrics.cell, metrics.cell, 10, 10);

                    if (selection.contains(cell)) {
                        g.setColor(Theme.GREEN_LIGHT);
                        g.setStroke(new BasicStroke(2.5f));
                        g.drawRoundRect(x + 1, y + 1, metrics.cell - 2, metrics.cell - 2, 10, 10);
                    }

                    char letter = grid[row][col];
                    String text = String.valueOf(letter);
                    int textX = x + (metrics.cell - fontMetrics.stringWidth(text)) / 2;
                    int textY = y + (metrics.cell - fontMetrics.getHeight()) / 2
                            + fontMetrics.getAscent();
                    g.setColor(foundCells.contains(cell) || selection.contains(cell)
                            ? Color.WHITE : Theme.CELL_TEXT);
                    g.drawString(text, textX, textY);
                }
            }
        } finally {
            g.dispose();
        }
    }

    private Color foundColor(Cell cell) {
        if (!animatedCells.contains(cell)) return Theme.GREEN;

        long elapsed = System.currentTimeMillis() - animationStart;
        float progress = Math.min(1f, elapsed / (float) HIGHLIGHT_MILLIS);
        return blend(Theme.GREEN, Theme.GREEN_LIGHT, 1f - progress);
    }

    private static Color blend(Color from, Color to, float ratio) {
        float clamped = Math.max(0f, Math.min(1f, ratio));
        return new Color(
                Math.round(from.getRed() + (to.getRed() - from.getRed()) * clamped),
                Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * clamped),
                Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * clamped));
    }

    private Metrics metrics() {
        int rows = Math.max(1, grid.length);
        int cols = Math.max(1, grid.length == 0 ? 1 : grid[0].length);
        int availableWidth = Math.max(1, getWidth() - 2 * MARGIN - (cols - 1) * GAP);
        int availableHeight = Math.max(1, getHeight() - 2 * MARGIN - (rows - 1) * GAP);
        int cell = Math.max(18, Math.min(availableWidth / cols, availableHeight / rows));
        int width = cols * cell + (cols - 1) * GAP;
        int height = rows * cell + (rows - 1) * GAP;
        int originX = Math.max(MARGIN, (getWidth() - width) / 2);
        int originY = Math.max(MARGIN, (getHeight() - height) / 2);
        return new Metrics(cell, originX, originY);
    }

    public record Cell(int row, int col) {
    }

    private record Metrics(int cell, int originX, int originY) {
    }
}
