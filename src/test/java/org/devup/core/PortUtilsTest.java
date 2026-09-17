package org.devup.core;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortUtilsTest {

    @Test
    void freePortIsNotInUse() throws IOException {
        int port;
        try (ServerSocket ss = new ServerSocket(0)) {
            port = ss.getLocalPort();
        }
        assertFalse(PortUtils.inUse(port));
    }

    @Test
    void boundPortIsInUse() throws IOException {
        try (ServerSocket ss = new ServerSocket(0)) {
            assertTrue(PortUtils.inUse(ss.getLocalPort()));
        }
    }

    @Test
    void resolveFreeSkipsOccupied() throws IOException {
        try (ServerSocket ss = new ServerSocket(0)) {
            int occupied = ss.getLocalPort();
            int free = PortUtils.resolveFree(occupied);
            assertTrue(free > occupied);
            assertFalse(PortUtils.inUse(free));
        }
    }
}
