package game.ui;

import game.config.Difficulty;
import game.config.GameConfig;
import game.dictionary.WordCategory;
import game.play.GameTimer;
import game.result.GameResult;
import game.result.ResultRepository;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.util.List;

public final class MainWindow extends JFrame {
    private static final String MENU = "menu";
    private static final String SETTINGS = "settings";
    private static final String GAME = "game";
    private static final String FINISH = "finish";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final ResultRepository repository = ResultRepository.defaultRepository();
    private final GamePanel gamePanel;
    private final JLabel finishTitle = new JLabel("Раунд пройден");
    private final JLabel finishSubtitle = new JLabel();
    private final JLabel finishScore = new JLabel();
    private final JLabel finishBreakdown = new JLabel();
    private final JLabel finishTotal = new JLabel();

    private Difficulty difficulty = Difficulty.NORMAL;
    private WordCategory category = WordCategory.defaultCategory();
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

        gamePanel = new GamePanel(this::roundFinished, this::showMenu, repository);
        root.add(buildMenu(), MENU);
        root.add(buildSettings(), SETTINGS);
        root.add(gamePanel, GAME);
        root.add(buildFinish(), FINISH);
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

        JLabel subtitle = new JLabel("Бесконечный поиск слов · сетка по сложности");
        subtitle.setFont(Theme.BODY);
        subtitle.setForeground(new Color(0xFFE0E0));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton play = Theme.button("Играть", Theme.GREEN);
        JButton settings = Theme.button("Настройки", Theme.BLUE);
        JButton records = Theme.button("Рекорды", Theme.BLUE);
        JButton exit = Theme.button("Выход", Theme.RED_DARK);

        play.setAlignmentX(Component.CENTER_ALIGNMENT);
        settings.setAlignmentX(Component.CENTER_ALIGNMENT);
        records.setAlignmentX(Component.CENTER_ALIGNMENT);
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);

        play.addActionListener(e -> startNewGame());
        settings.addActionListener(e -> cards.show(root, SETTINGS));
        records.addActionListener(e -> showLeaderboard());
        exit.addActionListener(e -> dispose());

        content.add(title);
        content.add(Box.createVerticalStrut(8));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(38));
        content.add(play);
        content.add(Box.createVerticalStrut(12));
        content.add(settings);
        content.add(Box.createVerticalStrut(12));
        content.add(records);
        content.add(Box.createVerticalStrut(12));
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

        JLabel title = new JLabel("Настройки");
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.RED);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(6));

        JLabel note = new JLabel("Категория задаёт словарь, сложность — размер, время и подсказки.");
        note.setFont(Theme.BODY);
        note.setForeground(Theme.MUTED);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(note);
        card.add(Box.createVerticalStrut(18));

        ButtonGroup difficultyGroup = new ButtonGroup();
        for (Difficulty value : Difficulty.values()) {
            GameConfig config = value.config();
            JRadioButton option = new JRadioButton(value.displayName()
                    + " — сетка " + config.height() + "×" + config.width()
                    + ", " + config.timeLimit().toMinutes() + " мин, подсказок " + config.hints());
            option.setFont(Theme.BODY);
            option.setOpaque(false);
            option.setForeground(Theme.TEXT);
            option.setAlignmentX(Component.LEFT_ALIGNMENT);
            option.setSelected(value == difficulty);
            option.addActionListener(e -> difficulty = value);
            difficultyGroup.add(option);
            card.add(option);
            card.add(Box.createVerticalStrut(8));
        }

        card.add(Box.createVerticalStrut(10));
        JLabel categoryTitle = new JLabel("Категория слов");
        categoryTitle.setFont(Theme.HEADING);
        categoryTitle.setForeground(Theme.BLUE);
        categoryTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(categoryTitle);
        card.add(Box.createVerticalStrut(8));

        ButtonGroup categoryGroup = new ButtonGroup();
        for (WordCategory value : WordCategory.all()) {
            JRadioButton option = new JRadioButton(value.displayName()
                    + " — " + value.words().size() + " слов");
            option.setFont(Theme.BODY);
            option.setOpaque(false);
            option.setForeground(Theme.TEXT);
            option.setAlignmentX(Component.LEFT_ALIGNMENT);
            option.setSelected(value == category);
            option.addActionListener(e -> category = value);
            categoryGroup.add(option);
            card.add(option);
            card.add(Box.createVerticalStrut(6));
        }

        card.add(Box.createVerticalStrut(20));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton back = Theme.button("Назад", Theme.BLUE);
        JButton play = Theme.button("Играть", Theme.GREEN);
        back.addActionListener(e -> cards.show(root, MENU));
        play.addActionListener(e -> startNewGame());
        actions.add(play);
        actions.add(back);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(actions);

        panel.add(card);
        return panel;
    }

    private JPanel buildFinish() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BACKGROUND);

        JPanel card = new JPanel();
        Theme.styleCard(card);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

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

        JButton next = Theme.button("Следующий раунд", Theme.GREEN);
        JButton menu = Theme.button("В меню", Theme.BLUE);
        next.setAlignmentX(Component.CENTER_ALIGNMENT);
        menu.setAlignmentX(Component.CENTER_ALIGNMENT);
        next.addActionListener(e -> {
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
        card.add(next);
        card.add(Box.createVerticalStrut(10));
        card.add(menu);

        panel.add(card);
        return panel;
    }

    private void startNewGame() {
        round = 1;
        totalScore = 0;
        showGame();
    }

    private void showGame() {
        cards.show(root, GAME);
        gamePanel.startRound(difficulty, category, round, totalScore);
    }

    private void showMenu() {
        gamePanel.stopRound();
        cards.show(root, MENU);
    }

    private void roundFinished(GameResult result, boolean timeExpired) {
        totalScore += result.totalScore();
        finishTitle.setText(timeExpired ? "Время вышло" : "Раунд пройден");
        finishSubtitle.setText("Раунд " + round + " · " + category.displayName() + ": найдено "
                + result.foundWords() + " из " + result.totalWords()
                + " · время " + GameTimer.format(result.elapsedSeconds()));
        finishScore.setText("Очки за раунд: " + result.totalScore());
        finishBreakdown.setText(GamePanel.breakdown(result.score()));
        finishTotal.setText("Всего очков: " + totalScore);
        cards.show(root, FINISH);
    }

    private void showLeaderboard() {
        GameConfig config = difficulty.config();
        String name = leaderboardName();
        List<GameResult> top = repository.top(
                config.height(), config.width(), name, 5);

        StringBuilder text = new StringBuilder();
        text.append("Сложность: ").append(difficulty.displayName())
                .append(" · ").append(category.displayName())
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
        return difficulty.displayName() + " · " + category.displayName();
    }
}
