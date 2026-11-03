package game.play;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTimerTest {

    @Test
    void doesNotExpireBeforeStart() {
        GameTimer timer = new GameTimer(Duration.ofSeconds(1));

        assertFalse(timer.isExpired());
        assertFalse(timer.isRunning());
        assertEquals(0, timer.elapsedSeconds());
        assertEquals(1, timer.remainingSeconds());
    }

    @Test
    void countsDownAndExpires() throws InterruptedException {
        GameTimer timer = new GameTimer(Duration.ofMillis(100));

        timer.start();
        assertTrue(timer.isRunning());
        assertFalse(timer.isExpired());

        Thread.sleep(150);

        assertTrue(timer.isExpired());
        assertEquals(0, timer.remainingSeconds());
    }

    @Test
    void freezesElapsedTimeAfterStop() throws InterruptedException {
        GameTimer timer = new GameTimer(Duration.ofSeconds(30));

        timer.start();
        Thread.sleep(50);
        timer.stop();

        long elapsedAfterStop = timer.elapsedSeconds();
        Thread.sleep(50);

        assertEquals(elapsedAfterStop, timer.elapsedSeconds());
        assertFalse(timer.isRunning());
        assertFalse(timer.isExpired());
    }

    @Test
    void formatsSecondsAsMinutesAndSeconds() {
        assertEquals("00:00", GameTimer.format(0));
        assertEquals("01:05", GameTimer.format(65));
        assertEquals("60:00", GameTimer.format(3600));
        assertEquals("00:00", GameTimer.format(-10));
    }

    @Test
    void rejectsNonPositiveLimit() {
        assertThrows(IllegalArgumentException.class, () -> new GameTimer(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new GameTimer(Duration.ofSeconds(-1)));
        assertThrows(IllegalArgumentException.class, () -> new GameTimer(null));
    }
}
