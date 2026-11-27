# zaeso-Words · журнал разработки

**«Поиск слов»** — игра для Windows и других систем с Java 8+. Игрок ищет русские или английские слова в сетке, набирает очки и может пройти общую для всех головоломку дня.

Разделы 01–13 содержат формулировки запросов из архива проекта и краткие записи о результатах. Архив сохранён в коммите `d614769`. Для старых записей в нём сохранились краткие ответы, таблицы и отдельные фрагменты кода. Ниже приведён также дословный фрагмент текущего диалога. Раздел «Следующие идеи» содержит черновики будущих запросов.

Редакторские формулировки описывают цель и критерии проверки каждого запроса. В приложении собраны методы из исходников версии 1.3.0.

## Интерфейс и совместимость

### 01 · Сделать кнопки главного меню видимыми

**Формулировка для технического задания.** Устранить зависимость видимости кнопок от системного Look & Feel. Унифицировать состояния наведения, нажатия и фокуса, затем проверить обратную связь при сохранении настроек.

**Запрос из архива**

> сделай игредетное оформление и красивое гуи просто кнопки не видно некоторые
> и не красиво выгледит плюс настройки нельзя соьхранить

**Запись об ответе из архива**

Причина: тема Windows не рисовала фон кнопки с отключённой заливкой.
Решение: `FlatButton` рисует себя сам. Добавлены градиентный фон меню и
кнопка «Сохранить» со статусом. Релиз `v1.1.1`.

### 02 · Сохранить Java 8 и различимость найденных слов

**Формулировка для технического задания.** Обеспечить совместимость исполнения с Java 8. Подобрать палитры для светлого и тёмного фона так, чтобы найденные слова оставались различимы по цвету.

**Запрос из архива**

> проверий что джава 8 вобще может работать и не го по правь цвет найденых
> слов а то бывает он сливаеться с основным и не много игредета и перецвета
> добавь для кросоты на всех минюшках

**Запись об ответе из архива**

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

### 03 · Оформить меню, настройки и итоги в одном стиле

**Формулировка для технического задания.** Согласовать визуальную иерархию меню, настроек и итогов. Выделить основные действия и состояние сохранения едиными элементами оформления.

**Запрос из архива**

> допели интерфейс сделай более красивый и так далее

**Запись об ответе из архива**

Добавлены градиент, блик, тень кнопок, декор меню, тени карточек, полоски
плиток и градиентная шапка рекордов. Релиз `v1.1.3`.

### 04 · Проверить игру на настоящей Java 8

**Формулировка для технического задания.** Проверить артефакт на исполняющей среде Java 8. Подтвердить открытие Swing-интерфейса, прохождение консольного раунда и совместимость байткода.

**Запрос из архива**

> что там на 8 тджаве

**Запись об ответе из архива**

| Проверка | Итог |
| --- | --- |
| GUI на Temurin 8 | Работает, stderr пуст |
| Консоль | Сетка генерируется |
| Байткод | Major 52 |
| CI | 147/147 |

Изменений кода и релиза не было.

## Игровой экран

### 05 · Управлять таймером, подсказками и завершением раунда

**Формулировка для технического задания.** Определить состояния активного раунда и реакции на ввод. Синхронизировать таймер, подсказки, прогресс и сохранение итогов при досрочном завершении.

**Запрос из архива**

> дороботой логику интерфейса самой игры и всё

**Запись об ответе из архива**

- Два способа выделения: протягивание и два клика.
- Правая кнопка и Escape отменяют.
- Предпросмотр слова под курсором.
- Счётчики «Найдено X/Y» и «Слова · X/Y».
- Красное время на последних 30 секундах.
- Кнопка подсказок гаснет при нуле.
- «Завершить раунд» сохраняет результат.

Релиз `v1.1.4`.

### 06 · Уточнить заголовки на экране итогов

**Формулировка для технического задания.** Сопоставить причины завершения раунда с однозначными заголовками итогового экрана. Учесть истечение времени, ручной выход и полное решение ежедневной сетки.

**Запрос из архива**

> дороботай ещё

**Запись об ответе из архива**

