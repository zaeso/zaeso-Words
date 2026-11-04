package game.generator;

import game.config.Difficulty;
import game.config.GameConfig;
import game.model.Direction;
import game.model.Placement;
import game.model.Puzzle;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridGeneratorTest {
    private static final List<String> WORDS =
            List.of("КОТ", "ДОМ", "СЛОН", "ТИГР", "ЛУНА", "ЗИМА", "ВЕСНА");

    private static final List<String> LARGE_WORDS = List.of(
            "АЛГОРИТМ", "ПРОГРАММА", "КОМПЬЮТЕР", "ИНТЕРНЕТ", "МОНИТОР",
            "КЛАВИАТУРА", "ПРОЦЕССОР", "ПЕРЕМЕННАЯ", "МАССИВ", "ЦИКЛ",
            "МЕТОД", "КЛАСС", "ОБЪЕКТ", "СТРОКА", "ФУНКЦИЯ",
            "ПИТОН", "ДЖАВА", "КОД", "СЕРВЕР", "БРАУЗЕР",
            "СИСТЕМА", "ДАННЫЕ", "ФАЙЛ", "ПАПКА", "СЕТЬ",
            "РАЗРАБОТКА", "ПРОЕКТ", "ПАМЯТЬ", "ЛОГИКА", "КОМПИЛЯТОР"
    );

    @Test
    void returnsNullWhenNotEnoughWords() {
        GridGenerator generator = new GridGenerator(1L);

        Puzzle puzzle = generator.generate(10, 10, List.of("КОТ"), 2);

        assertNull(puzzle);
    }

    @Test
    void returnsNullWhenThereIsNotEnoughSpace() {
        GridGenerator generator = new GridGenerator(2L);

        Puzzle puzzle = generator.generate(1, 1, List.of("А", "Б"), 2);

        assertNull(puzzle);
    }

    @Test
    void ignoresWordsLongerThanGrid() {
        GridGenerator generator = new GridGenerator(3L);

        Puzzle puzzle = generator.generate(5, 5, List.of("СЛИШКОМДЛИННОЕ"), 1);

        assertNull(puzzle);
    }

    @Test
    void normalizesAndDeduplicatesWords() {
        GridGenerator generator = new GridGenerator(4L);

        Puzzle puzzle = generator.generate(10, 10, List.of("кот", "КОТ", " к-о-т! "), 1);

        assertNotNull(puzzle);
        assertEquals(List.of("КОТ"), puzzle.getWords());
    }

    @Test
    void fillsWholeGridAndPlacesEveryReportedWord() {
        GridGenerator generator = new GridGenerator(5L);

        Puzzle puzzle = generator.generate(10, 10, WORDS, 3);

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

    @Test
    void sameSeedProducesSamePuzzle() {
        GridGenerator first = new GridGenerator(42L);
        GridGenerator second = new GridGenerator(42L);

        Puzzle firstPuzzle = first.generate(10, 10, WORDS, 3);
        Puzzle secondPuzzle = second.generate(10, 10, WORDS, 3);

        assertNotNull(firstPuzzle);
        assertNotNull(secondPuzzle);
        assertEquals(firstPuzzle.getPlacements(), secondPuzzle.getPlacements());
        assertEquals(firstPuzzle.getWords(), secondPuzzle.getWords());
        for (int row = 0; row < firstPuzzle.getGrid().length; row++) {
            assertArrayEquals(firstPuzzle.getGrid()[row], secondPuzzle.getGrid()[row],
                    "строка " + row + " должна совпадать при одинаковом seed");
        }
    }

    @Test
    void worksWithParametersOfEveryDifficulty() {
        for (Difficulty difficulty : Difficulty.values()) {
            GameConfig config = difficulty.config();
            GridGenerator generator = new GridGenerator(11L);

            Puzzle puzzle = generator.generate(
                    config.height(), config.width(), LARGE_WORDS, config.minWords());

            assertNotNull(puzzle, "не удалось сгенерировать сетку для уровня " + difficulty.name());
            assertEquals(config.height(), puzzle.getGrid().length, difficulty.name());
            assertEquals(config.width(), puzzle.getGrid()[0].length, difficulty.name());
            assertTrue(puzzle.getWords().size() >= config.minWords(), difficulty.name());
        }
    }

    @Test
    void placesWordInEveryDirection() {
        String word = "КОТ";

        for (Direction direction : Direction.ALL) {
            char[][] grid = emptyGrid(5, 5);

            boolean placed = new GridGenerator(6L).tryPlace(grid, word, 2, 2, direction);

            assertTrue(placed, "слово должно размещаться в направлении " + direction);
            for (int i = 0; i < word.length(); i++) {
                int row = 2 + i * direction.rowStep();
                int col = 2 + i * direction.colStep();
                assertEquals(word.charAt(i), grid[row][col],
                        "буква " + i + " в направлении " + direction);
            }
        }
    }

    @Test
    void rejectsWordThatLeavesGridInEveryDirection() {
        String word = "КОТ";

        for (Direction direction : Direction.ALL) {
            char[][] grid = emptyGrid(5, 5);
            int row = direction.rowStep() < 0 ? 0 : 4;
            int col = direction.colStep() < 0 ? 0 : 4;

            boolean placed = new GridGenerator(7L).tryPlace(grid, word, row, col, direction);

            assertFalse(placed, "слово не должно выходить за границы в направлении " + direction);
            for (char[] line : grid) {
                for (char cell : line) {
                    assertEquals('\0', cell, "неудачная попытка не должна менять сетку");
                }
            }
        }
    }

    @Test
    void allowsOverlapOnlyOnMatchingLetters() {
        String word = "КОТ";
        char[][] grid = emptyGrid(5, 5);
        grid[2][3] = 'О';

        boolean placed = new GridGenerator(8L).tryPlace(grid, word, 2, 2, Direction.ALL[0]);

        assertTrue(placed);
        assertEquals('К', grid[2][2]);
        assertEquals('О', grid[2][3]);
        assertEquals('Т', grid[2][4]);
    }

    @Test
    void rejectsOverlapOnDifferentLetterAndKeepsGrid() {
        String word = "КОТ";
        char[][] grid = emptyGrid(5, 5);
        grid[2][3] = 'А';

        boolean placed = new GridGenerator(9L).tryPlace(grid, word, 2, 2, Direction.ALL[0]);

        assertFalse(placed);
        assertEquals('А', grid[2][3]);
        assertEquals('\0', grid[2][2]);
        assertEquals('\0', grid[2][4]);
    }

    @Test
    void fillsEveryEmptyCellWithRussianLetter() {
        String alphabet = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
        GridGenerator generator = new GridGenerator(10L);

        Puzzle puzzle = generator.generate(10, 10, WORDS, 3);

        assertNotNull(puzzle);
        for (char[] line : puzzle.getGrid()) {
            for (char cell : line) {
                assertTrue(alphabet.indexOf(cell) >= 0,
                        "клетка должна содержать русскую букву, получено: " + cell);
            }
        }
    }

    private static char[][] emptyGrid(int height, int width) {
        char[][] grid = new char[height][width];
        for (char[] row : grid) {
            Arrays.fill(row, '\0');
        }
        return grid;
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
