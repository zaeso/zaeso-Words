package game.ui;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Короткие звуковые сигналы без внешних зависимостей. Синтез идёт в фоновом
 * потоке, поэтому интерфейс не подвисает; при отсутствии звуковой карты
 * сигналы молча пропускаются.
 */
public final class SoundPlayer {
    private static final float SAMPLE_RATE = 44_100f;
    private static final double VOLUME = 0.18;

    private SoundPlayer() {
    }

    public static void found() {
        play(new double[]{880}, 110);
    }

    public static void hint() {
        play(new double[]{660}, 90);
    }

    public static void error() {
        play(new double[]{220}, 120);
    }

    public static void finish() {
        play(new double[]{523.25, 659.25, 783.99}, 130);
    }

    private static void play(double[] frequencies, int millisecondsPerNote) {
        Thread thread = new Thread(() -> {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                line.open(format);
                line.start();
                for (double frequency : frequencies) {
                    byte[] data = tone(frequency, millisecondsPerNote);
                    line.write(data, 0, data.length);
                }
                line.drain();
            } catch (Exception ignored) {
                // Звук необязателен: на серверах и машинах без аудио игра работает без него.
            }
        }, "zaeso-words-sound");
        thread.setDaemon(true);
        thread.start();
    }

    private static int toneLength(int milliseconds) {
        return (int) (SAMPLE_RATE * milliseconds / 1000);
    }

    private static byte[] tone(double frequency, int milliseconds) {
        int samples = toneLength(milliseconds);
        byte[] data = new byte[samples * 2];
        int fade = Math.max(1, samples / 10);
        for (int i = 0; i < samples; i++) {
            double envelope = Math.min(1.0, Math.min(i, samples - i) / (double) fade);
            double value = Math.sin(2 * Math.PI * frequency * i / SAMPLE_RATE)
                    * VOLUME * envelope;
            short sample = (short) (value * Short.MAX_VALUE);
            data[i * 2] = (byte) (sample & 0xFF);
            data[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return data;
    }
}
