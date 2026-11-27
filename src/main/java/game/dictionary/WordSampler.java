package game.dictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

final class WordSampler {
    private final Map<Integer, List<String>> wordsByLength = new TreeMap<>();

    WordSampler(List<String> words) {
        for (String word : words) {
            wordsByLength.computeIfAbsent(word.length(), length -> new ArrayList<>()).add(word);
        }
    }

    long count(int maxLength) {
        return wordsByLength.entrySet().stream()
                .filter(entry -> entry.getKey() <= maxLength)
                .mapToLong(entry -> entry.getValue().size())
                .sum();
    }

    List<String> sample(int maxLength, int count, Random random) {
        if (count <= 0) return Collections.emptyList();

        List<List<String>> pools = new ArrayList<>();
        for (Map.Entry<Integer, List<String>> entry : wordsByLength.entrySet()) {
            if (entry.getKey() > maxLength) break;
            pools.add(new ArrayList<>(entry.getValue()));
        }

        List<String> selected = new ArrayList<>();
        while (!pools.isEmpty() && selected.size() < count) {
            int weight = pools.stream().mapToInt(WordSampler::weight).sum();
            int ticket = random.nextInt(weight);
            for (int i = 0; i < pools.size(); i++) {
                List<String> pool = pools.get(i);
                ticket -= weight(pool);
                if (ticket < 0) {
                    int index = random.nextInt(pool.size());
                    int last = pool.size() - 1;
                    selected.add(pool.set(index, pool.get(last)));
                    pool.remove(last);
                    if (pool.isEmpty()) pools.remove(i);
                    break;
                }
            }
        }
        return Collections.unmodifiableList(selected);
    }

    private static int weight(List<String> pool) {
        int length = pool.get(0).length();
        if (length <= 5) return 8;
        if (length <= 7) return 4;
        return 2;
    }
}
