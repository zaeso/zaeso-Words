package game.ui;

import game.config.Difficulty;
import game.config.GameConfig;
import game.config.GameSettings;
import game.config.UiSettings;
import game.dictionary.WordCategory;
import game.generator.GridGenerator.GenerationResult;
import game.generator.RoundGenerator;
import game.model.Placement;
import game.play.GameTimer;
import game.result.GameResult;
import game.result.ResultRepository;
import game.score.Score;
import game.session.GameSession;
import game.session.GameSession.FinishReason;
import game.session.GameSession.HintResult;
import game.session.GameSession.HintStatus;
import game.session.GameSession.WordResult;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class GamePanel extends JPanel {
    /** Спокойная палитра для светлого фона сетки. */
    private static final Color[] DEEP_WORD_COLORS = {
            new Color(0xC62828),
            new Color(0x1565C0),
            new Color(0x2E7D32),
            new Color(0x6A1B9A),
            new Color(0xE65100),
            new Color(0x00695C),
            new Color(0xAD1457),
            new Color(0x4527A0)
    };
    /** Яркая палитра для тёмного фона сетки: не сливается с ним. */
    private static final Color[] BRIGHT_WORD_COLORS = {
            new Color(0xEF5350),
            new Color(0x42A5F5),
            new Color(0x66BB6A),
            new Color(0xAB47BC),
            new Color(0xFFA726),
            new Color(0x26C6DA),
            new Color(0xEC407A),
            new Color(0x7E57C2)
    };

    /** Палитра найденных слов под яркость фона сетки. */
    static Color[] paletteForBackground(Color background) {
        double luminance = (0.299 * background.getRed()
                + 0.587 * background.getGreen()
                + 0.114 * background.getBlue()) / 255.0;
        return luminance < 0.45 ? BRIGHT_WORD_COLORS : DEEP_WORD_COLORS;
    }

    public interface Listener {
        void roundFinished(GameResult result, boolean timeExpired);
    }

    private final Listener listener;
    private final Runnable menuAction;
    private final ResultRepository repository;
    private final GridPanel gridPanel;
    private final JPanel wordsPanel = new JPanel(new GridLayout(0, 1, 0, 4));
    private final JLabel roundLabel = new JLabel();
    private final JLabel totalLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();
    private final JLabel hintsLabel = new JLabel();
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton hintButton = Theme.button("Подсказка", Theme.GREEN);
    private final JButton menuButton = Theme.button("В меню", Theme.RED_DARK);
    private final Timer ticker;

    private GameSession session;
    private UiSettings uiSettings = UiSettings.defaults();
    private final Map<String, Color> wordColors = new HashMap<>();
    private Color[] wordPalette = DEEP_WORD_COLORS;
    private int round;
    private int carriedScore;

    public GamePanel(Listener listener, Runnable menuAction, ResultRepository repository) {
        this.listener = listener;
        this.menuAction = menuAction;
        this.repository = repository;
        this.gridPanel = new GridPanel(this::submitWord);
        this.ticker = new Timer(250, this::tick);

        setLayout(new BorderLayout(14, 14));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        hintButton.addActionListener(e -> useHint());
        menuButton.addActionListener(e -> menuAction.run());
    }

    /** Применяет звук и анимации, не затрагивая текущую игровую сессию. */
    public void applyUiSettings(UiSettings settings) {
        this.uiSettings = settings;
        gridPanel.setAnimationsEnabled(settings.animationsEnabled());
    }

    public void startRound(GameSettings settings, int round, int carriedScore) {
        startRound(settings, round, carriedScore, null, null, new Random().nextLong());
    }

    /**
     * Запускает раунд с воспроизводимым seed. {@code resultLabel} заменяет
     * название сложности в сохранённом результате (например, «Ежедневная»),
     * а {@code title} — заголовок в шапке игры.
     */
    public void startRound(GameSettings settings, int round, int carriedScore,
                           String resultLabel, String title, long seed) {
        this.round = round;
        this.carriedScore = carriedScore;
        GameConfig config = settings.toConfig();
        WordCategory category = settings.category();
        GenerationResult generation = RoundGenerator.generate(config, category, seed);
        if (!generation.ok()) {
            session = null;
            ticker.stop();
            setControlsEnabled(false);
            statusLabel.setForeground(Theme.RED);
            statusLabel.setText(generation.error());
            return;
        }

        String label = resultLabel != null
                ? resultLabel
                : leaderboardName(settings.difficulty(), category);
        session = new GameSession(generation.puzzle(), config, label);
        session.start();
        wordColors.clear();
        wordPalette = paletteForBackground(Theme.BLUE_DARK);

        roundLabel.setText(title != null
                ? title
                : "Раунд " + round + " · " + settings.difficulty().displayName()
                        + " · " + category.displayName()
                        + " · " + config.height() + "×" + config.width());
        totalLabel.setText("Всего очков: " + carriedScore);
        gridPanel.setGrid(session.grid());
        gridPanel.setFoundCells(Collections.emptyMap());
        rebuildWordList();
        setControlsEnabled(true);
        statusLabel.setForeground(Theme.MUTED);
        statusLabel.setText("Выделите слово мышью");
        updateIndicators();
        ticker.restart();
    }

    public void stopRound() {
        ticker.stop();
        if (session != null && !session.isFinished()) {
            session.stop();
        }
    }

    private JPanel buildHeader() {
        GradientPanel header = GradientPanel.header();
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        roundLabel.setFont(Theme.HEADING);
        roundLabel.setForeground(Color.WHITE);
        totalLabel.setFont(Theme.BODY);
        totalLabel.setForeground(Color.WHITE);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(roundLabel);
        left.add(Box.createVerticalStrut(2));
        left.add(totalLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        right.setOpaque(false);
        timeLabel.setFont(Theme.BODY);
        timeLabel.setForeground(Color.WHITE);
        hintsLabel.setFont(Theme.BODY);
        hintsLabel.setForeground(Color.WHITE);
        right.add(timeLabel);
        right.add(hintsLabel);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(14, 0));
        center.setOpaque(false);
        center.add(gridPanel, BorderLayout.CENTER);

        wordsPanel.setOpaque(false);
        JScrollPane scroll = new JScrollPane(wordsPanel);
        scroll.setPreferredSize(new Dimension(240, 0));
        scroll.getViewport().setBackground(Theme.BACKGROUND);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                "Слова",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                Theme.SMALL,
                Theme.MUTED));
        center.add(scroll, BorderLayout.EAST);
        return center;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(10, 8));
        footer.setOpaque(false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(hintButton);
        actions.add(menuButton);

        statusLabel.setFont(Theme.BODY);
        statusLabel.setForeground(Theme.MUTED);
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);

        footer.add(actions, BorderLayout.NORTH);
        footer.add(statusLabel, BorderLayout.SOUTH);
        return footer;
    }

    private static String leaderboardName(Difficulty difficulty, WordCategory category) {
        return difficulty.displayName() + " · " + category.displayName();
    }

    private void submitWord(String word) {
        if (session == null || session.isFinished()) return;

        WordResult result = session.submitWord(word);
        if (result == WordResult.FOUND) {
            statusLabel.setForeground(Theme.GREEN);
            statusLabel.setText("Найдено: " + word.trim().toUpperCase());
            refreshFound();
            playSound(SoundPlayer::found);
        } else if (result == WordResult.EMPTY) {
            showError("Введите слово");
        } else if (result == WordResult.NON_CYRILLIC) {
            showError("Только русские буквы");
        } else if (result == WordResult.TOO_LONG) {
            showError("Слово слишком длинное");
        } else if (result == WordResult.NOT_IN_LIST) {
            showError("Такого слова нет в списке");
        } else if (result == WordResult.ALREADY_FOUND) {
            showError("Слово уже найдено");
        }

        updateIndicators();
        if (session.completed()) finish(FinishReason.ALL_WORDS_FOUND);
    }

    private void useHint() {
        if (session == null || session.isFinished()) return;
        HintResult hint = session.useHint();
        if (hint.status() == HintStatus.GIVEN) {
            Placement placement = hint.placement();
            gridPanel.addHintCell(new GridPanel.Cell(
                    placement.startRow(), placement.startCol()));
            statusLabel.setForeground(Theme.GREEN);
            statusLabel.setText("Подсказка " + hint.used() + " из " + hint.limit()
                    + ": слово из " + placement.word().length()
                    + " букв, первая «" + placement.word().charAt(0)
                    + "» подсвечена зелёным, направление — "
                    + placement.direction().description()
                    + ". Минус 30 секунд.");
            playSound(SoundPlayer::hint);
        } else if (hint.status() == HintStatus.DISABLED) {
            showError("В этом режиме подсказки отключены");
        } else if (hint.status() == HintStatus.LIMIT_REACHED) {
            showError("Подсказки закончились");
        } else if (hint.status() == HintStatus.NO_CANDIDATES) {
            showError("Все доступные подсказки уже открыты");
        }
        updateIndicators();
    }

    private void showError(String message) {
        statusLabel.setForeground(Theme.RED);
        statusLabel.setText(message);
        playSound(SoundPlayer::error);
    }

    private void playSound(Runnable sound) {
        if (uiSettings.soundEnabled()) {
            sound.run();
        }
    }

    private void refreshFound() {
        Map<GridPanel.Cell, Color> cells = new HashMap<>();
        for (String word : session.foundWords()) {
            Color color = wordColors.computeIfAbsent(word,
                    value -> wordPalette[wordColors.size() % wordPalette.length]);
            session.placementFor(word).ifPresent(placement -> {
                int rowStep = placement.direction().rowStep();
                int colStep = placement.direction().colStep();
                for (int i = 0; i < placement.word().length(); i++) {
                    cells.putIfAbsent(new GridPanel.Cell(
                            placement.startRow() + rowStep * i,
                            placement.startCol() + colStep * i), color);
                }
            });
        }
        gridPanel.setFoundCells(cells);
        rebuildWordList();
    }

    /** Цвет слова в списке: чуть темнее на светлой панели, чтобы не сливался. */
    private Color listColor(String word) {
        Color color = wordColors.get(word);
        if (color == null) return Theme.GREEN;
        return luminance(Theme.PANEL) < 0.45 ? color : color.darker();
    }

    private static double luminance(Color color) {
        return (0.299 * color.getRed()
                + 0.587 * color.getGreen()
                + 0.114 * color.getBlue()) / 255.0;
    }

    private void rebuildWordList() {
        wordsPanel.removeAll();
        for (String word : session.allWords()) {
            boolean found = session.isFound(word);
            JLabel label = new JLabel(found ? word : "• " + word);
            label.setFont(Theme.BODY);
            label.setForeground(found ? listColor(word) : Theme.TEXT);
            wordsPanel.add(label);
        }
        wordsPanel.revalidate();
        wordsPanel.repaint();
    }

    private void updateIndicators() {
        if (session == null) return;
        timeLabel.setText("Время " + GameTimer.format(session.remainingSeconds()));
        hintsLabel.setText("Подсказки " + (session.hintsLimit() - session.hintsUsed())
                + "/" + session.hintsLimit());
        totalLabel.setText("Всего очков: " + (carriedScore + session.score().total()));
    }

    private void tick(ActionEvent event) {
        if (session == null || session.isFinished()) return;
        updateIndicators();
        if (session.isExpired()) {
            finish(FinishReason.TIME_EXPIRED);
        }
    }

    private void finish(FinishReason reason) {
        if (session == null || session.isFinished()) return;
        ticker.stop();
        setControlsEnabled(false);
        GameResult result = session.finish(reason);
        playSound(SoundPlayer::finish);
        try {
            repository.save(result);
        } catch (IOException e) {
            statusLabel.setForeground(Theme.RED);
            statusLabel.setText("Результат не сохранён: " + e.getMessage());
        }
        listener.roundFinished(result, reason == FinishReason.TIME_EXPIRED);
    }

    private void setControlsEnabled(boolean enabled) {
        hintButton.setEnabled(enabled);
        gridPanel.setEnabled(enabled);
    }

    public static String breakdown(Score score) {
        return "Буквы: +" + score.wordPoints()
                + "   Время: +" + score.timeBonus()
                + "   Ошибки: −" + score.mistakePenalty()
                + "   Подсказки: −" + score.hintPenalty();
    }

    public static String formatDuration(Duration duration) {
        return GameTimer.format(duration.getSeconds());
    }
}
