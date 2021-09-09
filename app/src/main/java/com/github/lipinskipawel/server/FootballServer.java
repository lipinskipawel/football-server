package com.github.lipinskipawel.server;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.lipinskipawel.server.HandshakePolicy.webConnectionPolicy;
import static org.java_websocket.framing.CloseFrame.POLICY_VALIDATION;

public final class FootballServer extends WebSocketServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FootballServer.class);
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
            LOGGER.info(message);
            LOGGER.info("Connection has been closed");
            return;
        }
        this.table.add(url);
        this.resourceDescriptorWithConnection.compute(url, (k, v) -> conn);
        LOGGER.info("Server onOpen: {}", url);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        this.table.remove(conn.getResourceDescriptor());
        LOGGER.info("Server onClose: {}, reason: {}", code, reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        this.resourceDescriptorWithConnection.get(conn.getResourceDescriptor()).send(message);
        LOGGER.info("Server onMessage: {}", message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        LOGGER.error("Server onError: ", ex);
    }

    @Override
    public void onStart() {
        LOGGER.info("Server onStart");
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