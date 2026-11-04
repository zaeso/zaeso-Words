package game.config;

import java.time.Duration;
import java.util.Locale;

/**
 * Уровни сложности игры. Каждый уровень задаёт полный набор параметров, поэтому
 * генератор и таймер не содержат отдельных веток для разных режимов.
 */
public enum Difficulty {
    EASY("Лёгкий", new GameConfig(10, 10, 8, Duration.ofMinutes(7), 3)),
    NORMAL("Обычный", new GameConfig(10, 10, 10, Duration.ofMinutes(5), 2)),
    HARD("Сложный", new GameConfig(12, 10, 12, Duration.ofMinutes(4), 1));

    private final String displayName;
    private final GameConfig config;

    Difficulty(String displayName, GameConfig config) {
        this.displayName = displayName;
        this.config = config;
    }

    public String displayName() {
        return displayName;
    }

    public GameConfig config() {
        return config;
    }

    /**
     * Разбирает номер уровня (1–3) или название в любом регистре.
     * Буква «ё» и «е» считаются взаимозаменяемыми.
     *
     * @return уровень сложности или {@code null}, если ввод не распознан
     */
    public static Difficulty fromInput(String input) {
        if (input == null) return null;

        String normalized = normalize(input);
        if (normalized.isEmpty()) return null;

        for (Difficulty difficulty : values()) {
            if (String.valueOf(difficulty.ordinal() + 1).equals(normalized)
                    || difficulty.name().toLowerCase(Locale.ROOT).equals(normalized)
                    || normalize(difficulty.displayName).equals(normalized)) {
                return difficulty;
            }
        }
        return null;
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replace('ё', 'е');
    }
}
