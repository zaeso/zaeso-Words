package game.play;

import game.config.Difficulty;
import game.config.GameConfig;
import game.model.Direction;
import game.model.Placement;
import game.model.Puzzle;
import game.result.GameResult;
import game.result.ResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleGameTest {

    @TempDir
    Path tempDir;

    private ResultRepository repository() {
        return new ResultRepository(tempDir.resolve("results.json"));
    }

    @Test
    void acceptsCorrectWordAndShowsResult() {
        String output = runGame("кот\n", Duration.ofMinutes(1));

        assertContains("На игру отведено 01:00.", output);
        assertContains("Верно! Найдено: 1 из 1", output);
        assertContains("Игра завершена. Результат: 1 из 1", output);
        assertContains("Затраченное время:", output);
        assertFalse(output.contains("Причина завершения: истекло время."));
    }

    @Test
    void usesTimeLimitFromGameConfig() {
        String output = runGame(oneWordPuzzle(), "exit\n", Difficulty.HARD.config());

        assertContains("На игру отведено 04:00.", output);
    }

    @Test
    void endsGameWhenTimeExpires() {
        String output = runGame(delayedInput(200), Duration.ofMillis(100));

        assertContains("Игра завершена. Результат: 0 из 1", output);
        assertContains("Причина завершения: истекло время.", output);
    }

    @Test
    void rejectsEmptyInputAndContinues() {
        String output = runGame("\nкот\n", Duration.ofMinutes(1));

        assertContains("Пустой ввод. Введите слово целиком.", output);
        assertContains("Верно! Найдено: 1 из 1", output);
    }

    @Test
    void rejectsNonRussianInputAndContinues() {
        String output = runGame("cat\nкот\n", Duration.ofMinutes(1));

        assertContains("Используйте только русские буквы.", output);
        assertContains("Верно! Найдено: 1 из 1", output);
    }

    @Test
    void rejectsTooLongWordAndContinues() {
        String output = runGame("коткот\nкот\n", Duration.ofMinutes(1));

        assertContains("Слово слишком длинное.", output);
        assertContains("Верно! Найдено: 1 из 1", output);
    }

    @Test
    void acceptsExitInAnyCase() {
        String output = runGame("exit\n", Duration.ofMinutes(1));

        assertContains("Игра завершена. Результат: 0 из 1", output);
        assertFalse(output.contains("Верно!"));
    }

    @Test
    void rejectsDuplicateWord() {
        Puzzle puzzle = new Puzzle(
                new char[][]{{'К', 'О', 'Т'}, {'Д', 'О', 'М'}},
                List.of(
                        new Placement("КОТ", 0, 0, new Direction(0, 1)),
                        new Placement("ДОМ", 1, 0, new Direction(0, 1))
                )
        );

        String output = runGame(puzzle, "кот\nкот\nдом\n", Duration.ofMinutes(1));

        assertContains("Это слово уже найдено.", output);
        assertContains("Игра завершена. Результат: 2 из 2", output);
    }

    @Test
    void hintRevealsFirstLetterAndDirectionWithoutTheWholeWord() {
        String output = runGame(oneWordPuzzle(), "hint\nкот\n", hintConfig(2));

        assertContains("Подсказка 1 из 2", output);
        assertContains("слово из 3 букв начинается с «К»", output);
        assertContains("направление — вправо", output);
        assertFalse(output.contains("начинается с «К», направление — вправо. КОТ"), output);
        assertContains("Верно! Найдено: 1 из 1", output);
    }

    @Test
    void hintAcceptsRussianCommandAndCostsTime() {
        String output = runGame(oneWordPuzzle(), "ПОДСКАЗКА\nкот\n", hintConfig(2));

        assertContains("(минус 00:30)", output);
        assertContains("Осталось времени: 04:30.", output);
    }

    @Test
    void doesNotChargeTwiceForTheSameWord() {
        String output = runGame(oneWordPuzzle(), "hint\nhint\nкот\n", hintConfig(2));

        assertContains("Для оставшихся слов подсказки уже выданы.", output);
        assertContains("Подсказка 1 из 2", output);
        assertFalse(output.contains("Подсказка 2 из 2"), output);
    }

    @Test
    void stopsAtHintLimit() {
        Puzzle puzzle = twoWordPuzzle();

        String output = runGame(puzzle, "hint\nhint\nhint\nкот\nдом\n", hintConfig(1));

        assertContains("Подсказка 1 из 1", output);
        assertContains("Лимит подсказок исчерпан: 1 из 1.", output);
    }

    @Test
    void reportsUnavailableHints() {
        String output = runGame(oneWordPuzzle(), "hint\nкот\n", hintConfig(0));

        assertContains("В этом режиме подсказки недоступны.", output);
        assertFalse(output.contains("Доступно подсказок"), output);
    }

    @Test
    void savesFinishedGame() throws IOException {
        runGame(oneWordPuzzle(), "кот\n", hintConfig(2));

        List<GameResult> saved = repository().load();
        assertEquals(1, saved.size());
        GameResult result = saved.get(0);
        assertEquals("Тест", result.difficulty());
        assertEquals(1, result.height());
        assertEquals(3, result.width());
        assertEquals(1, result.foundWords());
        assertEquals(1, result.totalWords());
        assertEquals(0, result.hintsUsed());
        assertTrue(result.completed());
        assertEquals("все слова найдены", result.finishReason());
    }

    @Test
    void storesHintUsageInSavedResult() throws IOException {
        runGame(oneWordPuzzle(), "hint\nкот\n", hintConfig(2));

        GameResult result = repository().load().get(0);
        assertEquals(1, result.hintsUsed());
    }

    @Test
    void showsLeaderboardForBoardAndDifficulty() throws IOException {
        ResultRepository repository = repository();
        repository.save(new GameResult(1, "Тест", 1, 3, 1, 10, 10, 0, "все слова найдены"));
        repository.save(new GameResult(2, "Тест", 1, 3, 0, 1, 100, 2, "время вышло"));
        repository.save(new GameResult(3, "Другой", 1, 3, 1, 1, 1, 0, "все слова найдены"));

        String output = runGame("exit\n", hintConfig(2), repository, "Тест");

        assertContains("Лучшие результаты — Тест, 1x3:", output);
        assertContains("время 00:10", output);
        assertContains("время 01:40", output);
        assertTrue(output.indexOf("время 00:10") < output.indexOf("время 01:40"),
                () -> "Таблица рекордов должна быть отсортирована:\n" + output);
        assertFalse(output.contains("время 00:01"), output);
    }

    @Test
    void keepsPlayingWhenResultCannotBeSaved() throws IOException {
        Path blocked = tempDir.resolve("not-a-directory");
        java.nio.file.Files.writeString(blocked, "занято");
        ResultRepository broken = new ResultRepository(blocked.resolve("results.json"));

        String output = runGame(oneWordPuzzle(), "кот\n", hintConfig(2), broken, "Тест");

        assertContains("Не удалось сохранить результат:", output);
        assertContains("Игра завершена. Результат: 1 из 1", output);
    }

    private static void assertContains(String expected, String actual) {
        assertTrue(actual.contains(expected),
                () -> "Ожидалось: " + expected + "\nФактический вывод:\n" + actual);
    }

    private static GameConfig hintConfig(int hints) {
        return new GameConfig(1, 3, 1, Duration.ofMinutes(5), hints);
    }

    private String runGame(String input, Duration timeLimit) {
        return runGame(oneWordPuzzle(), input, timeLimit);
    }

    private String runGame(Puzzle puzzle, String input, Duration timeLimit) {
        return runGame(puzzle, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), timeLimit);
    }

    private String runGame(InputStream input, Duration timeLimit) {
        return runGame(oneWordPuzzle(), input, timeLimit);
    }

    private String runGame(Puzzle puzzle, InputStream input, Duration timeLimit) {
        GameConfig config = new GameConfig(
                puzzle.getGrid().length,
                puzzle.getGrid()[0].length,
                Math.max(1, puzzle.getWords().size()),
                timeLimit,
                2
        );
        return runGame(puzzle, input, config, repository(), "Тест");
    }

    private String runGame(Puzzle puzzle, String input, GameConfig config) {
        return runGame(puzzle, input, config, repository(), "Тест");
    }

    private String runGame(String input, GameConfig config, ResultRepository repository, String difficultyName) {
        return runGame(oneWordPuzzle(), input, config, repository, difficultyName);
    }

    private String runGame(Puzzle puzzle, String input, GameConfig config,
                           ResultRepository repository, String difficultyName) {
        return runGame(puzzle,
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                config,
                repository,
                difficultyName);
    }

    private String runGame(Puzzle puzzle, InputStream input, GameConfig config,
                           ResultRepository repository, String difficultyName) {
        Scanner scanner = new Scanner(input, StandardCharsets.UTF_8);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream captureStream = new PrintStream(captured, true, StandardCharsets.UTF_8);
        System.setOut(captureStream);
        try {
            new ConsoleGame(puzzle, scanner, config, difficultyName, repository).start();
        } finally {
            System.setOut(originalOut);
            captureStream.flush();
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    private static Puzzle oneWordPuzzle() {
        return new Puzzle(
                new char[][]{{'К', 'О', 'Т'}},
                List.of(new Placement("КОТ", 0, 0, new Direction(0, 1)))
        );
    }

    private static Puzzle twoWordPuzzle() {
        return new Puzzle(
                new char[][]{{'К', 'О', 'Т'}, {'Д', 'О', 'М'}},
                List.of(
                        new Placement("КОТ", 0, 0, new Direction(0, 1)),
                        new Placement("ДОМ", 1, 0, new Direction(0, 1))
                )
        );
    }

    private static InputStream delayedInput(long delayMillis) {
        return new InputStream() {
            private final long startedAt = System.nanoTime();
            private boolean sent;

            @Override
            public int read() throws IOException {
                if (sent) return -1;

                long remaining = delayMillis - (System.nanoTime() - startedAt) / 1_000_000L;
                if (remaining > 0) {
                    try {
                        Thread.sleep(remaining);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException(e);
                    }
                }
                sent = true;
                return '\n';
            }
        };
    }
}
