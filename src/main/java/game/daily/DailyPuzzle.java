package game.daily;

import game.config.Difficulty;
import game.config.GameConfig;
import game.config.GameSettings;
import game.dictionary.WordCategory;
import game.generator.GridGenerator.GenerationResult;
import game.generator.RoundGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Ежедневная головоломка. Параметры и seed зависят только от даты, поэтому
 * у всех игроков в один день получается одинаковая сетка независимо от
 * сохранённых настроек.
 */
public final class DailyPuzzle {
    /** Отдельное название сложности в истории результатов. */
    public static final String RESULT_LABEL = "Ежедневная";

    private static final Difficulty DIFFICULTY = Difficulty.NORMAL;
    private static final WordCategory CATEGORY = WordCategory.fromInput("Все русские слова");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private DailyPuzzle() {
    }

    public static LocalDate today() {
        return LocalDate.now();
    }

    /** Фиксированные параметры: сложность, словарь, сетка и время. */
    public static GameSettings settings() {
        GameConfig profile = DIFFICULTY.config();
        return new GameSettings(DIFFICULTY, CATEGORY,
                profile.height(), profile.width(), profile.timeLimit());
    }

    /**
     * Seed, однозначно определяемый календарной датой. Перемешивание
     * SplitMix64 равномерно распределяет соседние даты по всему диапазону.
     */
    public static long seed(LocalDate date) {
        Objects.requireNonNull(date, "date");
        long value = date.toEpochDay() + 0x9E3779B97F4A7C15L;
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    /** Генерирует сетку дня. */
    public static GenerationResult generate(LocalDate date) {
        GameSettings settings = settings();
        return RoundGenerator.generate(settings.toConfig(), settings.category(), seed(date));
    }

    public static String formatDate(LocalDate date) {
        return Objects.requireNonNull(date, "date").format(DATE_FORMAT);
    }
}
