package org.devup.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordGeneratorTest {

    @Test
    void generatesRequestedLength() {
        assertEquals(20, PasswordGenerator.generate(20).length());
    }

    @Test
    void usesUnambiguousAlphabet() {
        String pass = PasswordGenerator.generate(200);
        assertTrue(pass.matches("[A-Za-z0-9]+"));
        assertTrue(pass.chars().noneMatch(c -> c == 'I' || c == 'O' || c == 'l' || c == '0' || c == '1'));
    }

    @Test
    void generatesDifferentValues() {
        assertNotEquals(PasswordGenerator.generate(20), PasswordGenerator.generate(20));
    }
}
