package game.config;

import game.dictionary.WordCategory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Настройки, выбранные игроком перед генерацией: сложность, категория,
 * размер сетки и длительность раунда.
 *
 * <p>Запись проверяет параметры до генерации, поэтому интерфейс может показать
 * ошибку, не запуская игру. Число слов и подсказки по-прежнему берутся из
 * профиля сложности.</p>
 */
public final class GameSettings {
    public static final int MIN_MINUTES = 1;
    public static final int MAX_MINUTES = 60;

    /** Все допустимые размеры сетки. */
    public static final List<BoardSize> BOARD_SIZES = Collections.unmodifiableList(Arrays.asList(
            new BoardSize(10, 10),
            new BoardSize(11, 10),
            new BoardSize(10, 11),
            new BoardSize(12, 10),
            new BoardSize(10, 12)
    ));

    private final Difficulty difficulty;
    private final WordCategory category;
    private final int height;
    private final int width;
    private final Duration timeLimit;

    public GameSettings(Difficulty difficulty, WordCategory category,
                        int height, int width, Duration timeLimit) {
        Objects.requireNonNull(difficulty, "difficulty");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(timeLimit, "timeLimit");

        if (!isAllowedSize(height, width)) {
            throw new IllegalArgumentException("Недопустимый размер сетки: " + height + "×" + width + ".");
        }
        long minutes = timeLimit.toMinutes();
        if (minutes < MIN_MINUTES || minutes > MAX_MINUTES) {
            throw new IllegalArgumentException("Длительность должна быть от "
                    + MIN_MINUTES + " до " + MAX_MINUTES + " минут.");
        }
        this.difficulty = difficulty;
        this.category = category;
        this.height = height;
        this.width = width;
        this.timeLimit = timeLimit;
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public WordCategory category() {
        return category;
    }

    public int height() {
        return height;
    }

    public int width() {
        return width;
    }

    public Duration timeLimit() {
        return timeLimit;
    }

    /** Настройки по умолчанию: обычная сложность и первая категория. */
    public static GameSettings defaults() {
        Difficulty difficulty = Difficulty.NORMAL;
        GameConfig profile = difficulty.config();
        return new GameSettings(difficulty, WordCategory.defaultCategory(),
                profile.height(), profile.width(), profile.timeLimit());
    }

    public static boolean isAllowedSize(int height, int width) {
        return BOARD_SIZES.contains(new BoardSize(height, width));
    }

    /** Параметры, которые получают генератор и таймер. */
    public GameConfig toConfig() {
        GameConfig profile = difficulty.config();
        return new GameConfig(height, width, profile.minWords(), timeLimit, profile.hints());
    }

    /**
     * Проверяет, что в категории достаточно слов для выбранной сетки.
     *
     * @return текст ошибки или {@code null}, если параметры подходят
     */
    public String validationError() {
        int maxLength = Math.max(height, width);
        long suitable = category.suitableWordCount(maxLength);
        int required = difficulty.config().minWords();
        if (suitable < required) {
            return "В категории «" + category.displayName() + "» только " + suitable
                    + " слов длиной до " + maxLength + ", а для сложности «"
                    + difficulty.displayName() + "» нужно " + required + ".";
        }
        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof GameSettings)) return false;
        GameSettings settings = (GameSettings) other;
        return height == settings.height
                && width == settings.width
                && difficulty == settings.difficulty
                && category == settings.category
                && timeLimit.equals(settings.timeLimit);
    }

    @Override
    public int hashCode() {
        int result = difficulty.hashCode();
        result = 31 * result + category.hashCode();
        result = 31 * result + height;
        result = 31 * result + width;
        result = 31 * result + timeLimit.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "GameSettings[difficulty=" + difficulty + ", category=" + category
                + ", height=" + height + ", width=" + width + ", timeLimit=" + timeLimit + "]";
    }

    /** Размер сетки, доступный для выбора. */
    public static final class BoardSize {
        private final int height;
        private final int width;

        public BoardSize(int height, int width) {
            this.height = height;
            this.width = width;
        }

        public int height() {
            return height;
        }

        public int width() {
            return width;
        }

        @Override
        public String toString() {
            return height + "×" + width;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof BoardSize)) return false;
            BoardSize size = (BoardSize) other;
            return height == size.height && width == size.width;
        }

        @Override
        public int hashCode() {
            return 31 * height + width;
        }
    }
}
