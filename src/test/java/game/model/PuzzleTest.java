package game.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleTest {

    @Test
    void returnsWordsInPlacementOrder() {
        char[][] grid = {{'К', 'О', 'Т'}};
        Direction direction = new Direction(0, 1);
        Puzzle puzzle = new Puzzle(grid, Arrays.asList(
                new Placement("КОТ", 0, 0, direction),
                new Placement("ОКО", 0, 0, direction)
        ));

        assertEquals(Arrays.asList("КОТ", "ОКО"), puzzle.getWords());
    }

    @Test
    void keepsOwnCopyOfPlacements() {
        char[][] grid = {{'К', 'О', 'Т'}};
        List<Placement> mutablePlacements = new ArrayList<>();
        mutablePlacements.add(new Placement("КОТ", 0, 0, new Direction(0, 1)));

        Puzzle puzzle = new Puzzle(grid, mutablePlacements);
        mutablePlacements.clear();

        assertEquals(1, puzzle.getPlacements().size());
    }
}
