package game.score;

/**
 * Разбивка очков за завершённую игру. Все значения — очки, а не время.
 */
public final class Score {
    private final int wordPoints;
    private final int timeBonus;
    private final int mistakePenalty;
    private final int hintPenalty;

    public Score(int wordPoints, int timeBonus, int mistakePenalty, int hintPenalty) {
        this.wordPoints = wordPoints;
        this.timeBonus = timeBonus;
        this.mistakePenalty = mistakePenalty;
        this.hintPenalty = hintPenalty;
    }

    /** Конструктор для десериализации JSON. */
    public Score() {
        this(0, 0, 0, 0);
    }

    public int wordPoints() {
        return wordPoints;
    }

    public int timeBonus() {
        return timeBonus;
    }

    public int mistakePenalty() {
        return mistakePenalty;
    }

    public int hintPenalty() {
        return hintPenalty;
    }

    public int total() {
        return Math.max(0, wordPoints + timeBonus - mistakePenalty - hintPenalty);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Score)) return false;
        Score score = (Score) other;
        return wordPoints == score.wordPoints
                && timeBonus == score.timeBonus
                && mistakePenalty == score.mistakePenalty
                && hintPenalty == score.hintPenalty;
    }

    @Override
    public int hashCode() {
        int result = wordPoints;
        result = 31 * result + timeBonus;
        result = 31 * result + mistakePenalty;
        result = 31 * result + hintPenalty;
        return result;
    }

    @Override
    public String toString() {
        return "Score[wordPoints=" + wordPoints + ", timeBonus=" + timeBonus
                + ", mistakePenalty=" + mistakePenalty + ", hintPenalty=" + hintPenalty + "]";
    }
}
