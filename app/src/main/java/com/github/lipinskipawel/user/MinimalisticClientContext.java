package com.github.lipinskipawel.user;

import org.java_websocket.WebSocket;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

final class MinimalisticClientContext implements ConnectedClient {
    private static final Set<MinimalisticClientContext> existingConnectedClients = new HashSet<>();
    private final WebSocket connection;
    private final String username;

    private MinimalisticClientContext(WebSocket connection, String username) {
        this.connection = connection;
        this.username = username;
    }

    static Optional<ConnectedClient> from(final WebSocket connection, final String username) {
        existingConnectedClients.removeIf(it -> it.connection.isClosed());
        final var optionalClient = findBy(connection);
        if (optionalClient.isPresent()) {
            final var connectedClient = optionalClient.get();
            if (!connectedClient.getUsername().equals(username)) {
                return Optional.empty();
            }
        }
        if (optionalClient.isEmpty()) {
            final var newConnectedClient = new MinimalisticClientContext(connection, username);
            existingConnectedClients.add(newConnectedClient);
            return Optional.of(newConnectedClient);
        }
        return optionalClient;
    }

    static Optional<ConnectedClient> findBy(final WebSocket connection) {
        existingConnectedClients.removeIf(it -> it.connection.isClosed());
        return existingConnectedClients
                .stream()
                .filter(it -> it.connection.equals(connection))
                .map(it -> (ConnectedClient) it)
                .findFirst();
    }

    static Optional<ConnectedClient> findByUsername(final String username) {
        existingConnectedClients.removeIf(it -> it.connection.isClosed());
        return existingConnectedClients
                .stream()
                .filter(it -> it.username.equals(username))
                .map(it -> (ConnectedClient) it)
                .findFirst();
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
    public String getUsername() {
        return username;
    }

    @Override
    public void close() {
        connection.close();
        existingConnectedClients.removeIf(it -> it.equals(this));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinimalisticClientContext that = (MinimalisticClientContext) o;
        return Objects.equals(connection, that.connection) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connection, username);
    }
}
