package game.score;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScoreCalculatorTest {

    @Test
    void awardsPointsForEveryFoundLetter() {
        Score score = ScoreCalculator.calculate(List.of("КОТ", "ДОМ"), 3, 0, 0, 0);

        assertEquals(75, score.wordPoints());
        assertEquals(0, score.timeBonus());
        assertEquals(75, score.total());
    }

    @Test
    void addsTimeBonusOnlyWhenAllWordsAreFound() {
        Score incomplete = ScoreCalculator.calculate(List.of("КОТ"), 2, 300, 0, 0);
        Score completed = ScoreCalculator.calculate(List.of("КОТ", "ДОМ"), 2, 300, 0, 0);

        assertEquals(0, incomplete.timeBonus());
        assertEquals(600, completed.timeBonus());
        assertEquals(75 + 600, completed.total());    }

    @Test
    void subtractsMistakesAndHints() {
        Score score = ScoreCalculator.calculate(List.of("КОТ"), 1, 60, 2, 1);

        assertEquals(35, score.wordPoints());
        assertEquals(120, score.timeBonus());
        assertEquals(10, score.mistakePenalty());
        assertEquals(50, score.hintPenalty());
        assertEquals(95, score.total());
    }

    @Test
    void neverReturnsNegativeTotal() {
        Score score = ScoreCalculator.calculate(List.of(), 5, 0, 10, 3);

        assertEquals(0, score.total());
    }

    @Test
    void acceptsEmptyBoardWithoutWords() {
        Score score = ScoreCalculator.calculate(List.of(), 0, 100, 0, 0);

        assertEquals(0, score.wordPoints());
        assertEquals(0, score.timeBonus());
        assertEquals(0, score.total());
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(NullPointerException.class,
                () -> ScoreCalculator.calculate(null, 1, 0, 0, 0));
        assertThrows(NullPointerException.class,
                () -> ScoreCalculator.calculate(java.util.Arrays.asList("КОТ", null), 1, 0, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> ScoreCalculator.calculate(List.of(), -1, 0, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> ScoreCalculator.calculate(List.of(), 1, -1, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> ScoreCalculator.calculate(List.of(), 1, 0, -1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> ScoreCalculator.calculate(List.of(), 1, 0, 0, -1));
    }
}
