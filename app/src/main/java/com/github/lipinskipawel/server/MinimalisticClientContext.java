package com.github.lipinskipawel.server;

import org.java_websocket.WebSocket;

import java.util.Objects;

final class MinimalisticClientContext implements ConnectedClient {
    private final WebSocket connection;

    MinimalisticClientContext(WebSocket connection) {
        this.connection = connection;
    }

    static ConnectedClient createMinimalisticClientContext(final WebSocket connection) {
        return new MinimalisticClientContext(connection);
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
