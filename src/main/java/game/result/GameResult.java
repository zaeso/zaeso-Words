package game.result;

import game.score.Score;

import java.util.Objects;

/**
 * Завершённая игровая сессия. Хранится локально и участвует в таблице рекордов.
 * Время завершения хранится в секундах Unix, счёт может отсутствовать у старых
 * записей.
 */
public final class GameResult {
    private final long finishedAtEpochSeconds;
    private final String difficulty;
    private final int height;
    private final int width;
    private final int foundWords;
    private final int totalWords;
    private final long elapsedSeconds;
    private final int hintsUsed;
    private final Score score;
    private final String finishReason;

    public GameResult(long finishedAtEpochSeconds, String difficulty, int height, int width,
                      int foundWords, int totalWords, long elapsedSeconds, int hintsUsed,
                      Score score, String finishReason) {
        this.finishedAtEpochSeconds = finishedAtEpochSeconds;
        this.difficulty = difficulty;
        this.height = height;
        this.width = width;
        this.foundWords = foundWords;
        this.totalWords = totalWords;
        this.elapsedSeconds = elapsedSeconds;
        this.hintsUsed = hintsUsed;
        this.score = score;
        this.finishReason = finishReason;
    }

    public GameResult(long finishedAtEpochSeconds, String difficulty, int height, int width,
                      int foundWords, int totalWords, long elapsedSeconds, int hintsUsed,
                      String finishReason) {
        this(finishedAtEpochSeconds, difficulty, height, width, foundWords, totalWords,
                elapsedSeconds, hintsUsed, null, finishReason);
    }

    /** Конструктор для десериализации JSON. */
    public GameResult() {
        this(0, "", 0, 0, 0, 0, 0, 0, null, "");
    }

    public long finishedAtEpochSeconds() {
        return finishedAtEpochSeconds;
    }

    public String difficulty() {
        return difficulty;
    }

    public int height() {
        return height;
    }

    public int width() {
        return width;
    }

    public int foundWords() {
        return foundWords;
    }

    public int totalWords() {
        return totalWords;
    }

    public long elapsedSeconds() {
        return elapsedSeconds;
    }

    public int hintsUsed() {
        return hintsUsed;
    }

    public Score score() {
        return score;
    }

    public String finishReason() {
        return finishReason;
    }

    public boolean completed() {
        return foundWords == totalWords;
    }

    public int completionPercent() {
        if (totalWords == 0) return 0;
        return (int) Math.round(foundWords * 100.0 / totalWords);
    }

    public int totalScore() {
        return score == null ? 0 : score.total();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof GameResult)) return false;
        GameResult result = (GameResult) other;
        return finishedAtEpochSeconds == result.finishedAtEpochSeconds
                && height == result.height
                && width == result.width
                && foundWords == result.foundWords
                && totalWords == result.totalWords
                && elapsedSeconds == result.elapsedSeconds
                && hintsUsed == result.hintsUsed
                && Objects.equals(difficulty, result.difficulty)
                && Objects.equals(score, result.score)
                && Objects.equals(finishReason, result.finishReason);
    }

    @Override
    public int hashCode() {
        int result = (int) (finishedAtEpochSeconds ^ (finishedAtEpochSeconds >>> 32));
        result = 31 * result + (difficulty == null ? 0 : difficulty.hashCode());
        result = 31 * result + height;
        result = 31 * result + width;
        result = 31 * result + foundWords;
        result = 31 * result + totalWords;
        result = 31 * result + (int) (elapsedSeconds ^ (elapsedSeconds >>> 32));
        result = 31 * result + hintsUsed;
        result = 31 * result + (score == null ? 0 : score.hashCode());
        result = 31 * result + (finishReason == null ? 0 : finishReason.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "GameResult[finishedAtEpochSeconds=" + finishedAtEpochSeconds
                + ", difficulty=" + difficulty + ", height=" + height + ", width=" + width
                + ", foundWords=" + foundWords + ", totalWords=" + totalWords
                + ", elapsedSeconds=" + elapsedSeconds + ", hintsUsed=" + hintsUsed
                + ", score=" + score + ", finishReason=" + finishReason + "]";
    }
}
