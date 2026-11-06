package game.daily;

import game.result.GameResult;
import game.score.Score;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DailyStatisticsTest {
    private static final ZoneId UTC = ZoneOffset.UTC;
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 6);

    @Test
    void emptyHistoryGivesZeroes() {
        assertEquals(DailyStatistics.EMPTY,
                DailyStatistics.from(List.of(), TODAY, UTC));
    }

    @Test
    void ignoresRegularGames() {
        List<GameResult> results = List.of(
                result(TODAY, "Обычный", 500, 10),
                result(TODAY.minusDays(1), "Сложный", 400, 8));

        assertEquals(DailyStatistics.EMPTY, DailyStatistics.from(results, TODAY, UTC));
    }

    @Test
    void calculatesBestScoreWordsAndDaysPlayed() {
        List<GameResult> results = List.of(
                result(TODAY, DailyPuzzle.RESULT_LABEL, 300, 9),
                result(TODAY.minusDays(1), DailyPuzzle.RESULT_LABEL, 250, 7),
                result(TODAY.minusDays(2), DailyPuzzle.RESULT_LABEL, 100, 4));

        DailyStatistics statistics = DailyStatistics.from(results, TODAY, UTC);

        assertEquals(300, statistics.bestScore());
        assertEquals(3, statistics.currentStreak());
        assertEquals(3, statistics.daysPlayed());
        assertEquals(20, statistics.wordsFound());
    }

    @Test
    void streakContinuesFromYesterdayWhenTodayIsNotPlayedYet() {
        List<GameResult> results = List.of(
                result(TODAY.minusDays(1), DailyPuzzle.RESULT_LABEL, 200, 5),
                result(TODAY.minusDays(2), DailyPuzzle.RESULT_LABEL, 150, 4));

        assertEquals(2, DailyStatistics.from(results, TODAY, UTC).currentStreak());
    }

    @Test
    void missedDayBreaksStreak() {
        List<GameResult> results = List.of(
                result(TODAY.minusDays(2), DailyPuzzle.RESULT_LABEL, 200, 5),
                result(TODAY.minusDays(3), DailyPuzzle.RESULT_LABEL, 150, 4));

        assertEquals(0, DailyStatistics.from(results, TODAY, UTC).currentStreak());
    }

    @Test
    void severalGamesOnSameDayCountAsOneDay() {
        List<GameResult> results = List.of(
                result(TODAY, DailyPuzzle.RESULT_LABEL, 200, 5),
                result(TODAY, DailyPuzzle.RESULT_LABEL, 350, 6));

        DailyStatistics statistics = DailyStatistics.from(results, TODAY, UTC);

        assertEquals(350, statistics.bestScore());
        assertEquals(1, statistics.currentStreak());
        assertEquals(1, statistics.daysPlayed());
        assertEquals(11, statistics.wordsFound());
    }

    private static GameResult result(LocalDate date, String difficulty,
                                     int score, int foundWords) {
        long finishedAt = date.atStartOfDay(UTC).toEpochSecond();
        return new GameResult(finishedAt, difficulty, 10, 10, foundWords, 10, 60, 0,
                new Score(score, 0, 0, 0), "все слова найдены");
    }
}
