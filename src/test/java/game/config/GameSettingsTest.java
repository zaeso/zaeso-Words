package game.config;

import game.dictionary.WordCategory;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSettingsTest {

    @Test
    void defaultsUseNormalDifficultyAndFirstCategory() {
        GameSettings settings = GameSettings.defaults();

        assertEquals(Difficulty.NORMAL, settings.difficulty());
        assertEquals(WordCategory.defaultCategory(), settings.category());
        assertEquals(10, settings.height());
        assertEquals(10, settings.width());
        assertEquals(Duration.ofMinutes(5), settings.timeLimit());
    }

    @Test
    void toConfigKeepsProfileMinimumWordsAndHints() {
        GameSettings settings = new GameSettings(Difficulty.HARD,
                WordCategory.defaultCategory(), 12, 10, Duration.ofMinutes(9));

        GameConfig config = settings.toConfig();

        assertEquals(12, config.height());
        assertEquals(10, config.width());
        assertEquals(Difficulty.HARD.config().minWords(), config.minWords());
        assertEquals(Difficulty.HARD.config().hints(), config.hints());
        assertEquals(Duration.ofMinutes(9), config.timeLimit());
    }

    @Test
    void rejectsBoardSizeOutsideAllowedList() {
        assertThrows(IllegalArgumentException.class, () -> new GameSettings(
                Difficulty.NORMAL, WordCategory.defaultCategory(), 9, 9,
                Duration.ofMinutes(5)));
    }

    @Test
    void rejectsDurationOutsideRange() {
        assertThrows(IllegalArgumentException.class, () -> new GameSettings(
                Difficulty.NORMAL, WordCategory.defaultCategory(), 10, 10,
                Duration.ofSeconds(30)));
        assertThrows(IllegalArgumentException.class, () -> new GameSettings(
                Difficulty.NORMAL, WordCategory.defaultCategory(), 10, 10,
                Duration.ofMinutes(GameSettings.MAX_MINUTES + 1)));
    }

    @Test
    void allowsEveryOfferedBoardSize() {
        for (GameSettings.BoardSize size : GameSettings.BOARD_SIZES) {
            assertTrue(GameSettings.isAllowedSize(size.height(), size.width()));
            GameSettings settings = new GameSettings(Difficulty.NORMAL,
                    WordCategory.defaultCategory(), size.height(), size.width(),
                    Duration.ofMinutes(5));
            assertNull(settings.validationError(), settings::toString);
        }
    }
}
