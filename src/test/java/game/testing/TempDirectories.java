package game.testing;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Временные каталоги для тестов. Каталоги создаются внутри {@code target/},
 * поэтому не засоряют системный temp и удаляются вместе со сборкой.
 *
 * <p>Удаление выполняется с несколькими попытками: на Windows файл может быть
 * ещё занят антивирусом или индексатором сразу после записи. Ошибка очистки не
 * должна ронять тест, поэтому последняя попытка просто оставляет каталог в
 * {@code target/}.</p>
 */
public final class TempDirectories {
    private static final Path BASE = Path.of("target", "test-temp");
    private static final int DELETE_ATTEMPTS = 5;

    private TempDirectories() {
    }

    public static Path create(String prefix) {
        try {
            Files.createDirectories(BASE);
            return Files.createTempDirectory(BASE, prefix + "-");
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось создать временный каталог для теста.", e);
        }
    }

    public static void deleteQuietly(Path directory) {
        if (directory == null) return;

        for (int attempt = 1; attempt <= DELETE_ATTEMPTS; attempt++) {
            try {
                deleteContents(directory);
                Files.deleteIfExists(directory);
                return;
            } catch (IOException e) {
                if (attempt == DELETE_ATTEMPTS) {
                    return;
                }
                sleepBeforeRetry(attempt);
            }
        }
    }

    private static void deleteContents(Path directory) throws IOException {
        if (!Files.exists(directory)) return;

        try (Stream<Path> paths = Files.walk(directory)) {
            List<Path> ordered = paths.sorted(Comparator.reverseOrder()).toList();
            for (Path path : ordered) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(50L * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
