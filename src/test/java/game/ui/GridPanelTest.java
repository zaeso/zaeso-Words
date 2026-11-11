package game.ui;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridPanelTest {

    @Test
    void keepsSeparateColorsForFoundWords() {
        GridPanel panel = new GridPanel(word -> { });
        GridPanel.Cell first = new GridPanel.Cell(1, 1);
        GridPanel.Cell second = new GridPanel.Cell(2, 2);
        Color green = new Color(0x2E7D32);
        Color blue = new Color(0x1565C0);

        Map<GridPanel.Cell, Color> cells = new HashMap<>();
        cells.put(first, green);
        cells.put(second, blue);

        panel.setFoundCells(cells);

        assertEquals(green, panel.foundColorOf(first));
        assertEquals(blue, panel.foundColorOf(second));
        assertNull(panel.foundColorOf(new GridPanel.Cell(3, 3)));
    }

    @Test
    void hintCellsAreAddedAndClearedWithNewGrid() {
        GridPanel panel = new GridPanel(word -> { });
        GridPanel.Cell hint = new GridPanel.Cell(4, 5);

        panel.addHintCell(hint);
        assertTrue(panel.hasHintCell(hint));

        panel.clearHintCells();
        assertFalse(panel.hasHintCell(hint));

        panel.addHintCell(hint);
        panel.setGrid(new char[][]{{'А', 'Б'}, {'В', 'Г'}});
        assertFalse(panel.hasHintCell(hint));
    }
}
