package org.example.tstra.domain;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class LanguageUnitTest {

    @TestFactory
    Stream<DynamicTest> validateValidLanguages() {
        return Language.validLanguages.keySet()
            .stream()
            .map(language -> dynamicTest(language, () -> assertEquals(language, Language.of(language).getValue())));
    }

    @TestFactory
    Stream<DynamicTest> validateInvalidLanguages() {
        return Arrays.stream(Locale.getISOLanguages())
            .filter(language -> !Language.validLanguages.keySet().contains(language))
            .map(language -> dynamicTest(language, () -> assertThrows(Language.InvalidLanguageException.class, () -> Language.of(language))));
    }
}