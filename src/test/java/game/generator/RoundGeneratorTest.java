package game.generator;

import game.config.Difficulty;
import game.config.GameConfig;
import game.dictionary.WordCategory;
import game.generator.GridGenerator.GenerationResult;
import game.model.Puzzle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoundGeneratorTest {
    private static final GameConfig CONFIG = Difficulty.NORMAL.config();
    private static final WordCategory CATEGORY = WordCategory.defaultCategory();

    @Test
    void sameSeedProducesIdenticalRound() {
        GenerationResult first = RoundGenerator.generate(CONFIG, CATEGORY, 42L);
        GenerationResult second = RoundGenerator.generate(CONFIG, CATEGORY, 42L);

        assertTrue(first.ok(), first::error);
        assertTrue(second.ok(), second::error);

        Puzzle firstPuzzle = first.puzzle();
        Puzzle secondPuzzle = second.puzzle();
        assertEquals(firstPuzzle.getWords(), secondPuzzle.getWords());
        assertEquals(firstPuzzle.getPlacements(), secondPuzzle.getPlacements());
        for (int row = 0; row < firstPuzzle.getGrid().length; row++) {
            assertArrayEquals(firstPuzzle.getGrid()[row], secondPuzzle.getGrid()[row],
                    "строка " + row + " должна совпадать при одинаковом seed");
        }
    }

    @Test
    void differentSeedsProduceDifferentRounds() {
        GenerationResult first = RoundGenerator.generate(CONFIG, CATEGORY, 1L);
        GenerationResult second = RoundGenerator.generate(CONFIG, CATEGORY, 2L);

        assertTrue(first.ok(), first::error);
        assertTrue(second.ok(), second::error);
        assertNotEquals(first.puzzle().getPlacements(), second.puzzle().getPlacements());
    }
}
