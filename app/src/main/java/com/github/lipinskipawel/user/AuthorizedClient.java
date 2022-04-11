package com.github.lipinskipawel.user;

import org.java_websocket.WebSocket;

final class AuthorizedClient implements ConnectedClient {
    private final WebSocket connection;
    private final String username;

    public AuthorizedClient(WebSocket connection, String username) {
        this.connection = connection;
        this.username = username;
    }

    @Override
    public void send(String message) {
        this.connection.send(message);
    }

    @Override
    public String getUrl() {
        return this.connection.getResourceDescriptor();
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void close() {
        this.connection.close();
    }
}
