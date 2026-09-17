package org.devup.core;

import org.devup.db.DbPreset;

import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class DockerManager {

    private final ConfigStore config;
    private final Consumer<String> log;

    public DockerManager(ConfigStore config) {
        this(config, System.out::println);
    }

    public DockerManager(ConfigStore config, Consumer<String> log) {
        this.config = config;
        this.log = log;
    }

    public String passwordFor(DbPreset db) {
        config.set(db.id + ".password", DbPreset.FIXED_PASS);
        return DbPreset.FIXED_PASS;
    }

    private void ensureCredentials(DbPreset db) {
        if (DbPreset.CREDS_VERSION.equals(config.get("creds.version", ""))) return;
        if (exists(db)) {
            log.accept(db.id + ": credenciais mudaram, recriando container e volume...");
            DockerRunner.run("stop", db.container);
            DockerRunner.run("rm", "-v", db.container);
            DockerRunner.run("volume", "rm", db.volume);
        }
        config.set("creds.version", DbPreset.CREDS_VERSION);
    }

    public int portFor(DbPreset db) {
        String saved = config.get(db.id + ".port", String.valueOf(db.defaultPort));
        int port;
        try {
            port = Integer.parseInt(saved);
        } catch (NumberFormatException e) {
            port = db.defaultPort;
        }
        if (PortUtils.inUse(port) && !mapsToUs(db, port)) {
            port = PortUtils.resolveFree(db.defaultPort);
            log.accept("Porta " + saved + " ocupada, usando " + port + " para " + db.id);
        }
        config.set(db.id + ".port", String.valueOf(port));
        return port;
    }

    private boolean mapsToUs(DbPreset db, int port) {
        var r = DockerRunner.run("port", db.container, String.valueOf(db.defaultPort));
        return r.ok() && r.out().contains(String.valueOf(port));
    }

    public boolean exists(DbPreset db) {
        return DockerRunner.run("inspect", db.container).ok();
    }

    public boolean running(DbPreset db) {
        var r = DockerRunner.run("inspect", "-f", "{{.State.Running}}", db.container);
        return r.ok() && r.out().contains("true");
    }

    public String health(DbPreset db) {
        var r = DockerRunner.run("inspect", "-f", "{{.State.Health.Status}}", db.container);
        if (!r.ok() || r.out().isBlank() || r.out().contains("no value")) {
            return running(db) ? "rodando" : "parado";
        }
        return r.out().trim();
    }

    public String describe(DbPreset db) {
        if (!exists(db)) return "nunca criado";
        if (!running(db)) return "parado";
        String h = health(db);
        if (h.equals("healthy") || h.equals("rodando")) return "pronto";
        int port;
        try {
            port = Integer.parseInt(config.get(db.id + ".port", String.valueOf(db.defaultPort)));
        } catch (NumberFormatException e) {
            port = db.defaultPort;
        }
        if (jdbcOk(db, port, passwordFor(db))) return "pronto";
        return h.equals("starting") ? "iniciando" : h;
    }

    public void up(DbPreset db) {
        if (!DockerRunner.available()) {
            throw new IllegalStateException("Docker Desktop nao esta rodando. Abra o Docker Desktop, aguarde o icone ficar verde e clique em Ligar de novo.");
        }
        DockerNetwork.ensureNetwork();
        ensureCredentials(db);
        int port = portFor(db);
        String pass = passwordFor(db);
        if (!exists(db)) {
            log.accept("Criando " + db.container + " na porta " + port + "...");
            List<String> cmd = new ArrayList<>(List.of("run", "-d",
                    "--name", db.container,
                    "--network", DockerNetwork.NETWORK,
                    "--restart", "unless-stopped",
                    "-p", "127.0.0.1:" + port + ":" + db.defaultPort,
                    "-v", db.volume + ":/var/lib/" + db.id + "data"));
            if (db == DbPreset.POSTGRES) {
                cmd.addAll(List.of(
                        "-e", "POSTGRES_DB=" + DbPreset.FIXED_DB,
                        "-e", "POSTGRES_USER=" + DbPreset.FIXED_USER,
                        "-e", "POSTGRES_PASSWORD=" + pass,
                        "--health-cmd", "pg_isready -U " + DbPreset.FIXED_USER + " -d " + DbPreset.FIXED_DB,
                        "--health-interval", "5s", "--health-retries", "10",
                        db.image));
            } else {
                cmd.addAll(List.of(
                        "-e", "MYSQL_DATABASE=" + DbPreset.FIXED_DB,
                        "-e", "MYSQL_ROOT_PASSWORD=" + pass,
                        "--health-cmd", "mysqladmin ping -h localhost -u root -p$MYSQL_ROOT_PASSWORD",
                        "--health-interval", "5s", "--health-retries", "20",
                        db.image));
            }
            var r = DockerRunner.run(cmd);
            if (!r.ok()) throw new IllegalStateException("docker run falhou: " + r.merged());
        } else if (!running(db)) {
            log.accept("Iniciando container " + db.container + "...");
            DockerRunner.run("start", db.container);
        } else {
            log.accept(db.container + " ja esta rodando.");
        }
        waitReady(db, port, pass);
        log.accept(db.id + " pronto: " + db.jdbcUrl("localhost", port));
    }

    private void waitReady(DbPreset db, int port, String pass) {
        log.accept("Aguardando banco aceitar conexao...");
        long deadline = System.currentTimeMillis() + 90000;
        while (System.currentTimeMillis() < deadline) {
            if (jdbcOk(db, port, pass)) return;
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        log.accept("Tempo esgotado: container subiu mas o banco ainda nao respondeu.");
    }

    public boolean jdbcOk(DbPreset db, int port, String pass) {
        try {
            Class.forName(db.driverClass);
            try (var c = DriverManager.getConnection(db.jdbcUrl("127.0.0.1", port), DbPreset.FIXED_USER, pass);
                 var st = c.createStatement()) {
                st.execute("SELECT 1");
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public void down(DbPreset db) {
        if (!exists(db)) {
            log.accept(db.id + " nem existe, nada a fazer.");
            return;
        }
        DockerRunner.run("stop", db.container);
        log.accept(db.id + " parado.");
    }

    public void reset(DbPreset db) {
        down(db);
        DockerRunner.run("rm", "-v", db.container);
        DockerRunner.run("volume", "rm", db.volume);
        config.remove(db.id + ".password");
        log.accept(db.id + " resetado. Ligue de novo para recriar.");
    }

    public String logs(DbPreset db, int tail) {
        var r = DockerRunner.run("logs", "--tail", String.valueOf(tail), db.container);
        return r.ok() ? r.out() : r.merged();
    }
}
