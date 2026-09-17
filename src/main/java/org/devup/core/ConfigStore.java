package org.devup.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ConfigStore {
    private final Path file;
    private final Properties props = new Properties();

    public ConfigStore() {
        Path dir = Path.of(System.getProperty("user.home"), ".devup");
        try { Files.createDirectories(dir); } catch (Exception ignored) {}
        this.file = dir.resolve("config.properties");
        try (var in = Files.newInputStream(file)) { props.load(in); }
        catch (Exception ignored) {}
    }

    public synchronized String getOrCreate(String key, java.util.function.Supplier<String> gen) {
        String v = props.getProperty(key);
        if (v == null || v.isBlank()) {
            v = gen.get();
            props.setProperty(key, v);
            save();
        }
        return v;
    }

    public synchronized String get(String key, String def) {
        return props.getProperty(key, def);
    }

    public synchronized void set(String key, String value) {
        props.setProperty(key, value);
        save();
    }

    public synchronized void remove(String... keys) {
        for (String k : keys) props.remove(k);
        save();
    }

    private void save() {
        try (var out = Files.newOutputStream(file)) {
            props.store(out, "DevUp");
        } catch (Exception e) {
            System.err.println("Aviso: nao foi possivel salvar config: " + e.getMessage());
        }
    }

    public Path location() { return file; }
}
