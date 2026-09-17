package org.devup.db;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DbPresetTest {

    @Test
    void postgresJdbcUrl() {
        assertEquals("jdbc:postgresql://localhost:5432/devup_database",
                DbPreset.POSTGRES.jdbcUrl("localhost", 5432));
    }

    @Test
    void mysqlJdbcUrl() {
        assertEquals("jdbc:mysql://localhost:3306/devup_database?createDatabaseIfNotExist=true&serverTimezone=UTC",
                DbPreset.MYSQL.jdbcUrl("localhost", 3306));
    }

    @Test
    void fixedCredentials() {
        assertEquals("root", DbPreset.FIXED_USER);
        assertEquals("devup_database", DbPreset.FIXED_DB);
        assertEquals("devup_database_pass", DbPreset.FIXED_PASS);
    }

    @Test
    void ofResolvesById() {
        assertEquals(DbPreset.POSTGRES, DbPreset.of("postgres"));
        assertEquals(DbPreset.MYSQL, DbPreset.of("MYSQL"));
    }

    @Test
    void ofRejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> DbPreset.of("oracle"));
    }
}
