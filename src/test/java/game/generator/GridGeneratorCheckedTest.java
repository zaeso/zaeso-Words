package game.generator;

import game.generator.GridGenerator.GenerationResult;
import game.model.Puzzle;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridGeneratorCheckedTest {

    @Test
    void reportsHowManyWordsAreAvailable() {
        GenerationResult result = new GridGenerator(1L)
                .generateChecked(10, 10, Arrays.asList("КОТ", "ДОМ"), 5);

        assertFalse(result.ok());
        assertNull(result.puzzle());
        assertTrue(result.error().contains("Недостаточно слов"));
        assertTrue(result.error().contains("нужно 5"));
        assertTrue(result.error().contains("подходит 2 из 2"));
    }

    @Test
    void reportsEmptyDictionary() {
        GenerationResult result = new GridGenerator(2L)
                .generateChecked(10, 10, Arrays.asList(), 1);

        assertFalse(result.ok());
        assertTrue(result.error().contains("словарь пуст"));
    }

    @Test
    void reportsWhenNoWordFitsTheGrid() {
        GenerationResult result = new GridGenerator(3L)
                .generateChecked(5, 5, Arrays.asList("СЛИШКОМДЛИННОЕ"), 1);

        assertFalse(result.ok());
        assertTrue(result.error().contains("нет слов длиной до 5"));
    }

    @Test
    void returnsPuzzleWhenGenerationSucceeds() {
        GenerationResult result = new GridGenerator(4L)
                .generateChecked(10, 10, Arrays.asList("КОТ", "ДОМ", "СЛОН"), 2);

        assertTrue(result.ok());
        assertNull(result.error());
        Puzzle puzzle = result.puzzle();
        assertNotNull(puzzle);
        assertTrue(puzzle.getWords().size() >= 2);
    }
}
