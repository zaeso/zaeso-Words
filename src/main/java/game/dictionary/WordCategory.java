package game.dictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/**
 * Категория слов, загружаемая из ресурсов проекта. Список категорий задаётся
 * в {@code dictionaries/categories.txt}, поэтому новый словарь добавляется
 * без изменения Java-кода.
 */
public final class WordCategory {
    private static final String INDEX_RESOURCE = "/dictionaries/categories.txt";
    private static final String BASE_PATH = "/dictionaries/";
    private static final List<WordCategory> ALL = loadAll();

    private final String displayName;
    private final String fileName;
    private List<String> words;

    private WordCategory(String displayName, String fileName) {
        this.displayName = displayName;
        this.fileName = fileName;
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

    public synchronized List<String> words() {
        if (words == null) {
            words = loadWords(BASE_PATH + fileName);
        }
        return words;
    }

    @Override
    public String toString() {
        return displayName;
    }

    private static List<WordCategory> loadAll() {
        List<WordCategory> categories = new ArrayList<>();
        try (BufferedReader reader = open(INDEX_RESOURCE)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String value = line.trim();
                if (value.isEmpty() || value.startsWith("#")) continue;

                String[] parts = value.split("\\|", 2);
                if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                    throw new IllegalStateException(
                            "Некорректная строка индекса словарей: " + value);
                }
                categories.add(new WordCategory(parts[0].trim(), parts[1].trim()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать индекс словарей.", e);
        }

        if (categories.isEmpty()) {
            throw new IllegalStateException("В индексе нет ни одной категории слов.");
        }
        return Collections.unmodifiableList(categories);
    }

    private static List<String> loadWords(String resource) {
        LinkedHashSet<String> loaded = new LinkedHashSet<>();
        try (BufferedReader reader = open(resource)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toUpperCase(Locale.ROOT);
                if (!word.isEmpty() && !word.startsWith("#")) {
                    loaded.add(word);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать словарь " + resource, e);
        }
        return List.copyOf(loaded);
    }

    private static BufferedReader open(String resource) {
        InputStream stream = WordCategory.class.getResourceAsStream(resource);
        if (stream == null) {
            throw new IllegalStateException("Не найден ресурс словаря: " + resource);
        }
        return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replace('ё', 'е');
    }
}
