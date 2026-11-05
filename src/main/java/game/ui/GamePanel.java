package game.ui;

import game.config.Difficulty;
import game.config.GameConfig;
import game.dictionary.WordCategory;
import game.generator.GridGenerator;
import game.generator.GridGenerator.GenerationResult;
import game.model.Placement;
import game.model.Puzzle;
import game.play.GameTimer;
import game.result.GameResult;
import game.result.ResultRepository;
import game.score.Score;
import game.session.GameSession;
import game.session.GameSession.FinishReason;
import game.session.GameSession.HintResult;
import game.session.GameSession.WordResult;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
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
import java.util.HashSet;
import java.util.Set;

public final class GamePanel extends JPanel {
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
    private final JTextField input = new JTextField();
    private final JButton hintButton = Theme.button("Подсказка", Theme.GREEN);
    private final JButton findButton = Theme.button("Найти", Theme.BLUE);
    private final JButton menuButton = Theme.button("В меню", Theme.RED_DARK);
    private final Timer ticker;

    private GameSession session;
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
        findButton.addActionListener(e -> submitTyped());
        menuButton.addActionListener(e -> menuAction.run());
        input.addActionListener(e -> submitTyped());
    }

    public void startRound(Difficulty difficulty, WordCategory category,
                           int round, int carriedScore) {
        this.round = round;
        this.carriedScore = carriedScore;
        GameConfig config = difficulty.config();
        GenerationResult generation = generate(config, category);
        if (!generation.ok()) {
            session = null;
            ticker.stop();
            setControlsEnabled(false);
            statusLabel.setForeground(Theme.RED);
            statusLabel.setText(generation.error());
            return;
        }

        session = new GameSession(generation.puzzle(), config, leaderboardName(difficulty, category));
        session.start();

        roundLabel.setText("Раунд " + round + " · " + difficulty.displayName()
                + " · " + category.displayName());
        totalLabel.setText("Всего очков: " + carriedScore);
        gridPanel.setGrid(session.grid());
        gridPanel.setFoundCells(Set.of());
        rebuildWordList();
        setControlsEnabled(true);
        statusLabel.setForeground(Theme.MUTED);
        statusLabel.setText("Выделите слово мышью или введите его");
        input.setText("");
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
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.RED);
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
        scroll.setBorder(BorderFactory.createTitledBorder("Слова"));
        center.add(scroll, BorderLayout.EAST);
        return center;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(10, 8));
        footer.setOpaque(false);

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        input.setFont(Theme.BODY);
        input.setPreferredSize(new Dimension(0, 38));
        inputRow.add(input, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(findButton);
        actions.add(hintButton);
        actions.add(menuButton);
        inputRow.add(actions, BorderLayout.EAST);

        statusLabel.setFont(Theme.BODY);
        statusLabel.setForeground(Theme.MUTED);
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);

        footer.add(inputRow, BorderLayout.NORTH);
        footer.add(statusLabel, BorderLayout.SOUTH);
        return footer;
    }

    private GenerationResult generate(GameConfig config, WordCategory category) {
        return new GridGenerator().generateChecked(
                config.height(), config.width(), category.words(), config.minWords());
    }

    private static String leaderboardName(Difficulty difficulty, WordCategory category) {
        return difficulty.displayName() + " · " + category.displayName();
    }

    private void submitTyped() {
        if (session == null || session.isFinished()) return;
        String value = input.getText();
        if (session.isHintCommand(value)) {
            input.setText("");
            useHint();
            return;
        }
        submitWord(value);
        input.setText("");
        input.requestFocusInWindow();
    }

    private void submitWord(String word) {
        if (session == null || session.isFinished()) return;

        WordResult result = session.submitWord(word);
        switch (result) {
            case FOUND -> {
                statusLabel.setForeground(Theme.GREEN);
                statusLabel.setText("Найдено: " + word.trim().toUpperCase());
                refreshFound();
            }
            case EMPTY -> showError("Введите слово");
            case NON_CYRILLIC -> showError("Только русские буквы");
            case TOO_LONG -> showError("Слово слишком длинное");
            case NOT_IN_LIST -> showError("Такого слова нет в списке");
            case ALREADY_FOUND -> showError("Слово уже найдено");
        }

        updateIndicators();
        if (session.completed()) finish(FinishReason.ALL_WORDS_FOUND);
    }

    private void useHint() {
        if (session == null || session.isFinished()) return;
        HintResult hint = session.useHint();
        switch (hint.status()) {
            case GIVEN -> {
                Placement placement = hint.placement();
                statusLabel.setForeground(Theme.GREEN);
                statusLabel.setText("Подсказка " + hint.used() + " из " + hint.limit()
                        + ": слово из " + placement.word().length()
                        + " букв, первая «" + placement.word().charAt(0)
                        + "», направление — " + placement.direction().description()
                        + ". Минус 30 секунд.");
            }
            case DISABLED -> showError("В этом режиме подсказки отключены");
            case LIMIT_REACHED -> showError("Подсказки закончились");
            case NO_CANDIDATES -> showError("Все доступные подсказки уже открыты");
        }
        updateIndicators();
    }

    private void showError(String message) {
        statusLabel.setForeground(Theme.RED);
        statusLabel.setText(message);
    }

    private void refreshFound() {
        Set<GridPanel.Cell> cells = new HashSet<>();
        for (Placement placement : session.placements()) {
            if (!session.isFound(placement.word())) continue;
            int rowStep = placement.direction().rowStep();
            int colStep = placement.direction().colStep();
            for (int i = 0; i < placement.word().length(); i++) {
                cells.add(new GridPanel.Cell(
                        placement.startRow() + rowStep * i,
                        placement.startCol() + colStep * i));
            }
        }
        gridPanel.setFoundCells(cells);
        rebuildWordList();
    }

    private void rebuildWordList() {
        wordsPanel.removeAll();
        for (String word : session.allWords()) {
            boolean found = session.isFound(word);
            JLabel label = new JLabel((found ? "✓ " : "• ") + word);
            label.setFont(Theme.BODY);
            label.setForeground(found ? Theme.GREEN : Theme.TEXT);
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
        findButton.setEnabled(enabled);
        input.setEnabled(enabled);
        gridPanel.setEnabled(enabled);
    }

    public static String breakdown(Score score) {
        return "Буквы: +" + score.wordPoints()
                + "   Время: +" + score.timeBonus()
                + "   Ошибки: −" + score.mistakePenalty()
                + "   Подсказки: −" + score.hintPenalty();
    }

    public static String formatDuration(Duration duration) {
        return GameTimer.format(duration.toSeconds());
    }
}
