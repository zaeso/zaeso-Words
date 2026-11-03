package game.model;

import java.util.List;

public class Puzzle {
    private final char[][] grid;
    private final List<Placement> placements;

    public Puzzle(char[][] grid, List<Placement> placements) {
        this.grid = grid;
        this.placements = List.copyOf(placements);
    }

    public char[][] getGrid() {
        return grid;
    }

    public List<Placement> getPlacements() {
        return placements;
    }

    public List<String> getWords() {
        return placements.stream().map(Placement::word).toList();
    }
}
