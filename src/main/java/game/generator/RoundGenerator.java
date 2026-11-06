package game.generator;

import game.config.GameConfig;
import game.dictionary.WordCategory;
import game.generator.GridGenerator.GenerationResult;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Готовит один игровой раунд: выбирает слова из категории и размещает их на
 * сетке. Оба шага используют один и тот же {@link Random}, поэтому seed
 * полностью определяет получившуюся головоломку.
 */
public final class RoundGenerator {
    private RoundGenerator() {
    }

    /** Генерирует раунд со случайным seed. */
    public static GenerationResult generate(GameConfig config, WordCategory category) {
        return generate(config, category, new Random().nextLong());
    }

    /**
     * Генерирует раунд с воспроизводимым seed. Одинаковые параметры и seed
     * дают одинаковую выборку слов и одинаковую сетку.
     */
    public static GenerationResult generate(GameConfig config, WordCategory category, long seed) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(category, "category");

        Random random = new Random(seed);
        int maxLength = Math.max(config.height(), config.width());
        int sampleSize = Math.max(config.minWords() * 4, 30);
        List<String> pool = category.sampleWords(maxLength, sampleSize, random);

        return new GridGenerator(random).generateChecked(
                config.height(), config.width(), pool, config.minWords());
    }
}
