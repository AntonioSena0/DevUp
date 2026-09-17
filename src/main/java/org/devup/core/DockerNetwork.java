package org.devup.core;

public final class DockerNetwork {
    public static final String NETWORK = "devup-net";

    private DockerNetwork() {}

    public static void ensureNetwork() {
        var inspect = DockerRunner.run("network", "inspect", NETWORK);
        if (inspect.ok()) return;
        var create = DockerRunner.run("network", "create", "--driver", "bridge", NETWORK);
        if (!create.ok()) {
            throw new IllegalStateException("Nao foi possivel criar a network " + NETWORK + ": " + create.merged());
        }
    }
}
