package game.generator;

import game.model.Placement;
import game.model.Puzzle;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridGeneratorTest {
    private final GridGenerator generator = new GridGenerator();

    @Test
    void returnsNullWhenNotEnoughWords() {
        Puzzle puzzle = generator.generate(10, 10, List.of("КОТ"), 2);

        assertNull(puzzle);
    }

    @Test
    void ignoresWordsLongerThanGrid() {
        Puzzle puzzle = generator.generate(5, 5, List.of("СЛИШКОМДЛИННОЕ"), 1);

        assertNull(puzzle);
    }

    @Test
    void normalizesAndDeduplicatesWords() {
        Puzzle puzzle = generator.generate(10, 10, List.of("кот", "КОТ", " к-о-т! "), 1);

        assertNotNull(puzzle);
        assertEquals(List.of("КОТ"), puzzle.getWords());
    }

    @Test
    void fillsWholeGridAndPlacesEveryReportedWord() {
        List<String> words = List.of("КОТ", "ДОМ", "СЛОН", "ТИГР", "ЛУНА", "ЗИМА", "ВЕСНА");

        Puzzle puzzle = generator.generate(10, 10, words, 3);

        assertNotNull(puzzle);
        char[][] grid = puzzle.getGrid();
        assertEquals(10, grid.length);
        for (char[] row : grid) {
            assertEquals(10, row.length);
            for (char cell : row) {
                assertTrue(cell != '\0', "все клетки должны быть заполнены");
            }
        }

        assertTrue(puzzle.getPlacements().size() >= 3);
        for (Placement placement : puzzle.getPlacements()) {
            assertPlacementMatchesGrid(grid, placement);
        }
    }

    private static void assertPlacementMatchesGrid(char[][] grid, Placement placement) {
        String word = placement.word();
        for (int i = 0; i < word.length(); i++) {
            int row = placement.startRow() + i * placement.direction().rowStep();
            int col = placement.startCol() + i * placement.direction().colStep();

            assertTrue(row >= 0 && row < grid.length, "слово выходит за границы сетки по строкам");
            assertTrue(col >= 0 && col < grid[row].length, "слово выходит за границы сетки по столбцам");
            assertEquals(word.charAt(i), grid[row][col],
                    "буква слова " + word + " не совпадает с сеткой");
        }
    }
}
