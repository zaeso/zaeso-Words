package game.result;

import game.score.Score;
import game.testing.TempDirectories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultCsvExporterTest {

    private Path tempDir;

    @BeforeEach
    void createTempDir() {
        tempDir = TempDirectories.create("result-csv");
    }

    @AfterEach
    void deleteTempDir() {
        TempDirectories.deleteQuietly(tempDir);
    }

    @Test
    void exportsHistoryWithHeaderAndAllColumns() {
        GameResult result = new GameResult(1_700_000_000L, "Обычный", 10, 12,
                8, 10, 125, 2, new Score(500, 20, 5, 10), "все слова найдены");

        String csv = ResultCsvExporter.toCsv(List.of(result));
        List<String> lines = csv.lines().toList();

        assertEquals(2, lines.size());
        assertEquals(ResultCsvExporter.HEADER, lines.get(0).substring(1));
        String[] fields = lines.get(1).split(";", -1);
        assertTrue(fields[0].matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                () -> "Неверная дата: " + fields[0]);
        assertEquals("Обычный", fields[1]);
        assertEquals("10x12", fields[2]);
        assertEquals("8", fields[3]);
        assertEquals("10", fields[4]);
        assertEquals("80", fields[5]);
        assertEquals("125", fields[6]);
        assertEquals("2", fields[7]);
        assertEquals("505", fields[8]);
        assertEquals("все слова найдены", fields[9]);
    }

    @Test
    void escapesSeparatorsAndQuotes() {
        GameResult result = new GameResult(1L, "Обычный", 10, 10,
                1, 1, 1, 0, new Score(10, 0, 0, 0), "причина; с \"кавычками\"");

        String csv = ResultCsvExporter.toCsv(List.of(result));

        assertTrue(csv.contains("\"причина; с \"\"кавычками\"\"\""),
                () -> "Поле с разделителем и кавычками должно быть экранировано:\n" + csv);
    }

    @Test
    void writesUtf8BomForSpreadsheetEditors() throws IOException {
        Path file = tempDir.resolve("history.csv");
        GameResult result = new GameResult(1L, "Обычный", 10, 10,
                1, 1, 1, 0, new Score(10, 0, 0, 0), "все слова найдены");

        ResultCsvExporter.write(file, List.of(result));

        byte[] bytes = Files.readAllBytes(file);
        assertArrayEquals(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF},
                Arrays.copyOf(bytes, 3));
        String text = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(text.contains("Обычный"));
    }
}
