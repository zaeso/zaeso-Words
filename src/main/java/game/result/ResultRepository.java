package game.result;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Локальное хранилище результатов в JSON. Запись выполняется через временный
 * файл и атомарное переименование, чтение повреждённого файла не роняет игру.
 */
public final class ResultRepository {
    private static final String FILE_NAME = "results.json";

    private final Path file;
    private final ObjectMapper mapper;

    public ResultRepository(Path file) {
        this.file = file.toAbsolutePath();
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ResultRepository defaultRepository() {
        String home = System.getProperty("user.home", ".");
        return new ResultRepository(Path.of(home, ".zaeso-words", FILE_NAME));
    }

    public synchronized void save(GameResult result) throws IOException {
        List<GameResult> results = load();
        results.add(result);
        writeAtomically(results);
    }

    public synchronized List<GameResult> load() {
        if (!Files.exists(file)) return new ArrayList<>();

        try {
            GameResult[] stored = mapper.readValue(file.toFile(), GameResult[].class);
            return new ArrayList<>(Arrays.asList(stored));
        } catch (IOException | RuntimeException e) {
            backupCorruptFile();
            return new ArrayList<>();
        }
    }

    /**
     * Лучшие результаты для конкретных размера сетки и сложности: больше
     * очков, затем больше найденных слов, затем меньше затраченное время,
     * затем более ранняя игра. Записи без счёта считаются худшими.
     */
    public List<GameResult> top(int height, int width, String difficulty, int limit) {
        if (limit <= 0) return List.of();

        return load().stream()
                .filter(result -> result.height() == height && result.width() == width)
                .filter(result -> Objects.equals(result.difficulty(), difficulty))
                .sorted(Comparator
                        .comparingInt(GameResult::totalScore).reversed()
                        .thenComparing(Comparator
                                .comparingInt(GameResult::foundWords).reversed())
                        .thenComparingLong(GameResult::elapsedSeconds)
                        .thenComparingLong(GameResult::finishedAtEpochSeconds))
                .limit(limit)
                .toList();
    }

    public Path getFile() {
        return file;
    }

    private void writeAtomically(List<GameResult> results) throws IOException {
        Path parent = file.getParent();
        Files.createDirectories(parent);

        Path temporary = Files.createTempFile(parent, "results-", ".tmp");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), results);
            try {
                Files.move(temporary, file,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private void backupCorruptFile() {
        try {
            Path backup = file.resolveSibling(file.getFileName() + ".corrupt-" + System.currentTimeMillis());
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            // Повреждённый файл не должен мешать продолжить игру.
        }
    }
}
