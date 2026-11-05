package game.score;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LetterPointsTest {

    @Test
    void commonLettersCostLessThanRareOnes() {
        assertEquals(LetterPoints.COMMON, LetterPoints.pointsFor('О'));
        assertEquals(LetterPoints.RARE, LetterPoints.pointsFor('Ж'));
        assertEquals(LetterPoints.VERY_RARE, LetterPoints.pointsFor('Ё'));
        assertTrue(LetterPoints.pointsFor('Ё') > LetterPoints.pointsFor('О'));
    }

    @Test
    void ignoresLetterCase() {
        assertEquals(LetterPoints.pointsFor('О'), LetterPoints.pointsFor('о'));
        assertEquals(LetterPoints.pointsFor('Ё'), LetterPoints.pointsFor('ё'));
    }

    @Test
    void sumsLetterPointsForWord() {
        // К + О + Т = 15 + 10 + 10
        assertEquals(35, LetterPoints.pointsFor("КОТ"));
        // Ё + Ж = 30 + 25
        assertEquals(55, LetterPoints.pointsFor("ЁЖ"));
    }

    @Test
    void rareWordScoresMoreThanCommonWordOfSameLength() {
        assertTrue(LetterPoints.pointsFor("ЁЖ") > LetterPoints.pointsFor("ОО"));
    }

    @Test
    void rejectsNonRussianLetters() {
        assertThrows(IllegalArgumentException.class, () -> LetterPoints.pointsFor('A'));
        assertThrows(IllegalArgumentException.class, () -> LetterPoints.pointsFor("КОТ-1"));
        assertThrows(NullPointerException.class, () -> LetterPoints.pointsFor((String) null));
    }
}
