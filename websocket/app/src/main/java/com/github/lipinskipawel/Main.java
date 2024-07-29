package com.github.lipinskipawel;

import com.github.lipinskipawel.register.RegisterEntrypoint;
import com.github.lipinskipawel.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class Main {
    private static final ExecutorService pool = Executors.newSingleThreadExecutor();
    private static final WebSocketServer wsServer = new WebSocketServer();

    public static void main(String[] args) {
        try {
            wsServer.start(
                    new InetSocketAddress("localhost", 8080),
                    RegisterEntrypoint.getRegister()
            );
        } finally {
            wsServer.stop();
            pool.shutdown();
        }
    }
}
