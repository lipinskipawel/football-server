package com.github.lipinskipawel;

import com.github.lipinskipawel.server.FootballServer;

import java.net.InetSocketAddress;

final class Main {
    public static void main(String[] args) throws InterruptedException {
        final var wsServer = new FootballServer(new InetSocketAddress("localhost", 8080));
        try {
            wsServer.start();
            final var authServer = new AuthServer();
            authServer.startServer(new InetSocketAddress("localhost", 8090));
        } finally {
            wsServer.stop();
        }
    }
}
