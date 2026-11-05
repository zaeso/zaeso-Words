package game.score;

/**
 * Разбивка очков за завершённую игру. Все значения — очки, а не время.
 *
 * @param wordPoints    очки за длину найденных слов
 * @param timeBonus     бонус за оставшееся время
 * @param mistakePenalty штраф за ошибочные попытки
 * @param hintPenalty   штраф за использованные подсказки
 */
public record Score(int wordPoints, int timeBonus, int mistakePenalty, int hintPenalty) {

    public int total() {
        return Math.max(0, wordPoints + timeBonus - mistakePenalty - hintPenalty);
    }
}
