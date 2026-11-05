package game;

import game.config.Difficulty;
import game.config.GameConfig;
import game.dictionary.WordBank;
import game.generator.GridGenerator;
import game.model.Puzzle;
import game.play.ConsoleGame;
import game.ui.MainWindow;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
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

        System.out.println("=== КЛАССИЧЕСКИЙ СКАНВОРД ===");
        printDifficultyOptions();
        Difficulty difficulty = chooseDifficulty(scanner);
        if (difficulty == null) return;

        GameConfig profile = difficulty.config();
        System.out.println("Выбрана сложность: " + difficulty.displayName() + ".");

        int[] size = chooseBoardSize(scanner, profile);
        if (size == null) return;

        GameConfig config = profile.withBoardSize(size[0], size[1]);

        GridGenerator generator = new GridGenerator();
        Puzzle puzzle = generator.generate(
                config.height(), config.width(), WordBank.WORDS, config.minWords());

        if (puzzle == null) {
            System.out.println("Не удалось разместить минимум " + config.minWords()
                    + " слов. Попробуйте другой размер или добавьте слова в словарь.");
            return;
        }

        System.out.println("\nНаправления размещения: горизонталь, вертикаль и диагонали.");
        System.out.println("Слова могут пересекаться, только если буквы в общей клетке совпадают.");
        new ConsoleGame(puzzle, scanner, config, difficulty.displayName()).start();
    }

    static Difficulty chooseDifficulty(Scanner scanner) {
        while (true) {
            System.out.print("Сложность (1-3, Enter — обычный): ");

            if (!scanner.hasNextLine()) {
                System.out.println("\nВвод завершён.");
                return null;
            }

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return Difficulty.NORMAL;
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
        return (h == 10 && w == 10) || (h == 11 && w == 10)
                || (h == 10 && w == 11) || (h == 12 && w == 10)
                || (h == 10 && w == 12);
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
