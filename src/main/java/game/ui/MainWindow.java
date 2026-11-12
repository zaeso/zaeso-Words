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
import game.result.ResultCsvExporter;
import game.result.ResultRepository;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class MainWindow extends JFrame {
    private static final String MENU = "menu";
    private static final String SETTINGS = "settings";
    private static final String GAME = "game";
    private static final String FINISH = "finish";
    private static final DateTimeFormatter HISTORY_DATE =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final ResultRepository repository = ResultRepository.defaultRepository();
    private final SettingsStore settingsStore = SettingsStore.defaultStore();
    private GamePanel gamePanel;
    private JLabel finishTitle;
    private JLabel finishSubtitle;
    private JLabel finishScore;
    private JLabel finishTotal;
    private JLabel tileLetters;
    private JLabel tileTime;
    private JLabel tileMistakes;
    private JLabel tileHints;
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
        GradientPanel panel = GradientPanel.menu();
        panel.setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("ZAESO WORDS");
        title.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Бесконечный поиск слов · сетка дня и личная статистика");
        subtitle.setFont(Theme.BODY);
        subtitle.setForeground(Theme.SUBTITLE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel version = new JLabel("версия 1.1.3 · работает на Java 8+");
        version.setFont(Theme.SMALL);
        version.setForeground(new Color(255, 255, 255, 175));
        version.setAlignmentX(Component.CENTER_ALIGNMENT);

        FlatButton play = menuButton("Играть", Theme.GREEN);
        FlatButton daily = menuButton("Сетка дня", Theme.ORANGE);
        FlatButton statistics = menuButton("Статистика", Theme.TEAL);
        FlatButton settings = menuButton("Настройки", Theme.PURPLE);
        FlatButton records = menuButton("Рекорды", Theme.BLUE);
        FlatButton exit = menuButton("Выход", Theme.RED_DARK);

        play.addActionListener(e -> startNewGame());
        daily.addActionListener(e -> startDailyGame());
        statistics.addActionListener(e -> showStatistics());
        settings.addActionListener(e -> cards.show(root, SETTINGS));
        records.addActionListener(e -> showLeaderboard());
        exit.addActionListener(e -> dispose());

        content.add(title);
        content.add(Box.createVerticalStrut(8));
        content.add(accentBar());
        content.add(Box.createVerticalStrut(10));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(30));
        content.add(play);
        content.add(Box.createVerticalStrut(12));
        content.add(daily);
        content.add(Box.createVerticalStrut(12));
        content.add(statistics);
        content.add(Box.createVerticalStrut(12));
        content.add(settings);
        content.add(Box.createVerticalStrut(12));
        content.add(records);
        content.add(Box.createVerticalStrut(12));
        content.add(exit);
        content.add(Box.createVerticalStrut(26));
        content.add(version);

        panel.add(content);
        return panel;
    }

    /** Светлая полоска-акцент под заголовком меню. */
    private JComponent accentBar() {
        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                try {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(new Color(255, 255, 255, 150));
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
                } finally {
                    g.dispose();
                }
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(150, 5));
        bar.setMaximumSize(new Dimension(150, 5));
        bar.setAlignmentX(Component.CENTER_ALIGNMENT);
        return bar;
    }

    private FlatButton menuButton(String text, Color color) {
        FlatButton button = new FlatButton(text, color);
        button.large();
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 52));
        return button;
    }

    private JPanel buildSettings() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BACKGROUND);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.PANEL);
        card.setBorder(Theme.shadowCardBorder());

        GradientPanel header = new GradientPanel(Theme.BLUE, Theme.BLUE_DARK);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel headerTitle = new JLabel("Параметры игры");
        headerTitle.setFont(Theme.HEADING);
        headerTitle.setForeground(Color.WHITE);
        headerTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel headerNote = new JLabel("Все параметры проверяются до генерации и сохраняются между запусками.");
        headerNote.setFont(Theme.SMALL);
        headerNote.setForeground(new Color(0xE3F2FD));
        headerNote.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(headerTitle);
        header.add(Box.createVerticalStrut(4));
        header.add(headerNote);
        card.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        Theme.styleBody(body);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        card.add(body, BorderLayout.CENTER);

        difficultyBox = new JComboBox<>();
        for (Difficulty value : Difficulty.values()) {
            difficultyBox.addItem(value.displayName());
        }
        difficultyBox.setSelectedItem(settings.difficulty().displayName());
        body.add(settingRow("Сложность", difficultyBox));
        body.add(Box.createVerticalStrut(10));

        categoryBox = new JComboBox<>();
        List<WordCategory> categories = WordCategory.all();
        for (WordCategory value : categories) {
            categoryBox.addItem(value.displayName() + " — " + value.wordCountText());
        }
        categoryBox.setSelectedIndex(Math.max(0, categories.indexOf(settings.category())));
        body.add(settingRow("Категория", categoryBox));
        body.add(Box.createVerticalStrut(10));

        sizeBox = new JComboBox<>(GameSettings.BOARD_SIZES.toArray(new GameSettings.BoardSize[0]));
        sizeBox.setSelectedItem(new GameSettings.BoardSize(settings.height(), settings.width()));
        body.add(settingRow("Размер сетки", sizeBox));
        body.add(Box.createVerticalStrut(10));

        durationSpinner = new JSpinner(new SpinnerNumberModel(
                (int) settings.timeLimit().toMinutes(),
                GameSettings.MIN_MINUTES,
                GameSettings.MAX_MINUTES,
                1));
        body.add(settingRow("Длительность, мин", durationSpinner));
        body.add(Box.createVerticalStrut(16));

        JLabel appearanceTitle = new JLabel("Оформление");
        appearanceTitle.setFont(Theme.HEADING);
        appearanceTitle.setForeground(Theme.PURPLE);
        appearanceTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(appearanceTitle);
        body.add(Box.createVerticalStrut(8));

        themeBox = new JComboBox<>(ThemeChoice.values());
        themeBox.setSelectedItem(uiSettings.theme());
        body.add(settingRow("Тема", themeBox));
        body.add(Box.createVerticalStrut(10));

        soundBox = new JCheckBox("Звуковые эффекты", uiSettings.soundEnabled());
        styleCheckBox(soundBox);
        body.add(soundBox);
        body.add(Box.createVerticalStrut(8));

        animationsBox = new JCheckBox("Анимация подсветки", uiSettings.animationsEnabled());
        styleCheckBox(animationsBox);
        body.add(animationsBox);
        body.add(Box.createVerticalStrut(14));

        settingsStatus = new JLabel(" ");
        settingsStatus.setFont(Theme.BODY);
        settingsStatus.setForeground(Theme.MUTED);
        settingsStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(settingsStatus);
        body.add(Box.createVerticalStrut(18));

        difficultyBox.addActionListener(e -> applyDifficultyDefaults());
        categoryBox.addActionListener(e -> clearSettingsStatus());
        sizeBox.addActionListener(e -> clearSettingsStatus());
        durationSpinner.addChangeListener(e -> clearSettingsStatus());
        themeBox.addActionListener(e -> themeChanged());
        soundBox.addActionListener(e -> uiSettingChanged());
        animationsBox.addActionListener(e -> uiSettingChanged());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton save = Theme.button("Сохранить", Theme.BLUE);
        JButton play = Theme.button("Играть", Theme.GREEN);
        JButton back = Theme.button("Назад", Theme.MUTED);
        save.addActionListener(e -> applySettingsFromControls());
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
        actions.add(save);
        actions.add(play);
        actions.add(back);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(actions);

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
            showSettingsStatus("Настройки сохранены в ~/.zaeso-words/settings.properties.", Theme.GREEN);
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

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.PANEL);
        card.setBorder(Theme.shadowCardBorder());

        GradientPanel header = new GradientPanel(Theme.RED, Theme.RED_DARK);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        finishTitle = new JLabel("Раунд пройден");
        finishTitle.setFont(Theme.TITLE);
        finishTitle.setForeground(Color.WHITE);
        finishTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        finishSubtitle = new JLabel();
        finishSubtitle.setFont(Theme.SMALL);
        finishSubtitle.setForeground(new Color(0xFFE0E0));
        finishSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(finishTitle);
        header.add(Box.createVerticalStrut(6));
        header.add(finishSubtitle);
        card.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        Theme.styleBody(body);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        card.add(body, BorderLayout.CENTER);

        finishScore = new JLabel();
        finishScore.setFont(Theme.HEADING);
        finishScore.setForeground(Theme.BLUE);
        finishScore.setHorizontalAlignment(SwingConstants.CENTER);
        finishScore.setAlignmentX(Component.CENTER_ALIGNMENT);

        finishTotal = new JLabel();
        finishTotal.setFont(Theme.HEADING);
        finishTotal.setForeground(Theme.GREEN);
        finishTotal.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel scoreTile = new JPanel();
        scoreTile.setLayout(new BoxLayout(scoreTile, BoxLayout.Y_AXIS));
        scoreTile.setOpaque(true);
        scoreTile.setBackground(Theme.PANEL);
        scoreTile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BLUE, 1, true),
                BorderFactory.createEmptyBorder(16, 26, 16, 26)));
        scoreTile.setMaximumSize(new Dimension(520, 220));
        scoreTile.add(finishScore);

        JPanel breakdown = new JPanel(new java.awt.GridLayout(1, 4, 8, 0));
        breakdown.setOpaque(false);
        breakdown.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        breakdown.add(miniTile(tileLetters = new JLabel(), "Буквы", Theme.GREEN));
        breakdown.add(miniTile(tileTime = new JLabel(), "Время", Theme.BLUE));
        breakdown.add(miniTile(tileMistakes = new JLabel(), "Ошибки", Theme.RED));
        breakdown.add(miniTile(tileHints = new JLabel(), "Подсказки", Theme.PURPLE));
        scoreTile.add(breakdown);
        scoreTile.setAlignmentX(Component.CENTER_ALIGNMENT);

        body.add(scoreTile);
        body.add(Box.createVerticalStrut(14));
        body.add(finishTotal);
        body.add(Box.createVerticalStrut(26));

        nextRoundButton = Theme.button("Следующий раунд", Theme.GREEN);
        JButton menu = Theme.button("В меню", Theme.BLUE);
        nextRoundButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        menu.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextRoundButton.addActionListener(e -> {
            round++;
            showGame();
        });
        menu.addActionListener(e -> showMenu());

        body.add(nextRoundButton);
        body.add(Box.createVerticalStrut(10));
        body.add(menu);

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
        if (timeExpired) {
            finishTitle.setText("Время вышло");
        } else if ("выход игрока".equals(result.finishReason())) {
            finishTitle.setText("Раунд завершён");
        } else {
            finishTitle.setText(dailyMode
                    ? "Сетка дня пройдена" : "Раунд пройден");
        }

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
        game.score.Score score = result.score();
        tileLetters.setText("+" + score.wordPoints());
        tileTime.setText("+" + score.timeBonus());
        tileMistakes.setText("−" + score.mistakePenalty());
        tileHints.setText("−" + score.hintPenalty());
        finishTotal.setText("Всего очков: " + totalScore);
        nextRoundButton.setVisible(!dailyMode);
        cards.show(root, FINISH);
    }

    private void showStatistics() {
        DailyStatistics statistics = DailyStatistics.from(repository.load());

        JDialog dialog = new JDialog(this, "Личная статистика", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Theme.BACKGROUND);

        GradientPanel header = new GradientPanel(Theme.TEAL, Theme.TEAL.darker());
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        JLabel title = new JLabel("Личная статистика · сетка дня");
        title.setFont(Theme.HEADING);
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        dialog.add(header, BorderLayout.NORTH);

        JPanel tiles = new JPanel(new java.awt.GridLayout(2, 2, 12, 12));
        tiles.setOpaque(false);
        tiles.setBorder(BorderFactory.createEmptyBorder(18, 18, 6, 18));
        tiles.add(statTile("Лучший счёт", String.valueOf(statistics.bestScore()), Theme.RED));
        tiles.add(statTile("Серия дней", String.valueOf(statistics.currentStreak()), Theme.GREEN));
        tiles.add(statTile("Дней с играми", String.valueOf(statistics.daysPlayed()), Theme.BLUE));
        tiles.add(statTile("Найдено слов", String.valueOf(statistics.wordsFound()), Theme.PURPLE));
        dialog.add(tiles, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(0, 18, 18, 18));
        JButton close = Theme.button("Закрыть", Theme.TEAL);
        close.addActionListener(e -> dialog.dispose());
        actions.add(close);
        dialog.add(actions, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setSize(Math.max(420, dialog.getWidth()), dialog.getHeight());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /** Мини-плитка разбора очков на экране итогов. */
    private JPanel miniTile(JLabel value, String caption, Color color) {
        value.setFont(Theme.HEADING);
        value.setForeground(color);
        value.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(Theme.SMALL);
        captionLabel.setForeground(Theme.MUTED);
        captionLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel tile = new JPanel(new BorderLayout(0, 2));
        tile.setOpaque(true);
        tile.setBackground(Theme.PANEL);
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1, true),
                BorderFactory.createEmptyBorder(10, 8, 10, 8)));
        tile.add(value, BorderLayout.CENTER);
        tile.add(captionLabel, BorderLayout.SOUTH);
        return tile;
    }

    private JPanel statTile(String caption, String value, Color color) {
        JPanel tile = new JPanel(new BorderLayout());
        tile.setBackground(Theme.PANEL);
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1, true),
                BorderFactory.createEmptyBorder()));

        JPanel strip = new JPanel();
        strip.setBackground(color);
        strip.setPreferredSize(new Dimension(1, 6));

        JPanel inner = new JPanel(new BorderLayout(0, 4));
        inner.setOpaque(false);
        inner.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(Theme.TITLE);
        valueLabel.setForeground(color);

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(Theme.BODY);
        captionLabel.setForeground(Theme.MUTED);

        inner.add(valueLabel, BorderLayout.NORTH);
        inner.add(captionLabel, BorderLayout.SOUTH);
        tile.add(strip, BorderLayout.NORTH);
        tile.add(inner, BorderLayout.CENTER);
        return tile;
    }

    private void showLeaderboard() {
        GameConfig config = settings.toConfig();
        String name = leaderboardName();
        List<GameResult> history = repository.history(
                config.height(), config.width(), name);

        JDialog dialog = new JDialog(this, "Рекорды", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Theme.BACKGROUND);

        GradientPanel header = new GradientPanel(Theme.BLUE, Theme.BLUE_DARK);
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        JLabel headerTitle = new JLabel("Полная таблица рекордов");
        headerTitle.setFont(Theme.HEADING);
        headerTitle.setForeground(Color.WHITE);
        JLabel headerDetails = new JLabel(settings.difficulty().displayName()
                + " · " + settings.category().displayName()
                + " · сетка " + config.height() + "×" + config.width()
                + " · записей: " + history.size());
        headerDetails.setFont(Theme.SMALL);
        headerDetails.setForeground(new Color(0xE3F2FD));
        header.add(headerTitle, BorderLayout.WEST);
        header.add(headerDetails, BorderLayout.EAST);
        dialog.add(header, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"#", "Дата", "Очки", "Найдено", "Время", "Подсказки", "Причина"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (int i = 0; i < history.size(); i++) {
            GameResult result = history.get(i);
            model.addRow(new Object[]{
                    i + 1,
                    historyDate(result.finishedAtEpochSeconds()),
                    result.totalScore(),
                    result.foundWords() + "/" + result.totalWords(),
                    GameTimer.format(result.elapsedSeconds()),
                    result.hintsUsed(),
                    result.finishReason()
            });
        }

        JTable table = new JTable(model);
        table.setFont(Theme.BODY);
        table.setRowHeight(26);
        table.setBackground(Theme.PANEL);
        table.setForeground(Theme.TEXT);
        table.setGridColor(Theme.BORDER);
        table.setSelectionBackground(Theme.BLUE);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setFont(Theme.SMALL);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 16, 0, 16),
                BorderFactory.createLineBorder(Theme.BORDER)));
        scroll.setPreferredSize(new Dimension(820, 420));
        dialog.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
        JButton export = Theme.button("Экспорт CSV", Theme.GREEN);
        JButton close = Theme.button("Закрыть", Theme.RED_DARK);
        export.setEnabled(!repository.load().isEmpty());
        export.addActionListener(e -> exportHistory(dialog));
        close.addActionListener(e -> dialog.dispose());
        actions.add(export);
        actions.add(close);
        dialog.add(actions, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void exportHistory(Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Экспорт истории в CSV");
        chooser.setSelectedFile(new File("zaeso-words-history.csv"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path path = chooser.getSelectedFile().toPath();
        if (!path.getFileName().toString().toLowerCase().endsWith(".csv")) {
            path = path.resolveSibling(path.getFileName() + ".csv");
        }

        try {
            ResultCsvExporter.write(path, repository.load());
            JOptionPane.showMessageDialog(parent,
                    "История экспортирована:\n" + path,
                    "Экспорт CSV", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                    "Не удалось экспортировать историю:\n" + e.getMessage(),
                    "Ошибка экспорта", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String historyDate(long epochSeconds) {
        return Instant.ofEpochSecond(epochSeconds)
                .atZone(ZoneId.systemDefault())
                .format(HISTORY_DATE);
    }

    private String leaderboardName() {
        return settings.difficulty().displayName() + " · " + settings.category().displayName();
    }
}
