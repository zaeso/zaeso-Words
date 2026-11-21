# Журнал запросов и ответов

История работы над **zaeso-Words** — десктопной игры «Поиск слов» на Java 8+:
что просили, что сделано, какими фрагментами кода и в каком релизе вышло.
Запросы приведены дословно, ответы — сжато. Фрагменты кода настоящие,
длинные файлы сокращены до ключевых мест.

---

## Содержание

1. [Об игре](#1-об-игре)
2. [ИИ-модели и инструменты](#2-ии-модели-и-инструменты)
3. [Хронология](#3-хронология)
4. [Релизы](#4-релизы)
5. [Архитектура и файлы](#5-архитектура-и-файлы)
6. [Экраны и элементы](#6-экраны-и-элементы)
7. [UI-классы](#7-ui-классы)
8. [Ключевой код](#8-ключевой-код)
9. [Управление мышью](#9-управление-мышью)
10. [Логика и генерация](#10-логика-и-генерация)
11. [Очки и результаты](#11-очки-и-результаты)
12. [Настройки](#12-настройки)
13. [Java 8](#13-java-8)
14. [Тесты](#14-тесты)
15. [CI и запуск](#15-ci-и-запуск)
16. [Запросы и ответы](#16-запросы-и-ответы)
17. [Ограничения](#17-ограничения)
18. [Словарь](#18-словарь)
19. [Быстрые команды](#19-быстрые-команды)
20. [Что можно улучшить](#20-что-можно-улучшить)

---

## 1. Об игре

| Параметр | Значение |
| --- | --- |
| Жанр | Поиск слов |
| Платформа | Windows, macOS, Linux |
| Минимальная Java | 8 (достаточно JRE) |
| Интерфейс | Swing, полностью мышиный |
| Сборка | Maven, исполняемый JAR |
| Тесты | JUnit 5, 147 тестов в 22 классах |
| Репозиторий | https://github.com/zaeso/zaeso-Words |

Сетка букв скрывает слова выбранной категории. Слова находятся выделением
мышью по строке, столбцу или диагонали. За слова начисляются очки, за
ошибки и подсказки — штрафы. Есть обычный режим, ежедневная сетка с seed
из даты и таблица рекордов с экспортом в CSV.

---

## 2. ИИ-модели и инструменты

### 2.1. Модели

| Модель | Провайдер | Роль |
| --- | --- | --- |
| **DeepSeek V4.1 Flash Free** | opencode | Основная: код, ревью, документация, разбор ошибок |
| Модели генерации изображений | — | **Не использовались** |

Других языковых моделей не применялось. Все скриншоты — настоящие снимки
Swing-приложения, сделанные утилитами `ShotMenu`, `ShotGame` и другими,
а не сгенерированные картинки.

### 2.2. Инструменты

| Инструмент | Версия / роль | Зачем |
| --- | --- | --- |
| OpenCode | среда агента | Правка файлов, сборка, git, проверки |
| Maven | 3.9.x | `clean verify`, тесты, shade |
| JBR | 21 | JDK для сборки и скриншотов |
| Eclipse Temurin JRE | 1.8.0_512 | Проверка запуска на Java 8 |
| JUnit Jupiter | 5.11.3 | Тесты |
| Jackson databind | 2.18.2 | JSON-хранилище |
| GitHub Actions | ubuntu-24.04 | CI и релиз |
| Git + PowerShell | — | Коммиты, теги, проверки API |

### 2.3. Что делала модель

| Задача | Как решалась |
| --- | --- |
| Проектирование UI | Собственные компоненты вместо Look & Feel |
| Код | `FlatButton`, `GradientPanel`, `BadgeLabel`, `WordChip` |
| Ошибки | Чтение вывода Maven, правка импортов |
| Проверка | `mvn clean verify`, 147/147 |
| Java 8 | Запуск JAR на Temurin 1.8.0_512 |
| Документация | `README.md`, дорожная карта, этот журнал |
| Релизы | Тег, push, проверка через GitHub API |

### 2.4. Ограничения подхода

- Интерфейс проверяется по скриншотам, а не «на глаз» модели.
- Эмодзи запрещены: Java 8 рисует их квадратами.
- Только API Java 8: без `var`, `List.of`, `record`, текстовых блоков.
- Скриншоты и проверки — на реальном JAR.

---

## 3. Хронология

| Дата | Запрос (кратко) | Результат | Релиз |
| --- | --- | --- | --- |
| 11.11 | Не видно кнопок, настройки не сохраняются | `FlatButton`, кнопка «Сохранить» | `v1.1.1` |
| 11.11 | Java 8, цвета найденных слов, меню | Палитра по яркости, акценты тем | `v1.1.2` |
| 12.11 | Ещё красивее | Градиенты, тени, декор | `v1.1.3` |
| 12.11 | Что там на Java 8 | Проверка GUI и консоли | — |
| 12.11 | Доработать логику игры | Два способа выделения, счётчики | `v1.1.4` |
| 12.11 | Ещё доработка | Причины финиша, «Завершить раунд» | `v1.1.4` |
| 12.11 | Без ввода с клавиатуры | Убран клавиатурный ввод | `v1.1.4` |
| 12.11 | Ещё красивее — игровой экран | Бейджи, тени, плитки очков | `v1.1.4` |
| 12.11 | Покажи интерфейс | Сводная картинка экранов | — |
| 12.11 | Это сетка дня | Снимок ежедневного режима | — |
| 12.11 | Где слово | Поиск «ПТО» по 8 направлениям | — |
| 14.11 | Нормальный список слов | Чипы, алфавит, подсветка по клику | `v1.1.5` |
| 21.11 | Журнал промтов и ответов | `PROMPTS.md` + ссылка в README | docs |
| 21.11 | Больше кода и таблиц | Расширение журнала | docs |
| 21.11 | Побольше в 5 раз, какие ИИ | Расширенный журнал, затем сокращён | docs |

---

## 4. Релизы

| Версия | Дата | Главное | Ключевые файлы |
| --- | --- | --- | --- |
| `v1.1.1` | 11.11 | Видимые кнопки, «Сохранить» | `FlatButton`, `GradientPanel`, `MainWindow` |
| `v1.1.2` | 11.11 | Цвета найденных слов, Java 8 | `GamePanel`, `GridPanel`, `Theme` |
| `v1.1.3` | 12.11 | Градиенты, тени, декор | `FlatButton`, `GradientPanel` |
| `v1.1.4` | 12.11 | Логика игры, бейджи, плитки | `GamePanel`, `GridPanel`, `BadgeLabel` |
| `v1.1.5` | 14.11 | Список слов в чипах | `GamePanel` |

### 4.1. Ассеты v1.1.5

| Файл | Размер | Назначение |
| --- | ---: | --- |
| `zaeso-words-1.1.5.jar` | 5 635 365 байт | Исполняемый JAR |
| `run-game.bat` | 1 137 байт | Запуск двойным кликом |

### 4.2. Где хранится версия

| Файл | Что содержит |
| --- | --- |
| `pom.xml` | `<version>1.1.5</version>` |
| `README.md` | Имя JAR в командах |
| `ci.yml` | Артефакт и путь к JAR |
| `Roadmap` | Таблица истории |
| `run-game.bat` | Версии нет — ищет `zaeso-words-*.jar` |

Последний релиз: https://github.com/zaeso/zaeso-Words/releases/latest

---

## 5. Архитектура и файлы

### 5.1. Пакеты

| Пакет | Назначение |
| --- | --- |
| `game.model` | Сетка, размещение, направления |
| `game.config` | Сложность, настройки, темы |
| `game.generator` | Генерация сетки |
| `game.session` | Общая логика раунда |
| `game.play` | Консольный режим и таймер |
| `game.score` | Буквы и очки |
| `game.result` | JSON, CSV, результат |
| `game.dictionary` | Категории и словари |
| `game.daily` | Сетка дня и статистика |
| `game.ui` | Swing-интерфейс |

### 5.2. Карта файлов

| Файл | Строк | Роль |
| --- | ---: | --- |
| `ui/MainWindow.java` | 847 | Окно и все экраны |
| `ui/GamePanel.java` | 533 | Экран игры |
| `ui/GridPanel.java` | 446 | Сетка букв |
| `session/GameSession.java` | 354 | Логика раунда |
| `Main.java` | 305 | Точка входа |
| `play/ConsoleGame.java` | 281 | Консольный цикл |
| `generator/GridGenerator.java` | 219 | Размещение слов |
| `dictionary/WordCategory.java` | 197 | Категории |
| `dictionary/RussianWords.java` | 192 | Словарь |
| `config/GameSettings.java` | 178 | Валидация |
| `config/SettingsStore.java` | 165 | properties |
| `ui/Theme.java` | 161 | Палитра |
| `ui/FlatButton.java` | 148 | Кнопка |
| `result/GameResult.java` | 144 | Результат |
| `daily/DailyStatistics.java` | 132 | Статистика |
| `result/ResultRepository.java` | 126 | JSON |
| `play/GameTimer.java` | 97 | Время |
| `config/GameConfig.java` | 94 | Параметры |
| `score/ScoreCalculator.java` | 70 | Очки |
| `score/Score.java` | 69 | Разбивка |
| `model/Direction.java` | 65 | 8 направлений |
| `score/LetterPoints.java` | 65 | Стоимость букв |
| `result/ResultCsvExporter.java` | 65 | CSV |
| `daily/DailyPuzzle.java` | 62 | Сетка дня |
| `config/UiSettings.java` | 61 | Оформление |
| `model/Placement.java` | 58 | Размещение |
| `config/Difficulty.java` | 56 | Профили |
| `config/ThemeChoice.java` | 44 | Темы |
| `ui/BadgeLabel.java` | 44 | Бейдж |
| `generator/RoundGenerator.java` | 41 | Раунд |
| `ui/GradientPanel.java` | 71 | Градиент |
| `model/Puzzle.java` | 30 | Модель сетки |
| `dictionary/WordBank.java` | 11 | Словарь по умолчанию |
| `run-game.bat` | 37 | Запуск |
| `.github/workflows/ci.yml` | 103 | CI |

---

## 6. Экраны и элементы

| Экран | Элементы | Что умеет |
| --- | --- | --- |
| Главное меню | Градиент, декор, 6 кнопок | Играть, сетка дня, статистика, настройки, рекорды, выход |
| Настройки | Карточка, поля, «Сохранить» | Сложность, категория, сетка, время, тема, звук, анимации |
| Игра | Бейджи, сетка, список слов | Выделение, подсказки, предпросмотр, прогресс |
| Итоги | Счёт и 4 плитки разбора | Буквы, время, ошибки, подсказки |
| Статистика | Диалог, 4 плитки | Лучший счёт, серия, дни, слова |
| Рекорды | Таблица, экспорт CSV | История и выгрузка |

### 6.1. Бейджи шапки

| Бейдж | Содержимое | Особенность |
| --- | --- | --- |
| Найдено | `Найдено X/Y` | После каждого слова |
| Время | `Время MM:SS` | Краснеет на последних 30 с |
| Подсказки | `Подсказки N/M` | Кнопка гаснет при нуле |
| Очки | `Всего очков: N` | С перенесёнными очками |

### 6.2. Список слов

| Состояние | Оформление |
| --- | --- |
| Найдено | Цветная плашка, текст по яркости |
| Не найдено | Нейтральная плашка, серый текст |
| Клик по найденному | Слово вспыхивает на сетке |
| Заголовок | `Слова · X/Y` |

---

## 7. UI-классы

| Класс | Строк | Роль |
| --- | ---: | --- |
| `FlatButton` | 148 | Саморисующаяся кнопка: градиент, тень, блик |
| `GradientPanel` | 71 | Градиентный фон и декор |
| `BadgeLabel` | 44 | Полупрозрачная плашка |
| `Theme` | 161 | Палитра, шрифты, границы |
| `GridPanel` | 446 | Выделение и подсветка |
| `GamePanel` | 533 | Игра, таймер, слова |
| `MainWindow` | 847 | Окно и экраны |

### 7.1. Шрифты

| Константа | Размер | Где |
| --- | ---: | --- |
| `TITLE` | 34 | Заголовки, крупные значения |
| `HEADING` | 22 | Плитки |
| `BODY` | 16 | Текст и чипы |
| `SMALL` | 13 | Подписи |
| `BUTTON` | 16 | Кнопки и бейджи |

---

## 8. Ключевой код

### 8.1. `FlatButton` — кнопка рисует себя сама

```java
Color fillTop = mix(base, Color.WHITE, 0.26);
Color fillBottom = base;
Color edge = border;

if (!active) {
    fillTop = mix(base, Theme.MUTED, 0.42);
    fillBottom = mix(base, Theme.MUTED, 0.55);
} else if (pressed) {
    fillTop = mix(base, Color.BLACK, 0.10);
    fillBottom = base.darker();
    edge = fillBottom.darker();
} else if (hovered) {
    fillTop = mix(base, Color.WHITE, 0.44);
    fillBottom = mix(base, Color.WHITE, 0.10);
}

if (active && !pressed) {
    g.setColor(new Color(0, 0, 0, hovered ? 60 : 34));
    g.fillRoundRect(2, 4, width - 4, height - 4, ARC, ARC);
}

g.setPaint(new GradientPaint(0, 1, fillTop, 0, height - 1, fillBottom));
g.fillRoundRect(0, 0, width - 1, height - 1, ARC, ARC);
g.setColor(edge);
g.drawRoundRect(0, 0, width - 1, height - 1, ARC, ARC);
```

### 8.2. `GradientPanel` — декор меню

```java
g.setColor(new Color(255, 255, 255, 20));
g.fillOval(-width / 4, -height / 5, width / 2, height / 2);
g.fillOval(width * 3 / 4, height / 8, width / 3, height / 3);

g.setPaint(new GradientPaint(0, height * 3 / 4, new Color(0, 0, 0, 0),
        0, height, new Color(0, 0, 0, 42)));
g.fillRect(0, 0, width, height);
```

### 8.3. `BadgeLabel` — плашка

```java
g.setColor(new Color(255, 255, 255, 38));
g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
g.setColor(new Color(255, 255, 255, 60));
g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
```

### 8.4. `Theme` — тень карточки

```java
public static Border shadowCardBorder() {
    return BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(
                            0, 0, 3, 3, new Color(30, 36, 48, 36)),
                    BorderFactory.createLineBorder(BORDER, 1, true)),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)
    );
}
```

### 8.5. `GridPanel` — два способа выделения

```java
// Второй клик: завершение выделения по прямой от первой буквы.
if (anchor != null && !dragged) {
    List<Cell> line = line(anchor, current);
    if (line.size() >= 2) {
        selection.clear();
        selection.addAll(line);
        submitSelection();
        return;
    }
}

// Новое выделение: клик задает первую букву.
anchor = current;
selection.clear();
selection.add(current);
dragged = false;
```

```java
public void mouseDragged(MouseEvent e) {
    if (anchor == null) return;
    Cell current = cellAt(e.getPoint());
    if (current == null) return;
    List<Cell> line = line(anchor, current);
    if (line.equals(selection)) return;
    dragged = true;
    selection.clear();
    selection.addAll(line);
    notifyPreview();
    repaint();
}
```

### 8.6. `GridPanel` — клетки

```java
Color foundColor = foundCells.get(cell);
g.setColor(new Color(0, 0, 0, 46));
g.fillRoundRect(x + 2, y + 3, metrics.cell, metrics.cell, 10, 10);
if (foundColor != null) {
    g.setColor(foundColor(cell, foundColor));
} else if (hintCells.contains(cell)) {
    g.setColor(HINT_GREEN);
} else if (selection.contains(cell)) {
    g.setColor(Theme.BLUE);
} else {
    g.setColor(Theme.CELL);
}
g.fillRoundRect(x, y, metrics.cell, metrics.cell, 10, 10);
```

### 8.7. `GamePanel` — индикаторы

```java
long remaining = session.remainingSeconds();
timeLabel.setText("Время " + GameTimer.format(remaining));
timeLabel.setForeground(remaining <= 30
        ? new Color(0xFF8A80) : Color.WHITE);
foundLabel.setText("Найдено " + found + "/" + total);
int hintsLeft = session.hintsLimit() - session.hintsUsed();
hintButton.setEnabled(hintsLeft > 0 && !session.isFinished());
```

### 8.8. `GamePanel` — чипы слов

```java
private void rebuildWordList() {
    wordsPanel.removeAll();
    List<String> words = new java.util.ArrayList<>(session.allWords());
    java.util.Collections.sort(words);
    for (String word : words) {
        wordsPanel.add(new WordChip(word, session.isFound(word)));
    }
    wordsPanel.revalidate();
    wordsPanel.repaint();
}
```

```java
private void flashWord(String word) {
    if (session == null) return;
    java.util.Set<GridPanel.Cell> cells = new java.util.HashSet<>();
    session.placementFor(word).ifPresent(placement -> {
        int rowStep = placement.direction().rowStep();
        int colStep = placement.direction().colStep();
        for (int i = 0; i < placement.word().length(); i++) {
            cells.add(new GridPanel.Cell(
                    placement.startRow() + rowStep * i,
                    placement.startCol() + colStep * i));
        }
    });
    gridPanel.flashCells(cells);
}
```

### 8.9. `GamePanel` — отрисовка чипа

```java
if (found) {
    g.setColor(color);
    g.fillRoundRect(0, 0, width - 1, height - 1, ARC, ARC);
    g.setColor(color.darker());
    g.drawRoundRect(0, 0, width - 1, height - 1, ARC, ARC);
} else {
    g.setColor(Theme.PANEL);
    g.fillRoundRect(0, 0, width - 1, height - 1, ARC, ARC);
    g.setColor(Theme.BORDER);
    g.drawRoundRect(0, 0, width - 1, height - 1, ARC, ARC);
}
```

---

## 9. Управление мышью

| Действие | Как | Результат |
| --- | --- | --- |
| Выбрать слово | Протянуть от первой буквы до последней | Проверка слова |
| Выбрать слово | Клик по первой, клик по последней | Выделение по прямой |
| Отменить | Правая кнопка | Выделение снято |
| Отменить | Escape | Выделение снято |
| Подсказка | Кнопка «Подсказка» | Первая буква зелёным, −30 с |
| Завершить раунд | «Завершить раунд» | Результат сохранён |
| Найти на сетке | Клик по плашке слова | Слово вспыхивает |

Клавиатурного ввода слов нет.

---

## 10. Логика и генерация

### 10.1. Общая логика

`GameSession` — единственное место, где считаются слова, ошибки, подсказки,
время и очки. GUI и консоль используют один класс, поэтому правила совпадают.

| Метод | Что делает |
| --- | --- |
| `submit(word)` | Проверяет слово |
| `isFound(word)` | Найдено ли |
| `foundWords()` / `allWords()` | Наборы слов |
| `placementFor(word)` | Начало и направление |
| `hint()` | Подсказка |
| `remainingSeconds()` | Остаток времени |
| `finish(reason)` | Завершение раунда |

### 10.2. Направления

| Направление | Строка | Столбец |
| --- | ---: | ---: |
| Вправо | 0 | +1 |
| Влево | 0 | −1 |
| Вниз | +1 | 0 |
| Вверх | −1 | 0 |
| Вниз-вправо | +1 | +1 |
| Вниз-влево | +1 | −1 |
| Вверх-вправо | −1 | +1 |
| Вверх-влево | −1 | −1 |

### 10.3. Алгоритм генерации

1. Нормализовать словарь: верхний регистр, без дублей.
2. Отбросить слова длиннее максимального размера.
3. Создать пустую сетку.
4. Перемешать слова, длинные — раньше.
5. Выбрать клетку и одно из 8 направлений.
6. Проверить границы и пересечения только на одинаковых буквах.
7. Записать слово или повторить попытку.
8. Заполнить пустые клетки случайными буквами.
9. При неудаче — перезапуск, затем понятная ошибка.

### 10.4. Ежедневная сетка

```java
public static long seed(LocalDate date) {
    Objects.requireNonNull(date, "date");
    long value = date.toEpochDay() + 0x9E3779B97F4A7C15L;
    value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
    value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
    return value ^ (value >>> 31);
}

public static GenerationResult generate(LocalDate date) {
    GameSettings settings = settings();
    return RoundGenerator.generate(settings.toConfig(), settings.category(), seed(date));
}
```

| Свойство | Значение |
| --- | --- |
| Сложность | Обычная |
| Сетка | 10×10 |
| Категория | Все русские слова |
| Время | 5 минут |
| Seed | SplitMix64 от `toEpochDay()` |
| Название в истории | `Ежедневная` |

---

## 11. Очки и результаты

### 11.1. Формула

```text
очки за слова     = сумма стоимости букв найденных слов
бонус за время    = 2 × оставшиеся секунды, если найдены все слова
штраф за ошибки   = 5 × число ошибок
штраф за подсказки = 50 × число подсказок
итог = max(0, слова + бонус − штрафы)
```

```java
boolean completed = totalWords > 0 && foundWords.size() >= totalWords;
int timeBonus = completed
        ? (int) Math.min(Integer.MAX_VALUE,
                remainingSeconds * POINTS_PER_REMAINING_SECOND)
        : 0;

return new Score(
        wordPoints,
        timeBonus,
        mistakes * MISTAKE_PENALTY,
        hintsUsed * HINT_PENALTY
);
```

### 11.2. Стоимость букв

| Очки | Буквы |
| ---: | --- |
| 10 | О, Е, А, И, Н, Т, С, Р, В, Л |
| 15 | К, М, Д, П, У, Я |
| 20 | Ы, Ь, Г, З, Б, Ч |
| 25 | Х, Ж, Ш, Ю, Ц, Щ, Э, Ф, Ъ, Ё |

### 11.3. Причины финиша

| Причина | Заголовок |
| --- | --- |
| `TIME_EXPIRED` | «Время вышло» |
| `PLAYER_EXIT` | «Раунд завершён» |
| `ALL_WORDS_FOUND`, день | «Сетка дня пройдена» |
| `ALL_WORDS_FOUND`, обычный | «Раунд пройден» |

### 11.4. Плитки разбора

| Плитка | Цвет | Значение |
| --- | --- | --- |
| Буквы | зелёный | `+wordPoints` |
| Время | синий | `+timeBonus` |
| Ошибки | красный | `−mistakePenalty` |
| Подсказки | фиолетовый | `−hintPenalty` |

### 11.5. Хранилище

| Файл | Формат | Что хранит |
| --- | --- | --- |
| `~/.zaeso-words/results.json` | JSON | Результаты раундов |
| `~/.zaeso-words/settings.properties` | Properties | Настройки |
| CSV-экспорт | CSV | История для таблиц |

---

## 12. Настройки

| Параметр | Где | По умолчанию |
| --- | --- | --- |
| Сложность | Настройки / консоль | Обычная |
| Категория | Настройки / консоль | Технологии |
| Размер сетки | Настройки / консоль | Из профиля |
| Время | Настройки / консоль | Из профиля |
| Тема | Настройки | Классика |
| Звук | Настройки | Включён |
| Анимации | Настройки | Включены |

### 12.1. Профили

| Уровень | Сетка | Слов | Время | Подсказки |
| --- | --- | ---: | ---: | ---: |
| Лёгкий | 10×10 | 8 | 7 мин | 3 |
| Обычный | 10×10 | 10 | 5 мин | 2 |
| Сложный | 12×10 | 12 | 4 мин | 1 |

### 12.2. Проверки `GameSettings`

| Проверка | Сообщение |
| --- | --- |
| Размер сетки | Пояснение с диапазоном |
| Время 1–60 минут | Пояснение с диапазоном |
| Хватает слов | Сколько нужно и сколько есть |
| Слова помещаются | Сколько подходит по длине |

---

## 13. Java 8

Код собирается с `maven.compiler.release=8`, байткод — major 52.

| Современный API | Вместо него |
| --- | --- |
| `List.of` / `Map.of` | `Arrays.asList`, `HashMap` |
| `List.copyOf` | `new ArrayList<>(...)` |
| `var` | Явный тип |
| Текстовые блоки | Конкатенация |
| `String.isBlank` | `trim().isEmpty()` |
| `Optional.isEmpty` | `!isPresent()` |
| `Stream.toList` | `collect(Collectors.toList())` |
| `Files.readString` | `BufferedReader` |
| `record` | final-класс |
| `switch`-выражения | Классический `switch` |
| `instanceof` с переменной | Приведение |
| Лямбды, `Optional`, `Stream`, `java.time` | **Есть в Java 8** |

### 13.1. Как проверяли

| Проверка | Инструмент | Результат |
| --- | --- | --- |
| Сборка | Maven, release 8 | BUILD SUCCESS |
| Байткод | Заголовок класса | Major 52 |
| Консоль | Temurin 1.8.0_512 | Работает, stderr пуст |
| GUI | Temurin 1.8.0_512 | Открывается, stderr пуст |
| CI | Temurin 8 | 147/147 |
| Запускатор | `run-game.bat` | Понимает `1.8.0_...` |

---

## 14. Тесты

Всего **147 тестов** в **22 классах**. Запуск: `mvn clean verify`.

| Класс | Тестов | Что проверяет |
| --- | ---: | --- |
| `DifficultyTest` | 5 | Профили сложности |
| `GameConfigTest` | 3 | Параметры генерации |
| `GameSettingsTest` | 5 | Валидация |
| `SettingsStoreTest` | 5 | properties |
| `UiSettingsTest` | 7 | Тема, звук, анимации |
| `DailyPuzzleTest` | 5 | Seed и стабильность |
| `DailyStatisticsTest` | 6 | Серия и суммы |
| `WordCategoryTest` | 6 | Категории |
| `GridGeneratorCheckedTest` | 4 | Ошибки генерации |
| `GridGeneratorTest` | 13 | Размещение слов |
| `RoundGeneratorTest` | 2 | Воспроизводимость |
| `MainTest` | 21 | Аргументы и режимы |
| `DirectionTest` | 3 | Шаги направлений |
| `PuzzleTest` | 2 | Модель сетки |
| `ConsoleGameTest` | 18 | Консольный цикл |
| `GameTimerTest` | 9 | Время |
| `ResultCsvExporterTest` | 3 | CSV |
| `ResultRepositoryTest` | 11 | JSON |
| `LetterPointsTest` | 5 | Стоимость букв |
| `ScoreCalculatorTest` | 6 | Формула |
| `GameSessionTest` | 6 | Логика раунда |
| `GridPanelTest` | 2 | Выделение |
| **Итого** | **147** | |

```text
[INFO] Tests run: 147, Failures: 0, Errors: 0, Skipped: 0
[INFO] Building jar: target/zaeso-words-1.1.5.jar
[INFO] BUILD SUCCESS
```

---

## 15. CI и запуск

### 15.1. CI

| Job | Шаги | Результат |
| --- | --- | --- |
| `test` | JDK 8 → `mvn verify` → JDK 17 → `mvn verify` | Тесты на двух Java |
| `release` | На теге: сборка и `action-gh-release` | JAR + `run-game.bat` |

```yaml
on:
  push:
    branches: [main]
    tags: ['v*']
  pull_request:

jobs:
  test:
    runs-on: ubuntu-24.04
    steps:
      - uses: actions/checkout@v5
      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '8'
          cache: maven
      - run: mvn --batch-mode --no-transfer-progress verify
      - uses: actions/upload-artifact@v4
        with:
          name: zaeso-words-jar-java8
          path: target/zaeso-words-*.jar
```

### 15.2. Запуск

```bash
java -jar zaeso-words-1.1.5.jar
java -jar zaeso-words-1.1.5.jar --console
```

`run-game.bat`:

| Шаг | Что делает |
| --- | --- |
| `chcp 65001` | UTF-8 для русского текста |
| `for %%f in (zaeso-words-*.jar)` | Ищет JAR рядом |
| `java -version` | Достаёт версию |
| `if "!MAJOR!"=="1"` | Понимает `1.8.0_512` |
| `if !MAJOR! LSS 8` | Отказ на Java 7 |
| `start "" javaw` | Запуск без консоли |

---

## 16. Запросы и ответы

### 16.1. Не видно кнопок

> сделай игредетное оформление и красивое гуи просто кнопки не видно некоторые
> и не красиво выгледит плюс настройки нельзя соьхранить

Причина: тема Windows не рисовала фон кнопки с отключённой заливкой.
Решение: `FlatButton` рисует себя сам. Добавлены градиентный фон меню и
кнопка «Сохранить» со статусом. Релиз `v1.1.1`.

### 16.2. Java 8 и цвета слов

> проверий что джава 8 вобще может работать и не го по правь цвет найденых
> слов а то бывает он сливаеться с основным и не много игредета и перецвета
> добавь для кросоты на всех минюшках

```java
static Color[] paletteForBackground(Color background) {
    double luminance = (0.299 * background.getRed()
            + 0.587 * background.getGreen()
            + 0.114 * background.getBlue()) / 255.0;
    return luminance < 0.45 ? BRIGHT_WORD_COLORS : DEEP_WORD_COLORS;
}
```

Буква на плашке переключается по яркости:

```java
g.setColor(isDark(foundCells.get(cell))
        ? Color.WHITE : new Color(0x263238));
```

| Тема | Фиолетовый | Оранжевый | Бирюзовый |
| --- | --- | --- | --- |
| Классика | `0x6A1B9A` | `0xE65100` | `0x00695C` |
| Океан | `0x7B1FA2` | `0xF57C00` | `0x00897B` |
| Лес | `0x6A4C93` | `0xB26A00` | `0x386641` |
| Тёмная | `0x9C27B0` | `0xFFB74D` | `0x26A69A` |

Проверено на настоящей Temurin 1.8.0_512. Релиз `v1.1.2`.

### 16.3. Ещё красивее

> допели интерфейс сделай более красивый и так далее

Добавлены градиент, блик, тень кнопок, декор меню, тени карточек, полоски
плиток и градиентная шапка рекордов. Релиз `v1.1.3`.

### 16.4. Что там на Java 8

> что там на 8 тджаве

| Проверка | Итог |
| --- | --- |
| GUI на Temurin 8 | Работает, stderr пуст |
| Консоль | Сетка генерируется |
| Байткод | Major 52 |
| CI | 147/147 |

Изменений кода и релиза не было.

### 16.5. Логика игрового экрана

> дороботой логику интерфейса самой игры и всё

- Два способа выделения: протягивание и два клика.
- Правая кнопка и Escape отменяют.
- Предпросмотр слова под курсором.
- Счётчики «Найдено X/Y» и «Слова · X/Y».
- Красное время на последних 30 секундах.
- Кнопка подсказок гаснет при нуле.
- «Завершить раунд» сохраняет результат.

Релиз `v1.1.4`.

### 16.6. Ещё доработка

> дороботай ещё

| Причина | Заголовок |
| --- | --- |
| `TIME_EXPIRED` | «Время вышло» |
| `PLAYER_EXIT` | «Раунд завершён» |
| Все слова, день | «Сетка дня пройдена» |
| Все слова, обычный | «Раунд пройден» |

Релиз `v1.1.4`.

### 16.7. Без клавиатуры

> без вода с клавиатуры

Клавиатурный ввод убран. Из клавиш остался Escape для отмены выделения.
Релиз `v1.1.4`.

### 16.8. Красивее игровой экран

> модешь ещё красивей интерфейс

Добавлены бейджи шапки, тени клеток и четыре плитки разбора очков. Релиз
`v1.1.4`.

### 16.9. Покажи интерфейс

> покажи пто

Собрана сводная картинка экранов: меню, настройки, игра, статистика, плюс
отдельный снимок статистики.

### 16.10. Это сетка дня

> это сетка дня

| Параметр | Значение |
| --- | --- |
| Заголовок | `Сетка дня · 12.11.2026 · 10×10` |
| Подсказки | 2 из 2 |
| Слов | 14 |
| Seed | Из даты |

Сетка совпала с ежедневным пазлом. Изменений кода не было.

### 16.11. Где слово

> где оно

Поиск по 8 направлениям:

```java
int[][] dirs = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
for (int i = 0; i <= steps; i++) {
    word.append(grid[r + dirs[d][0] * i][c + dirs[d][1] * i]);
}
```

| Буква «ПТО» | Строка | Столбец |
| --- | ---: | ---: |
| П | 10 | 8 |
| Т | 9 | 7 |
| О | 8 | 6 |

Направление — диагональ снизу вверх. В игре достаточно кликнуть П, затем О.

### 16.12. Нормальный список слов

> напиши обнову потом камитом релизом доделай список слов нормальный

| Было | Стало |
| --- | --- |
| `JLabel` в списке | Чипы `WordChip` |
| Порядок генератора | Алфавит |
| Цвет только у текста | Цветная заливка плашки |
| Нет связи с сеткой | Клик подсвечивает слово |

Релиз `v1.1.5`.

### 16.13. Журнал и его расширение

> сделвй в гите выгрузку промтов и ответов в удобном виде
> больше с частицами кода и так далее хочу с табличками и так далее
> побольше раз 5 и какие иишки использованые были

Создан и расширен `PROMPTS.md`, добавлена ссылка в `README.md`. Затем по
просьбе автора журнал сокращён вдвое. Коммиты `a0fa099`, `feccbcf` и
последующие.

---

## 17. Ограничения

| Ограничение | Причина | Обход |
| --- | --- | --- |
| Нет эмодзи | Java 8 рисует квадраты | Цвета и текст |
| Нет ввода с клавиатуры | Требование автора | Мышь |
| Шрифт `Segoe UI` | Единый вид | Swing подставит замену |
| Генерация может не удаться | Случайная раскладка | Сообщение и повтор |
| Большой словарь не в памяти | Память | Режим `stream` |
| Слова до 10 букв | Иначе не помещаются | Отсечение длинных |
| Скриншоты — не автотесты | Нужна визуальная проверка | Утилиты `Shot*` |

Особенности: пересечения клеток получают цвет последнего найденного слова;
клик по слову перезапускает подсветку; подсказка тратит 30 секунд;
ежедневная сетка не зависит от настроек.

---

## 18. Словарь

| Термин | Значение |
| --- | --- |
| `Placement` | Слово, начало, направление |
| `Puzzle` | Сетка и размещения |
| `GenerationResult` | Пазл или ошибка генерации |
| `GameSession` | Логика раунда |
| `GameResult` | Сохранённый итог |
| `FinishReason` | Причина завершения |
| `WordChip` | Плашка слова |
| `BadgeLabel` | Бейдж шапки |
| `FlatButton` | Саморисующаяся кнопка |
| Seed | Число, определяющее генерацию |
| SplitMix64 | Перемешивание seed из даты |

---

## 19. Быстрые команды

| Задача | Команда |
| --- | --- |
| Собрать и прогнать тесты | `mvn clean verify` |
| Собрать без тестов | `mvn -DskipTests package` |
| Запустить GUI | `java -jar target/zaeso-words-1.1.5.jar` |
| Запустить консоль | `java -jar target/zaeso-words-1.1.5.jar --console` |
| Проверить на Java 8 | `temurin8\bin\java.exe -jar ...jar --console` |
| Поставить тег | `git tag -s v1.1.5 -m "..."` |
| Опубликовать | `git push origin main` и `git push origin v1.1.5` |
| Проверить latest | `GET /repos/zaeso/zaeso-Words/releases/latest` |

### 19.1. Чек-лист перед релизом

| Шаг | Признак готовности |
| --- | --- |
| Версия поднята | `pom.xml`, `README.md`, `ci.yml`, Roadmap |
| Тесты | 147/147, BUILD SUCCESS |
| Java 8 | Консоль и GUI запускаются, stderr пуст |
| Коммит | Изменения в `main` |
| Тег | Подписанный `v<версия>` отправлен |
| Релиз | JAR и `run-game.bat` прикреплены, `latest` указывает на новую версию |

---

## 20. Что можно улучшить

| Идея | Польза |
| --- | --- |
| Своя категория из файла | Добавлять слова без пересборки |
| Фильтр рекордов по категории | Сравнивать результаты честнее |
| Пауза в одиночной игре | Удобно на долгих раундах |
| Экспорт в JSON | Обмен историей между устройствами |
| Анимация появления сетки | Более живой старт раунда |
| Настройка числа подсказок | Гибкость сверх профилей сложности |
| Английский интерфейс | Больше аудитории |

---

## Итог

| Показатель | Значение |
| --- | --- |
| Релизов | 5 (`v1.1.1` … `v1.1.5`) |
| Последний | `v1.1.5`, 14.11.2026 |
| Тестов | 147 в 22 классах |
| Java | 8+ (Temurin 1.8.0_512) |
| Основная ИИ-модель | DeepSeek V4.1 Flash Free |
| Модели картинок | Не использовались |

Ссылка на журнал есть в конце `README.md`.
