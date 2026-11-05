package game.score;

import java.util.Collection;
import java.util.Objects;

/**
 * Считает очки по формуле, не зависящей от консоли:
 *
 * <ul>
 *     <li>очки за каждую букву найденного слова по её редкости
 *     (см. {@link LetterPoints});</li>
 *     <li>2 очка за каждую оставшуюся секунду, но только если найдены все слова;</li>
 *     <li>5 очков штрафа за каждую ошибку;</li>
 *     <li>50 очков штрафа за каждую подсказку.</li>
 * </ul>
 *
 * <p>Итог не может быть меньше нуля. Класс не работает с вводом/выводом, поэтому
 * формулу можно проверить отдельно от консольного интерфейса.</p>
 */
public final class ScoreCalculator {
    public static final int POINTS_PER_REMAINING_SECOND = 2;
    public static final int MISTAKE_PENALTY = 5;
    public static final int HINT_PENALTY = 50;

    private ScoreCalculator() {
    }

    /**
     * Рассчитывает очки завершённой игры.
     *
     * @param foundWords      найденные слова
     * @param totalWords      общее число слов в головоломке
     * @param remainingSeconds оставшееся время
     * @param mistakes        число ошибочных попыток
     * @param hintsUsed       число использованных подсказок
     */
    public static Score calculate(Collection<String> foundWords, int totalWords,
                                  long remainingSeconds, int mistakes, int hintsUsed) {
        Objects.requireNonNull(foundWords, "foundWords");
        if (totalWords < 0) {
            throw new IllegalArgumentException("Общее число слов не может быть отрицательным.");
        }
        if (remainingSeconds < 0) {
            throw new IllegalArgumentException("Оставшееся время не может быть отрицательным.");
        }
        if (mistakes < 0) {
            throw new IllegalArgumentException("Число ошибок не может быть отрицательным.");
        }
        if (hintsUsed < 0) {
            throw new IllegalArgumentException("Число подсказок не может быть отрицательным.");
        }

        int wordPoints = 0;
        for (String word : foundWords) {
            wordPoints += LetterPoints.pointsFor(Objects.requireNonNull(word, "word"));
        }

        boolean completed = totalWords > 0 && foundWords.size() >= totalWords;
        int timeBonus = completed
                ? (int) Math.min(Integer.MAX_VALUE, remainingSeconds * POINTS_PER_REMAINING_SECOND)
                : 0;

        return new Score(
                wordPoints,
                timeBonus,
                mistakes * MISTAKE_PENALTY,
                hintsUsed * HINT_PENALTY
        );
    }
}
