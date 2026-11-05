package game;

import game.config.Difficulty;
import game.config.GameConfig;
import game.generator.GridGenerator;
import game.model.Puzzle;
import game.play.ConsoleGame;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
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

        List<String> words = List.of(
                "АЛГОРИТМ", "ПРОГРАММА", "КОМПЬЮТЕР", "ИНТЕРНЕТ", "МОНИТОР",
                "КЛАВИАТУРА", "ПРОЦЕССОР", "ПЕРЕМЕННАЯ", "МАССИВ", "ЦИКЛ",
                "МЕТОД", "КЛАСС", "ОБЪЕКТ", "СТРОКА", "ФУНКЦИЯ",
                "ПИТОН", "ДЖАВА", "КОД", "СЕРВЕР", "БРАУЗЕР",
                "СИСТЕМА", "ДАННЫЕ", "ФАЙЛ", "ПАПКА", "СЕТЬ",
                "РАЗРАБОТКА", "ПРОЕКТ", "ПАМЯТЬ", "ЛОГИКА", "КОМПИЛЯТОР"
        );

        GridGenerator generator = new GridGenerator();
        Puzzle puzzle = generator.generate(
                config.height(), config.width(), words, config.minWords());

        if (puzzle == null) {
            System.out.println("Не удалось разместить минимум " + config.minWords()
                    + " слов. Попробуйте другой размер или добавьте слова в словарь.");
            return;
        }

        System.out.println("\nНаправления размещения: горизонталь, вертикаль и диагонали.");
        System.out.println("Слова могут пересекаться, только если буквы в общей клетке совпадают.");
        new ConsoleGame(puzzle, scanner, config, difficulty.displayName()).start();
    }

    /**
     * Запрашивает уровень сложности. Пустой ввод выбирает обычный уровень,
     * а некорректный ввод повторяет запрос.
     *
     * @return выбранная сложность или {@code null}, если ввод завершён
     */
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

    /**
     * Запрашивает допустимую комбинацию высоты и ширины. При ошибке оба
     * значения запрашиваются заново.
     *
     * @return размеры сетки или {@code null}, если ввод завершён
     */
    static int[] chooseBoardSize(Scanner scanner) {
        return readBoardSize(scanner, null, null);
    }

    /**
     * Запрашивает размер сетки, подставляя значения из профиля сложности при
     * пустом вводе.
     */
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

    /**
     * Читает целое число и повторяет запрос при пустом или некорректном вводе.
     *
     * @return введённое число или {@code null}, если ввод завершён
     */
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
