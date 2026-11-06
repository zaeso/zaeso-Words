package game;

import game.config.Difficulty;
import game.config.GameConfig;
import game.config.GameSettings;
import game.config.SettingsStore;
import game.dictionary.WordCategory;
import game.generator.GridGenerator;
import game.generator.GridGenerator.GenerationResult;
import game.model.Puzzle;
import game.play.ConsoleGame;
import game.ui.MainWindow;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        boolean console = Arrays.stream(args)
                .anyMatch(argument -> argument.equalsIgnoreCase("--console"));

        if (console) {
            runConsole();
            return;
        }
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Графический интерфейс недоступен. Запустите с --console.");
            return;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Остаётся стандартное оформление Swing.
        }
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }

    private static void runConsole() {
        Scanner scanner = new Scanner(System.in);
        SettingsStore settingsStore = SettingsStore.defaultStore();
        GameSettings saved = settingsStore.load();

        System.out.println("=== КЛАССИЧЕСКИЙ СКАНВОРД ===");
        printCategoryOptions();
        WordCategory category = chooseCategory(scanner, saved.category());
        if (category == null) return;
        System.out.println("Выбрана категория: " + category.displayName() + ".");

        printDifficultyOptions();
        Difficulty difficulty = chooseDifficulty(scanner, saved.difficulty());
        if (difficulty == null) return;

        GameConfig profile = difficulty.config();
        System.out.println("Выбрана сложность: " + difficulty.displayName() + ".");

        boolean sameDifficulty = difficulty == saved.difficulty();
        int defaultHeight = sameDifficulty ? saved.height() : profile.height();
        int defaultWidth = sameDifficulty ? saved.width() : profile.width();
        int[] size = chooseBoardSize(scanner, defaultHeight, defaultWidth);
        if (size == null) return;

        Duration defaultTime = sameDifficulty ? saved.timeLimit() : profile.timeLimit();
        Duration timeLimit = chooseDuration(scanner, defaultTime);
        if (timeLimit == null) return;

        GameSettings settings;
        try {
            settings = new GameSettings(difficulty, category, size[0], size[1], timeLimit);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        String validationError = settings.validationError();
        if (validationError != null) {
            System.out.println(validationError);
            return;
        }

        try {
            settingsStore.save(settings);
        } catch (IOException e) {
            System.out.println("Не удалось сохранить настройки: " + e.getMessage());
        }

        GameConfig config = settings.toConfig();
        GenerationResult generation = new GridGenerator().generateChecked(
                config.height(), config.width(), category.words(), config.minWords());

        if (!generation.ok()) {
            System.out.println(generation.error());
            return;
        }
        Puzzle puzzle = generation.puzzle();

        System.out.println("Параметры: " + difficulty.displayName() + ", "
                + category.displayName() + ", сетка " + config.height() + "×" + config.width()
                + ", " + config.timeLimit().toMinutes() + " мин.");
        System.out.println("\nНаправления размещения: горизонталь, вертикаль и диагонали.");
        System.out.println("Слова могут пересекаться, только если буквы в общей клетке совпадают.");
        new ConsoleGame(puzzle, scanner, config, leaderboardName(difficulty, category)).start();
    }

    static String leaderboardName(Difficulty difficulty, WordCategory category) {
        return difficulty.displayName() + " · " + category.displayName();
    }

    static WordCategory chooseCategory(Scanner scanner) {
        return chooseCategory(scanner, WordCategory.defaultCategory());
    }

    static WordCategory chooseCategory(Scanner scanner, WordCategory fallback) {
        while (true) {
            System.out.print("Категория (1-" + WordCategory.all().size()
                    + ", Enter — " + fallback.displayName() + "): ");

            if (!scanner.hasNextLine()) {
                System.out.println("\nВвод завершён.");
                return null;
            }

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return fallback;
            }

            WordCategory category = WordCategory.fromInput(input);
            if (category != null) {
                return category;
            }

            System.out.println("Неизвестная категория. Введите номер или название.");
        }
    }

    static Difficulty chooseDifficulty(Scanner scanner) {
        return chooseDifficulty(scanner, Difficulty.NORMAL);
    }

    static Difficulty chooseDifficulty(Scanner scanner, Difficulty fallback) {
        while (true) {
            System.out.print("Сложность (1-3, Enter — "
                    + fallback.displayName().toLowerCase() + "): ");

            if (!scanner.hasNextLine()) {
                System.out.println("\nВвод завершён.");
                return null;
            }

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return fallback;
            }

            Difficulty difficulty = Difficulty.fromInput(input);
            if (difficulty != null) {
                return difficulty;
            }

            System.out.println("Неизвестная сложность. Введите 1, 2, 3 или название.");
        }
    }

    static int[] chooseBoardSize(Scanner scanner) {
        return readBoardSize(scanner, null, null);
    }

    static int[] chooseBoardSize(Scanner scanner, GameConfig defaults) {
        return readBoardSize(scanner, defaults.height(), defaults.width());
    }

    static int[] chooseBoardSize(Scanner scanner, int defaultHeight, int defaultWidth) {
        return readBoardSize(scanner, defaultHeight, defaultWidth);
    }

    static Duration chooseDuration(Scanner scanner, Duration defaultLimit) {
        int defaultMinutes = (int) defaultLimit.toMinutes();
        while (true) {
            System.out.print("Длительность в минутах (" + GameSettings.MIN_MINUTES
                    + "-" + GameSettings.MAX_MINUTES + ", Enter — " + defaultMinutes + "): ");

            if (!scanner.hasNextLine()) {
                System.out.println("\nВвод завершён.");
                return null;
            }

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return Duration.ofMinutes(defaultMinutes);
            }

            try {
                int minutes = Integer.parseInt(input);
                if (minutes >= GameSettings.MIN_MINUTES && minutes <= GameSettings.MAX_MINUTES) {
                    return Duration.ofMinutes(minutes);
                }
            } catch (NumberFormatException ignored) {
                // Ниже выводится общее пояснение.
            }
            System.out.println("Введите целое число минут от " + GameSettings.MIN_MINUTES
                    + " до " + GameSettings.MAX_MINUTES + ".");
        }
    }

    private static int[] readBoardSize(Scanner scanner, Integer defaultHeight, Integer defaultWidth) {
        while (true) {
            Integer height = chooseDimension(
                    dimensionPrompt("Высота сетки", defaultHeight), scanner, defaultHeight);
            if (height == null) return null;

            Integer width = chooseDimension(
                    dimensionPrompt("Ширина сетки", defaultWidth), scanner, defaultWidth);
            if (width == null) return null;

            if (isAllowedSize(height, width)) {
                return new int[]{height, width};
            }

            System.out.println("Недопустимый размер " + height + "x" + width + ".");
            printAllowedSizes();
        }
    }

    static Integer chooseDimension(String prompt, Scanner scanner) {
        return chooseDimension(prompt, scanner, null);
    }

    static Integer chooseDimension(String prompt, Scanner scanner, Integer defaultValue) {
        while (true) {
            System.out.print(prompt);

            if (!scanner.hasNextLine()) {
                System.out.println("\nВвод завершён.");
                return null;
            }

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                if (defaultValue != null) {
                    return defaultValue;
                }
                System.out.println("Введите число.");
                continue;
            }

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Ожидается целое число.");
            }
        }
    }

    static boolean isAllowedSize(int h, int w) {
        return GameSettings.isAllowedSize(h, w);
    }

    private static void printCategoryOptions() {
        System.out.println("Категории слов:");
        List<WordCategory> categories = WordCategory.all();
        for (int i = 0; i < categories.size(); i++) {
            WordCategory category = categories.get(i);
            System.out.printf("  %d. %s — %d слов.%n",
                    i + 1, category.displayName(), category.words().size());
        }
    }

    private static void printDifficultyOptions() {
        System.out.println("Уровни сложности:");
        for (Difficulty difficulty : Difficulty.values()) {
            GameConfig config = difficulty.config();
            System.out.printf("  %d. %s — сетка %dx%d, минимум %d слов, %d мин., подсказок: %d.%n",
                    difficulty.ordinal() + 1,
                    difficulty.displayName(),
                    config.height(),
                    config.width(),
                    config.minWords(),
                    config.timeLimit().toMinutes(),
                    config.hints());
        }
    }

    private static void printAllowedSizes() {
        System.out.println("Допустимые размеры: 10x10, 11x10, 10x11, 12x10, 10x12.");
    }

    private static String dimensionPrompt(String title, Integer defaultValue) {
        String prompt = title + " (10, 11 или 12";
        if (defaultValue != null) {
            prompt += ", Enter — " + defaultValue;
        }
        return prompt + "): ";
    }
}
