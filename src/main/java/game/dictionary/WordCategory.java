package game.dictionary;

import game.model.WordLanguage;

import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Метаданные категории и ленивый доступ к её словарю. */
public final class WordCategory {
    private static final List<WordCategory> ALL = DictionaryResources.loadCategories();

    private final String displayName;
    private final String fileName;
    private final WordLanguage language;
    private List<String> words;
    private WordSampler sampler;

    WordCategory(String displayName, String fileName, WordLanguage language) {
        this.displayName = displayName;
        this.fileName = fileName;
        this.language = language;
    }

    public static List<WordCategory> all() {
        return ALL;
    }

    public static WordCategory defaultCategory() {
        return ALL.get(0);
    }

    public static WordCategory fromInput(String input) {
        if (input == null) return null;

        String normalized = normalize(input);
        if (normalized.isEmpty()) return null;

        for (WordCategory category : ALL) {
            if (String.valueOf(ALL.indexOf(category) + 1).equals(normalized)
                    || normalize(category.displayName).equals(normalized)
                    || normalize(category.fileName).equals(normalized)
                    || normalize(category.fileName.replace(".txt", "")).equals(normalized)) {
                return category;
            }
        }
        return null;
    }

    public String displayName() {
        return displayName;
    }

    public String fileName() {
        return fileName;
    }

    public WordLanguage language() {
        return language;
    }

    public List<String> sampleWords(int maxLength, int count, Random random) {
        return sampler().sample(maxLength, count, random);
    }

    public long suitableWordCount(int maxLength) {
        return sampler().count(maxLength);
    }

    public String wordCountText() {
        return words().size() + " слов";
    }

    public synchronized List<String> words() {
        if (words == null) {
            words = DictionaryResources.loadWords(fileName, language);
        }
        return words;
    }

    private synchronized WordSampler sampler() {
        if (sampler == null) {
            sampler = new WordSampler(words());
        }
        return sampler;
    }

    @Override
    public String toString() {
        return displayName;
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replace('ё', 'е');
    }
}
