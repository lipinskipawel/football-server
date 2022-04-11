package com.github.lipinskipawel.user;

import com.github.lipinskipawel.api.QueryRegister;
import com.github.lipinskipawel.register.RegisterEntrypoint;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class is an improved factory for {@link ConnectedClient} objects. Not only this class acts like a factory, but
 * also caches its object ({@link ConnectedClient}). Therefore, it must remove any connections that are closed.
 * <p>
 * This class acts like a bridge between App module (WebSocket connections/Football server) and Auth module (HTTP connections/
 * Authorization module).
 * <p>
 * This class is not Thread safe.
 */
public final class ConnectedClientFactory {
    private final Map<WebSocket, String> authenticatedWithToken;
    private final Map<WebSocket, ConnectedClient> authenticatedConnectedClients;
    private final QueryRegister register;

    public ConnectedClientFactory() {
        this.authenticatedWithToken = new HashMap<>();
        this.authenticatedConnectedClients = new HashMap<>();
        this.register = RegisterEntrypoint.getRegister();
    }

    public ConnectedClientFactory(QueryRegister register) {
        this.authenticatedWithToken = new HashMap<>();
        this.authenticatedConnectedClients = new HashMap<>();
        this.register = register;
    }

    /**
     * This method is a factory for {@link ConnectedClient} objects.
     *
     * <p>This method does <strong>NOT</strong> acts like a cache. In order to retrieve already accepted
     * {@link ConnectedClient} use {@link #findBy(WebSocket)} and {@link #findByUsername(String)}.</p>
     *
     * <p> This method is an entrypoint for any sort of authorization.
     * It will check whether given token has been already registered as well as {@link WebSocket} itself.</p>
     *
     * @param connection that tires to authenticate
     * @param token      that was passed along the way with the connection
     * @return instance of {@link ConnectedClient}
     */
    public ConnectedClient from(final WebSocket connection, final String token) {
        if (!register.isRegistered(token)) {
            throw new RuntimeException("Username wasn't registered");
        }
        clearConnection();
        final var authenticatedUser = authenticatedWithToken.get(connection);
        if (noConnectionAndNoToken(token, authenticatedUser)) {
            authenticatedWithToken.put(connection, token);
            authenticatedConnectedClients.put(connection, new AuthorizedClient(connection, register.usernameForToken(token).get()));
            return authenticatedConnectedClients.get(connection);
        }
        throw new RuntimeException("Already authenticated");
    }

    /**
     * Prevent holding closed connections.
     */
    private void clearConnection() {
        authenticatedWithToken.entrySet().removeIf(it -> it.getKey().isClosed());
        authenticatedConnectedClients.entrySet().removeIf(it -> it.getKey().isClosed());
    }

    private boolean noConnectionAndNoToken(String token, String authenticatedUser) {
        return authenticatedUser == null && !authenticatedWithToken.containsValue(token);
    }

    /**
     * Tries to find {@link ConnectedClient}.
     *
     * @param connection which will be used to find {@link ConnectedClient}
     * @return Optional {@link ConnectedClient}
     */
    public Optional<ConnectedClient> findBy(final WebSocket connection) {
        clearConnection();
        final var authenticated = authenticatedConnectedClients.get(connection);
        if (authenticated != null) {
            return Optional.of(authenticated);
        }
        return Optional.empty();
    }

    /**
     * Tries to find {@link ConnectedClient}.
     *
     * @param username which will be used to find {@link ConnectedClient}
     * @return Optional {@link ConnectedClient}
     */
    public Optional<ConnectedClient> findByUsername(final String username) {
        clearConnection();
        final var connectedClients = authenticatedConnectedClients
                .values()
                .stream()
                .filter(it -> it.getUsername().equals(username))
                .toList();
        if (connectedClients.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(connectedClients.get(0));
    }
}
