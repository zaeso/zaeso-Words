package game.play;

import game.config.GameConfig;
import game.model.Puzzle;
import game.result.GameResult;
import game.result.ResultRepository;
import game.score.Score;
import game.session.GameSession;
import game.session.GameSession.FinishReason;
import game.session.GameSession.HintResult;
import game.session.GameSession.HintStatus;
import game.session.GameSession.WordResult;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

/**
 * Отладочный консольный интерфейс. Игровая логика находится в
 * {@link GameSession} и используется также графическим интерфейсом.
 */
public class ConsoleGame {
    private static final Duration DEFAULT_TIME_LIMIT = Duration.ofMinutes(5);
    private static final int DEFAULT_HINTS = 2;
    private static final int LEADERBOARD_SIZE = 5;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Scanner scanner;
    private final GameSession session;
    private final ResultRepository resultRepository;

    public ConsoleGame(Puzzle puzzle, Scanner scanner) {
        this(puzzle, scanner, defaultConfig(puzzle), "Обычный", ResultRepository.defaultRepository());
    }

    public ConsoleGame(Puzzle puzzle, Scanner scanner, Duration timeLimit) {
        this(puzzle, scanner, defaultConfig(puzzle, timeLimit), "Пользовательская",
                ResultRepository.defaultRepository());
    }

    public ConsoleGame(Puzzle puzzle, Scanner scanner, GameConfig config) {
        this(puzzle, scanner, config, "Пользовательская", ResultRepository.defaultRepository());
    }

    public ConsoleGame(Puzzle puzzle, Scanner scanner, GameConfig config, String difficultyName) {
        this(puzzle, scanner, config, difficultyName, ResultRepository.defaultRepository());
    }

