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
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultRepositoryTest {

    private Path tempDir;

    @BeforeEach
    void createTempDir() {
        tempDir = TempDirectories.create("result-repository");
    }

    @AfterEach
    void deleteTempDir() {
        TempDirectories.deleteQuietly(tempDir);
    }

    @Test
    void createsNestedDirectoryAndPersistsResult() throws IOException {
        Path file = tempDir.resolve("nested").resolve("results.json");
        ResultRepository repository = new ResultRepository(file);

        repository.save(result(1, "Обычный", 10, 10, 8, 10, 120, 1));

        assertTrue(Files.exists(file), "Файл результатов должен быть создан");
        ResultRepository reopened = new ResultRepository(file);
        assertEquals(1, reopened.load().size());
        assertEquals(8, reopened.load().get(0).foundWords());
    }

    @Test
    void appendsResultsAndKeepsExistingOnes() throws IOException {
        ResultRepository repository = new ResultRepository(tempDir.resolve("results.json"));

        repository.save(result(1, "Обычный", 10, 10, 8, 10, 120, 0));
        repository.save(result(2, "Обычный", 10, 10, 10, 10, 60, 1));

        List<GameResult> results = repository.load();
        assertEquals(2, results.size());
        assertEquals(8, results.get(0).foundWords());
        assertEquals(10, results.get(1).foundWords());
    }

    @Test
    void returnsEmptyListWhenFileDoesNotExist() {
        ResultRepository repository = new ResultRepository(tempDir.resolve("missing.json"));

        assertTrue(repository.load().isEmpty());
        assertTrue(repository.top(10, 10, "Обычный", 5).isEmpty());
    }

    @Test
    void ranksByFoundWordsThenTime() throws IOException {
        ResultRepository repository = new ResultRepository(tempDir.resolve("results.json"));
        repository.save(result(1, "Обычный", 10, 10, 5, 10, 100, 0));
        repository.save(result(2, "Обычный", 10, 10, 10, 10, 200, 0));
        repository.save(result(3, "Обычный", 10, 10, 10, 10, 50, 0));
        repository.save(result(4, "Обычный", 12, 10, 10, 10, 10, 0));
        repository.save(result(5, "Сложный", 10, 10, 10, 10, 10, 0));

        List<GameResult> top = repository.top(10, 10, "Обычный", 10);

        assertEquals(3, top.size());
        assertEquals(3, top.get(0).finishedAtEpochSeconds());
        assertEquals(2, top.get(1).finishedAtEpochSeconds());
        assertEquals(1, top.get(2).finishedAtEpochSeconds());
    }

    @Test
    void ranksByScoreBeforeFoundWords() throws IOException {
        ResultRepository repository = new ResultRepository(tempDir.resolve("results.json"));
        repository.save(new GameResult(1, "Обычный", 10, 10, 5, 10, 100, 0,
                new Score(500, 0, 0, 0), "выход игрока"));
        repository.save(new GameResult(2, "Обычный", 10, 10, 10, 10, 200, 0,
                new Score(100, 0, 0, 0), "все слова найдены"));

        List<GameResult> top = repository.top(10, 10, "Обычный", 10);

        assertEquals(1, top.get(0).finishedAtEpochSeconds());
        assertEquals(2, top.get(1).finishedAtEpochSeconds());
    }

    @Test
    void loadsOldRecordsWithoutScore() throws IOException {
        Path file = tempDir.resolve("results.json");
        writeUtf8(file, "[{\n"
                + "  \"finishedAtEpochSeconds\": 1,\n"
                + "  \"difficulty\": \"Обычный\",\n"
                + "  \"height\": 10,\n"
                + "  \"width\": 10,\n"
                + "  \"foundWords\": 3,\n"
                + "  \"totalWords\": 5,\n"
                + "  \"elapsedSeconds\": 60,\n"
                + "  \"hintsUsed\": 1,\n"
                + "  \"finishReason\": \"выход игрока\"\n"
                + "}]");

        GameResult result = new ResultRepository(file).load().get(0);

        assertNull(result.score());
        assertEquals(0, result.totalScore());
    }

    @Test
    void limitsLeaderboardSize() throws IOException {        ResultRepository repository = new ResultRepository(tempDir.resolve("results.json"));
        for (long i = 1; i <= 5; i++) {
            repository.save(result(i, "Обычный", 10, 10, 10, 10, i, 0));
        }

        assertEquals(2, repository.top(10, 10, "Обычный", 2).size());
        assertTrue(repository.top(10, 10, "Обычный", 0).isEmpty());
    }

    @Test
    void historyReturnsEveryMatchingResultWithoutLimit() throws IOException {
        ResultRepository repository = new ResultRepository(tempDir.resolve("results.json"));
        for (long i = 1; i <= 8; i++) {
            repository.save(result(i, "Обычный", 10, 10, 10, 10, i, 0));
        }
        repository.save(result(9, "Другой", 10, 10, 10, 10, 9, 0));

        List<GameResult> history = repository.history(10, 10, "Обычный");

        assertEquals(8, history.size());
        assertEquals(5, repository.top(10, 10, "Обычный", 5).size());
    }

    @Test
    void survivesCorruptFileAndBacksItUp() throws IOException {
        Path file = tempDir.resolve("results.json");
        Files.write(file, "{ это не JSON".getBytes(StandardCharsets.UTF_8));
        ResultRepository repository = new ResultRepository(file);

        assertTrue(repository.load().isEmpty());

        try (Stream<Path> files = Files.list(tempDir)) {
            assertTrue(files.anyMatch(path -> path.getFileName().toString().startsWith("results.json.corrupt-")),
                    "Повреждённый файл должен быть сохранён как резервная копия");
        }

        repository.save(result(1, "Обычный", 10, 10, 1, 1, 5, 0));
        assertEquals(1, repository.load().size());
    }

    @Test
    void savesValidJson() throws IOException {
        Path file = tempDir.resolve("results.json");
        new ResultRepository(file).save(result(1, "Обычный", 10, 10, 1, 1, 5, 0));

        String json = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        assertTrue(json.contains("\"difficulty\""), json);
        assertTrue(json.contains("\"Обычный\""), json);
        assertTrue(json.contains("\"score\""), json);
        assertFalse(json.trim().isEmpty(), "JSON не должен быть пустым");
    }

    @Test
    void defaultRepositoryUsesUserHome() {
        Path file = ResultRepository.defaultRepository().getFile();

        assertTrue(file.endsWith(Paths.get(".zaeso-words", "results.json")), file.toString());
    }

    private static GameResult result(long finishedAt, String difficulty, int height, int width,
                                     int found, int total, long elapsed, int hints) {
        return new GameResult(finishedAt, difficulty, height, width, found, total, elapsed, hints,
                "все слова найдены");
    }

    private static void writeUtf8(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }
}
