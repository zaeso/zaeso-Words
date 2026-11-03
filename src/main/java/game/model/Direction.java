package game.model;

public record Direction(int rowStep, int colStep) {
    public static final Direction[] ALL = {
            new Direction(0, 1),   // вправо
            new Direction(0, -1),  // влево
            new Direction(1, 0),   // вниз
            new Direction(-1, 0),  // вверх
            new Direction(1, 1),   // диагональ вниз-вправо
            new Direction(1, -1),  // диагональ вниз-влево
            new Direction(-1, 1),  // диагональ вверх-вправо
            new Direction(-1, -1)  // диагональ вверх-влево
    };
}