    ConsoleGame(Puzzle puzzle, Scanner scanner, GameConfig config,
                String difficultyName, ResultRepository resultRepository) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.resultRepository = Objects.requireNonNull(resultRepository, "resultRepository");
        this.session = new GameSession(puzzle, config, difficultyName);
    }

    public void start() {
        Set<String> allWords = new LinkedHashSet<>(session.allWords());
        System.out.println("\nНайди спрятанные слова. Вводи слово целиком.");
        System.out.println("На игру отведено " + GameTimer.format(session.limitSeconds()) + ".");
        if (session.hintsLimit() > 0) {
            System.out.println("Доступно подсказок: " + session.hintsLimit()
                    + ". HINT или ПОДСКАЗКА открывает букву и направление за "
                    + GameTimer.format(GameSession.HINT_PENALTY.toSeconds()) + ".");
        }
        printGrid();
        printWordList(allWords);

        session.start();
        Thread monitor = startTimeMonitor();
        boolean timeExpired = false;

        try {
            while (!session.completed()) {
                if (session.isExpired()) {
                    timeExpired = true;
                    break;
                }

                System.out.printf("%nСлово (осталось %s; HINT — подсказка, EXIT — выход): ",
                        GameTimer.format(session.remainingSeconds()));
                String answer = scanner.nextLine();

                if (session.isExpired()) {
                    timeExpired = true;
                    break;
                }

                if (session.isExitCommand(answer)) break;
                if (session.isHintCommand(answer)) {
                    useHint();
                    if (session.isExpired()) {
                        timeExpired = true;
                        break;
                    }
                    continue;
                }

                switch (session.submitWord(answer)) {
                    case EMPTY -> System.out.println("Пустой ввод. Введите слово целиком.");
                    case NON_CYRILLIC -> System.out.println("Используйте только русские буквы.");
                    case TOO_LONG -> System.out.println("Слово слишком длинное.");
                    case NOT_IN_LIST -> System.out.println("Такого слова нет в списке.");
                    case ALREADY_FOUND -> System.out.println("Это слово уже найдено.");
                    case FOUND -> System.out.println("Верно! Найдено: "
                            + session.foundCount() + " из " + session.totalWords());
                }
            }
        } finally {
            session.stop();
            monitor.interrupt();
        }

        System.out.println("\nИгра завершена. Результат: "
                + session.foundCount() + " из " + session.totalWords());
        System.out.println("Затраченное время: " + GameTimer.format(session.elapsedSeconds()) + ".");
        if (timeExpired) {
            System.out.println("Причина завершения: истекло время.");
        }
        System.out.println("Оставшиеся слова: ");
        session.remainingWords().forEach(System.out::println);

        FinishReason reason = session.completed()
                ? FinishReason.ALL_WORDS_FOUND
                : timeExpired ? FinishReason.TIME_EXPIRED : FinishReason.PLAYER_EXIT;
        saveAndShowResults(session.finish(reason));
    }

    /**
     * Открывает одну подсказку: длину, первую букву и направление слова.
     * За подсказку списывается время; повторно за то же слово штраф не берётся.
     */
    private void useHint() {
        HintResult hint = session.useHint();
        if (hint.status() == HintStatus.DISABLED) {
            System.out.println("В этом режиме подсказки недоступны.");
            return;
        }
        if (hint.status() == HintStatus.LIMIT_REACHED) {
            System.out.println("Лимит подсказок исчерпан: "
                    + hint.limit() + " из " + hint.limit() + ".");
            return;
        }
        if (hint.status() == HintStatus.NO_CANDIDATES) {
            System.out.println("Для оставшихся слов подсказки уже выданы.");
            return;
        }

        var placement = hint.placement();
        System.out.printf("Подсказка %d из %d (минус %s): слово из %d букв начинается с «%c», направление — %s.%n",
                hint.used(),
                hint.limit(),
                GameTimer.format(GameSession.HINT_PENALTY.toSeconds()),
                placement.word().length(),
                placement.word().charAt(0),
                placement.direction().description());
        System.out.println("Осталось времени: " + GameTimer.format(session.remainingSeconds()) + ".");
    }

    private void saveAndShowResults(GameResult result) {
        printScore(result);

        try {
            resultRepository.save(result);
        } catch (IOException e) {
            System.out.println("Не удалось сохранить результат: " + e.getMessage());
        }

        printLeaderboard(result.height(), result.width());
    }

    private void printScore(GameResult result) {
        Score score = result.score();
        System.out.println("\nСчёт: " + result.totalScore());
        System.out.println("  Буквы: +" + score.wordPoints());
        System.out.println("  Время: +" + score.timeBonus());
        System.out.println("  Ошибки: -" + score.mistakePenalty());
        System.out.println("  Подсказки: -" + score.hintPenalty());
    }

    private void printLeaderboard(int height, int width) {
        List<GameResult> top = resultRepository.top(
                height, width, session.difficultyName(), LEADERBOARD_SIZE);
        System.out.println("\nЛучшие результаты — " + session.difficultyName()
                + ", " + height + "x" + width + ":");
        if (top.isEmpty()) {
            System.out.println("Пока нет сохранённых игр.");
            return;
        }

        for (int i = 0; i < top.size(); i++) {
            GameResult result = top.get(i);
            String date = Instant.ofEpochSecond(result.finishedAtEpochSeconds())
                    .atZone(ZoneId.systemDefault())
                    .format(DATE_FORMAT);
            System.out.printf("%2d. %d/%d (%d%%), очки: %d, время %s, подсказок: %d, %s%n",
                    i + 1,
                    result.foundWords(),
                    result.totalWords(),
                    result.completionPercent(),
                    result.totalScore(),
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
            while (session.isRunning()) {
                if (session.isExpired()) {
                    System.out.println("\n[ТАЙМЕР] Время вышло! Нажмите Enter, чтобы увидеть результат.");
                    return;
                }

                long remaining = session.remainingSeconds();
                if (session.elapsedSeconds() > 0
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
        char[][] grid = session.grid();
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
            System.out.print((session.isFound(word) ? "[+] " : "[ ] ") + word + "   ");
        }
        System.out.println();
    }
}
