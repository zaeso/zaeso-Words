package game.config;

import java.util.Objects;

/**
 * Оформление и звук интерфейса. Настройки не участвуют в игровой логике и
 * применяются только компонентами Swing.
 */
public final class UiSettings {
    private final ThemeChoice theme;
    private final boolean soundEnabled;
    private final boolean animationsEnabled;

    public UiSettings(ThemeChoice theme, boolean soundEnabled, boolean animationsEnabled) {
        Objects.requireNonNull(theme, "theme");
        this.theme = theme;
        this.soundEnabled = soundEnabled;
        this.animationsEnabled = animationsEnabled;
    }

    public ThemeChoice theme() {
        return theme;
    }

    public boolean soundEnabled() {
        return soundEnabled;
    }

    public boolean animationsEnabled() {
        return animationsEnabled;
    }

    /** Тема «Классика», звук и анимации включены. */
    public static UiSettings defaults() {
        return new UiSettings(ThemeChoice.CLASSIC, true, true);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof UiSettings)) return false;
        UiSettings settings = (UiSettings) other;
        return soundEnabled == settings.soundEnabled
                && animationsEnabled == settings.animationsEnabled
                && theme == settings.theme;
    }

    @Override
    public int hashCode() {
        int result = theme.hashCode();
        result = 31 * result + (soundEnabled ? 1 : 0);
        result = 31 * result + (animationsEnabled ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UiSettings[theme=" + theme + ", soundEnabled=" + soundEnabled
                + ", animationsEnabled=" + animationsEnabled + "]";
    }
}
