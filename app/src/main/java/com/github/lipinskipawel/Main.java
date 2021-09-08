package com.github.lipinskipawel;

import com.github.lipinskipawel.server.FootballServer;
import com.github.lipinskipawel.server.Table;

import java.net.InetSocketAddress;

final class Main {
    public static void main(String[] args) {
        final var server = new FootballServer(new InetSocketAddress("localhost", 8080), new Table());
        server.run();
    }
}
