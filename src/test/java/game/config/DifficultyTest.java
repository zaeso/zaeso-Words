package game.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DifficultyTest {

    @Test
    void providesThreeOrderedLevels() {
        Difficulty[] levels = Difficulty.values();

        assertEquals(3, levels.length);
        assertEquals(Difficulty.EASY, levels[0]);
        assertEquals(Difficulty.NORMAL, levels[1]);
        assertEquals(Difficulty.HARD, levels[2]);
    }

    @Test
    void everyLevelHasPositiveParameters() {
        for (Difficulty difficulty : Difficulty.values()) {
            GameConfig config = difficulty.config();

            assertNotNull(difficulty.displayName());
            assertTrue(config.height() > 0, difficulty.name());
            assertTrue(config.width() > 0, difficulty.name());
            assertTrue(config.minWords() > 0, difficulty.name());
            assertTrue(!config.timeLimit().isZero() && !config.timeLimit().isNegative(),
                    difficulty.name());
            assertTrue(config.hints() >= 0, difficulty.name());
        }
    }

    @Test
    void parsesNumberAndNameInAnyCase() {
        assertEquals(Difficulty.EASY, Difficulty.fromInput("1"));
        assertEquals(Difficulty.NORMAL, Difficulty.fromInput("2"));
        assertEquals(Difficulty.HARD, Difficulty.fromInput("3"));
        assertEquals(Difficulty.EASY, Difficulty.fromInput("лёгкий"));
        assertEquals(Difficulty.NORMAL, Difficulty.fromInput("ОБЫЧНЫЙ"));
        assertEquals(Difficulty.HARD, Difficulty.fromInput("сложный"));
        assertEquals(Difficulty.HARD, Difficulty.fromInput("HARD"));
    }

    @Test
    void treatsYoAndYeAsEquivalent() {
        assertEquals(Difficulty.EASY, Difficulty.fromInput("легкий"));
    }

    @Test
    void rejectsUnknownInput() {
        assertNull(Difficulty.fromInput(null));
        assertNull(Difficulty.fromInput(""));
        assertNull(Difficulty.fromInput("очень сложный"));
        assertNull(Difficulty.fromInput("4"));
    }
}
