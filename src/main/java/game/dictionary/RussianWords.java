package game.dictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Большой словарь всех русских слов. Файл не загружается в память целиком:
 * для каждой длины сетки один раз строится случайная выборка, из которой
 * раунд берёт очередные слова. Так игра получает случайные слова из полного
 * списка без сотен мегабайт памяти.
 */
final class RussianWords {
    private static final String RESOURCE = "/dictionaries/all-russian.txt";
    private static final int POOL_SIZE = 4000;
    /**
     * Слова длиннее 10 букв в сетке 10×12 почти не размещаются: они помещаются
     * только целиком в строку, а случайная раскладка переполняет сетку. Поэтому
     * для игровых раундов берём слова не длиннее 10 букв.
     */
    private static final int MAX_GAME_WORD_LENGTH = 10;

    private static final Map<Integer, List<String>> POOLS = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> COUNTS = new ConcurrentHashMap<>();

    private RussianWords() {
    }

    static List<String> sample(int maxLength, int count, Random random) {
        if (count <= 0) {
            return List.of();
        }
        int limit = Math.min(maxLength, MAX_GAME_WORD_LENGTH);
        List<String> pool = POOLS.computeIfAbsent(limit, RussianWords::buildPool);
        if (pool.isEmpty()) {
            return List.of();
        }

        List<String> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, random);
        return List.copyOf(shuffled.subList(0, Math.min(count, shuffled.size())));
    }

    static long count(int maxLength) {
        int limit = Math.min(maxLength, MAX_GAME_WORD_LENGTH);
        return COUNTS.computeIfAbsent(limit, RussianWords::countWords);
    }

    private static List<String> buildPool(int maxLength) {
        List<String> reservoir = new ArrayList<>(POOL_SIZE);
        Random random = new Random(31L * maxLength + 17L);
        long seen = 0;

        try (BufferedReader reader = open()) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() > maxLength) continue;

                seen++;
                String word = line.toUpperCase(Locale.ROOT);
                if (reservoir.size() < POOL_SIZE) {
                    reservoir.add(word);
                } else {
                    long pick = Math.floorMod(random.nextLong(), seen);
                    if (pick < POOL_SIZE) {
                        reservoir.set((int) pick, word);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать словарь всех русских слов.", e);
        }

        Collections.shuffle(reservoir, random);
        return List.copyOf(reservoir);
    }

    private static long countWords(int maxLength) {
        long total = 0;
        try (BufferedReader reader = open()) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() <= maxLength) {
                    total++;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать словарь всех русских слов.", e);
        }
        return total;
    }

    private static BufferedReader open() {
        InputStream stream = RussianWords.class.getResourceAsStream(RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("Не найден ресурс словаря: " + RESOURCE);
        }
        return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }
}
