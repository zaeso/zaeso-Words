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
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiSettingsTest {

    private Path tempDir;

    @BeforeEach
    void createTempDir() {
        tempDir = TempDirectories.create("ui-settings");
    }

    @AfterEach
    void deleteTempDir() {
        TempDirectories.deleteQuietly(tempDir);
    }

    @Test
    void defaultsUseClassicThemeWithSoundAndAnimations() {
        UiSettings defaults = UiSettings.defaults();

        assertEquals(ThemeChoice.CLASSIC, defaults.theme());
        assertTrue(defaults.soundEnabled());
        assertTrue(defaults.animationsEnabled());
    }

    @Test
    void missingFileUsesDefaults() {
        SettingsStore store = new SettingsStore(tempDir.resolve("missing.properties"));

        assertEquals(UiSettings.defaults(), store.loadUi());
    }

    @Test
    void savesAndReloadsUiPreferences() throws IOException {
        Path file = tempDir.resolve("settings.properties");
        UiSettings expected = new UiSettings(ThemeChoice.DARK, false, false);

        new SettingsStore(file).saveUi(expected);

        assertEquals(expected, new SettingsStore(file).loadUi());
    }

    @Test
    void uiPreferencesDoNotEraseGameSettings() throws IOException {
        Path file = tempDir.resolve("settings.properties");
        GameSettings game = new GameSettings(Difficulty.HARD,
                WordCategory.all().get(1), 12, 10, Duration.ofMinutes(9));

        SettingsStore store = new SettingsStore(file);
        store.save(game);
        store.saveUi(new UiSettings(ThemeChoice.OCEAN, false, true));

        assertEquals(game, store.load());
        assertEquals(new UiSettings(ThemeChoice.OCEAN, false, true), store.loadUi());
    }

    @Test
    void gameSettingsDoNotEraseUiPreferences() throws IOException {
        Path file = tempDir.resolve("settings.properties");
        UiSettings ui = new UiSettings(ThemeChoice.FOREST, false, false);

        SettingsStore store = new SettingsStore(file);
        store.saveUi(ui);
        store.save(GameSettings.defaults());

        assertEquals(ui, store.loadUi());
    }

    @Test
    void invalidValuesFallBackToDefaults() throws IOException {
        Path file = tempDir.resolve("settings.properties");
        Files.writeString(file, """
                theme=unknown
                sound=maybe
                animations=42
                """, StandardCharsets.UTF_8);

        assertEquals(UiSettings.defaults(), new SettingsStore(file).loadUi());
    }

    @Test
    void parsesThemeByEnumNameAndDisplayName() {
        assertEquals(ThemeChoice.OCEAN, ThemeChoice.fromInput("OCEAN"));
        assertEquals(ThemeChoice.OCEAN, ThemeChoice.fromInput("океан"));
        assertEquals(ThemeChoice.DARK, ThemeChoice.fromInput(" Тёмная "));
        assertNull(ThemeChoice.fromInput("нет такой темы"));
    }
}
