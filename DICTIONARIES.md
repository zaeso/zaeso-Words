# Словари zaeso-Words

Игра читает словари из ресурсов JAR. Для игры нужны только Java и JAR. Python нужен для пересборки словарей из исходных данных.

| Ресурс | Источник и условия |
| --- | --- |
| `all-russian.txt` | [OpenCorpora](https://opencorpora.org/) через [pymorphy3-dicts-ru](https://pypi.org/project/pymorphy3-dicts-ru/2.4.417150.4580142/), ревизия 417150. Обработанный список распространяется по [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/). |
| `english.txt` | [Princeton WordNet 3.0](https://wordnet.princeton.edu/license-and-commercial-use), архив [NLTK Data](https://github.com/nltk/nltk_data/blob/gh-pages/packages/corpora/wordnet.zip). Текст лицензии включён в JAR как `dictionaries/WORDNET-LICENSE.txt`. |
| Фильтр лексики | [LDNOOBW, Shutterstock и участники](https://github.com/LDNOOBW/List-of-Dirty-Naughty-Obscene-and-Otherwise-Bad-Words/tree/5faf2ba42d7b1c0977169ec3611df25a3c08eb13), [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/). Исходные списки фильтра в игру не включены. |

Русский список содержит начальные формы существительных с меткой `NOUN` и падежом `nomn`. Исключены помеченные имена собственные, сокращения, разговорные и устаревшие формы, а также слова из фильтра и их известные леммы. Существительные, употребляемые только во множественном числе, могут оставаться в именительном множественного. Тематические словари сверены с общим русским списком.

Английский список состоит из однословных существительных WordNet. Сборщик оставляет строчные слова источника, обрабатывает фильтр лексики и исключает игровые команды. Оба списка включают слова длиной от 3 до 12 букв. Фильтр покрывает записи своего источника, и может пропускать редкие или контекстно нежелательные слова.

## Пересборка

Установите Python 3.10+ и `pymorphy3==2.0.6`, `pymorphy3-dicts-ru==2.4.417150.4580142`. В папку входных данных поместите `wordnet.zip`, а файлы `ru` и `en` из указанного коммита LDNOOBW сохраните как `blocked-ru.txt` и `blocked-en.txt`.

```shell
python tools/build_dictionaries.py target/dictionary-inputs
```

| Файл | SHA-256 |
| --- | --- |
| `wordnet.zip` | `cbda5ea6eef7f36a97a43d4a75f85e07fccbb4f23657d27b4ccbc93e2646ab59` |
| `blocked-ru.txt` | `2598adabd2b761733d72a68d8ee4fcc1d2c38f2f85b6ff489985c6ce1d93bf75` |
| `blocked-en.txt` | `af851ecef1d5f212caba17339b12ac39cc2fef7d78c74876f67237644fcee8bd` |
