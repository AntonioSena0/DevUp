package org.devup.core;

import org.devup.db.DbPreset;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class DockerHealthTest {

    @Test
    void describeAlwaysReturnsKnownState() {
        assumeTrue(DockerRunner.available(), "Docker indisponivel, pulando teste de integracao");
        DockerManager mgr = new DockerManager(new ConfigStore(), msg -> {});
        Set<String> known = Set.of("nunca criado", "parado", "rodando",
                "healthy", "starting", "unhealthy", "pronto", "iniciando");
        for (DbPreset db : DbPreset.values()) {
            assertTrue(known.contains(mgr.describe(db)), "Estado inesperado para " + db.id);
        }
    }

    @Test
    void failedInspectIsNotOk() {
        assumeTrue(DockerRunner.available(), "Docker indisponivel, pulando teste de integracao");
        DockerRunner.Result r = DockerRunner.run("inspect", "devup-container-que-nao-existe-12345");
        assertTrue(!r.ok());
    }
}
