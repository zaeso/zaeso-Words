"""Build the bundled dictionaries offline from the inputs listed in DICTIONARIES.md."""

import argparse
from collections import Counter
from pathlib import Path
import re
from zipfile import ZipFile

import pymorphy3


ROOT = Path(__file__).resolve().parents[1]
DICTIONARIES = ROOT / "src/main/resources/dictionaries"
RUSSIAN = re.compile(r"[а-яё]{3,12}")
ENGLISH = re.compile(r"[a-z]{3,12}")
THEMES = ("technology", "animals", "space", "nature", "food")


def blocked_words(path, alphabet, fold=lambda word: word):
    return {
        fold(word)
        for line in path.read_text(encoding="utf-8").splitlines()
        if (word := line.strip().lower()) and alphabet.fullmatch(word)
    }


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


def english_nouns(archive, blocked):
    words = set()
    for line in archive.read("wordnet/data.noun").decode("ascii").splitlines():
        if not line or not line[0].isdigit():
            continue
        fields = line.partition("|")[0].split()
        for index in range(int(fields[3], 16)):
            word = fields[4 + 2 * index]
            if ENGLISH.fullmatch(word) and word not in blocked and word not in {"hint", "exit"}:
                words.add(word)
    return words


def write_words(path, words, notice=""):
    path.write_text(notice + "\n".join(word.upper() for word in sorted(words)) + "\n",
                    encoding="utf-8", newline="\n")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("inputs", type=Path)
    args = parser.parse_args()

    russian = russian_nouns(blocked_words(
        args.inputs / "blocked-ru.txt", RUSSIAN, lambda word: word.replace("ё", "е")))
    with ZipFile(args.inputs / "wordnet.zip") as archive:
        english = english_nouns(archive, blocked_words(args.inputs / "blocked-en.txt", ENGLISH))
        license_text = archive.read("wordnet/LICENSE").decode("utf-8")

    for name, words in (("all-russian", russian), ("english", english)):
        playable = sum(len(word) <= 10 for word in words)
        if playable < 10_000:
            raise ValueError(f"{name}: only {playable} words fit a 10-letter side")
        notice = ""
        if name == "all-russian":
            notice = ("# Derived from OpenCorpora revision 417150 (https://opencorpora.org/).\n"
                      "# Filtered to nominative noun lemmas. CC BY-SA 3.0.\n"
                      "# https://creativecommons.org/licenses/by-sa/3.0/\n")
        write_words(DICTIONARIES / f"{name}.txt", words, notice)
        print(name, len(words), "lengths:", dict(sorted(Counter(map(len, words)).items())))

    for name in THEMES:
        path = DICTIONARIES / f"{name}.txt"
        theme = {word.lower() for word in path.read_text(encoding="utf-8").splitlines()}
        retained = theme & russian
        if sum(len(word) <= 10 for word in retained) < 12:
            raise ValueError(f"{name}: too few playable nouns")
        write_words(path, retained)
        print(name, len(retained), "retained")

    (DICTIONARIES / "WORDNET-LICENSE.txt").write_text(
        "\n".join(line.rstrip() for line in license_text.splitlines()).rstrip() + "\n",
        encoding="utf-8", newline="\n")


if __name__ == "__main__":
    main()
