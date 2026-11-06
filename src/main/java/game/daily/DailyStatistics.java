package game.daily;

import game.result.GameResult;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Личная статистика по ежедневным головоломкам. Считается из уже сохранённых
 * результатов, поэтому отдельное хранилище не нужно.
 *
 * @param bestScore     лучший счёт за ежедневную игру
 * @param currentStreak сколько дней подряд игрок проходит сетку дня
 * @param daysPlayed    сколько разных дней с ежедневными играми
 * @param wordsFound    сколько слов найдено во всех ежедневных играх
 */
public record DailyStatistics(int bestScore, int currentStreak,
                              int daysPlayed, int wordsFound) {

    public static final DailyStatistics EMPTY = new DailyStatistics(0, 0, 0, 0);

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
                .toList();
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
}
