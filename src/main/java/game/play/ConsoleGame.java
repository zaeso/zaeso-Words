package game.play;

import game.config.GameConfig;
import game.model.Placement;
import game.model.Puzzle;
import game.result.GameResult;
import game.result.ResultRepository;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

public class ConsoleGame {
    private static final Duration DEFAULT_TIME_LIMIT = Duration.ofMinutes(5);
    private static final int DEFAULT_HINTS = 2;
    private static final Duration HINT_PENALTY = Duration.ofSeconds(30);
    private static final int LEADERBOARD_SIZE = 5;
    private static final Pattern CYRILLIC_WORD = Pattern.compile("[А-ЯЁ]+");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Puzzle puzzle;
    private final Scanner scanner;
    private final GameConfig config;
    private final String difficultyName;
    private final ResultRepository resultRepository;
    private final Set<String> found = new HashSet<>();
    private final Set<String> hintedWords = new HashSet<>();
    private final GameTimer timer;
    private int hintsUsed;

    public ConsoleGame(Puzzle puzzle, Scanner scanner) {
        this(puzzle, scanner, defaultConfig(puzzle), "Обычный", ResultRepository.defaultRepository());
    }

    public ConsoleGame(Puzzle puzzle, Scanner scanner, Duration timeLimit) {
        this(puzzle, scanner, defaultConfig(puzzle, timeLimit), "Пользовательская",
                ResultRepository.defaultRepository());
    }

    /**
     * Создаёт игру с параметрами выбранного уровня сложности.
     */
    public ConsoleGame(Puzzle puzzle, Scanner scanner, GameConfig config) {
        this(puzzle, scanner, config, "Пользовательская", ResultRepository.defaultRepository());
    }

    /**
     * Создаёт игру с параметрами сложности и её названием для таблицы рекордов.
     */
    public ConsoleGame(Puzzle puzzle, Scanner scanner, GameConfig config, String difficultyName) {
        this(puzzle, scanner, config, difficultyName, ResultRepository.defaultRepository());
    }

    ConsoleGame(Puzzle puzzle, Scanner scanner, GameConfig config,
                String difficultyName, ResultRepository resultRepository) {
        this.puzzle = Objects.requireNonNull(puzzle, "puzzle");
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.config = Objects.requireNonNull(config, "config");
        this.difficultyName = Objects.requireNonNull(difficultyName, "difficultyName");
        this.resultRepository = Objects.requireNonNull(resultRepository, "resultRepository");
        this.timer = new GameTimer(config.timeLimit());
    }