| Причина | Заголовок |
| --- | --- |
| `TIME_EXPIRED` | «Время вышло» |
| `PLAYER_EXIT` | «Раунд завершён» |
| Все слова, день | «Сетка дня пройдена» |
| Все слова, обычный | «Раунд пройден» |

Релиз `v1.1.4`.

### 07 · Дать два способа выделять слова мышью

**Формулировка для технического задания.** Перевести выбор слов на жесты мышью. Поддержать перетаскивание и выбор по двум клеткам, сохранив Escape для сброса выделения.

**Запрос из архива**

> без вода с клавиатуры

**Запись об ответе из архива**

Клавиатурный ввод убран. Из клавиш остался Escape для отмены выделения.
Релиз `v1.1.4`.

### 08 · Улучшить читаемость игровой сетки

**Формулировка для технического задания.** Разграничить основные показатели активного раунда и детализацию начисленных очков. Выделить время, число найденных слов и остаток подсказок.

**Запрос из архива**

> модешь ещё красивей интерфейс

**Запись об ответе из архива**

Добавлены бейджи шапки, тени клеток и четыре плитки разбора очков. Релиз
`v1.1.4`.

### 09 · Показать интерфейс на реальной сборке

**Формулировка для технического задания.** Подготовить визуальное представление действующей сборки. Показать основные экраны и отдельно продемонстрировать статистику.

**Запрос из архива**

> покажи пто

**Запись об ответе из архива**

Собрана сводная картинка экранов: меню, настройки, игра, статистика, плюс
отдельный снимок статистики.

### 10 · Проверить, что открыта сетка дня

**Формулировка для технического задания.** Определить по интерфейсу, соответствует ли показанная сетка ежедневному режиму. Сверить заголовок, дату, размеры поля и лимит подсказок.

**Запрос из архива**

> это сетка дня

**Запись об ответе из архива**

| Параметр | Значение |
| --- | --- |
| Заголовок | `Сетка дня · 12.11.2026 · 10×10` |
| Подсказки | 2 из 2 |
| Слов | 14 |
| Seed | Из даты |

Сетка совпала с ежедневным пазлом. Изменений кода не было.

### 11 · Найти слово на показанной сетке

**Формулировка для технического задания.** Восстановить координаты указанной последовательности букв. Проверить восемь направлений и указать начало, конец и направление выделения.

**Запрос из архива**

> где оно

**Запись об ответе из архива**

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

### 12 · Привести список слов в порядок

**Формулировка для технического задания.** Организовать список раунда по алфавиту и связать каждую найденную запись с координатами слова на поле. Проверить цветовую согласованность.

**Запрос из архива**

> напиши обнову потом камитом релизом доделай список слов нормальный

**Запись об ответе из архива**

| Было | Стало |
| --- | --- |
| `JLabel` в списке | Чипы `WordChip` |
| Порядок генератора | Алфавит |
| Цвет только у текста | Цветная заливка плашки |
| Нет связи с сеткой | Клик подсвечивает слово |

Релиз `v1.1.5`.

## Сборка и документация

### 13 · Собрать историю запросов в один документ

**Формулировка для технического задания.** Подготовить читаемую выгрузку диалога для сдачи проекта. Сохранить исходные формулировки, указать степень полноты ответов и привести относящиеся к решениям фрагменты кода.

**Запрос из архива**

> сделвй в гите выгрузку промтов и ответов в удобном виде
> больше с частицами кода и так далее хочу с табличками и так далее
> побольше раз 5 и какие иишки использованые были

**Запись об ответе из архива**

Создан и расширен `PROMPTS.md`, добавлена ссылка в `README.md`. Затем по
просьбе автора журнал сокращён вдвое. Коммиты `a0fa099`, `feccbcf` и
последующие.

### 14 · Новый визуальный стиль и два языка

**Формулировка для технического задания.** Обновить визуальную систему игры и развести русский и английский режимы. Зафиксировать единый алфавит для поля, списка слов и проверки ввода в пределах раунда.

**Пользователь**

