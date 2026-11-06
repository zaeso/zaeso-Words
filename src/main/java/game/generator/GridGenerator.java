package game.generator;

import game.model.Direction;
import game.model.Placement;
import game.model.Puzzle;

import java.util.*;

public class GridGenerator {
    private static final int ATTEMPTS_PER_WORD = 300;
    private static final int RESTARTS = 100;

    private final Random random;

    /**
     * Создаёт генератор со случайным начальным состоянием.
     */
    public GridGenerator() {
        this(new Random());
    }

    /**
     * Создаёт генератор с воспроизводимой последовательностью случайных чисел.
     * Одинаковый seed при одинаковых входных данных даёт одинаковую сетку.
     *
     * @param seed начальное состояние генератора случайных чисел
     */
    public GridGenerator(long seed) {
        this(new Random(seed));
    }

    /**
     * Создаёт генератор с переданным источником случайных чисел.
     *
     * @param random источник случайных чисел, не может быть {@code null}
     */
    public GridGenerator(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    public Puzzle generate(int height, int width, List<String> sourceWords, int minWords) {
        return generateChecked(height, width, sourceWords, minWords).puzzle();
    }

    public GenerationResult generateChecked(int height, int width,
                                            List<String> sourceWords, int minWords) {
        int maxLength = Math.max(height, width);
        List<String> source = sourceWords == null ? List.of() : sourceWords;
        List<String> words = source.stream()
                .filter(Objects::nonNull)
                .map(s -> s.toUpperCase(Locale.ROOT).replaceAll("[^А-ЯЁ]", ""))
                .filter(s -> !s.isBlank())
                .distinct()
                .filter(s -> s.length() <= maxLength)
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();

        if (words.size() < minWords) {
            return GenerationResult.failure(insufficientWordsMessage(
                    height, width, minWords, words.size(), source.size(), maxLength));
        }

        for (int restart = 0; restart < RESTARTS; restart++) {
            char[][] grid = new char[height][width];
            List<Placement> placed = new ArrayList<>();
            List<String> shuffled = new ArrayList<>(words);
            Collections.shuffle(shuffled, random);
            // Длинные слова размещаем раньше: так меньше вероятность, что для них не останется места.
            shuffled.sort(Comparator.comparingInt(String::length).reversed());

            for (String word : shuffled) {
                boolean preferDiagonal = random.nextBoolean();
                for (int attempt = 0; attempt < ATTEMPTS_PER_WORD; attempt++) {
                    Direction direction = preferDiagonal && attempt < ATTEMPTS_PER_WORD / 2
                            ? Direction.ALL[4 + random.nextInt(4)]
                            : randomDirection();
                    int row = random.nextInt(height);
                    int col = random.nextInt(width);

                    if (tryPlace(grid, word, row, col, direction)) {
                        placed.add(new Placement(word, row, col, direction));
                        break;
                    }
                }
            }

            if (placed.size() >= minWords) {
                fillEmptyCells(grid);
                return GenerationResult.success(new Puzzle(grid, placed));
            }
        }

        return GenerationResult.failure(
                "Не удалось разместить минимум " + minWords + " слов на сетке "
                        + height + "×" + width + ". Попробуйте другой размер или словарь.");
    }

    /**
     * Диагонали выбираются чаще прямых направлений: из-за границ сетки
     * диагональное размещение удаётся реже, поэтому при равном выборе
     * диагональных слов в готовой сетке почти не остаётся.
     */
    Direction randomDirection() {
        if (random.nextInt(10) < 6) {
            return Direction.ALL[4 + random.nextInt(4)];
        }
        return Direction.ALL[random.nextInt(4)];
    }

    private static String insufficientWordsMessage(int height, int width, int minWords,
                                                   int suitable, int total, int maxLength) {
        if (total == 0) {
            return "Выбранный словарь пуст.";
        }
        if (suitable == 0) {
            return "В словаре нет слов длиной до " + maxLength + " букв для сетки "
                    + height + "×" + width + ".";
        }
        return "Недостаточно слов для сетки " + height + "×" + width
                + ": нужно " + minWords + ", подходит " + suitable
                + " из " + total + " (максимальная длина слова — " + maxLength + ").";
    }

    public record GenerationResult(Puzzle puzzle, String error) {
        public static GenerationResult success(Puzzle puzzle) {
            return new GenerationResult(Objects.requireNonNull(puzzle), null);
        }

        public static GenerationResult failure(String error) {
            return new GenerationResult(null, Objects.requireNonNull(error));
        }

        public boolean ok() {
            return puzzle != null;
        }
    }

    /**
     * Пытается разместить слово. Если слово выходит за границы или пересекает
     * чужую букву, сетка остаётся без изменений.
     *
     * @return {@code true}, если слово размещено
     */
    boolean tryPlace(char[][] grid, String word, int row, int col, Direction direction) {
        if (!canPlace(grid, word, row, col, direction)) return false;

        place(grid, word, row, col, direction);
        return true;
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
