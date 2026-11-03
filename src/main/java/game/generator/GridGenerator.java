package game.generator;

import game.model.Direction;
import game.model.Placement;
import game.model.Puzzle;

import java.util.*;

public class GridGenerator {
    private final Random random = new Random();
    private static final int ATTEMPTS_PER_WORD = 300;
    private static final int RESTARTS = 100;

    public Puzzle generate(int height, int width, List<String> sourceWords, int minWords) {
        List<String> words = sourceWords.stream()
                .filter(Objects::nonNull)
                .map(s -> s.toUpperCase(Locale.ROOT).replaceAll("[^А-ЯЁ]", ""))
                .filter(s -> !s.isBlank())
                .distinct()
                .filter(s -> s.length() <= Math.max(height, width))
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();

        if (words.size() < minWords) return null;

        for (int restart = 0; restart < RESTARTS; restart++) {
            char[][] grid = new char[height][width];
            List<Placement> placed = new ArrayList<>();
            List<String> shuffled = new ArrayList<>(words);
            Collections.shuffle(shuffled, random);
            // Длинные слова размещаем раньше: так меньше вероятность, что для них не останется места.
            shuffled.sort(Comparator.comparingInt(String::length).reversed());

            for (String word : shuffled) {
                for (int attempt = 0; attempt < ATTEMPTS_PER_WORD; attempt++) {
                    Direction direction = Direction.ALL[random.nextInt(Direction.ALL.length)];
                    int row = random.nextInt(height);
                    int col = random.nextInt(width);

                    if (canPlace(grid, word, row, col, direction)) {
                        place(grid, word, row, col, direction);
                        placed.add(new Placement(word, row, col, direction));
                        break;
                    }
                }
            }

            if (placed.size() >= minWords) {
                fillEmptyCells(grid);
                return new Puzzle(grid, placed);
            }
        }
        return null;
    }

    private boolean canPlace(char[][] grid, String word, int row, int col, Direction d) {
        int endRow = row + (word.length() - 1) * d.rowStep();
        int endCol = col + (word.length() - 1) * d.colStep();

        if (endRow < 0 || endRow >= grid.length || endCol < 0 || endCol >= grid[0].length)
            return false;

        for (int i = 0; i < word.length(); i++) {
            int r = row + i * d.rowStep();
            int c = col + i * d.colStep();
            char existing = grid[r][c];
            if (existing != '\0' && existing != word.charAt(i)) return false;
        }
        return true;
    }

    private void place(char[][] grid, String word, int row, int col, Direction d) {
        for (int i = 0; i < word.length(); i++) {
            grid[row + i * d.rowStep()][col + i * d.colStep()] = word.charAt(i);
        }
    }

    private void fillEmptyCells(char[][] grid) {
        String alphabet = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                if (grid[r][c] == '\0') {
                    grid[r][c] = alphabet.charAt(random.nextInt(alphabet.length()));
                }
            }
        }
    }
}
