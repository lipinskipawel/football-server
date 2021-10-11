package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.Player;
import org.java_websocket.WebSocket;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

final class MinimalisticClientContext implements ConnectedClient {
    private static final Set<MinimalisticClientContext> existingConnectedClients = new HashSet<>();
    private final WebSocket connection;
    private final Player player;

    private MinimalisticClientContext(WebSocket connection) {
        this.connection = connection;
        this.player = Player.fromUrl(connection.getResourceDescriptor());
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

    static Optional<ConnectedClient> findBy(final Player player) {
        existingConnectedClients.removeIf(it -> it.connection.isClosed());
        return existingConnectedClients
                .stream()
                .filter(it -> it.player.equals(player))
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
    public void close() {
        connection.close();
        existingConnectedClients.removeIf(it -> it.equals(this));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinimalisticClientContext that = (MinimalisticClientContext) o;
        return Objects.equals(connection, that.connection) && Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connection, player);
    }
}
