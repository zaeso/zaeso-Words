package game.dictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Большой словарь всех русских слов. Файл не загружается в память целиком:
 * для каждой длины сетки один раз строится случайная выборка, из которой
 * раунд берёт очередные слова. Так игра получает случайные слова из полного
 * списка без сотен мегабайт памяти.
 *
 * <p>Выборка стратифицирована по длине: коротких слов в полном словаре мало,
 * поэтому без этого сетка почти целиком состояла бы из длинных слов.</p>
 */
final class RussianWords {
    private static final String RESOURCE = "/dictionaries/all-russian.txt";
    private static final int POOL_SIZE_PER_LENGTH = 2000;
    /**
     * Слова длиннее 10 букв в сетке 10×12 почти не размещаются: они помещаются
     * только целиком в строку, а случайная раскладка переполняет сетку. Поэтому
     * для игровых раундов берём слова не длиннее 10 букв.
     */
    private static final int MAX_GAME_WORD_LENGTH = 10;

    private static final Map<Integer, Map<Integer, List<String>>> POOLS =
            new ConcurrentHashMap<>();
    private static final Map<Integer, Long> COUNTS = new ConcurrentHashMap<>();

    private RussianWords() {
    }

    static List<String> sample(int maxLength, int count, Random random) {
        if (count <= 0) {
            return List.of();
        }
        int limit = Math.min(maxLength, MAX_GAME_WORD_LENGTH);
        Map<Integer, List<String>> byLength =
                POOLS.computeIfAbsent(limit, RussianWords::buildPools);
        if (byLength.isEmpty()) {
            return List.of();
        }

        int totalWeight = byLength.keySet().stream()
                .mapToInt(RussianWords::lengthWeight)
                .sum();

        Set<String> picked = new LinkedHashSet<>();
        int attempts = 0;
        int maxAttempts = Math.max(100, count * 50);
        while (picked.size() < count && attempts++ < maxAttempts) {
            int length = pickLength(byLength, totalWeight, random);
            List<String> pool = byLength.get(length);
            picked.add(pool.get(random.nextInt(pool.size())));
        }

        if (picked.size() < count) {
            List<String> rest = new ArrayList<>();
            byLength.values().forEach(rest::addAll);
            Collections.shuffle(rest, random);
            for (String word : rest) {
                if (picked.size() >= count) break;
                picked.add(word);
            }
        }

        List<String> result = new ArrayList<>(picked);
        Collections.shuffle(result, random);
        return List.copyOf(result);
    }

    static long count(int maxLength) {
        int limit = Math.min(maxLength, MAX_GAME_WORD_LENGTH);
        return COUNTS.computeIfAbsent(limit, RussianWords::countWords);
    }

    /**
     * Короткие слова нужны чаще: они дают пересечения и диагонали. Длинные
     * оставляем, но в небольшой доле, иначе сетка переполняется.
     */
    private static int lengthWeight(int length) {
        if (length <= 4) return 6;
        if (length <= 6) return 5;
        if (length <= 8) return 3;
        return 1;
    }

    private static int pickLength(Map<Integer, List<String>> byLength,
                                  int totalWeight, Random random) {
        int ticket = random.nextInt(totalWeight);
        int last = 0;
        for (int length : byLength.keySet()) {
            last = length;
            ticket -= lengthWeight(length);
            if (ticket < 0) {
                return length;
            }
        }
        return last;
    }

    private static Map<Integer, List<String>> buildPools(int maxLength) {
        Map<Integer, Reservoir> reservoirs = new LinkedHashMap<>();
        for (int length = 3; length <= maxLength; length++) {
            reservoirs.put(length, new Reservoir(POOL_SIZE_PER_LENGTH));
        }

        Random random = new Random(31L * maxLength + 17L);
        try (BufferedReader reader = open()) {
            String line;
            while ((line = reader.readLine()) != null) {
                int length = line.length();
                if (length < 3 || length > maxLength) continue;
                reservoirs.get(length).add(line.toUpperCase(Locale.ROOT), random);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать словарь всех русских слов.", e);
        }

        Map<Integer, List<String>> pools = new LinkedHashMap<>();
        reservoirs.forEach((length, reservoir) -> {
            reservoir.shuffle(random);
            pools.put(length, List.copyOf(reservoir.words));
        });
        return Collections.unmodifiableMap(pools);
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

    /** Резервуарная выборка фиксированного размера из всех слов одной длины. */
    private static final class Reservoir {
        private final int capacity;
        private final List<String> words;
        private long seen;

        private Reservoir(int capacity) {
            this.capacity = capacity;
            this.words = new ArrayList<>(Math.min(capacity, 256));
        }

        private void add(String word, Random random) {
            seen++;
            if (words.size() < capacity) {
                words.add(word);
            } else {
                long pick = Math.floorMod(random.nextLong(), seen);
                if (pick < capacity) {
                    words.set((int) pick, word);
                }
            }
        }

        private void shuffle(Random random) {
            Collections.shuffle(words, random);
        }
    }
}
