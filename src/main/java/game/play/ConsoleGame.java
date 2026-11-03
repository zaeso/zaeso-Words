package game.play;

import game.model.Puzzle;

import java.time.Duration;
import java.util.*;

public class ConsoleGame {
    private static final Duration DEFAULT_TIME_LIMIT = Duration.ofMinutes(5);

    private final Puzzle puzzle;
    private final Scanner scanner;
    private final Set<String> found = new HashSet<>();
    private final GameTimer timer;

    public ConsoleGame(Puzzle puzzle, Scanner scanner) {
        this(puzzle, scanner, DEFAULT_TIME_LIMIT);
    }

    public ConsoleGame(Puzzle puzzle, Scanner scanner, Duration timeLimit) {
        this.puzzle = puzzle;
        this.scanner = scanner;
        this.timer = new GameTimer(timeLimit);
    }

    public void start() {
        Set<String> allWords = new LinkedHashSet<>(puzzle.getWords());
        System.out.println("\nНайди спрятанные слова. Вводи слово целиком.");
        System.out.println("На игру отведено " + GameTimer.format(timer.getLimitSeconds()) + ".");
        printGrid();
        printWordList(allWords);

        timer.start();
        Thread monitor = startTimeMonitor();
        boolean timeExpired = false;

        try {
            while (found.size() < allWords.size()) {
                if (timer.isExpired()) {
                    timeExpired = true;
                    break;
                }

                System.out.printf("%nСлово (осталось %s, или EXIT для выхода): ",
                        GameTimer.format(timer.remainingSeconds()));
                String answer = scanner.nextLine().trim().toUpperCase(Locale.ROOT);

                if (timer.isExpired()) {
                    timeExpired = true;
                    break;
                }

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
        } finally {
            timer.stop();
            monitor.interrupt();
        }

        System.out.println("\nИгра завершена. Результат: " + found.size() + " из " + allWords.size());
        System.out.println("Затраченное время: " + GameTimer.format(timer.elapsedSeconds()) + ".");
        if (timeExpired) {
            System.out.println("Причина завершения: истекло время.");
        }
        System.out.println("Оставшиеся слова: ");
        allWords.stream().filter(w -> !found.contains(w)).forEach(System.out::println);
    }

    /**
     * Фоновый поток сообщает об оставшемся времени, пока игрок думает над
     * ответом. Поток демонический и завершается вместе с игрой.
     */
    private Thread startTimeMonitor() {
        Thread monitor = new Thread(() -> {
            long lastNotified = -1;
            while (timer.isRunning()) {
                if (timer.isExpired()) {
                    System.out.println("\n[ТАЙМЕР] Время вышло! Нажмите Enter, чтобы увидеть результат.");
                    return;
                }

                long remaining = timer.remainingSeconds();
                if (timer.elapsedSeconds() > 0
                        && remaining != lastNotified
                        && (remaining == 60 || remaining == 30 || remaining <= 10)) {
                    System.out.printf("%n[ТАЙМЕР] Осталось %s.%n", GameTimer.format(remaining));
                    lastNotified = remaining;
                }

                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "game-timer");
        monitor.setDaemon(true);
        monitor.start();
        return monitor;
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
