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
import static org.java_websocket.framing.CloseFrame.POLICY_VALIDATION;

public final class FootballServer extends WebSocketServer {
    private final Map<String, WebSocket> resourceDescriptorWithConnection;
    private final Table table;

    public FootballServer(final InetSocketAddress address, final Table table) {
        super(address);
        this.table = table;
        this.resourceDescriptorWithConnection = new ConcurrentHashMap<>(64);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        final var url = conn.getResourceDescriptor();
        if (!this.table.canConnect(url)) {
            final var message = "Server does not allow more than 2 clients to connect to the same endpoint";
            conn.closeConnection(POLICY_VALIDATION, message);
            System.out.println(message);
            System.out.println("Connection has been closed");
            return;
        }
        this.resourceDescriptorWithConnection.compute(url, (k, v) -> conn);
        this.table.add(url);
        System.out.printf("Server onOpen: %s%n", url);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        this.table.remove(conn.getResourceDescriptor());
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