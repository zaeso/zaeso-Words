package game.dictionary;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordCategoryTest {

    @Test
    void loadsCategoriesFromResourceIndex() {
        List<WordCategory> categories = WordCategory.all();

        assertTrue(categories.size() >= 5);
        assertEquals("Технологии", WordCategory.defaultCategory().displayName());
        assertTrue(WordCategory.all().stream()
                .anyMatch(category -> category.displayName().equals("Животные")));
    }

    @Test
    void everyCategoryHasEnoughShortWordsForHardGrid() {
        for (WordCategory category : WordCategory.all()) {
            long usable = category.words().stream()
                    .filter(word -> word.length() <= 12)
                    .distinct()
                    .count();

            assertTrue(usable >= 12,
                    () -> "Категория " + category.displayName() + " содержит только " + usable + " слов");
        }
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
