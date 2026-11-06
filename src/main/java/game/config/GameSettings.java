package game.config;

import game.dictionary.WordCategory;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Настройки, выбранные игроком перед генерацией: сложность, категория,
 * размер сетки и длительность раунда.
 *
 * <p>Запись проверяет параметры до генерации, поэтому интерфейс может показать
 * ошибку, не запуская игру. Число слов и подсказки по-прежнему берутся из
 * профиля сложности.</p>
 *
 * @param difficulty выбранный уровень сложности
 * @param category   выбранная категория слов
 * @param height     высота сетки
 * @param width      ширина сетки
 * @param timeLimit  длительность раунда
 */
public record GameSettings(Difficulty difficulty, WordCategory category,
                           int height, int width, Duration timeLimit) {

    public static final int MIN_MINUTES = 1;
    public static final int MAX_MINUTES = 60;

    /** Размер сетки, доступный для выбора. */
    public record BoardSize(int height, int width) {
        @Override
        public String toString() {
            return height + "×" + width;
        }
    }

    /** Все допустимые размеры сетки. */
    public static final List<BoardSize> BOARD_SIZES = List.of(
            new BoardSize(10, 10),
            new BoardSize(11, 10),
            new BoardSize(10, 11),
            new BoardSize(12, 10),
            new BoardSize(10, 12)
    );

    public GameSettings {
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
        long suitable = category.words().stream()
                .filter(word -> word.length() <= maxLength)
                .distinct()
                .count();
        int required = difficulty.config().minWords();
        if (suitable < required) {
            return "В категории «" + category.displayName() + "» только " + suitable
                    + " слов длиной до " + maxLength + ", а для сложности «"
                    + difficulty.displayName() + "» нужно " + required + ".";
        }
        return null;
    }
}
