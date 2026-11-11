package game.config;

import java.time.Duration;
import java.util.Objects;

/**
 * Параметры одной игры. Конфигурация не зависит от консоли, поэтому её могут
 * использовать разные интерфейсы и уровни сложности.
 */
public final class GameConfig {
    private final int height;
    private final int width;
    private final int minWords;
    private final Duration timeLimit;
    private final int hints;

    public GameConfig(int height, int width, int minWords, Duration timeLimit, int hints) {
        if (height < 1) {
            throw new IllegalArgumentException("Высота сетки должна быть положительной.");
        }
        if (width < 1) {
            throw new IllegalArgumentException("Ширина сетки должна быть положительной.");
        }
        if (minWords < 1) {
            throw new IllegalArgumentException("Минимальное число слов должно быть положительным.");
        }
        if (timeLimit == null || timeLimit.isZero() || timeLimit.isNegative()) {
            throw new IllegalArgumentException("Лимит времени должен быть положительным.");
        }
        if (hints < 0) {
            throw new IllegalArgumentException("Число подсказок не может быть отрицательным.");
        }
        this.height = height;
        this.width = width;
        this.minWords = minWords;
        this.timeLimit = timeLimit;
        this.hints = hints;
    }

    public int height() {
        return height;
    }

    public int width() {
        return width;
    }

    public int minWords() {
        return minWords;
    }

    public Duration timeLimit() {
        return timeLimit;
    }

    public int hints() {
        return hints;
    }

    /**
     * Возвращает копию конфигурации с другим размером сетки.
     */
    public GameConfig withBoardSize(int newHeight, int newWidth) {
        return new GameConfig(newHeight, newWidth, minWords, timeLimit, hints);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof GameConfig)) return false;
        GameConfig config = (GameConfig) other;
        return height == config.height
                && width == config.width
                && minWords == config.minWords
                && hints == config.hints
                && timeLimit.equals(config.timeLimit);
    }

    @Override
    public int hashCode() {
        int result = height;
        result = 31 * result + width;
        result = 31 * result + minWords;
        result = 31 * result + timeLimit.hashCode();
        result = 31 * result + hints;
        return result;
    }

    @Override
    public String toString() {
        return "GameConfig[height=" + height + ", width=" + width + ", minWords=" + minWords
                + ", timeLimit=" + timeLimit + ", hints=" + hints + "]";
    }
}
