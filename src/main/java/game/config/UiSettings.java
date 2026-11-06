package game.config;

import java.util.Objects;

/**
 * Оформление и звук интерфейса. Настройки не участвуют в игровой логике и
 * применяются только компонентами Swing.
 *
 * @param theme             выбранная цветовая схема
 * @param soundEnabled      включать звуковые эффекты
 * @param animationsEnabled включать анимацию подсветки найденных букв
 */
public record UiSettings(ThemeChoice theme, boolean soundEnabled, boolean animationsEnabled) {

    public UiSettings {
        Objects.requireNonNull(theme, "theme");
    }

    /** Тема «Классика», звук и анимации включены. */
    public static UiSettings defaults() {
        return new UiSettings(ThemeChoice.CLASSIC, true, true);
    }
}
