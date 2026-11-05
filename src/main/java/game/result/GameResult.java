package game.result;

import game.score.Score;

/**
 * Завершённая игровая сессия. Хранится локально и участвует в таблице рекордов.
 *
 * @param finishedAtEpochSeconds время завершения в секундах Unix
 * @param difficulty название уровня сложности
 * @param height высота сетки
 * @param width ширина сетки
 * @param foundWords сколько слов найдено
 * @param totalWords сколько слов было в головоломке
 * @param elapsedSeconds фактически затраченное время без штрафов за подсказки
 * @param hintsUsed сколько подсказок использовано
 * @param score итоговый счёт и его разбивка; {@code null} для старых записей
 * @param finishReason причина завершения
 */
public record GameResult(
        long finishedAtEpochSeconds,
        String difficulty,
        int height,
        int width,
        int foundWords,
        int totalWords,
        long elapsedSeconds,
        int hintsUsed,
        Score score,
        String finishReason
) {
    /**
     * Конструктор для записей без счёта, например для старых JSON-файлов и тестов.
     */
    public GameResult(long finishedAtEpochSeconds, String difficulty, int height, int width,
                      int foundWords, int totalWords, long elapsedSeconds, int hintsUsed,
                      String finishReason) {
        this(finishedAtEpochSeconds, difficulty, height, width, foundWords, totalWords,
                elapsedSeconds, hintsUsed, null, finishReason);
    }

    public boolean completed() {
        return foundWords == totalWords;
    }

    public int completionPercent() {
        if (totalWords == 0) return 0;
        return (int) Math.round(foundWords * 100.0 / totalWords);
    }

    /**
     * Итоговый счёт или {@code 0}, если запись сделана до появления очков.
     */
    public int totalScore() {
        return score == null ? 0 : score.total();
    }
}
