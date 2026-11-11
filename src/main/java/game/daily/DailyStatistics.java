package game.daily;

import game.result.GameResult;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Личная статистика по ежедневным головоломкам. Считается из уже сохранённых
 * результатов, поэтому отдельное хранилище не нужно.
 */
public final class DailyStatistics {
    public static final DailyStatistics EMPTY = new DailyStatistics(0, 0, 0, 0);

    private final int bestScore;
    private final int currentStreak;
    private final int daysPlayed;
    private final int wordsFound;

    public DailyStatistics(int bestScore, int currentStreak, int daysPlayed, int wordsFound) {
        this.bestScore = bestScore;
        this.currentStreak = currentStreak;
        this.daysPlayed = daysPlayed;
        this.wordsFound = wordsFound;
    }

    public int bestScore() {
        return bestScore;
    }

    public int currentStreak() {
        return currentStreak;
    }

    public int daysPlayed() {
        return daysPlayed;
    }

    public int wordsFound() {
        return wordsFound;
    }

    /** Считает статистику по текущей дате и часовому поясу системы. */
    public static DailyStatistics from(List<GameResult> results) {
        return from(results, LocalDate.now(), ZoneId.systemDefault());
    }

    /**
     * Считает статистику для явно заданных даты и часового пояса. Такой
     * вариант используется в тестах и не зависит от времени запуска.
     */
    public static DailyStatistics from(List<GameResult> results,
                                       LocalDate today, ZoneId zone) {
        Objects.requireNonNull(results, "results");
        Objects.requireNonNull(today, "today");
        Objects.requireNonNull(zone, "zone");

        List<GameResult> daily = results.stream()
                .filter(Objects::nonNull)
                .filter(result -> DailyPuzzle.RESULT_LABEL.equals(result.difficulty()))
                .collect(Collectors.toList());
        if (daily.isEmpty()) {
            return EMPTY;
        }

        int bestScore = daily.stream()
                .mapToInt(GameResult::totalScore)
                .max()
                .orElse(0);
        int wordsFound = daily.stream()
                .mapToInt(GameResult::foundWords)
                .sum();
        TreeSet<LocalDate> days = daily.stream()
                .map(result -> Instant.ofEpochSecond(result.finishedAtEpochSeconds())
                        .atZone(zone)
                        .toLocalDate())
                .collect(Collectors.toCollection(TreeSet::new));

        return new DailyStatistics(bestScore, streak(days, today), days.size(), wordsFound);
    }

    /**
     * Серия продолжается, если сетка пройдена сегодня или вчера. Если
     * последняя игра была раньше, серия уже прервалась.
     */
    private static int streak(TreeSet<LocalDate> days, LocalDate today) {
        LocalDate cursor = days.contains(today) ? today : today.minusDays(1);
        if (!days.contains(cursor)) {
            return 0;
        }

        int streak = 0;
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof DailyStatistics)) return false;
        DailyStatistics statistics = (DailyStatistics) other;
        return bestScore == statistics.bestScore
                && currentStreak == statistics.currentStreak
                && daysPlayed == statistics.daysPlayed
                && wordsFound == statistics.wordsFound;
    }

    @Override
    public int hashCode() {
        int result = bestScore;
        result = 31 * result + currentStreak;
        result = 31 * result + daysPlayed;
        result = 31 * result + wordsFound;
        return result;
    }

    @Override
    public String toString() {
        return "DailyStatistics[bestScore=" + bestScore + ", currentStreak=" + currentStreak
                + ", daysPlayed=" + daysPlayed + ", wordsFound=" + wordsFound + "]";
    }
}
