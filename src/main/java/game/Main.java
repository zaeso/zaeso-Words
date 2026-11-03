package game;

import game.generator.GridGenerator;
import game.model.Puzzle;
import game.play.ConsoleGame;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== КЛАССИЧЕСКИЙ СКАНВОРД ===");
        printAllowedSizes();
        int[] size = chooseBoardSize(scanner);
        if (size == null) return;

        int height = size[0];
        int width = size[1];

        List<String> words = List.of(
                "АЛГОРИТМ", "ПРОГРАММА", "КОМПЬЮТЕР", "ИНТЕРНЕТ", "МОНИТОР",
                "КЛАВИАТУРА", "ПРОЦЕССОР", "ПЕРЕМЕННАЯ", "МАССИВ", "ЦИКЛ",
                "МЕТОД", "КЛАСС", "ОБЪЕКТ", "СТРОКА", "ФУНКЦИЯ",
                "ПИТОН", "ДЖАВА", "КОД", "СЕРВЕР", "БРАУЗЕР",
                "СИСТЕМА", "ДАННЫЕ", "ФАЙЛ", "ПАПКА", "СЕТЬ",
                "РАЗРАБОТКА", "ПРОЕКТ", "ПАМЯТЬ", "ЛОГИКА", "КОМПИЛЯТОР"
        );

        GridGenerator generator = new GridGenerator();
        Puzzle puzzle = generator.generate(height, width, words, 10);

        if (puzzle == null) {
            System.out.println("Не удалось разместить минимум 10 слов. Попробуйте другой размер или добавьте слова в словарь.");
            return;
        }

        System.out.println("\nНаправления размещения: горизонталь, вертикаль и диагонали.");
        System.out.println("Слова могут пересекаться, только если буквы в общей клетке совпадают.");
        new ConsoleGame(puzzle, scanner).start();
    }

    /**
     * Запрашивает допустимую комбинацию высоты и ширины. При ошибке оба
     * значения запрашиваются заново.
     *
     * @return размеры сетки или {@code null}, если ввод завершён
     */
    static int[] chooseBoardSize(Scanner scanner) {
        while (true) {
            Integer height = chooseDimension("Высота сетки (10, 11 или 12): ", scanner);
            if (height == null) return null;

            Integer width = chooseDimension("Ширина сетки (10 или 11 или 12): ", scanner);
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
        while (true) {
            System.out.print(prompt);

            if (!scanner.hasNextLine()) {
                System.out.println("\nВвод завершён.");
                return null;
            }

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
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

    private static void printAllowedSizes() {
        System.out.println("Допустимые размеры: 10x10, 11x10, 10x11, 12x10, 10x12.");
    }
}
