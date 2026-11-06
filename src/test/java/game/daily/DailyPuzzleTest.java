package game.daily;

import game.config.Difficulty;
import game.config.GameSettings;
import game.generator.GridGenerator.GenerationResult;
import game.model.Puzzle;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DailyPuzzleTest {
    private static final LocalDate DATE = LocalDate.of(2026, 11, 6);

    @Test
    void seedIsStableForSameDate() {
        assertEquals(DailyPuzzle.seed(DATE), DailyPuzzle.seed(LocalDate.of(2026, 11, 6)));
    }

    @Test
    void differentDatesHaveDifferentSeeds() {
        assertNotEquals(DailyPuzzle.seed(DATE), DailyPuzzle.seed(DATE.plusDays(1)));
        assertNotEquals(DailyPuzzle.seed(DATE), DailyPuzzle.seed(DATE.minusYears(1)));
    }

    @Test
    void settingsDoNotDependOnPlayerPreferences() {
        GameSettings settings = DailyPuzzle.settings();

        assertEquals(Difficulty.NORMAL, settings.difficulty());
        assertEquals("Все русские слова", settings.category().displayName());
        assertEquals(10, settings.height());
        assertEquals(10, settings.width());
        assertEquals(5, settings.timeLimit().toMinutes());
    }

    @Test
    void sameDateProducesSameGrid() {
        GenerationResult first = DailyPuzzle.generate(DATE);
        GenerationResult second = DailyPuzzle.generate(DATE);

        assertTrue(first.ok(), first::error);
        assertTrue(second.ok(), second::error);

        Puzzle firstPuzzle = first.puzzle();
        Puzzle secondPuzzle = second.puzzle();
        assertEquals(firstPuzzle.getWords(), secondPuzzle.getWords());
        assertEquals(firstPuzzle.getPlacements(), secondPuzzle.getPlacements());
        for (int row = 0; row < firstPuzzle.getGrid().length; row++) {
            assertArrayEquals(firstPuzzle.getGrid()[row], secondPuzzle.getGrid()[row],
                    "строка " + row + " должна совпадать в один и тот же день");
        }
    }

    @Test
    void formatsDateForInterface() {
        assertEquals("06.11.2026", DailyPuzzle.formatDate(DATE));
    }
}
