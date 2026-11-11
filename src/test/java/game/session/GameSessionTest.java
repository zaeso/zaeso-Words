package game.session;

import game.config.GameConfig;
import game.model.Direction;
import game.model.Placement;
import game.model.Puzzle;
import game.result.GameResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionTest {

    @Test
    void acceptsOnlyListedWordsAndCountsMistakes() {
        GameSession session = session(2);

        assertEquals(GameSession.WordResult.EMPTY, session.submitWord(" "));
        assertEquals(GameSession.WordResult.NON_CYRILLIC, session.submitWord("cat"));
        assertEquals(GameSession.WordResult.TOO_LONG, session.submitWord("коткот"));
        assertEquals(GameSession.WordResult.NOT_IN_LIST, session.submitWord("лес"));
        assertEquals(1, session.mistakes());

        assertEquals(GameSession.WordResult.FOUND, session.submitWord("кот"));
        assertEquals(GameSession.WordResult.ALREADY_FOUND, session.submitWord("кот"));
        assertEquals(2, session.mistakes());
        assertEquals(1, session.foundCount());
    }

    @Test
    void recognizesCommands() {
        GameSession session = session(2);

        assertTrue(session.isHintCommand("hint"));
        assertTrue(session.isHintCommand("ПОДСКАЗКА"));
        assertTrue(session.isExitCommand("exit"));
        assertFalse(session.isHintCommand("слово"));
    }

    @Test
    void hintCostsTimeAndDoesNotRepeatForSameWord() {
        GameSession session = session(5);

        GameSession.HintResult first = session.useHint();
        assertTrue(first.given());
        assertEquals(1, first.used());
        assertEquals(1, session.hintsUsed());
        assertEquals(270, session.remainingSeconds());

        GameSession.HintResult second = session.useHint();
        assertTrue(second.given());
        assertEquals(2, session.hintsUsed());
        assertFalse(second.placement().word().equals(first.placement().word()));

        GameSession.HintResult third = session.useHint();
        assertEquals(GameSession.HintStatus.NO_CANDIDATES, third.status());
        assertEquals(2, session.hintsUsed());
    }

    @Test
    void respectsHintLimit() {
        GameSession session = session(2);

        assertTrue(session.useHint().given());
        assertTrue(session.useHint().given());
        assertEquals(GameSession.HintStatus.LIMIT_REACHED, session.useHint().status());
        assertEquals(2, session.hintsUsed());
    }

    @Test
    void reportsDisabledHints() {
        GameSession session = session(0);

        assertEquals(GameSession.HintStatus.DISABLED, session.useHint().status());
    }

    @Test
    void finishCreatesResultWithScore() {
        GameSession session = session(2);
        session.start();
        session.submitWord("кот");
        session.submitWord("дом");

        GameResult result = session.finish(GameSession.FinishReason.ALL_WORDS_FOUND);

        assertTrue(session.completed());
        assertTrue(result.completed());
        assertNotNull(result.score());
        assertEquals(2, result.foundWords());
        assertEquals("Тест", result.difficulty());
        assertEquals("все слова найдены", result.finishReason());
        assertThrows(IllegalStateException.class,
                () -> session.finish(GameSession.FinishReason.PLAYER_EXIT));
    }

    private static GameSession session(int hints) {
        Puzzle puzzle = new Puzzle(
                new char[][]{{'К', 'О', 'Т'}, {'Д', 'О', 'М'}},
                Arrays.asList(
                        new Placement("КОТ", 0, 0, new Direction(0, 1)),
                        new Placement("ДОМ", 1, 0, new Direction(0, 1))
                )
        );
        GameConfig config = new GameConfig(2, 3, 1, Duration.ofMinutes(5), hints);
        return new GameSession(puzzle, config, "Тест");
    }
}
