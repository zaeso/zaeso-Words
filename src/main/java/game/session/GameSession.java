package game.session;

import game.config.GameConfig;
import game.model.Placement;
import game.model.Puzzle;
import game.play.GameTimer;
import game.result.GameResult;
import game.score.Score;
import game.score.ScoreCalculator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Состояние одной игровой партии. Не знает о консоли или Swing: интерфейсы
 * используют одни и те же методы, поэтому правила игры не дублируются.
 */
public final class GameSession {
    public static final Duration HINT_PENALTY = Duration.ofSeconds(30);
    private static final Pattern CYRILLIC_WORD = Pattern.compile("[А-ЯЁ]+");

    private final Puzzle puzzle;
    private final GameConfig config;
    private final String difficultyName;
    private final GameTimer timer;
    private final Set<String> allWords;
    private final Set<String> found = new LinkedHashSet<>();
    private final Set<String> hintedWords = new LinkedHashSet<>();
    private final int maxWordLength;

    private int hintsUsed;
    private int mistakes;
    private boolean finished;

    public GameSession(Puzzle puzzle, GameConfig config, String difficultyName) {
        this.puzzle = Objects.requireNonNull(puzzle, "puzzle");
        this.config = Objects.requireNonNull(config, "config");
        this.difficultyName = Objects.requireNonNull(difficultyName, "difficultyName");
        this.timer = new GameTimer(config.timeLimit());
        this.allWords = new LinkedHashSet<>(puzzle.getWords());
        this.maxWordLength = allWords.stream().mapToInt(String::length).max().orElse(0);
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public boolean isRunning() {
        return timer.isRunning();
    }

    public boolean isExpired() {
        return timer.isExpired();
    }

    public long remainingSeconds() {
        return timer.remainingSeconds();
    }

    public long elapsedSeconds() {
        return timer.elapsedSeconds();
    }

    public long limitSeconds() {
        return timer.getLimitSeconds();
    }

    public WordResult submitWord(String raw) {
        if (finished) {
            throw new IllegalStateException("Партия уже завершена.");
        }

        String answer = normalize(raw);
        if (answer.isEmpty()) {
            return WordResult.EMPTY;
        }
        if (!CYRILLIC_WORD.matcher(answer).matches()) {
            return WordResult.NON_CYRILLIC;
        }
        if (answer.length() > maxWordLength) {
            return WordResult.TOO_LONG;
        }
        if (!allWords.contains(answer)) {
            mistakes++;
            return WordResult.NOT_IN_LIST;
        }
        if (found.contains(answer)) {
            mistakes++;
            return WordResult.ALREADY_FOUND;
        }

        found.add(answer);
        return WordResult.FOUND;
    }

    /**
     * Открывает подсказку: длину, первую букву и направление слова.
     * Штраф за одно и то же слово повторно не списывается.
     */
    public HintResult useHint() {
        if (finished) {
            throw new IllegalStateException("Партия уже завершена.");
        }
        if (config.hints() == 0) {
            return new HintResult(HintStatus.DISABLED, null, hintsUsed, config.hints());
        }
        if (hintsUsed >= config.hints()) {
            return new HintResult(HintStatus.LIMIT_REACHED, null, hintsUsed, config.hints());
        }

        Optional<Placement> candidate = puzzle.getPlacements().stream()
                .filter(placement -> !found.contains(placement.word()))
                .filter(placement -> !hintedWords.contains(placement.word()))
                .max(Comparator.comparingInt(placement -> placement.word().length()));

        if (!candidate.isPresent()) {
            return new HintResult(HintStatus.NO_CANDIDATES, null, hintsUsed, config.hints());
        }
        Placement placement = candidate.get();
        hintedWords.add(placement.word());
        hintsUsed++;
        timer.penalize(HINT_PENALTY);
        return new HintResult(HintStatus.GIVEN, placement, hintsUsed, config.hints());
    }

    public GameResult finish(FinishReason reason) {
        Objects.requireNonNull(reason, "reason");
        if (finished) {
            throw new IllegalStateException("Партия уже завершена.");
        }
        finished = true;
        timer.stop();

        Score score = score();
        return new GameResult(
                Instant.now().getEpochSecond(),
                difficultyName,
                grid().length,
                grid()[0].length,
                found.size(),
                allWords.size(),
                timer.elapsedSeconds(),
                hintsUsed,
                score,
                reason.description()
        );
    }

    public Score score() {
        return ScoreCalculator.calculate(
                found,
                allWords.size(),
                timer.remainingSeconds(),
                mistakes,
                hintsUsed
        );
    }

    public boolean completed() {
        return !allWords.isEmpty() && found.size() == allWords.size();
    }

    public boolean isFinished() {
        return finished;
    }

    public String normalize(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
    }

    public boolean isExitCommand(String raw) {
        return normalize(raw).equals("EXIT");
    }

    public boolean isHintCommand(String raw) {
        String command = normalize(raw);
        return command.equals("HINT") || command.equals("ПОДСКАЗКА");
    }

    public int foundCount() {
        return found.size();
    }

    public int totalWords() {
        return allWords.size();
    }

    public int hintsUsed() {
        return hintsUsed;
    }

    public int hintsLimit() {
        return config.hints();
    }

    public int mistakes() {
        return mistakes;
    }

    public Set<String> foundWords() {
        return Collections.unmodifiableSet(found);
    }

    public List<String> allWords() {
        return Collections.unmodifiableList(new ArrayList<>(allWords));
    }

    public List<String> remainingWords() {
        List<String> remaining = new ArrayList<>();
        for (String word : allWords) {
            if (!found.contains(word)) {
                remaining.add(word);
            }
        }
        return Collections.unmodifiableList(remaining);
    }

    public boolean isFound(String word) {
        return found.contains(normalize(word));
    }

    public char[][] grid() {
        return puzzle.getGrid();
    }

    public List<Placement> placements() {
        return puzzle.getPlacements();
    }

    public Optional<Placement> placementFor(String word) {
        String normalized = normalize(word);
        return puzzle.getPlacements().stream()
                .filter(placement -> placement.word().equals(normalized))
                .findFirst();
    }

    public Puzzle puzzle() {
        return puzzle;
    }

    public GameConfig config() {
        return config;
    }

    public String difficultyName() {
        return difficultyName;
    }

    public enum WordResult {
        FOUND,
        EMPTY,
        NON_CYRILLIC,
        TOO_LONG,
        NOT_IN_LIST,
        ALREADY_FOUND
    }

    public enum FinishReason {
        ALL_WORDS_FOUND("все слова найдены"),
        TIME_EXPIRED("время вышло"),
        PLAYER_EXIT("выход игрока");

        private final String description;

        FinishReason(String description) {
            this.description = description;
        }

        public String description() {
            return description;
        }
    }

    public enum HintStatus {
        GIVEN,
        DISABLED,
        LIMIT_REACHED,
        NO_CANDIDATES
    }

    public static final class HintResult {
        private final HintStatus status;
        private final Placement placement;
        private final int used;
        private final int limit;

        public HintResult(HintStatus status, Placement placement, int used, int limit) {
            this.status = status;
            this.placement = placement;
            this.used = used;
            this.limit = limit;
        }

        public HintStatus status() {
            return status;
        }

        public Placement placement() {
            return placement;
        }

        public int used() {
            return used;
        }

        public int limit() {
            return limit;
        }

        public boolean given() {
            return status == HintStatus.GIVEN;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof HintResult)) return false;
            HintResult result = (HintResult) other;
            return used == result.used
                    && limit == result.limit
                    && status == result.status
                    && Objects.equals(placement, result.placement);
        }

        @Override
        public int hashCode() {
            int result = status.hashCode();
            result = 31 * result + (placement == null ? 0 : placement.hashCode());
            result = 31 * result + used;
            result = 31 * result + limit;
            return result;
        }

        @Override
        public String toString() {
            return "HintResult[status=" + status + ", placement=" + placement
                    + ", used=" + used + ", limit=" + limit + "]";
        }
    }
}
