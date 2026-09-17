package org.devup.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigStoreTest {

    @TempDir
    Path tempHome;

    @Test
    void storesAndReadsValues() {
        String originalHome = System.getProperty("user.home");
        System.setProperty("user.home", tempHome.toString());
        try {
            ConfigStore store = new ConfigStore();
            store.set("mysql.port", "3307");
            assertEquals("3307", store.get("mysql.port", "3306"));
            assertEquals("fallback", store.get("missing", "fallback"));
            String generated = store.getOrCreate("mysql.password", () -> "secret");
            assertEquals("secret", generated);
            assertEquals("secret", new ConfigStore().get("mysql.password", ""));
        } finally {
            System.setProperty("user.home", originalHome);
        }
    }
}
