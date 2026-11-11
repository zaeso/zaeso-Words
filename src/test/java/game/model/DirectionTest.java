package game.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectionTest {

    @Test
    void containsAllEightUniqueDirections() {
        Set<String> directions = Arrays.stream(Direction.ALL)
                .map(direction -> direction.rowStep() + ":" + direction.colStep())
                .collect(Collectors.toSet());

        assertEquals(8, Direction.ALL.length);
        assertEquals(8, directions.size());
        assertTrue(directions.containsAll(Arrays.asList(
                "0:1", "0:-1", "1:0", "-1:0",
                "1:1", "1:-1", "-1:1", "-1:-1"
        )));
    }

    @Test
    void describesEveryDirection() {
        assertEquals("вправо", new Direction(0, 1).description());
        assertEquals("влево", new Direction(0, -1).description());
        assertEquals("вниз", new Direction(1, 0).description());
        assertEquals("вверх", new Direction(-1, 0).description());
        assertEquals("по диагонали вниз-вправо", new Direction(1, 1).description());
        assertEquals("по диагонали вниз-влево", new Direction(1, -1).description());
        assertEquals("по диагонали вверх-вправо", new Direction(-1, 1).description());
        assertEquals("по диагонали вверх-влево", new Direction(-1, -1).description());
    }

    @Test
    void rejectsUnknownDirectionDescription() {
        assertThrows(IllegalStateException.class, () -> new Direction(2, 2).description());
    }
}
