package game.dictionary;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordCategoryTest {

    @Test
    void loadsCategoriesFromResourceIndex() {
        List<WordCategory> categories = WordCategory.all();

        assertTrue(categories.size() >= 6);
        assertEquals("Технологии", WordCategory.defaultCategory().displayName());
        assertTrue(WordCategory.all().stream()
                .anyMatch(category -> category.displayName().equals("Животные")));
        assertTrue(WordCategory.all().stream()
                .anyMatch(category -> category.displayName().equals("Все русские слова")
                        && category.streamed()));
    }

    @Test
    void everyCategoryHasEnoughShortWordsForHardGrid() {
        for (WordCategory category : WordCategory.all()) {
            long usable = category.suitableWordCount(12);

            assertTrue(usable >= 12,
                    () -> "Категория " + category.displayName() + " содержит только " + usable + " слов");
        }
    }

    @Test
    void samplesRandomWordsFromCompleteRussianDictionary() {
        WordCategory all = WordCategory.all().stream()
                .filter(WordCategory::streamed)
                .findFirst()
                .orElseThrow();

        List<String> first = all.sampleWords(12, 30, new Random(1L));
        List<String> second = all.sampleWords(12, 30, new Random(2L));

        assertEquals(30, first.size());
        assertTrue(first.stream().allMatch(word -> word.length() <= 12));
        assertTrue(first.stream().allMatch(word -> word.matches("[А-ЯЁ]+")));
        long shortWords = first.stream().filter(word -> word.length() <= 5).count();
        assertTrue(shortWords >= 8,
                () -> "в выборке должно быть много коротких слов, а их " + shortWords);
        assertNotEquals(first, second);
    }

    @Test
    void completeDictionaryBuildsTwelveWordPuzzleOnWideGrid() {
        WordCategory all = WordCategory.all().stream()
                .filter(WordCategory::streamed)
                .findFirst()
                .orElseThrow();

        List<String> pool = all.sampleWords(12, 48, new Random(7L));
        var result = new game.generator.GridGenerator(7L)
                .generateChecked(10, 12, pool, 12);

        assertTrue(result.ok(), result::error);
        assertTrue(result.puzzle().getPlacements().size() >= 12);
    }

    @Test
    void parsesCategoryByNumberNameAndFileName() {
        WordCategory second = WordCategory.all().get(1);

        assertSame(second, WordCategory.fromInput("2"));
        assertSame(second, WordCategory.fromInput(second.displayName().toUpperCase()));
        assertSame(second, WordCategory.fromInput(second.fileName().replace(".txt", "")));
        assertEquals(null, WordCategory.fromInput("неизвестно"));
    }

    @Test
    void wordsAreLoadedLazilyAndCannotBeModified() {
        WordCategory category = WordCategory.defaultCategory();

        assertNotNull(category.words());
        assertFalse(category.words().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> category.words().add("СЛОВО"));
    }
}
