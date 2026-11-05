package game.dictionary;

import java.util.List;

/** Словарь категории по умолчанию. */
public final class WordBank {
    public static final List<String> WORDS = WordCategory.defaultCategory().words();

    private WordBank() {
    }
}
