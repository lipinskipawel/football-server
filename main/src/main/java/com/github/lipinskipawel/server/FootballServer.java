package com.github.lipinskipawel.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public final class FootballServer extends WebSocketServer {

    public FootballServer(final InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        final var url = conn.getResourceDescriptor();
        System.out.printf("Server onOpen: %s%n", url);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.printf("Server onClose: %d, reason: %s", code, reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.printf("Server onMessage: %s%n", message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.printf("Server onError: %s%n", ex);
    }

    @Override
    public void onStart() {
        System.out.println("Server onStart");
    }
}