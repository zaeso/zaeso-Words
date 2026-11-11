package game.model;

/**
 * Направление размещения слова на сетке. Восемь констант покрывают все
 * варианты: по строкам, столбцам и обеим диагоналям.
 */
public final class Direction {
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

    private final int rowStep;
    private final int colStep;

    public Direction(int rowStep, int colStep) {
        this.rowStep = rowStep;
        this.colStep = colStep;
    }

    public int rowStep() {
        return rowStep;
    }

    public int colStep() {
        return colStep;
    }

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

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Direction)) return false;
        Direction direction = (Direction) other;
        return rowStep == direction.rowStep && colStep == direction.colStep;
    }

    @Override
    public int hashCode() {
        return 31 * rowStep + colStep;
    }

    @Override
    public String toString() {
        return "Direction[rowStep=" + rowStep + ", colStep=" + colStep + "]";
    }
}
