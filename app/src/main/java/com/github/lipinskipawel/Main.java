package com.github.lipinskipawel;

import com.github.lipinskipawel.register.RegisterEntrypoint;
import com.github.lipinskipawel.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class Main {
    private static final ExecutorService pool = Executors.newSingleThreadExecutor();
    private static final WebSocketServer wsServer = new WebSocketServer();
    private static final AuthServer httpServer = new AuthServer();

    public static void main(String[] args) {
        try {
            pool.submit(() -> wsServer.start(
                    new InetSocketAddress("localhost", 8080),
                    RegisterEntrypoint.getRegister()
            ));

            httpServer.startServer(new InetSocketAddress("localhost", 8090));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            wsServer.stop();
            pool.shutdown();
        }
    }
}
