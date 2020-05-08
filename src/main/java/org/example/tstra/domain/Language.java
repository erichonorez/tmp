package org.example.tstra.domain;

import javafx.util.Pair;
import lombok.NonNull;

import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;


public enum Language {

    FR("fr"),
    NL("nl");

    static final Map<String, Language> validLanguages = EnumSet.allOf(Language.class)
        .stream()
        .map(e -> new Pair<>(e.value, e))
        .collect(Collectors.toMap(tuple -> tuple.getKey(), tuple -> tuple.getValue()));

    private final String value;

    Language(@NonNull String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static Language of(String language) throws InvalidLanguageException {
        if (!Language.validLanguages.keySet().contains(language)) {
            throw new InvalidLanguageException();
        }
        return Language.validLanguages.get(language);
    }

    public static class InvalidLanguageException extends Exception { }
}
