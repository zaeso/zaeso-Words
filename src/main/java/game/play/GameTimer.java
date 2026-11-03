package game.play;

import java.time.Duration;

/**
 * Таймер обратного отсчёта для игровой сессии.
 *
 * <p>Время считается по монотонным часам ({@link System#nanoTime()}), поэтому
 * изменение системного времени не влияет на результат.</p>
 */
public final class GameTimer {
    private final long limitNanos;
    private final long limitSeconds;

    private long startedAt;
    private long stoppedAt;
    private boolean started;
    private boolean running;

    public GameTimer(Duration limit) {
        if (limit == null || limit.isZero() || limit.isNegative()) {
            throw new IllegalArgumentException("Лимит времени должен быть положительным.");
        }
        this.limitNanos = limit.toNanos();
        this.limitSeconds = limit.getSeconds();
    }

    /** Запускает отсчёт. Повторный вызов во время работы игнорируется. */
    public synchronized void start() {
        if (running) return;

        startedAt = System.nanoTime();
        stoppedAt = 0;
        started = true;
        running = true;
    }

    /** Останавливает отсчёт. Повторный вызов безопасен. */
    public synchronized void stop() {
        if (!running) return;

        stoppedAt = System.nanoTime();
        running = false;
    }

    public synchronized boolean isRunning() {
        return running;
    }

    /** Прошло ли время, отведённое на игру. */
    public synchronized boolean isExpired() {
        return started && elapsedNanos() >= limitNanos;
    }

    public synchronized long elapsedSeconds() {
        return elapsedNanos() / 1_000_000_000L;
    }

    /**
     * Сколько секунд осталось. Значение округляется вверх, чтобы на последней
     * секунде отображалось {@code 00:01}, а не {@code 00:00}.
     */
    public synchronized long remainingSeconds() {
        long remainingNanos = limitNanos - elapsedNanos();
        if (remainingNanos <= 0) return 0;
        return (remainingNanos + 999_999_999L) / 1_000_000_000L;
    }

    public long getLimitSeconds() {
        return limitSeconds;
    }

    private long elapsedNanos() {
        if (!started) return 0;

        long end = running ? System.nanoTime() : stoppedAt;
        return Math.max(0, end - startedAt);
    }

    /** Форматирует количество секунд как {@code MM:SS}. */
    public static String format(long totalSeconds) {
        long safeSeconds = Math.max(0, totalSeconds);
        return String.format("%02d:%02d", safeSeconds / 60, safeSeconds % 60);
    }
}