    public void start() {
        Set<String> allWords = new LinkedHashSet<>(puzzle.getWords());
        int maxWordLength = allWords.stream().mapToInt(String::length).max().orElse(0);
        System.out.println("\nНайди спрятанные слова. Вводи слово целиком.");
        System.out.println("На игру отведено " + GameTimer.format(timer.getLimitSeconds()) + ".");
        if (config.hints() > 0) {
            System.out.println("Доступно подсказок: " + config.hints()
                    + ". HINT или ПОДСКАЗКА открывает букву и направление за "
                    + GameTimer.format(HINT_PENALTY.toSeconds()) + ".");
        }
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

                System.out.printf("%nСлово (осталось %s; HINT — подсказка, EXIT — выход): ",
                        GameTimer.format(timer.remainingSeconds()));
                String answer = scanner.nextLine().trim().toUpperCase(Locale.ROOT);

                if (timer.isExpired()) {
                    timeExpired = true;
                    break;
                }

                if (answer.equals("EXIT")) break;
                if (answer.equals("HINT") || answer.equals("ПОДСКАЗКА")) {
                    useHint();
                    if (timer.isExpired()) {
                        timeExpired = true;
                        break;
                    }
                    continue;
                }
                if (answer.isEmpty()) {
                    System.out.println("Пустой ввод. Введите слово целиком.");
                } else if (!CYRILLIC_WORD.matcher(answer).matches()) {
                    System.out.println("Используйте только русские буквы.");
                } else if (answer.length() > maxWordLength) {
                    System.out.println("Слово слишком длинное.");
                } else if (!allWords.contains(answer)) {
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

        saveAndShowResults(allWords, timeExpired);
    }

    /**
     * Открывает одну подсказку: длину, первую букву и направление слова.
     * За подсказку списывается время; повторно за то же слово штраф не берётся.
     */
    private void useHint() {
        if (config.hints() == 0) {
            System.out.println("В этом режиме подсказки недоступны.");
            return;
        }
        if (hintsUsed >= config.hints()) {
            System.out.println("Лимит подсказок исчерпан: " + config.hints() + " из " + config.hints() + ".");
            return;
        }

        Optional<Placement> candidate = puzzle.getPlacements().stream()
                .filter(placement -> !found.contains(placement.word()))
                .filter(placement -> !hintedWords.contains(placement.word()))
                .max(Comparator.comparingInt(placement -> placement.word().length()));

        if (candidate.isEmpty()) {
            System.out.println("Для оставшихся слов подсказки уже выданы.");
            return;
        }

        Placement placement = candidate.get();
        hintedWords.add(placement.word());
        hintsUsed++;
        timer.penalize(HINT_PENALTY);

        System.out.printf("Подсказка %d из %d (минус %s): слово из %d букв начинается с «%c», направление — %s.%n",
                hintsUsed,
                config.hints(),
                GameTimer.format(HINT_PENALTY.toSeconds()),
                placement.word().length(),
                placement.word().charAt(0),
                placement.direction().description());
        System.out.println("Осталось времени: " + GameTimer.format(timer.remainingSeconds()) + ".");
    }

    private void saveAndShowResults(Set<String> allWords, boolean timeExpired) {
        int height = puzzle.getGrid().length;
        int width = puzzle.getGrid()[0].length;
        String reason;
        if (found.size() == allWords.size()) {
            reason = "все слова найдены";
        } else if (timeExpired) {
            reason = "время вышло";
        } else {
            reason = "выход игрока";
        }

        GameResult result = new GameResult(
                Instant.now().getEpochSecond(),
                difficultyName,
                height,
                width,
                found.size(),
                allWords.size(),
                timer.elapsedSeconds(),
                hintsUsed,
                reason
        );

        try {
            resultRepository.save(result);
        } catch (IOException e) {
            System.out.println("Не удалось сохранить результат: " + e.getMessage());
        }

        printLeaderboard(height, width);
    }

    private void printLeaderboard(int height, int width) {
        List<GameResult> top = resultRepository.top(height, width, difficultyName, LEADERBOARD_SIZE);
        System.out.println("\nЛучшие результаты — " + difficultyName + ", " + height + "x" + width + ":");
        if (top.isEmpty()) {
            System.out.println("Пока нет сохранённых игр.");
            return;
        }

        for (int i = 0; i < top.size(); i++) {
            GameResult result = top.get(i);
            String date = Instant.ofEpochSecond(result.finishedAtEpochSeconds())
                    .atZone(ZoneId.systemDefault())
                    .format(DATE_FORMAT);
            System.out.printf("%2d. %d/%d (%d%%), время %s, подсказок: %d, %s%n",
                    i + 1,
                    result.foundWords(),
                    result.totalWords(),
                    result.completionPercent(),
                    GameTimer.format(result.elapsedSeconds()),
                    result.hintsUsed(),
                    date);
        }
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

    private static GameConfig defaultConfig(Puzzle puzzle) {
        return defaultConfig(puzzle, DEFAULT_TIME_LIMIT);
    }

    private static GameConfig defaultConfig(Puzzle puzzle, Duration timeLimit) {
        int height = puzzle.getGrid().length;
        int width = puzzle.getGrid()[0].length;
        int minWords = Math.max(1, puzzle.getWords().size());
        return new GameConfig(height, width, minWords, timeLimit, DEFAULT_HINTS);
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
