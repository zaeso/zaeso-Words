package game.config;

import java.time.Duration;

/**
 * Параметры одной игры. Конфигурация не зависит от консоли, поэтому её могут
 * использовать разные интерфейсы и уровни сложности.
 *
 * @param height    высота сетки
 * @param width     ширина сетки
 * @param minWords  минимальное число слов, которое нужно разместить
 * @param timeLimit длительность игры
 * @param hints     число доступных подсказок
 */
public record GameConfig(int height, int width, int minWords, Duration timeLimit, int hints) {

    public GameConfig {
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
    }

    /**
     * Возвращает копию конфигурации с другим размером сетки.
     */
    public GameConfig withBoardSize(int newHeight, int newWidth) {
        return new GameConfig(newHeight, newWidth, minWords, timeLimit, hints);
    }
}
