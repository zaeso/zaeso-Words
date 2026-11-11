package game;

import game.config.Difficulty;
import game.dictionary.WordCategory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @Test
    void acceptsOnlyAllowedBoardSizes() {
        assertTrue(Main.isAllowedSize(10, 10));
        assertTrue(Main.isAllowedSize(11, 10));
        assertTrue(Main.isAllowedSize(10, 11));
        assertTrue(Main.isAllowedSize(12, 10));
        assertTrue(Main.isAllowedSize(10, 12));

        assertFalse(Main.isAllowedSize(9, 10));
        assertFalse(Main.isAllowedSize(10, 13));
        assertFalse(Main.isAllowedSize(-10, 10));
    }

    @Test
    void repeatsBoardSizeAfterInvalidCombination() {
        int[][] size = new int[1][];

        String output = captureOutput(() ->
                size[0] = Main.chooseBoardSize(scanner("5\n5\n10\n10\n")));

        assertArrayEquals(new int[]{10, 10}, size[0], output);
        assertTrue(output.contains("Недопустимый размер 5x5."), output);
        assertTrue(output.contains("Допустимые размеры:"), output);
    }

    @Test
    void repeatsDimensionAfterNonNumericInput() {
        Integer[] value = new Integer[1];

        String output = captureOutput(() ->
                value[0] = Main.chooseDimension("Размер: ", scanner("abc\n10\n")));

        assertTrue(value[0] == 10, output);
        assertTrue(output.contains("Ожидается целое число."), output);
    }

    @Test
    void repeatsDimensionAfterBlankInput() {
        Integer[] value = new Integer[1];

        String output = captureOutput(() ->
                value[0] = Main.chooseDimension("Размер: ", scanner("\n12\n")));

        assertTrue(value[0] == 12, output);
        assertTrue(output.contains("Введите число."), output);
    }

    @Test
    void treatsMinusOneAsValueAndRepeatsBoardSize() {
        int[][] size = new int[1][];

        String output = captureOutput(() ->
                size[0] = Main.chooseBoardSize(scanner("-1\n10\n10\n10\n")));

        assertArrayEquals(new int[]{10, 10}, size[0], output);
        assertTrue(output.contains("Недопустимый размер -1x10."), output);
    }

    @Test
    void stopsWhenInputEnds() {
        int[][] size = new int[1][];

        String output = captureOutput(() ->
                size[0] = Main.chooseBoardSize(scanner("")));

        assertNull(size[0], output);
        assertTrue(output.contains("Ввод завершён."), output);
    }

    @Test
    void choosesDifficultyByNumber() {
        Difficulty[] chosen = new Difficulty[1];

        String output = captureOutput(() -> chosen[0] = Main.chooseDifficulty(scanner("1\n")));

        assertEquals(Difficulty.EASY, chosen[0], output);
    }

    @Test
    void choosesDifficultyByNameInAnyCase() {
        Difficulty[] chosen = new Difficulty[1];

        String output = captureOutput(() -> chosen[0] = Main.chooseDifficulty(scanner("СЛОЖНЫЙ\n")));

        assertEquals(Difficulty.HARD, chosen[0], output);
    }

    @Test
    void choosesNormalDifficultyOnBlankInput() {
        Difficulty[] chosen = new Difficulty[1];

        String output = captureOutput(() -> chosen[0] = Main.chooseDifficulty(scanner("\n")));

        assertEquals(Difficulty.NORMAL, chosen[0], output);
    }

    @Test
    void repeatsDifficultyAfterUnknownInput() {
        Difficulty[] chosen = new Difficulty[1];

        String output = captureOutput(() -> chosen[0] = Main.chooseDifficulty(scanner("unknown\n3\n")));

        assertEquals(Difficulty.HARD, chosen[0], output);
        assertTrue(output.contains("Неизвестная сложность."), output);
    }

    @Test
    void stopsDifficultyChoiceWhenInputEnds() {
        Difficulty[] chosen = new Difficulty[1];

        String output = captureOutput(() -> chosen[0] = Main.chooseDifficulty(scanner("")));

        assertNull(chosen[0], output);
        assertTrue(output.contains("Ввод завершён."), output);
    }

    @Test
    void usesBoardSizeFromDifficultyProfileOnBlankInput() {
        int[][] size = new int[1][];

        String output = captureOutput(() ->
                size[0] = Main.chooseBoardSize(scanner("\n\n"), Difficulty.HARD.config()));

        assertArrayEquals(new int[]{12, 10}, size[0], output);
    }

    @Test
    void choosesCategoryByNumber() {
        WordCategory[] chosen = new WordCategory[1];

        String output = captureOutput(() -> chosen[0] = Main.chooseCategory(scanner("2\n")));

        assertEquals(WordCategory.all().get(1), chosen[0], output);
    }

    @Test
    void choosesCategoryByNameInAnyCase() {
        WordCategory expected = WordCategory.all().get(2);
        WordCategory[] chosen = new WordCategory[1];

        String output = captureOutput(() ->
                chosen[0] = Main.chooseCategory(scanner(expected.displayName().toUpperCase() + "\n")));

        assertEquals(expected, chosen[0], output);
    }

    @Test
    void choosesDefaultCategoryOnBlankInput() {
        WordCategory[] chosen = new WordCategory[1];

        String output = captureOutput(() -> chosen[0] = Main.chooseCategory(scanner("\n")));

        assertEquals(WordCategory.defaultCategory(), chosen[0], output);
    }

    @Test
    void repeatsCategoryAfterUnknownInput() {
        WordCategory expected = WordCategory.all().get(1);
        WordCategory[] chosen = new WordCategory[1];

        String output = captureOutput(() ->
                chosen[0] = Main.chooseCategory(scanner("unknown\n" + expected.displayName() + "\n")));

        assertEquals(expected, chosen[0], output);
        assertTrue(output.contains("Неизвестная категория."), output);
    }

    @Test
    void stopsCategoryChoiceWhenInputEnds() {
        WordCategory[] chosen = new WordCategory[1];

        String output = captureOutput(() -> chosen[0] = Main.chooseCategory(scanner("")));

        assertNull(chosen[0], output);
        assertTrue(output.contains("Ввод завершён."), output);
    }

    @Test
    void choosesDurationFromSavedDefaultOnBlankInput() {
        Duration[] chosen = new Duration[1];

        String output = captureOutput(() ->
                chosen[0] = Main.chooseDuration(scanner("\n"), Duration.ofMinutes(9)));

        assertEquals(Duration.ofMinutes(9), chosen[0], output);
    }

    @Test
    void repeatsDurationAfterInvalidInput() {
        Duration[] chosen = new Duration[1];

        String output = captureOutput(() ->
                chosen[0] = Main.chooseDuration(scanner("0\nabc\n61\n15\n"), Duration.ofMinutes(5)));

        assertEquals(Duration.ofMinutes(15), chosen[0], output);
        assertTrue(output.contains("Введите целое число минут от 1 до 60."), output);
    }

    @Test
    void stopsDurationChoiceWhenInputEnds() {
        Duration[] chosen = new Duration[1];

        String output = captureOutput(() ->
                chosen[0] = Main.chooseDuration(scanner(""), Duration.ofMinutes(5)));

        assertNull(chosen[0], output);
        assertTrue(output.contains("Ввод завершён."), output);
    }

    @Test
    void usesSavedDefaultsForCategoryAndDifficulty() {
        WordCategory category = WordCategory.all().get(2);
        WordCategory[] chosenCategory = new WordCategory[1];
        Difficulty[] chosenDifficulty = new Difficulty[1];

        String output = captureOutput(() -> {
            chosenCategory[0] = Main.chooseCategory(scanner("\n"), category);
            chosenDifficulty[0] = Main.chooseDifficulty(scanner("\n"), Difficulty.HARD);
        });

        assertEquals(category, chosenCategory[0], output);
        assertEquals(Difficulty.HARD, chosenDifficulty[0], output);
        assertTrue(output.contains("Enter — " + category.displayName()), output);
        assertTrue(output.contains("Enter — сложный"), output);
    }

    private static Scanner scanner(String input) {
        return new Scanner(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                "UTF-8"
        );
    }

    private static String captureOutput(Runnable action) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream captureStream;
        try {
            captureStream = new PrintStream(captured, true, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
        System.setOut(captureStream);
        try {
            action.run();
        } finally {
            System.setOut(originalOut);
            captureStream.flush();
        }
        return new String(captured.toByteArray(), StandardCharsets.UTF_8);
    }
}
