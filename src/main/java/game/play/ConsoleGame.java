package game.play;

import game.model.Puzzle;

import java.util.*;

public class ConsoleGame {
    private final Puzzle puzzle;
    private final Scanner scanner;
    private final Set<String> found = new HashSet<>();

    public ConsoleGame(Puzzle puzzle, Scanner scanner) {
        this.puzzle = puzzle;
        this.scanner = scanner;
    }

    public void start() {
        Set<String> allWords = new LinkedHashSet<>(puzzle.getWords());
        System.out.println("\nНайди спрятанные слова. Вводи слово целиком.");
        printGrid();
        printWordList(allWords);

        while (found.size() < allWords.size()) {
            System.out.print("\nСлово (или EXIT для выхода): ");
            String answer = scanner.nextLine().trim().toUpperCase(Locale.ROOT);

            if (answer.equals("EXIT")) break;
            if (!allWords.contains(answer)) {
                System.out.println("Такого слова нет в списке.");
            } else if (found.contains(answer)) {
                System.out.println("Это слово уже найдено.");
            } else {
                found.add(answer);
                System.out.println("Верно! Найдено: " + found.size() + " из " + allWords.size());
            }
        }

        System.out.println("\nИгра завершена. Результат: " + found.size() + " из " + allWords.size());
        System.out.println("Оставшиеся слова: ");
        allWords.stream().filter(w -> !found.contains(w)).forEach(System.out::println);
    }

    private void printGrid() {
        char[][] grid = puzzle.getGrid();
        System.out.print("    ");
        for (int c = 1; c <= grid[0].length; c++) System.out.printf("%2d ", c);
        System.out.println();

        for (int r = 0; r < grid.length; r++) {
            System.out.printf("%2d  ", r + 1);
            for (int c = 0; c < grid[r].length; c++) {
                System.out.printf("%2c ", grid[r][c]);
            }
            System.out.println();
        }
    }

    private void printWordList(Set<String> words) {
        System.out.println("\nСписок слов:");
        for (String word : words) {
            System.out.print((found.contains(word) ? "[+] " : "[ ] ") + word + "   ");
        }
        System.out.println();
    }
}
