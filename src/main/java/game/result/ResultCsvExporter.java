package game.result;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Экспорт истории игр в CSV. Файл пишется в UTF-8 с BOM и разделителем
 * «;», поэтому русские названия и таблица корректно открываются в Excel.
 */
public final class ResultCsvExporter {
    public static final String HEADER =
            "Дата;Сложность;Сетка;Найдено;Всего слов;Процент;Время (с);Подсказки;Очки;Причина";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final char BOM = '\uFEFF';

    private ResultCsvExporter() {
    }

    public static void write(Path file, List<GameResult> results) throws IOException {
        Files.writeString(file, toCsv(results), StandardCharsets.UTF_8);
    }

    public static String toCsv(List<GameResult> results) {
        StringBuilder csv = new StringBuilder();
        csv.append(BOM).append(HEADER).append("\r\n");

        for (GameResult result : results) {
            csv.append(escape(date(result.finishedAtEpochSeconds()))).append(';')
                    .append(escape(result.difficulty())).append(';')
                    .append(result.height()).append('x').append(result.width()).append(';')
                    .append(result.foundWords()).append(';')
                    .append(result.totalWords()).append(';')
                    .append(result.completionPercent()).append(';')
                    .append(result.elapsedSeconds()).append(';')
                    .append(result.hintsUsed()).append(';')
                    .append(result.totalScore()).append(';')
                    .append(escape(result.finishReason())).append("\r\n");
        }
        return csv.toString();
    }

    private static String date(long epochSeconds) {
        return Instant.ofEpochSecond(epochSeconds)
                .atZone(ZoneId.systemDefault())
                .format(DATE_FORMAT);
    }

    private static String escape(String value) {
        if (value == null) return "";
        if (value.indexOf(';') < 0 && value.indexOf('"') < 0
                && value.indexOf('\n') < 0 && value.indexOf('\r') < 0) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
