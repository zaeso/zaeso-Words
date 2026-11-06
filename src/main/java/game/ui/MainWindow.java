package game.ui;

import game.config.Difficulty;
import game.config.GameConfig;
import game.config.GameSettings;
import game.config.SettingsStore;
import game.config.ThemeChoice;
import game.config.UiSettings;
import game.daily.DailyPuzzle;
import game.daily.DailyStatistics;
import game.dictionary.WordCategory;
import game.play.GameTimer;
import game.result.GameResult;
import game.result.ResultRepository;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public final class MainWindow extends JFrame {
    private static final String MENU = "menu";
    private static final String SETTINGS = "settings";
    private static final String GAME = "game";
    private static final String FINISH = "finish";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final ResultRepository repository = ResultRepository.defaultRepository();
    private final SettingsStore settingsStore = SettingsStore.defaultStore();
    private GamePanel gamePanel;
    private JLabel finishTitle;
    private JLabel finishSubtitle;
    private JLabel finishScore;
    private JLabel finishBreakdown;
    private JLabel finishTotal;
    private JLabel settingsStatus;
    private JButton nextRoundButton;

    private GameSettings settings;
    private GameSettings activeSettings;
    private UiSettings uiSettings;
    private boolean dailyMode;
    private LocalDate dailyDate;
    private JComboBox<String> difficultyBox;
    private JComboBox<String> categoryBox;
    private JComboBox<GameSettings.BoardSize> sizeBox;
    private JComboBox<ThemeChoice> themeBox;
    private JCheckBox soundBox;
    private JCheckBox animationsBox;
    private JSpinner durationSpinner;
    private boolean updatingSettingsControls;
    private int round = 1;
    private int totalScore;

    public MainWindow() {
        super("zaeso-Words");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(1180, Math.max(900, screen.width - 120));
        int height = Math.min(760, Math.max(600, screen.height - 160));
        setMinimumSize(new Dimension(Math.min(900, width), Math.min(600, height)));
        setSize(width, height);
        setLocationRelativeTo(null);

        settings = settingsStore.load();
        activeSettings = settings;
        uiSettings = settingsStore.loadUi();
        Theme.apply(uiSettings.theme());

        rebuildUi();
        setContentPane(root);

        showMenu();
    }

    private JPanel buildMenu() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.RED);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("ZAESO WORDS");
        title.setFont(Theme.TITLE);
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Бесконечный поиск слов · сетка дня и личная статистика");
        subtitle.setFont(Theme.BODY);
        subtitle.setForeground(Theme.SUBTITLE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton play = Theme.button("Играть", Theme.GREEN);
        JButton daily = Theme.button("Сетка дня", Theme.GREEN);
        JButton statistics = Theme.button("Статистика", Theme.BLUE);
        JButton settings = Theme.button("Настройки", Theme.BLUE);
        JButton records = Theme.button("Рекорды", Theme.BLUE);
        JButton exit = Theme.button("Выход", Theme.RED_DARK);

        play.setAlignmentX(Component.CENTER_ALIGNMENT);
        daily.setAlignmentX(Component.CENTER_ALIGNMENT);
        statistics.setAlignmentX(Component.CENTER_ALIGNMENT);
        settings.setAlignmentX(Component.CENTER_ALIGNMENT);
        records.setAlignmentX(Component.CENTER_ALIGNMENT);
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);

        play.addActionListener(e -> startNewGame());
        daily.addActionListener(e -> startDailyGame());
        statistics.addActionListener(e -> showStatistics());
        settings.addActionListener(e -> cards.show(root, SETTINGS));
        records.addActionListener(e -> showLeaderboard());
        exit.addActionListener(e -> dispose());

        content.add(title);
        content.add(Box.createVerticalStrut(8));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(30));
        content.add(play);
        content.add(Box.createVerticalStrut(10));
        content.add(daily);
        content.add(Box.createVerticalStrut(10));
        content.add(statistics);
        content.add(Box.createVerticalStrut(10));
        content.add(settings);
        content.add(Box.createVerticalStrut(10));
        content.add(records);
        content.add(Box.createVerticalStrut(10));
        content.add(exit);

        panel.add(content);
        return panel;
    }

    private JPanel buildSettings() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BACKGROUND);

        JPanel card = new JPanel();
        Theme.styleCard(card);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Параметры игры");
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.RED);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(6));

        JLabel note = new JLabel("Все параметры проверяются до генерации и сохраняются между запусками.");
        note.setFont(Theme.BODY);
        note.setForeground(Theme.MUTED);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(note);
        card.add(Box.createVerticalStrut(18));

        difficultyBox = new JComboBox<>();
        for (Difficulty value : Difficulty.values()) {
            difficultyBox.addItem(value.displayName());
        }
        difficultyBox.setSelectedItem(settings.difficulty().displayName());
        card.add(settingRow("Сложность", difficultyBox));
        card.add(Box.createVerticalStrut(10));

        categoryBox = new JComboBox<>();
        List<WordCategory> categories = WordCategory.all();
        for (WordCategory value : categories) {
            categoryBox.addItem(value.displayName() + " — " + value.wordCountText());
        }
        categoryBox.setSelectedIndex(Math.max(0, categories.indexOf(settings.category())));
        card.add(settingRow("Категория", categoryBox));
        card.add(Box.createVerticalStrut(10));

        sizeBox = new JComboBox<>(GameSettings.BOARD_SIZES.toArray(new GameSettings.BoardSize[0]));
        sizeBox.setSelectedItem(new GameSettings.BoardSize(settings.height(), settings.width()));
        card.add(settingRow("Размер сетки", sizeBox));
        card.add(Box.createVerticalStrut(10));

        durationSpinner = new JSpinner(new SpinnerNumberModel(
                (int) settings.timeLimit().toMinutes(),
                GameSettings.MIN_MINUTES,
                GameSettings.MAX_MINUTES,
                1));
        card.add(settingRow("Длительность, мин", durationSpinner));
        card.add(Box.createVerticalStrut(18));

        JLabel appearanceTitle = new JLabel("Оформление");
        appearanceTitle.setFont(Theme.HEADING);
        appearanceTitle.setForeground(Theme.BLUE);
        appearanceTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(appearanceTitle);
        card.add(Box.createVerticalStrut(8));

        themeBox = new JComboBox<>(ThemeChoice.values());
        themeBox.setSelectedItem(uiSettings.theme());
        card.add(settingRow("Тема", themeBox));
        card.add(Box.createVerticalStrut(10));

        soundBox = new JCheckBox("Звуковые эффекты", uiSettings.soundEnabled());
        styleCheckBox(soundBox);
        card.add(soundBox);
        card.add(Box.createVerticalStrut(8));

        animationsBox = new JCheckBox("Анимация подсветки", uiSettings.animationsEnabled());
        styleCheckBox(animationsBox);
        card.add(animationsBox);
        card.add(Box.createVerticalStrut(14));

        settingsStatus = new JLabel(" ");
        settingsStatus.setFont(Theme.SMALL);
        settingsStatus.setForeground(Theme.MUTED);
        settingsStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(settingsStatus);
        card.add(Box.createVerticalStrut(18));

        difficultyBox.addActionListener(e -> applyDifficultyDefaults());
        categoryBox.addActionListener(e -> clearSettingsStatus());
        sizeBox.addActionListener(e -> clearSettingsStatus());
        durationSpinner.addChangeListener(e -> clearSettingsStatus());
        themeBox.addActionListener(e -> themeChanged());
        soundBox.addActionListener(e -> uiSettingChanged());
        animationsBox.addActionListener(e -> uiSettingChanged());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton back = Theme.button("Назад", Theme.BLUE);
        JButton play = Theme.button("Играть", Theme.GREEN);
        back.addActionListener(e -> {
            if (applySettingsFromControls()) {
                cards.show(root, MENU);
            }
        });
        play.addActionListener(e -> {
            if (applySettingsFromControls()) {
                startNewGame();
            }
        });
        actions.add(play);
        actions.add(back);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(actions);

        panel.add(card);
        return panel;
    }

    private JPanel settingRow(String labelText, JComponent control) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(Theme.BODY);
        label.setForeground(Theme.TEXT);
        label.setPreferredSize(new Dimension(170, 30));

        control.setFont(Theme.BODY);

        row.add(label, BorderLayout.WEST);
        row.add(control, BorderLayout.CENTER);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return row;
    }

    private void applyDifficultyDefaults() {
        if (updatingSettingsControls) return;

        Difficulty selected = selectedDifficulty();
        if (selected == null) return;

        GameConfig profile = selected.config();
        updatingSettingsControls = true;
        try {
            sizeBox.setSelectedItem(new GameSettings.BoardSize(profile.height(), profile.width()));
            durationSpinner.setValue((int) profile.timeLimit().toMinutes());
        } finally {
            updatingSettingsControls = false;
        }
        showSettingsStatus("Размер и время обновлены по сложности «"
                + selected.displayName() + "».", Theme.MUTED);
    }

    private boolean applySettingsFromControls() {
        Difficulty selectedDifficulty = selectedDifficulty();
        WordCategory selectedCategory = selectedCategory();
        GameSettings.BoardSize size = (GameSettings.BoardSize) sizeBox.getSelectedItem();

        if (selectedDifficulty == null || selectedCategory == null || size == null) {
            showSettingsStatus("Выберите сложность, категорию и размер сетки.", Theme.RED);
            return false;
        }

        GameSettings candidate;
        try {
            candidate = new GameSettings(selectedDifficulty, selectedCategory,
                    size.height(), size.width(),
                    Duration.ofMinutes((Integer) durationSpinner.getValue()));
        } catch (IllegalArgumentException e) {
            showSettingsStatus(e.getMessage(), Theme.RED);
            return false;
        }

        String error = candidate.validationError();
        if (error != null) {
            showSettingsStatus(error, Theme.RED);
            return false;
        }

        settings = candidate;
        gamePanel.applyUiSettings(uiSettings);
        try {
            settingsStore.save(candidate);
            settingsStore.saveUi(uiSettings);
            showSettingsStatus("Настройки сохранены.", Theme.GREEN);
        } catch (IOException e) {
            showSettingsStatus("Не удалось сохранить настройки: " + e.getMessage(), Theme.RED);
        }
        return true;
    }

    private void styleCheckBox(JCheckBox box) {
        box.setFont(Theme.BODY);
        box.setOpaque(false);
        box.setForeground(Theme.TEXT);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void themeChanged() {
        UiSettings candidate = uiSettingsFromControls();
        if (candidate == null) return;

        uiSettings = candidate;
        persistUi();
        Theme.apply(uiSettings.theme());
        rebuildUi();
        cards.show(root, SETTINGS);
    }

    private void uiSettingChanged() {
        UiSettings candidate = uiSettingsFromControls();
        if (candidate == null) return;

        uiSettings = candidate;
        gamePanel.applyUiSettings(uiSettings);
        persistUi();
    }

    private UiSettings uiSettingsFromControls() {
        if (themeBox == null || soundBox == null || animationsBox == null) return null;
        ThemeChoice theme = (ThemeChoice) themeBox.getSelectedItem();
        if (theme == null) return null;
        return new UiSettings(theme, soundBox.isSelected(), animationsBox.isSelected());
    }

    private void persistUi() {
        try {
            settingsStore.saveUi(uiSettings);
            showSettingsStatus("Оформление сохранено.", Theme.GREEN);
        } catch (IOException e) {
            showSettingsStatus("Не удалось сохранить оформление: " + e.getMessage(), Theme.RED);
        }
    }

    private void rebuildUi() {
        root.removeAll();
        gamePanel = new GamePanel(this::roundFinished, this::showMenu, repository);
        gamePanel.applyUiSettings(uiSettings);
        root.add(buildMenu(), MENU);
        root.add(buildSettings(), SETTINGS);
        root.add(gamePanel, GAME);
        root.add(buildFinish(), FINISH);
        root.revalidate();
        root.repaint();
    }

    private Difficulty selectedDifficulty() {
        int index = difficultyBox.getSelectedIndex();
        return index < 0 ? null : Difficulty.values()[index];
    }

    private WordCategory selectedCategory() {
        int index = categoryBox.getSelectedIndex();
        return index < 0 ? null : WordCategory.all().get(index);
    }

    private void showSettingsStatus(String text, Color color) {
        settingsStatus.setForeground(color);
        settingsStatus.setText(text);
    }

    private void clearSettingsStatus() {
        if (updatingSettingsControls) return;
        settingsStatus.setText(" ");
    }

    private JPanel buildFinish() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BACKGROUND);

        JPanel card = new JPanel();
        Theme.styleCard(card);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        finishTitle = new JLabel("Раунд пройден");
        finishSubtitle = new JLabel();
        finishScore = new JLabel();
        finishBreakdown = new JLabel();
        finishTotal = new JLabel();

        finishTitle.setFont(Theme.TITLE);
        finishTitle.setForeground(Theme.RED);
        finishTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        finishSubtitle.setFont(Theme.BODY);
        finishSubtitle.setForeground(Theme.MUTED);
        finishSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        finishScore.setFont(Theme.HEADING);
        finishScore.setForeground(Theme.BLUE);
        finishScore.setHorizontalAlignment(SwingConstants.CENTER);
        finishScore.setAlignmentX(Component.CENTER_ALIGNMENT);

        finishBreakdown.setFont(Theme.BODY);
        finishBreakdown.setForeground(Theme.TEXT);
        finishBreakdown.setAlignmentX(Component.CENTER_ALIGNMENT);

        finishTotal.setFont(Theme.BODY);
        finishTotal.setForeground(Theme.GREEN);
        finishTotal.setAlignmentX(Component.CENTER_ALIGNMENT);

        nextRoundButton = Theme.button("Следующий раунд", Theme.GREEN);
        JButton menu = Theme.button("В меню", Theme.BLUE);
        nextRoundButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        menu.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextRoundButton.addActionListener(e -> {
            round++;
            showGame();
        });
        menu.addActionListener(e -> showMenu());

        card.add(finishTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(finishSubtitle);
        card.add(Box.createVerticalStrut(22));
        card.add(finishScore);
        card.add(Box.createVerticalStrut(8));
        card.add(finishBreakdown);
        card.add(Box.createVerticalStrut(10));
        card.add(finishTotal);
        card.add(Box.createVerticalStrut(26));
        card.add(nextRoundButton);
        card.add(Box.createVerticalStrut(10));
        card.add(menu);

        panel.add(card);
        return panel;
    }

    private void startNewGame() {
        dailyMode = false;
        dailyDate = null;
        activeSettings = settings;
        round = 1;
        totalScore = 0;
        showGame();
    }

    private void startDailyGame() {
        dailyMode = true;
        dailyDate = DailyPuzzle.today();
        activeSettings = DailyPuzzle.settings();
        round = 1;
        totalScore = 0;
        showGame();
    }

    private void showGame() {
        cards.show(root, GAME);
        if (dailyMode) {
            gamePanel.startRound(activeSettings, round, totalScore,
                    DailyPuzzle.RESULT_LABEL,
                    "Сетка дня · " + DailyPuzzle.formatDate(dailyDate)
                            + " · " + activeSettings.height() + "×" + activeSettings.width(),
                    DailyPuzzle.seed(dailyDate));
        } else {
            gamePanel.startRound(activeSettings, round, totalScore);
        }
    }

    private void showMenu() {
        gamePanel.stopRound();
        cards.show(root, MENU);
    }

    private void roundFinished(GameResult result, boolean timeExpired) {
        totalScore += result.totalScore();
        finishTitle.setText(timeExpired
                ? "Время вышло"
                : (dailyMode ? "Сетка дня пройдена" : "Раунд пройден"));

        String mode = dailyMode
                ? "Сетка дня · " + DailyPuzzle.formatDate(dailyDate)
                : "Раунд " + round;
        String difficultyName = dailyMode
                ? DailyPuzzle.RESULT_LABEL
                : activeSettings.difficulty().displayName();
        finishSubtitle.setText(mode + " · " + difficultyName
                + " · " + activeSettings.category().displayName()
                + " · сетка " + activeSettings.height() + "×" + activeSettings.width()
                + ": найдено " + result.foundWords() + " из " + result.totalWords()
                + " · время " + GameTimer.format(result.elapsedSeconds()));
        finishScore.setText("Очки за раунд: " + result.totalScore());
        finishBreakdown.setText(GamePanel.breakdown(result.score()));
        finishTotal.setText("Всего очков: " + totalScore);
        nextRoundButton.setVisible(!dailyMode);
        cards.show(root, FINISH);
    }

    private void showStatistics() {
        DailyStatistics statistics = DailyStatistics.from(repository.load());

        StringBuilder text = new StringBuilder();
        text.append("Лучший счёт: ").append(statistics.bestScore()).append('\n')
                .append("Серия дней: ").append(statistics.currentStreak()).append('\n')
                .append("Дней со статистикой: ").append(statistics.daysPlayed()).append('\n')
                .append("Найдено слов: ").append(statistics.wordsFound());

        JOptionPane.showMessageDialog(this, text.toString(),
                "Личная статистика", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showLeaderboard() {
        GameConfig config = settings.toConfig();
        String name = leaderboardName();
        List<GameResult> top = repository.top(
                config.height(), config.width(), name, 5);

        StringBuilder text = new StringBuilder();
        text.append("Сложность: ").append(settings.difficulty().displayName())
                .append(" · ").append(settings.category().displayName())
                .append(" · сетка ").append(config.height()).append("×").append(config.width())
                .append("\n\n");
        if (top.isEmpty()) {
            text.append("Пока нет сохранённых игр.");
        } else {
            for (int i = 0; i < top.size(); i++) {
                GameResult result = top.get(i);
                text.append(i + 1).append(". ")
                        .append(result.totalScore()).append(" очков · ")
                        .append(result.foundWords()).append("/").append(result.totalWords())
                        .append(" · ").append(GameTimer.format(result.elapsedSeconds()))
                        .append("\n");
            }
        }

        JOptionPane.showMessageDialog(this, text.toString(), "Рекорды",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private String leaderboardName() {
        return settings.difficulty().displayName() + " · " + settings.category().displayName();
    }
}
