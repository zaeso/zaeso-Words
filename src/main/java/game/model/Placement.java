package game.model;

/** Размещённое в сетке слово: текст, стартовая клетка и направление. */
public final class Placement {
    private final String word;
    private final int startRow;
    private final int startCol;
    private final Direction direction;

    public Placement(String word, int startRow, int startCol, Direction direction) {
        this.word = word;
        this.startRow = startRow;
        this.startCol = startCol;
        this.direction = direction;
    }

    public String word() {
        return word;
    }

    public int startRow() {
        return startRow;
    }

    public int startCol() {
        return startCol;
    }

    public Direction direction() {
        return direction;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Placement)) return false;
        Placement placement = (Placement) other;
        return startRow == placement.startRow
                && startCol == placement.startCol
                && word.equals(placement.word)
                && direction.equals(placement.direction);
    }

    @Override
    public int hashCode() {
        int result = word.hashCode();
        result = 31 * result + startRow;
        result = 31 * result + startCol;
        result = 31 * result + direction.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Placement[word=" + word + ", startRow=" + startRow
                + ", startCol=" + startCol + ", direction=" + direction + "]";
    }
}
