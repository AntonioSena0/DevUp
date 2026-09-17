package org.devup.db;

public enum DbPreset {
    POSTGRES("postgres", "devup-postgres", "postgres:16", 5432, "devup-pgdata", "org.postgresql.Driver"),
    MYSQL("mysql", "devup-mysql", "mysql:8.4", 3306, "devup-mysqldata", "com.mysql.cj.jdbc.Driver");

    public static final String FIXED_USER = "root";
    public static final String FIXED_DB = "devup_database";
    public static final String FIXED_PASS = "devup_database_pass";
    public static final String CREDS_VERSION = "3";

    public final String id;
    public final String container;
    public final String image;
    public final int defaultPort;
    public final String volume;
    public final String driverClass;

    DbPreset(String id, String container, String image, int defaultPort, String volume, String driverClass) {
        this.id = id;
        this.container = container;
        this.image = image;
        this.defaultPort = defaultPort;
        this.volume = volume;
        this.driverClass = driverClass;
    }

    public static DbPreset of(String id) {
        for (DbPreset p : values()) if (p.id.equalsIgnoreCase(id)) return p;
        throw new IllegalArgumentException("Banco desconhecido: " + id);
    }

    public String jdbcUrl(String host, int port) {
        if (this == POSTGRES) return "jdbc:postgresql://" + host + ":" + port + "/" + FIXED_DB;
        return "jdbc:mysql://" + host + ":" + port + "/" + FIXED_DB + "?createDatabaseIfNotExist=true&serverTimezone=UTC";
    }
}
