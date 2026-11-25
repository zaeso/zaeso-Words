package game.score;

import java.util.Objects;

/**
 * Стоимость букв в очках. Чем реже буква встречается в русских словах, тем
 * больше очков она приносит.
 *
 * <table>
 *     <caption>Стоимость букв</caption>
 *     <tr><th>Очки</th><th>Буквы</th></tr>
 *     <tr><td>10</td><td>О, Е, А, И, Н, Т, С, Р, В, Л</td></tr>
 *     <tr><td>15</td><td>К, М, Д, П, У, Я</td></tr>
 *     <tr><td>20</td><td>Ы, Ь, Г, З, Б, Ч</td></tr>
 *     <tr><td>25</td><td>Й, Х, Ж, Ш, Ю, Ц, Щ, Э, Ф</td></tr>
 *     <tr><td>30</td><td>Ъ, Ё</td></tr>
 * </table>
 */
public final class LetterPoints {
    public static final int COMMON = 10;
    public static final int REGULAR = 15;
    public static final int LESS_COMMON = 20;
    public static final int RARE = 25;
    public static final int VERY_RARE = 30;

    private LetterPoints() {
    }

    /**
     * Возвращает стоимость одной русской или английской буквы без учёта регистра.
     *
     * @throws IllegalArgumentException если символ не относится к игровым алфавитам
     */
    public static int pointsFor(char letter) {
        switch (Character.toUpperCase(letter)) {
            case 'О': case 'Е': case 'А': case 'И': case 'Н':
            case 'Т': case 'С': case 'Р': case 'В': case 'Л':
            case 'E': case 'T': case 'A': case 'O': case 'I':
            case 'N': case 'S': case 'R': case 'H': case 'L':
                return COMMON;
            case 'К': case 'М': case 'Д': case 'П': case 'У': case 'Я':
            case 'D': case 'C': case 'U': case 'M': case 'W': case 'F':
                return REGULAR;
            case 'Ы': case 'Ь': case 'Г': case 'З': case 'Б': case 'Ч':
            case 'G': case 'Y': case 'P': case 'B': case 'V':
                return LESS_COMMON;
            case 'Й': case 'Х': case 'Ж': case 'Ш': case 'Ю':
            case 'Ц': case 'Щ': case 'Э': case 'Ф':
            case 'K': case 'J': case 'X':
                return RARE;
            case 'Ъ': case 'Ё':
            case 'Q': case 'Z':
                return VERY_RARE;
            default:
                throw new IllegalArgumentException(
                        "Неподдерживаемая буква: " + letter);
        }
    }

    /**
     * Суммирует стоимость всех букв слова.
     */
    public static int pointsFor(String word) {
        Objects.requireNonNull(word, "word");
        int points = 0;
        for (int i = 0; i < word.length(); i++) {
            points += pointsFor(word.charAt(i));
        }
        return points;
    }
}
