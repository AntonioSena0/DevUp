package org.devup.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class PortUtils {
    private PortUtils() {}

    public static boolean inUse(int port) {
        try (Socket s = new Socket("127.0.0.1", port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static int resolveFree(int preferred) {
        for (int p = preferred; p < preferred + 100; p++) {
            if (!inUse(p)) {
                try (ServerSocket ss = new ServerSocket(p)) {
                    ss.setReuseAddress(true);
                    return p;
                } catch (IOException ignored) {}
            }
        }
        throw new IllegalStateException("Nenhuma porta livre perto de " + preferred);
    }
}
