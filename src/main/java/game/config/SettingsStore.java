package game.config;

import game.dictionary.WordCategory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;

/**
 * Локальное хранилище выбранных настроек в properties-файле. Значения по
 * умолчанию переживают перезапуск игры, а повреждённый или устаревший файл
 * не мешает запуску: для некорректных полей берутся значения профиля.
 *
 * <p>Параметры игры и оформления лежат в одном файле, но сохраняются
 * независимо: запись одной группы не стирает ключи другой.</p>
 */
public final class SettingsStore {
    private static final String FILE_NAME = "settings.properties";
    private static final String THEME = "theme";
    private static final String SOUND = "sound";
    private static final String ANIMATIONS = "animations";

    private final Path file;

    public SettingsStore(Path file) {
        this.file = Objects.requireNonNull(file, "file").toAbsolutePath();
    }

    public static SettingsStore defaultStore() {
        String home = System.getProperty("user.home", ".");
        return new SettingsStore(Paths.get(home, ".zaeso-words", FILE_NAME));
    }

    public Path getFile() {
        return file;
    }

    public GameSettings load() {
        Properties properties = readProperties();

        Difficulty difficulty = Difficulty.fromInput(properties.getProperty("difficulty"));
        if (difficulty == null) {
            difficulty = Difficulty.NORMAL;
        }
        GameConfig profile = difficulty.config();

        WordCategory category = WordCategory.fromInput(properties.getProperty("category"));
        if (category == null) {
            category = WordCategory.defaultCategory();
        }

        int height = intProperty(properties, "height", profile.height());
        int width = intProperty(properties, "width", profile.width());
        if (!GameSettings.isAllowedSize(height, width)) {
            height = profile.height();
            width = profile.width();
        }

        int minutes = intProperty(properties, "minutes",
                (int) profile.timeLimit().toMinutes());
        if (minutes < GameSettings.MIN_MINUTES || minutes > GameSettings.MAX_MINUTES) {
            minutes = (int) profile.timeLimit().toMinutes();
        }

        return new GameSettings(difficulty, category, height, width,
                Duration.ofMinutes(minutes));
    }

    public UiSettings loadUi() {
        Properties properties = readProperties();
        ThemeChoice theme = ThemeChoice.fromInput(properties.getProperty(THEME));
        if (theme == null) {
            theme = ThemeChoice.CLASSIC;
        }
        boolean sound = booleanProperty(properties, SOUND, true);
        boolean animations = booleanProperty(properties, ANIMATIONS, true);
        return new UiSettings(theme, sound, animations);
    }

    public void save(GameSettings settings) throws IOException {
        Objects.requireNonNull(settings, "settings");

        Properties properties = readProperties();
        properties.setProperty("difficulty", settings.difficulty().name());
        properties.setProperty("category", settings.category().fileName());
        properties.setProperty("height", String.valueOf(settings.height()));
        properties.setProperty("width", String.valueOf(settings.width()));
        properties.setProperty("minutes",
                String.valueOf(settings.timeLimit().toMinutes()));
        write(properties);
    }

    public void saveUi(UiSettings settings) throws IOException {
        Objects.requireNonNull(settings, "settings");

        Properties properties = readProperties();
        properties.setProperty(THEME, settings.theme().name());
        properties.setProperty(SOUND, String.valueOf(settings.soundEnabled()));
        properties.setProperty(ANIMATIONS, String.valueOf(settings.animationsEnabled()));
        write(properties);
    }

    private Properties readProperties() {
        Properties properties = new Properties();
        if (!Files.isRegularFile(file)) {
            return properties;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException | RuntimeException e) {
            properties.clear();
        }
        return properties;
    }

    private void write(Properties properties) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path temporary = Files.createTempFile(parent, "settings-", ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            properties.store(writer, "zaeso-Words settings");
        }

        try {
            Files.move(temporary, file,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static int intProperty(Properties properties, String key, int fallback) {
        String value = properties.getProperty(key);
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static boolean booleanProperty(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key);
        if (value == null) return fallback;
        String normalized = value.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalized.equals("true")) return true;
        if (normalized.equals("false")) return false;
        return fallback;
    }
}
