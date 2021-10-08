package com.github.lipinskipawel.server;

import org.java_websocket.WebSocket;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

final class MinimalisticClientContext implements ConnectedClient {
    private static final Set<MinimalisticClientContext> existingConnectedClients = new HashSet<>();
    private final WebSocket connection;

    private MinimalisticClientContext(WebSocket connection) {
        this.connection = connection;
    }

    static ConnectedClient from(final WebSocket connection) {
        existingConnectedClients.removeIf(it -> it.connection.isClosed());
        final var clientContext = existingConnectedClients
                .stream()
                .filter(it -> it.connection.equals(connection))
                .findFirst()
                .orElse(new MinimalisticClientContext(connection));
        existingConnectedClients.add(clientContext);
        return clientContext;
    }

    @Override
    public void send(String message) {
        connection.send(message);
    }

    @Override
    public String getUrl() {
        return connection.getResourceDescriptor();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinimalisticClientContext that = (MinimalisticClientContext) o;
        return Objects.equals(connection, that.connection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connection);
    }
}
