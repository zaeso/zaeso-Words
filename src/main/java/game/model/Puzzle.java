package game.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Puzzle {
    private final char[][] grid;
    private final List<Placement> placements;
    private final WordLanguage language;

    public Puzzle(char[][] grid, List<Placement> placements) {
        this(grid, placements, WordLanguage.RUSSIAN);
    }

    public Puzzle(char[][] grid, List<Placement> placements, WordLanguage language) {
        this.grid = grid;
        this.placements = Collections.unmodifiableList(new ArrayList<>(placements));
        this.language = java.util.Objects.requireNonNull(language, "language");
    }

    public WordLanguage language() {
        return language;
    }

    public char[][] getGrid() {
        return grid;
    }

    public List<Placement> getPlacements() {
        return placements;
    }

    public List<String> getWords() {
        return placements.stream()
                .map(Placement::word)
                .collect(Collectors.toList());
    }
}
