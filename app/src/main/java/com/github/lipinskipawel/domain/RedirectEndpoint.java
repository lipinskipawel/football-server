package com.github.lipinskipawel.domain;

import com.github.lipinskipawel.user.ConnectedClient;
import com.github.lipinskipawel.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * This class manages the logic around the redirect endpoints.
 *
 * <p>{@code RedirectEndpoint#createNewRedirectEndpoint} is used as an parameter for the
 * {@link Lobby#pair(Supplier, ConnectedClient, ConnectedClient)} method to pair two connected user into a game.
 * Redirect endpoint is used in the {@link FootballServer} class to check whether a given connected user can join the
 * game.
 */
final class RedirectEndpoint {
    private final Map<Pair<String>, String> usernamesToRedirectEndpoint;
    private final Map<String, Pair<String>> redirectEndpointToUsernames;

    RedirectEndpoint() {
        this.usernamesToRedirectEndpoint = new HashMap<>();
        this.redirectEndpointToUsernames = new HashMap<>();
    }

    String createNewRedirectEndpoint(final String firstUsername, final String secondUsername) {
        final var pair = new Pair<>(firstUsername, secondUsername);
        final var rndEndpoint = "/game/" + UUID.randomUUID();
        final var endpoint = this.usernamesToRedirectEndpoint.computeIfAbsent(pair, k -> rndEndpoint);
        this.redirectEndpointToUsernames.put(endpoint, pair);
        return endpoint;
    }

    boolean canJoin(final String username, final String endpointToJoin) {
        final var pair = this.redirectEndpointToUsernames.get(endpointToJoin);
        if (pair == null) {
            return false;
        }
        return pair.getFirst().equals(username) || pair.getSecond().equals(username);
    }
}
