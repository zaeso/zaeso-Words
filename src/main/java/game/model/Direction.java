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

    /** Человекочитаемое название направления. */
    public String description() {
        if (rowStep == 0 && colStep == 1) return "вправо";
        if (rowStep == 0 && colStep == -1) return "влево";
        if (rowStep == 1 && colStep == 0) return "вниз";
        if (rowStep == -1 && colStep == 0) return "вверх";
        if (rowStep == 1 && colStep == 1) return "по диагонали вниз-вправо";
        if (rowStep == 1 && colStep == -1) return "по диагонали вниз-влево";
        if (rowStep == -1 && colStep == 1) return "по диагонали вверх-вправо";
        if (rowStep == -1 && colStep == -1) return "по диагонали вверх-влево";
        throw new IllegalStateException("Неизвестное направление: " + rowStep + ":" + colStep);
    }
}
