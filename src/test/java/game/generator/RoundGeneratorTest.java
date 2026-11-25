package game.generator;

import game.config.Difficulty;
import game.config.GameConfig;
import game.dictionary.WordCategory;
import game.generator.GridGenerator.GenerationResult;
import game.model.Puzzle;
import game.session.GameSession;
import game.session.GameSession.WordResult;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoundGeneratorTest {
    private static final GameConfig CONFIG = Difficulty.NORMAL.config();
    private static final WordCategory CATEGORY = WordCategory.defaultCategory();

    @Test
    void englishRoundsUseOneAlphabetFromGridToScore() {
        WordCategory category = WordCategory.fromInput("english");
        for (long seed = 0; seed < 12; seed++) {
            GenerationResult round = RoundGenerator.generate(
                    Difficulty.HARD.config(), category, seed);
            assertTrue(round.ok(), round::error);
            Puzzle puzzle = round.puzzle();
            for (char[] row : puzzle.getGrid()) {
                assertTrue(new String(row).matches("[A-Z]+"));
            }
            assertTrue(
                    puzzle.getWords().stream().allMatch(word -> word.matches("[A-Z]+")));
            GameSession session = new GameSession(
                    puzzle, Difficulty.HARD.config(), category.displayName());
            assertEquals(
                    WordResult.NON_CYRILLIC, session.submitWord("КОТ"));
            assertEquals(
                    WordResult.FOUND,
                    session.submitWord(puzzle.getWords().get(0).toLowerCase(Locale.ROOT)));
            assertTrue(session.score().total() > 0);
        }
    }

    @Test
    void sameSeedProducesIdenticalRound() {
        GenerationResult first = RoundGenerator.generate(CONFIG, CATEGORY, 42L);
        GenerationResult second = RoundGenerator.generate(CONFIG, CATEGORY, 42L);

        assertTrue(first.ok(), first::error);
        assertTrue(second.ok(), second::error);

        Puzzle firstPuzzle = first.puzzle();
        Puzzle secondPuzzle = second.puzzle();
        assertEquals(firstPuzzle.getWords(), secondPuzzle.getWords());
        assertEquals(firstPuzzle.getPlacements(), secondPuzzle.getPlacements());
        for (int row = 0; row < firstPuzzle.getGrid().length; row++) {
            assertArrayEquals(firstPuzzle.getGrid()[row], secondPuzzle.getGrid()[row],
                    "строка " + row + " должна совпадать при одинаковом seed");
        }
    }

    @Test
    void differentSeedsProduceDifferentRounds() {
        GenerationResult first = RoundGenerator.generate(CONFIG, CATEGORY, 1L);
        GenerationResult second = RoundGenerator.generate(CONFIG, CATEGORY, 2L);

        assertTrue(first.ok(), first::error);
        assertTrue(second.ok(), second::error);
        assertNotEquals(first.puzzle().getPlacements(), second.puzzle().getPlacements());
    }
}
