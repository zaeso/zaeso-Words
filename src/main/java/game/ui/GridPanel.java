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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class GridPanel extends JPanel {
    private static final int GAP = 4;
    private static final int MARGIN = 14;
    private static final int HIGHLIGHT_MILLIS = 700;
    private static final Color HINT_GREEN = new Color(0x81C784);
    private static final Color HINT_GREEN_BORDER = new Color(0x1B5E20);

    private final Consumer<String> onWordSelected;
    private final Consumer<String> onPreview;
    private final Timer animationTimer;
    private char[][] grid = new char[0][0];
    private final Map<Cell, Color> foundCells = new HashMap<>();
    private final Set<Cell> animatedCells = new HashSet<>();
    private final Set<Cell> hintCells = new HashSet<>();
    private final List<Cell> selection = new ArrayList<>();
    private boolean animationsEnabled = true;
    private boolean dragged;
    private long animationStart;
    private Cell anchor;

    public GridPanel(Consumer<String> onWordSelected) {
        this(onWordSelected, null);
    }

    /** @param onPreview получает выделяемое слово во время выбора (может быть null). */
    public GridPanel(Consumer<String> onWordSelected, Consumer<String> onPreview) {
        this.onWordSelected = onWordSelected;
        this.onPreview = onPreview;
        this.animationTimer = new Timer(40, e -> animateHighlight());
        setOpaque(true);
        setBackground(Theme.BLUE_DARK);
        setPreferredSize(new Dimension(520, 460));
        setFocusable(true);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    resetSelection();
                    repaint();
                    return;
                }
                if (e.getButton() != MouseEvent.BUTTON1) return;
                requestFocusInWindow();
                Cell current = cellAt(e.getPoint());
                if (current == null) return;

                // Второй клик: завершение выделения по прямой от первой буквы.
                if (anchor != null && !dragged) {
                    List<Cell> line = line(anchor, current);
                    if (line.size() >= 2) {
                        selection.clear();
                        selection.addAll(line);
                        submitSelection();
                        repaint();
                        return;
                    }
                }

                // Новое выделение: клик задает первую букву.
                anchor = current;
                selection.clear();
                selection.add(current);
                dragged = false;
                notifyPreview();
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (anchor == null) return;
                Cell current = cellAt(e.getPoint());
                if (current == null) return;
                List<Cell> line = line(anchor, current);
                if (line.equals(selection)) return;
                dragged = true;
                selection.clear();
                selection.addAll(line);
                notifyPreview();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                boolean wasDragged = dragged;
                dragged = false;
                if (wasDragged) {
                    if (selection.size() >= 2) {
                        submitSelection();
                    } else {
                        resetSelection();
                    }
                    repaint();
                }
                // Одиночный клик сохраняет выделение до второго клика или отмены.
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent event) {
                if (event.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    resetSelection();
                    repaint();
                }
            }
        });
    }

    public void setGrid(char[][] grid) {
        this.grid = grid;
        this.foundCells.clear();
        this.animatedCells.clear();
        this.hintCells.clear();
        resetSelection();
        this.animationTimer.stop();
        setBackground(Theme.BLUE_DARK);
        repaint();
    }

    /** Сбрасывает выделение мышью: по клику, Escape или новому раунду. */
    public void resetSelection() {
        selection.clear();
        anchor = null;
        dragged = false;
        notifyPreview();
    }

    private void submitSelection() {
        StringBuilder word = new StringBuilder();
        for (Cell cell : selection) {
            word.append(grid[cell.row][cell.col]);
        }
        onWordSelected.accept(word.toString());
        resetSelection();
    }

    private void notifyPreview() {
        if (onPreview == null) return;
        if (selection.isEmpty()) {
            onPreview.accept("");
            return;
        }
        StringBuilder word = new StringBuilder();
        for (Cell cell : selection) {
            word.append(grid[cell.row][cell.col]);
        }
        onPreview.accept(word.toString());
    }

    /**
     * Подсвечивает найденные слова. Каждая клетка получает цвет своего слова,
     * поэтому соседние найденные слова визуально различаются.
     */
    public void setFoundCells(Map<Cell, Color> cells) {
        Set<Cell> added = new HashSet<>(cells.keySet());
        added.removeAll(foundCells.keySet());

        foundCells.clear();
        foundCells.putAll(cells);

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

    /** Добавляет клетку подсказки: одна буква слова подсвечивается зелёным. */
    public void addHintCell(Cell cell) {
        if (cell != null) {
            hintCells.add(cell);
            repaint();
        }
    }

    /** Снимает подсветку подсказок, например при начале нового раунда. */
    public void clearHintCells() {
        if (!hintCells.isEmpty()) {
            hintCells.clear();
            repaint();
        }
    }

    boolean hasHintCell(Cell cell) {
        return hintCells.contains(cell);
    }

    Color foundColorOf(Cell cell) {
        return foundCells.get(cell);
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
        if (!straight) {
            List<Cell> only = new ArrayList<>(1);
            only.add(from);
            return only;
        }

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
            g.setColor(new Color(255, 255, 255, 16));
            g.fillOval(-getWidth() / 4, -getHeight() / 5,
                    getWidth() / 2, getHeight() / 2);
            g.fillOval(getWidth() * 3 / 4, getHeight() / 6,
                    getWidth() / 3, getHeight() / 3);

            int fontSize = Math.max(14, (int) (metrics.cell * 0.46));
            g.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
            FontMetrics fontMetrics = g.getFontMetrics();

            for (int row = 0; row < grid.length; row++) {
                for (int col = 0; col < grid[row].length; col++) {
                    Cell cell = new Cell(row, col);
                    int x = metrics.originX + col * (metrics.cell + GAP);
                    int y = metrics.originY + row * (metrics.cell + GAP);

                    Color foundColor = foundCells.get(cell);
                    g.setColor(new Color(0, 0, 0, 46));
                    g.fillRoundRect(x + 2, y + 3, metrics.cell, metrics.cell, 10, 10);
                    if (foundColor != null) {
                        g.setColor(foundColor(cell, foundColor));
                    } else if (hintCells.contains(cell)) {
                        g.setColor(HINT_GREEN);
                    } else if (selection.contains(cell)) {
                        g.setColor(Theme.BLUE);
                    } else {
                        g.setColor(Theme.CELL);
                    }
                    g.fillRoundRect(x, y, metrics.cell, metrics.cell, 10, 10);

                    if (hintCells.contains(cell) && !foundCells.containsKey(cell)) {
                        g.setColor(HINT_GREEN_BORDER);
                        g.setStroke(new BasicStroke(2f));
                        g.drawRoundRect(x + 1, y + 1, metrics.cell - 2, metrics.cell - 2, 10, 10);
                    }

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
                    if (hintCells.contains(cell) && !foundCells.containsKey(cell)) {
                        g.setColor(HINT_GREEN_BORDER);
                    } else if (foundCells.containsKey(cell)) {
                        g.setColor(isDark(foundCells.get(cell))
                                ? Color.WHITE : new Color(0x263238));
                    } else if (selection.contains(cell)) {
                        g.setColor(Color.WHITE);
                    } else {
                        g.setColor(Theme.CELL_TEXT);
                    }
                    g.drawString(text, textX, textY);
                }
            }
        } finally {
            g.dispose();
        }
    }

    private Color foundColor(Cell cell, Color base) {
        if (!animatedCells.contains(cell)) return base;

        long elapsed = System.currentTimeMillis() - animationStart;
        float progress = Math.min(1f, elapsed / (float) HIGHLIGHT_MILLIS);
        return blend(base, Theme.GREEN_LIGHT, 1f - progress);
    }

    private static boolean isDark(Color color) {
        double luminance = (0.299 * color.getRed()
                + 0.587 * color.getGreen()
                + 0.114 * color.getBlue()) / 255.0;
        return luminance < 0.55;
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

    /** Клетка сетки: строка и столбец, отсчитываемые от нуля. */
    public static final class Cell {
        private final int row;
        private final int col;

        public Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int row() {
            return row;
        }

        public int col() {
            return col;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof Cell)) return false;
            Cell cell = (Cell) other;
            return row == cell.row && col == cell.col;
        }

        @Override
        public int hashCode() {
            return 31 * row + col;
        }

        @Override
        public String toString() {
            return "Cell[row=" + row + ", col=" + col + "]";
        }
    }

    private static final class Metrics {
        private final int cell;
        private final int originX;
        private final int originY;

        Metrics(int cell, int originX, int originY) {
            this.cell = cell;
            this.originX = originX;
            this.originY = originY;
        }
    }
}
