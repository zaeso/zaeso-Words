package game.config;

import game.dictionary.WordCategory;
import game.testing.TempDirectories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SettingsStoreTest {

    private Path tempDir;

    @BeforeEach
    void createTempDir() {
        tempDir = TempDirectories.create("settings-store");
    }

    @AfterEach
    void deleteTempDir() {
        TempDirectories.deleteQuietly(tempDir);
    }

    @Test
    void returnsDefaultsWhenFileDoesNotExist() {
        SettingsStore store = new SettingsStore(tempDir.resolve("missing.properties"));

        GameSettings settings = store.load();

        assertEquals(GameSettings.defaults(), settings);
    }

    @Test
    void savesAndReloadsAllParameters() throws IOException {
        Path file = tempDir.resolve("nested").resolve("settings.properties");
        GameSettings expected = new GameSettings(Difficulty.EASY,
                WordCategory.all().get(2), 11, 10, Duration.ofMinutes(9));

        new SettingsStore(file).save(expected);

        assertEquals(expected, new SettingsStore(file).load());
    }

    @Test
    void survivesCorruptFile() throws IOException {
        Path file = tempDir.resolve("settings.properties");
        Files.write(file, "difficulty=\\uZZZZ".getBytes(StandardCharsets.UTF_8));

        assertEquals(GameSettings.defaults(), new SettingsStore(file).load());
    }

    @Test
    void fallsBackToDifficultyProfileForInvalidFields() throws IOException {
        Path file = tempDir.resolve("settings.properties");
        writeUtf8(file, "difficulty=HARD\n"
                + "category=unknown\n"
                + "height=999\n"
                + "width=3\n"
                + "minutes=999\n");

        GameSettings settings = new SettingsStore(file).load();

        assertEquals(Difficulty.HARD, settings.difficulty());
        assertEquals(WordCategory.defaultCategory(), settings.category());
        assertEquals(12, settings.height());
        assertEquals(10, settings.width());
        assertEquals(Duration.ofMinutes(4), settings.timeLimit());
    }

    @Test
    void defaultStoreUsesUserHome() {
        Path file = SettingsStore.defaultStore().getFile();

        assertEquals(Paths.get(System.getProperty("user.home"), ".zaeso-words", "settings.properties"),
                file);
    }

    private static void writeUtf8(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }
}
