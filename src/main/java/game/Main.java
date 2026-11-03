package game;

import game.generator.GridGenerator;
import game.model.Direction;
import game.model.Puzzle;
import game.play.ConsoleGame;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== КЛАССИЧЕСКИЙ СКАНВОРД ===");
        int height = choose("Высота сетки (10, 11 или 12): ", scanner);
        int width = choose("Ширина сетки (10 или 11 или 12): ", scanner);

        if (!isAllowedSize(height, width)) {
            System.out.println("Недопустимый размер. Используйте: 10x10, 11x10, 10x11, 12x10 или 10x12.");
            return;
        }

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

    private static int choose(String prompt, Scanner scanner) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static boolean isAllowedSize(int h, int w) {
        return (h == 10 && w == 10) || (h == 11 && w == 10)
                || (h == 10 && w == 11) || (h == 12 && w == 10)
                || (h == 10 && w == 12);
    }
}
