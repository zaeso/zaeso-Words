package game.config;

import java.util.Locale;

/** Цветовые схемы интерфейса, доступные в настройках. */
public enum ThemeChoice {
    CLASSIC("Классика"),
    OCEAN("Океан"),
    FOREST("Лес"),
    DARK("Тёмная");

    private final String displayName;

    ThemeChoice(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    /**
     * Разбирает название темы в любом регистре.
     *
     * @return тема или {@code null}, если значение неизвестно
     */
    public static ThemeChoice fromInput(String input) {
        if (input == null) return null;
        String normalized = input.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) return null;
        for (ThemeChoice choice : values()) {
            if (choice.name().toLowerCase(Locale.ROOT).equals(normalized)
                    || choice.displayName.toLowerCase(Locale.ROOT).equals(normalized)) {
                return choice;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
