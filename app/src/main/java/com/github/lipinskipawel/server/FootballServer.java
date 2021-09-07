package com.github.lipinskipawel.server;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.lipinskipawel.server.HandshakePolicy.webConnectionPolicy;

public final class FootballServer extends WebSocketServer {
    private final Map<String, WebSocket> resourceDescriptorWithConnection;

    public FootballServer(final InetSocketAddress address) {
        super(address);
        this.resourceDescriptorWithConnection = new ConcurrentHashMap<>(64);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        final var url = conn.getResourceDescriptor();
        this.resourceDescriptorWithConnection.compute(url, (k, v) -> conn);
        System.out.printf("Server onOpen: %s%n", url);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.printf("Server onClose: %d, reason: %s%n", code, reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.printf("Server onMessage: %s%n", message);
        this.resourceDescriptorWithConnection.get(conn.getResourceDescriptor()).send(message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.printf("Server onError: %s%n", ex);
    }

    @Override
    public void onStart() {
        System.out.println("Server onStart");
    }

    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(final WebSocket conn,
                                                                       final Draft draft,
                                                                       final ClientHandshake request)
            throws InvalidDataException {
        final var builder = super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
        webConnectionPolicy(request);
        return builder;
    }
}