> до делай игру доведи до идеала [@Codex Security](plugin://codex-security@openai-curated-remote)  [@Data](plugin://data-analytics@openai-curated-remote)  [@Template Creator](plugin://template-creator@openai-primary-runtime)  [@Visualize](plugin://visualize@openai-curated-remote)  нормальные названия кадого запроса и сделай всё сегодня от визуала до запуска не ниже джавы 8

**Модель**

> Проверю, как сейчас устроена игра, что мешает запуску и какие названия видит игрок. Затем доработаю интерфейс и игровой цикл и проверю совместимость с Java 8.

**Пользователь**

> сделай новый визуал с нуля

**Модель**

> Заменю визуальный стиль игры целиком. Сделаю спокойную палитру, выделю основные действия и оформлю меню, сетку и итоги в одном стиле. Затем заново сниму экраны на Java 8.

**Пользователь**

> и добавь англиский только руские слова или только англиские список руских слов удали все маты и так же сделай с англиским

**Модель**

> Добавлю отдельные русские и английские словари. В каждом раунде список, подсказки и случайные буквы будут на одном языке. Большой русский список заменю проверяемым списком обычных слов и добавлю такой же английский.

**Из итогового ответа модели**

> Собрал **zaeso-Words 1.2.0** с новым визуальным стилем и русскими и английскими головоломками.

> **Проверено на Temurin 8u512.** Прошли все 150 тестов. Отдельно проверил запуск Swing, оба языка, подсказки, сетку дня и сохранение результатов.

### 15 · Переписать описание проекта и журнал

**Формулировка для технического задания.** Переработать документацию и метаданные Maven. Привязать описание сборки к исполняемому JAR и подтвердить совместимость с Java 8.

**Пользователь**

> пром экс хмл перепеши фулл красивые придумай и так далее сообщения и запросы

**Модель**

> Перепишу `PROMPTS.md` целиком: у каждого запроса будет понятное название, новая формулировка и конкретный результат. `pom.xml` тоже приведу в порядок и проверю, что сборка по-прежнему работает с Java 8.

### 16 · Опубликовать релиз

**Формулировка для технического задания.** Опубликовать версию на GitHub. Связать тег с проверенным коммитом и подтвердить доступность архива Windows и JAR.

**Пользователь**

> релиз

**Модель**

> Подготовлю релиз 1.2.0 на GitHub. Сначала проверю ветку, доступ к репозиторию и готовность файлов для публикации.

### 17 · Уточнить задание по выгрузке

**Формулировка для технического задания.** Сопоставить структуру журнала с требованиями учебного задания. Разграничить дословные сообщения, архивные выдержки и редакторские формулировки.

**Пользователь** · сообщение со скриншотом задания

> вот про это

**Модель**

> На скриншоте речь о выгрузке реальных запросов и ответов модели. Сейчас `PROMPTS.md` содержит пересказ, поэтому восстановлю дословные пары там, где есть исходный текст. Одновременно проверю, завершилась ли публикация игры.

### 18 · Расширить словари и разделить обработку данных и генерацию

**Формулировка для технического задания.** Увеличить каждый общий словарь до десяти тысяч слов и более. В русском списке оставить существительные в именительном падеже. Разделить формирование словарей, загрузку ресурсов, выборку и генерацию поля.

**Пользователь**

> [@Cyber Defense](plugin://cyber-defense@local-cyber-defense) доделай промты там чась кода добавь и сделай больше слов хотя бы 10к русских и англиских в руском только существлительные импадеже код на пиши с новой архетектурой и декомпазицией и релиз

**Модель**

> Расширю `PROMPTS.md` фрагментами кода и вынесу обработку словарей из игрового цикла. Для русского списка отберу существительные в именительном падеже. Затем проверю оба списка, сборку на Java 8 и подготовлю релиз. Навык Cyber Defense применю для защитной проверки загрузчика словарей.

**Как реализовано**

1. [Сборщик словарей](tools/build_dictionaries.py) обрабатывает внешние источники отдельно от игры. Для русского списка он берёт начальную форму слова и проверяет часть речи и падеж по OpenCorpora.

   ```python
   for word, (paradigm, form) in dictionary.words.iteritems():
       if form != 0 or not RUSSIAN.fullmatch(word):
           continue
       tag = dictionary.build_tag_info(paradigm, form)
       if tag.POS != "NOUN" or tag.case != "nomn" or tag.grammemes & disallowed:
           continue
       folded = word.replace("ё", "е")
       if folded not in blocked and folded not in blocked_lemmas:
           accepted.add(word)
   ```

2. [DictionaryResources](src/main/java/game/dictionary/DictionaryResources.java) читает встроенные UTF-8-ресурсы и проверяет имена файлов и язык. [WordCategory](src/main/java/game/dictionary/WordCategory.java) хранит настройки категории. [WordSampler](src/main/java/game/dictionary/WordSampler.java) выбирает слова без повторов и отдаёт приоритет коротким словам.

   ```java
   int index = random.nextInt(pool.size());
   int last = pool.size() - 1;
   selected.add(pool.set(index, pool.get(last)));
   pool.remove(last);
   if (pool.isEmpty()) pools.remove(i);
   ```

3. [RoundGenerator](src/main/java/game/generator/RoundGenerator.java) передаёт выбранные слова и алфавит в генератор поля. Источник случайных чисел общий для обоих шагов. С одинаковым словарём и seed получается одна и та же сетка.

   ```java
   Random random = new Random(seed);
   int maxLength = Math.max(config.height(), config.width());
   int sampleSize = Math.max(config.minWords() * 4, 30);
   List<String> pool = category.sampleWords(maxLength, sampleSize, random);

   return new GridGenerator(random).generateChecked(
           config.height(), config.width(), pool, config.minWords(), category.language());
   ```

**Результат.** В версии 1.3.0 доступны 52 026 русских и 37 713 английских слов. На Temurin 8u512 прошли 152 теста. В обоих консольных режимах проверены настройка словаря, генерация поля, подсказка и запись результата.

## Проверка и запуск

```powershell
mvn verify
.\run-game.bat
.\run-game.bat --console
```

На Temurin 8 проверены главное меню, настройки, русская и английская сетки, подсказка, итоги, переход к следующему раунду, сетка дня и запись результатов.

Снимки со сборки 1.2.0 — [меню](target/preview-menu.png), [русский раунд](target/preview-game.png), [английский раунд](target/preview-english.png), [итоги](target/preview-result.png). Снимки находятся в локальной папке сборки.

Исполняемый файл после сборки — `target/zaeso-words-1.3.0.jar`. Параметры и история хранятся в `~/.zaeso-words/`.

## Следующие идеи

### 19 · Показать результаты за последние семь дней

**Запрос.** Добавь к статистике сетки дня компактный график лучших результатов. Покажи дни без игры отдельным состоянием и используй локальную историю.

### 20 · Объяснить правила перед первой игрой

**Запрос.** Добавь короткий экран «Как играть» с примером выбора диагонального слова. Опиши подсказку и штрафы на одном примере.

### 21 · Повторить ежедневную сетку после выхода

**Запрос.** Сделай действие «Повторить сетку» на экране итогов ежедневной игры. Используй ту же дату и отдельную попытку в истории.

## Приложение. Код и архитектурные решения

Основной путь данных проходит от ресурсов словаря через выборку и генератор к игровой сессии. Swing отображает её состояние, а репозиторий сохраняет итог раунда.

### Подготовка русских словарей

В фильтр попадают леммы OpenCorpora, у которых начальная форма имеет метки `NOUN` и `nomn`. Перед включением слова проверяются словарные метки и известные леммы из списка исключений. [Исходник](tools/build_dictionaries.py#L27).

```python
def russian_nouns(blocked):
    analyzer = pymorphy3.MorphAnalyzer()
    dictionary = analyzer.dictionary
    disallowed = {"Name", "Surn", "Patr", "Geox", "Orgn", "Trad", "Abbr",
                  "Slng", "Infr", "Arch", "Erro", "Dist", "Vulg"}
    accepted = set()
    blocked_lemmas = set()
    for word in blocked:
        blocked_lemmas.update(parse.normal_form.replace("ё", "е")
                              for parse in analyzer.parse(word) if parse.is_known)

    for word, (paradigm, form) in dictionary.words.iteritems():
        if form != 0 or not RUSSIAN.fullmatch(word):
            continue
        tag = dictionary.build_tag_info(paradigm, form)
        if tag.POS != "NOUN" or tag.case != "nomn" or tag.grammemes & disallowed:
            continue
        folded = word.replace("ё", "е")
        if folded not in blocked and folded not in blocked_lemmas:
            accepted.add(word)
    return accepted
```

### Строгая загрузка индекса

Индекс связывает отображаемое имя, файл и язык. Проверка шаблона имени ограничивает загрузку встроенными ресурсами, а множество имён выявляет повторные записи. [Исходник](src/main/java/game/dictionary/DictionaryResources.java#L25).

```java
static List<WordCategory> loadCategories() {
    List<WordCategory> categories = new ArrayList<>();
    Set<String> fileNames = new HashSet<>();
    for (String row : readRows("categories.txt")) {
        String[] fields = row.split("\\|", -1);
        if (fields.length != 3 || fields[0].trim().isEmpty()
                || !fields[1].matches("[a-z][a-z0-9-]*\\.txt")
                || !fileNames.add(fields[1])) {
            throw new IllegalStateException("Некорректная строка индекса словарей: " + row);
        }
        WordLanguage language;
        switch (fields[2]) {
            case "ru": language = WordLanguage.RUSSIAN; break;
            case "en": language = WordLanguage.ENGLISH; break;
            default: throw new IllegalStateException("Неизвестный язык словаря: " + fields[2]);
        }
        categories.add(new WordCategory(fields[0].trim(), fields[1], language));
    }
    if (categories.isEmpty()) {
        throw new IllegalStateException("В индексе нет ни одной категории слов.");
    }
    return Collections.unmodifiableList(categories);
}
```

### Выборка с учётом длины

Сэмплер копирует подходящие группы, выбирает группу по весу и удаляет выбранный элемент из рабочей копии. Порядок групп определён заранее, поэтому один seed даёт одинаковую выборку. [Исходник](src/main/java/game/dictionary/WordSampler.java#L26).

```java
List<String> sample(int maxLength, int count, Random random) {
    if (count <= 0) return Collections.emptyList();

    List<List<String>> pools = new ArrayList<>();
    for (Map.Entry<Integer, List<String>> entry : wordsByLength.entrySet()) {
        if (entry.getKey() > maxLength) break;
        pools.add(new ArrayList<>(entry.getValue()));
    }

    List<String> selected = new ArrayList<>();
    while (!pools.isEmpty() && selected.size() < count) {
        int weight = pools.stream().mapToInt(WordSampler::weight).sum();
        int ticket = random.nextInt(weight);
        for (int i = 0; i < pools.size(); i++) {
            List<String> pool = pools.get(i);
            ticket -= weight(pool);
            if (ticket < 0) {
                int index = random.nextInt(pool.size());
                int last = pool.size() - 1;
                selected.add(pool.set(index, pool.get(last)));
                pool.remove(last);
                if (pool.isEmpty()) pools.remove(i);
                break;
            }
        }
    }
    return Collections.unmodifiableList(selected);
}
```

### Размещение слова без частичной записи

Генератор проверяет границы и пересечения до записи. Если хотя бы одна клетка занята другой буквой, вся попытка отклоняется. [Исходник](src/main/java/game/generator/GridGenerator.java#L187).

```java
boolean tryPlace(char[][] grid, String word, int row, int col, Direction direction) {
    if (!canPlace(grid, word, row, col, direction)) return false;

    place(grid, word, row, col, direction);
    return true;
}
```

### Проверка ответа и учёт ошибок

Сессия нормализует ввод и проверяет алфавит текущей головоломки. В штраф за ошибки входят отсутствующие и повторно найденные слова. [Исходник](src/main/java/game/session/GameSession.java#L84).

```java
public WordResult submitWord(String raw) {
    if (finished) {
        throw new IllegalStateException("Партия уже завершена.");
    }

    String answer = normalize(raw);
    if (answer.isEmpty()) {
        return WordResult.EMPTY;
    }
    if (!puzzle.language().accepts(answer)) {
        return WordResult.NON_CYRILLIC;
    }
    if (answer.length() > maxWordLength) {
        return WordResult.TOO_LONG;
    }
    if (!allWords.contains(answer)) {
        mistakes++;
        return WordResult.NOT_IN_LIST;
    }
    if (found.contains(answer)) {
        mistakes++;
        return WordResult.ALREADY_FOUND;
    }

    found.add(answer);
    return WordResult.FOUND;
}
```

### Выбор подсказки и штраф времени

Подсказка выбирает самое длинное из слов, которые игрок ещё не нашёл и по которым ещё не получал подсказку. При выдаче уменьшается остаток времени. [Исходник](src/main/java/game/session/GameSession.java#L116).

```java
public HintResult useHint() {
    if (finished) {
        throw new IllegalStateException("Партия уже завершена.");
    }
    if (config.hints() == 0) {
        return new HintResult(HintStatus.DISABLED, null, hintsUsed, config.hints());
    }
    if (hintsUsed >= config.hints()) {
        return new HintResult(HintStatus.LIMIT_REACHED, null, hintsUsed, config.hints());
    }

    Optional<Placement> candidate = puzzle.getPlacements().stream()
            .filter(placement -> !found.contains(placement.word()))
            .filter(placement -> !hintedWords.contains(placement.word()))
            .max(Comparator.comparingInt(placement -> placement.word().length()));

    if (!candidate.isPresent()) {
        return new HintResult(HintStatus.NO_CANDIDATES, null, hintsUsed, config.hints());
    }
    Placement placement = candidate.get();
    hintedWords.add(placement.word());
    hintsUsed++;
    timer.penalize(HINT_PENALTY);
    return new HintResult(HintStatus.GIVEN, placement, hintsUsed, config.hints());
}
```

### Генерация ежедневной сетки

Seed вычисляется из календарной даты. Результат воспроизводим для одинаковой даты, версии словаря и алгоритма генерации. [Исходник](src/main/java/game/daily/DailyPuzzle.java#L45).

```java
public static long seed(LocalDate date) {
    Objects.requireNonNull(date, "date");
    long value = date.toEpochDay() + 0x9E3779B97F4A7C15L;
    value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
    value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
    return value ^ (value >>> 31);
}
```

### Загрузка игрового поля в фоне

Генерация выполняется в `SwingWorker`. При завершении работы интерфейс проверяет, относится ли результат к текущему раунду, прежде чем открыть сетку. [Исходник](src/main/java/game/ui/GamePanel.java#L150).

```java
public void startRound(GameSettings settings, int round, int carriedScore,
                       String resultLabel, String title, long seed) {
    stopRound();
    this.carriedScore = carriedScore;
    GameConfig config = settings.toConfig();
    WordCategory category = settings.category();
    session = null;
    setControlsEnabled(false);
    gridPanel.setGrid(new char[0][0]);
    wordsPanel.removeAll();
    wordsPanel.revalidate();
    wordsPanel.repaint();
    wordsBorder.setTitle("Слова");
    progress.setIndeterminate(true);
    totalLabel.setText("Всего очков: " + carriedScore);
    foundLabel.setText("Найдено —");
    timeLabel.setText("Время —");
    timeLabel.setForeground(Color.WHITE);
    hintsLabel.setText("Подсказки —");
    roundLabel.setText(title != null
            ? title
            : "Раунд " + round + " · " + settings.difficulty().displayName()
                    + " · " + category.displayName()
                    + " · " + config.height() + "×" + config.width());
    statusLabel.setForeground(Theme.MUTED);
    statusLabel.setText("Готовим сетку…");

    roundLoader = new SwingWorker<GenerationResult, Void>() {
        @Override
        protected GenerationResult doInBackground() {
            return RoundGenerator.generate(config, category, seed);
        }

        @Override
        protected void done() {
            if (roundLoader != this) return;
            roundLoader = null;
            progress.setIndeterminate(false);
            try {
                openRound(get(), settings, config, resultLabel);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                showError("Подготовка сетки прервана.");
            } catch (ExecutionException e) {
                showError("Не удалось подготовить сетку. Вернитесь в меню и попробуйте снова.");
            }
        }
    };
    roundLoader.execute();
}
```

### Атомарная запись результатов

История записывается во временный файл в том же каталоге. Затем репозиторий заменяет основной файл атомарным перемещением, если файловая система его поддерживает. [Исходник](src/main/java/game/result/ResultRepository.java#L104).

```java
private void writeAtomically(List<GameResult> results) throws IOException {
    Path parent = file.getParent();
    Files.createDirectories(parent);

    Path temporary = Files.createTempFile(parent, "results-", ".tmp");
    try {
        mapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), results);
        try {
            Files.move(temporary, file,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    } finally {
        Files.deleteIfExists(temporary);
    }
}
```

### Контракт двух словарей

Тест проверяет минимальное число слов для поля 10×10, отсутствие дублей, соответствие алфавиту и воспроизводимость выборки. [Исходник](src/test/java/game/dictionary/WordCategoryTest.java#L31).

```java
void bothGeneralDictionariesContainTenThousandPlayableWords() {
    for (String name : new String[]{"all-russian", "english"}) {
        WordCategory category = WordCategory.fromInput(name);
        assertTrue(category.suitableWordCount(10) >= 10_000, name);
        assertEquals(category.words().size(), category.words().stream().distinct().count());
        assertTrue(category.words().stream().allMatch(category.language()::accepts));
        assertEquals(category.sampleWords(10, 48, new Random(7)),
                category.sampleWords(10, 48, new Random(7)));
    }
}
```

### Проверка границ и пересечений

Конечная клетка вычисляется из направления и длины слова. Если отрезок помещается в поле, метод проверяет совместимость каждой буквы с уже занятыми клетками. [Исходник](src/main/java/game/generator/GridGenerator.java#L194).

```java
private boolean canPlace(char[][] grid, String word, int row, int col, Direction d) {
    int endRow = row + (word.length() - 1) * d.rowStep();
    int endCol = col + (word.length() - 1) * d.colStep();

    if (endRow < 0 || endRow >= grid.length || endCol < 0 || endCol >= grid[0].length)
        return false;

    for (int i = 0; i < word.length(); i++) {
        int r = row + i * d.rowStep();
        int c = col + i * d.colStep();
        char existing = grid[r][c];
        if (existing != '\0' && existing != word.charAt(i)) return false;
    }
    return true;
}
```

### Начисление очков и штрафов

Очки за буквы, бонус за оставшееся время и штрафы рассчитываются независимо. Бонус времени начисляется после нахождения всех слов. [Исходник](src/main/java/game/score/ScoreCalculator.java#L37).

```java
public static Score calculate(Collection<String> foundWords, int totalWords,
                              long remainingSeconds, int mistakes, int hintsUsed) {
    Objects.requireNonNull(foundWords, "foundWords");
    if (totalWords < 0) {
        throw new IllegalArgumentException("Общее число слов не может быть отрицательным.");
    }
    if (remainingSeconds < 0) {
        throw new IllegalArgumentException("Оставшееся время не может быть отрицательным.");
    }
    if (mistakes < 0) {
        throw new IllegalArgumentException("Число ошибок не может быть отрицательным.");
    }
    if (hintsUsed < 0) {
        throw new IllegalArgumentException("Число подсказок не может быть отрицательным.");
    }

    int wordPoints = 0;
    for (String word : foundWords) {
        wordPoints += LetterPoints.pointsFor(Objects.requireNonNull(word, "word"));
    }

    boolean completed = totalWords > 0 && foundWords.size() >= totalWords;
    int timeBonus = completed
            ? (int) Math.min(Integer.MAX_VALUE, remainingSeconds * POINTS_PER_REMAINING_SECOND)
            : 0;

    return new Score(
            wordPoints,
            timeBonus,
            mistakes * MISTAKE_PENALTY,
            hintsUsed * HINT_PENALTY
    );
}
```

## Документы проекта

- [Правила и запуск](README.md)
- [Дорожная карта](zaeso-Words-Roadmap.md)
- [Сборка Maven](pom.xml)
