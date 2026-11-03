package game;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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

    private static Scanner scanner(String input) {
        return new Scanner(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8
        );
    }

    private static String captureOutput(Runnable action) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream captureStream = new PrintStream(captured, true, StandardCharsets.UTF_8);
        System.setOut(captureStream);
        try {
            action.run();
        } finally {
            System.setOut(originalOut);
            captureStream.flush();
        }
        return captured.toString(StandardCharsets.UTF_8);
    }
}
