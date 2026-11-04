package game.play;

import game.config.Difficulty;
import game.config.GameConfig;
import game.model.Direction;
import game.model.Placement;
import game.model.Puzzle;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleGameTest {

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

    private static void assertContains(String expected, String actual) {
        assertTrue(actual.contains(expected),
                () -> "Ожидалось: " + expected + "\nФактический вывод:\n" + actual);
    }

    private static String runGame(String input, Duration timeLimit) {
        return runGame(oneWordPuzzle(), input, timeLimit);
    }

    private static String runGame(Puzzle puzzle, String input, Duration timeLimit) {
        return runGame(puzzle, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), timeLimit);
    }

    private static String runGame(InputStream input, Duration timeLimit) {
        return runGame(oneWordPuzzle(), input, timeLimit);
    }

    private static String runGame(Puzzle puzzle, InputStream input, Duration timeLimit) {
        Scanner scanner = new Scanner(input, StandardCharsets.UTF_8);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream captureStream = new PrintStream(captured, true, StandardCharsets.UTF_8);
        System.setOut(captureStream);
        try {
            new ConsoleGame(puzzle, scanner, timeLimit).start();
        } finally {
            System.setOut(originalOut);
            captureStream.flush();
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    private static String runGame(Puzzle puzzle, String input, GameConfig config) {
        Scanner scanner = new Scanner(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8
        );

        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream captureStream = new PrintStream(captured, true, StandardCharsets.UTF_8);
        System.setOut(captureStream);
        try {
            new ConsoleGame(puzzle, scanner, config).start();
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
