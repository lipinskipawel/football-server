package com.github.lipinskipawel.user;

import com.github.lipinskipawel.api.QueryRegister;
import com.github.lipinskipawel.spi.Parser;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ConnectedClientFactory {
    private record ConnectedClientWithToken(String token, ConnectedClient client) {
    }

    private final Map<Channel, ConnectedClientWithToken> authenticatedConnectedClients;
    private final QueryRegister register;
    private final Parser parser;

    public ConnectedClientFactory(QueryRegister register, Parser parser) {
        this.authenticatedConnectedClients = new ConcurrentHashMap<>();
        this.register = register;
        this.parser = parser;
    }

    /**
     * This method is a factory for {@link ConnectedClient} objects.
     *
     * <p>This method does <strong>NOT</strong> acts like a cache. In order to retrieve already accepted
     * {@link ConnectedClient} use {@link #findBy(Channel)} and {@link #findByUsername(String)}.</p>
     *
     * <p> This method is an entrypoint for any sort of authorization.
     * It will check whether given token has been already registered as well as {@link Channel} itself.</p>
     *
     * @param connection that tires to authenticate
     * @param token      that was passed along the way with the connection
     * @return instance of {@link ConnectedClient}
     */
    public ConnectedClient from(final Channel connection, final String token) {
        final var maybeUsername = register.findUsernameByToken(token);
        if (maybeUsername.isPresent()) {
            final var username = maybeUsername.get();
            clearConnection();
            final var authenticatedUser = authenticatedConnectedClients.get(connection);
            if (noConnectionAndNoToken(token, authenticatedUser)) {
                authenticatedConnectedClients.put(connection, new ConnectedClientWithToken(token, new AuthorizedClient(connection, username, parser)));
                return authenticatedConnectedClients.get(connection).client;
            }
            throw new RuntimeException("Already authenticated");
        }
        throw new RuntimeException("Username wasn't registered");
    }

    /**
     * Prevent holding closed connections.
     */
    private void clearConnection() {
        authenticatedConnectedClients.entrySet().removeIf(it -> !it.getKey().isActive());
    }

    private boolean noConnectionAndNoToken(String token, ConnectedClientWithToken authenticatedUser) {
        return authenticatedUser == null && authenticatedConnectedClients
                .values()
                .stream()
                .map(it -> it.token)
                .noneMatch(it -> it.equals(token));
    }

    /**
     * Tries to find {@link ConnectedClient}.
     *
     * @param connection which will be used to find {@link ConnectedClient}
     * @return Optional {@link ConnectedClient}
     */
    public Optional<ConnectedClient> findBy(final Channel connection) {
        final var authenticated = authenticatedConnectedClients.get(connection);
        if (authenticated != null) {
            return Optional.of(authenticated.client);
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
        final var connectedClients = authenticatedConnectedClients
                .values()
                .stream()
                .filter(it -> it.client.getUsername().equals(username))
                .toList();
        if (connectedClients.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(connectedClients.get(0).client);
    }
}
