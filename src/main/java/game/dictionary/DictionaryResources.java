package game.dictionary;

import game.model.WordLanguage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class DictionaryResources {
    private static final String BASE_PATH = "/dictionaries/";

    private DictionaryResources() {
    }

    static List<WordCategory> loadCategories() {
        List<WordCategory> categories = new ArrayList<>();
        Set<String> fileNames = new HashSet<>();
        for (String row : readRows("categories.txt")) {
            String[] fields = row.split("\\|", -1);
            if (fields.length != 3 || fields[0].trim().isEmpty()
                    || !fields[1].matches("[a-z][a-z0-9-]*\\.txt")
                    || !fileNames.add(fields[1])) {
                throw new IllegalStateException("Некорректная строка индекса словарей: " + row);
            }
            WordLanguage language;
            switch (fields[2]) {
                case "ru": language = WordLanguage.RUSSIAN; break;
                case "en": language = WordLanguage.ENGLISH; break;
                default: throw new IllegalStateException("Неизвестный язык словаря: " + fields[2]);
            }
            categories.add(new WordCategory(fields[0].trim(), fields[1], language));
        }
        if (categories.isEmpty()) {
            throw new IllegalStateException("В индексе нет ни одной категории слов.");
        }
        return Collections.unmodifiableList(categories);
    }

    static List<String> loadWords(String fileName, WordLanguage language) {
        Set<String> words = new LinkedHashSet<>();
        for (String row : readRows(fileName)) {
            String word = row.toUpperCase(Locale.ROOT);
            if (!language.accepts(word)) {
                throw new IllegalStateException("Недопустимое слово в словаре " + fileName);
            }
            words.add(word);
        }
        if (words.isEmpty()) {
            throw new IllegalStateException("Пустой словарь: " + fileName);
        }
        return Collections.unmodifiableList(new ArrayList<>(words));
    }

    private static List<String> readRows(String fileName) {
        InputStream stream = DictionaryResources.class.getResourceAsStream(BASE_PATH + fileName);
        if (stream == null) {
            throw new IllegalStateException("Не найден ресурс словаря: " + fileName);
        }
        List<String> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8.newDecoder()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String row = line.trim();
                if (!row.isEmpty() && !row.startsWith("#")) {
                    rows.add(row);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать словарь " + fileName, e);
        }
        return rows;
    }
}
