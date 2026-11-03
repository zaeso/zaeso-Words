package game.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectionTest {

    @Test
    void containsAllEightUniqueDirections() {
        Set<String> directions = Arrays.stream(Direction.ALL)
                .map(direction -> direction.rowStep() + ":" + direction.colStep())
                .collect(Collectors.toSet());

        assertEquals(8, Direction.ALL.length);
        assertEquals(8, directions.size());
        assertTrue(directions.containsAll(List.of(
                "0:1", "0:-1", "1:0", "-1:0",
                "1:1", "1:-1", "-1:1", "-1:-1"
        )));
    }
}
