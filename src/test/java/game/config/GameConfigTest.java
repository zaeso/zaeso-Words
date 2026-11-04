package game.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameConfigTest {

    @Test
    void acceptsValidConfiguration() {
        GameConfig config = new GameConfig(10, 12, 8, Duration.ofMinutes(5), 2);

        assertEquals(10, config.height());
        assertEquals(12, config.width());
        assertEquals(8, config.minWords());
        assertEquals(Duration.ofMinutes(5), config.timeLimit());
        assertEquals(2, config.hints());
    }

    @Test
    void replacesBoardSizeKeepingOtherParameters() {
        GameConfig config = new GameConfig(10, 10, 8, Duration.ofMinutes(5), 2);

        GameConfig resized = config.withBoardSize(12, 10);

        assertEquals(12, resized.height());
        assertEquals(10, resized.width());
        assertEquals(config.minWords(), resized.minWords());
        assertEquals(config.timeLimit(), resized.timeLimit());
        assertEquals(config.hints(), resized.hints());
    }

    @Test
    void rejectsInvalidParameters() {
        Duration validTime = Duration.ofMinutes(5);

        assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(0, 10, 8, validTime, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(10, 0, 8, validTime, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(10, 10, 0, validTime, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(10, 10, 8, null, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(10, 10, 8, Duration.ZERO, 2));
        assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(10, 10, 8, Duration.ofMinutes(-1), 2));
        assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(10, 10, 8, validTime, -1));
    }
}
