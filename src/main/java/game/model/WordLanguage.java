package game.model;

import java.util.regex.Pattern;

public enum WordLanguage {
    RUSSIAN("АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ", "Используйте только русские буквы."),
    ENGLISH("ABCDEFGHIJKLMNOPQRSTUVWXYZ", "Используйте только английские буквы.");

    private final String alphabet;
    private final Pattern letters;
    private final Pattern otherCharacters;
    private final String inputMessage;

    WordLanguage(String alphabet, String inputMessage) {
        this.alphabet = alphabet;
        this.letters = Pattern.compile("[" + alphabet + "]+");
        this.otherCharacters = Pattern.compile("[^" + alphabet + "]");
        this.inputMessage = inputMessage;
    }

    public String alphabet() {
        return alphabet;
    }

    public boolean accepts(String word) {
        return letters.matcher(word).matches();
    }

    public String keepLetters(String word) {
        return otherCharacters.matcher(word).replaceAll("");
    }

    public String inputMessage() {
        return inputMessage;
    }
}
